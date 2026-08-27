/*
 * Embedded TG WS Proxy controller.
 *
 * Runs the native MTProto WS proxy in-process and wires it into Telegram's
 * own proxy mechanism (SharedConfig / ConnectionsManager). Because the core
 * lives in the same process as Telegram, no foreground service is required:
 * the proxy is alive for as long as the app is.
 */

package org.telegram.messenger.wsproxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;

import java.io.File;
import java.security.SecureRandom;

public final class WsProxyController {

    public static final String PREFS = "wsproxy";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SECRET = "secret";
    private static final String KEY_POOL = "pool_size";
    private static final String KEY_CF = "cf_enabled";
    private static final String KEY_PORT_BOUND = "bound_port";

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 1443;
    private static final int PORT_RANGE = 12; // 1443..1454
    private static final int DEFAULT_POOL = 4;

    private static volatile boolean running;
    private static volatile boolean starting;
    private static volatile int boundPort = PORT;

    private WsProxyController() {
    }

    public static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isAvailable() {
        return WsProxyNative.isAvailable();
    }

    /** Enabled by default: the embedded proxy auto-starts on first launch. */
    public static boolean isEnabled() {
        return prefs().getBoolean(KEY_ENABLED, true);
    }

    /** Bring the proxy up synchronously if it should be running but isn't. */
    public static synchronized boolean ensureRunningSync() {
        if (!isEnabled()) {
            return false;
        }
        if (running) {
            return true;
        }
        if (!WsProxyNative.isAvailable()) {
            return false;
        }
        final String secret = ensureSecret();
        boolean ok = startNative(secret);
        if (ok) {
            running = true;
            starting = false;
            applyToTelegram(true);
        }
        return ok;
    }

    /** Bring the proxy up if it should be running but isn't (e.g. recovery after a failed start). */
    public static void ensureRunning() {
        if (isEnabled() && !isRunning()) {
            start();
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static int getBoundPort() {
        return boundPort;
    }

    public static int getPoolSize() {
        return prefs().getInt(KEY_POOL, DEFAULT_POOL);
    }

    public static void setPoolSize(int size) {
        if (size < 2) size = 2;
        if (size > 16) size = 16;
        prefs().edit().putInt(KEY_POOL, size).apply();
    }

    public static boolean isCloudflareEnabled() {
        return prefs().getBoolean(KEY_CF, true);
    }

    public static void setCloudflareEnabled(boolean enabled) {
        prefs().edit().putBoolean(KEY_CF, enabled).apply();
    }

    /** Enable/disable the embedded proxy and (re)apply it immediately. */
    public static void setEnabled(boolean enabled) {
        prefs().edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (enabled) {
            start();
        } else {
            stop();
        }
    }

    /** Called on app start to bring the proxy up if it was left enabled. */
    public static void startIfEnabled() {
        if (isEnabled()) {
            start();
        }
    }

    public static synchronized boolean start() {
        if (running || starting) {
            return true;
        }
        if (!WsProxyNative.isAvailable()) {
            FileLog.e("WsProxy: native core not available, cannot start");
            return false;
        }

        final String secret = ensureSecret();
        starting = true;

        // The native start does bind + CloudFlare init which can be slow — keep it off the
        // caller thread (UI / app init) so toggling and launch stay snappy. Telegram is pointed
        // at the proxy only once the core has actually bound a port (avoids a dead-port window
        // and lets us pick a free port if another instance already holds the default one).
        Thread t = new Thread(() -> {
            boolean ok = false;
            try {
                ok = startNative(secret);
                if (ok) {
                    applyToTelegram(true);
                }
            } finally {
                running = ok;
                starting = false;
            }
        }, "wsproxy-start");
        t.setDaemon(true);
        t.start();
        return true;
    }

    private static boolean startNative(String secret) {
        try {
            File cacheDir = new File(ApplicationLoader.applicationContext.getCacheDir(), "wsproxy");
            cacheDir.mkdirs();
            WsProxyNative.setCfProxyCacheDir(cacheDir.getAbsolutePath());
        } catch (Throwable t) {
            FileLog.e(t);
        }

        WsProxyNative.setPoolSize(getPoolSize());
        WsProxyNative.setCfProxyConfig(isCloudflareEnabled(), true, "");

        // Try the last-used port first, then scan a small range. -3 means the port is busy
        // (e.g. another Nitrogram copy holds it), so move on to the next one.
        int preferred = prefs().getInt(KEY_PORT_BOUND, PORT);
        for (int i = -1; i < PORT_RANGE; i++) {
            int p = (i < 0) ? preferred : (PORT + i);
            if (i >= 0 && p == preferred) {
                continue; // already tried as the preferred port
            }
            int res = WsProxyNative.startProxy(HOST, p, "", secret, false);
            if (res == 0) {
                boundPort = p;
                prefs().edit().putInt(KEY_PORT_BOUND, p).apply();
                FileLog.d("WsProxy: started on " + HOST + ":" + p);
                return true;
            }
            if (res == -1) { // already running natively in this process
                FileLog.d("WsProxy: already running on " + HOST + ":" + boundPort);
                return true;
            }
            FileLog.e("WsProxy: StartProxy on port " + p + " returned " + res);
        }
        return false;
    }

    public static synchronized void stop() {
        // Detach from the connection immediately (cheap), then tear the core down off-thread.
        applyToTelegram(false);
        final boolean wasRunning = running || starting;
        running = false;
        starting = false;
        if (wasRunning) {
            Thread t = new Thread(() -> {
                WsProxyNative.stopProxy();
                FileLog.d("WsProxy: stopped");
            }, "wsproxy-stop");
            t.setDaemon(true);
            t.start();
        }
    }

    public static String getStats() {
        return WsProxyNative.getStats();
    }

    // -----------------------------------------------------------------------

    private static String ensureSecret() {
        SharedPreferences prefs = prefs();
        String secret = prefs.getString(KEY_SECRET, null);
        if (!isValidSecret(secret)) {
            secret = generateSecret();
            prefs.edit().putString(KEY_SECRET, secret).apply();
        }
        return secret;
    }

    private static boolean isValidSecret(String value) {
        if (value == null || value.length() != 32) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    private static String generateSecret() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** The full MTProto secret (dd-prefixed) Telegram must connect with. Deterministic, from prefs. */
    public static String getMtprotoSecret() {
        String raw = prefs().getString(KEY_SECRET, null);
        return isValidSecret(raw) ? "dd" + raw : null;
    }

    /**
     * Point Telegram's connection layer at the local proxy (or detach it) silently.
     * The proxy is NOT added to the visible proxy list — only the connection prefs
     * and the native connection layer are touched.
     */
    private static void applyToTelegram(boolean enable) {
        try {
            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
            if (enable) {
                String secret = getMtprotoSecret();
                if (TextUtils.isEmpty(secret)) {
                    secret = "dd" + ensureSecret();
                }
                preferences.edit()
                        .putString("proxy_ip", HOST)
                        .putInt("proxy_port", boundPort)
                        .putString("proxy_user", "")
                        .putString("proxy_pass", "")
                        .putString("proxy_secret", secret)
                        .putBoolean("proxy_enabled", true)
                        .apply();
                ConnectionsManager.setProxySettings(true, HOST, boundPort, "", "", secret);

                // Ensure push token is registered to Telegram servers through the working proxy connection
                if (!TextUtils.isEmpty(org.telegram.messenger.SharedConfig.pushString)) {
                    for (int a = 0; a < org.telegram.messenger.UserConfig.MAX_ACCOUNT_COUNT; a++) {
                        if (org.telegram.messenger.UserConfig.getInstance(a).isClientActivated()) {
                            final int acc = a;
                            org.telegram.messenger.AndroidUtilities.runOnUIThread(() -> {
                                MessagesController.getInstance(acc).registerForPush(org.telegram.messenger.SharedConfig.pushType, org.telegram.messenger.SharedConfig.pushString);
                            });
                        }
                    }
                }
            } else {
                // Only detach if Telegram is currently pointed at our embedded (loopback) proxy.
                boolean ours = HOST.equals(preferences.getString("proxy_ip", ""));
                if (ours) {
                    preferences.edit()
                            .putBoolean("proxy_enabled", false)
                            .putString("proxy_ip", "")
                            .putString("proxy_secret", "")
                            .apply();
                    ConnectionsManager.setProxySettings(false, "", 0, "", "", "");
                }
            }
        } catch (Throwable t) {
            FileLog.e("WsProxy: applyToTelegram failed", t);
        }
    }
}
