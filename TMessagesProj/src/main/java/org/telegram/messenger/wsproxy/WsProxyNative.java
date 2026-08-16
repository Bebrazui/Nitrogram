package org.telegram.messenger.wsproxy;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.telegram.messenger.FileLog;

public final class WsProxyNative {

    public interface Core extends Library {
        int StartProxy(String host, int port, String dcIps, String secret, int verbose);
        int StopProxy();
        void SetPoolSize(int size);
        void SetCfProxyCacheDir(String cacheDir);
        void SetCfProxyConfig(int enabled, int priority, String userDomain);
        Pointer GetSecretWithPrefix();
        Pointer GetStats();
        void FreeString(Pointer p);
    }

    private static Core core;
    private static boolean loadTried;

    private WsProxyNative() {}

    private static synchronized Core core() {
        if (!loadTried) {
            loadTried = true;
            try {
                core = Native.load("tgwsproxy", Core.class);
                FileLog.d("WsProxy: native core loaded");
            } catch (Throwable t) {
                core = null;
                FileLog.e("WsProxy: native core unavailable", t);
            }
        }
        return core;
    }

    public static boolean isAvailable() {
        return core() != null;
    }

    public static int startProxy(String host, int port, String dcIps, String secret, boolean verbose) {
        Core c = core();
        if (c == null) return -100;
        try {
            return c.StartProxy(host, port, dcIps == null ? "" : dcIps, secret, verbose ? 1 : 0);
        } catch (Throwable t) {
            FileLog.e("WsProxy: StartProxy failed", t);
            return -101;
        }
    }

    public static int stopProxy() {
        Core c = core();
        if (c == null) return -100;
        try { return c.StopProxy(); }
        catch (Throwable t) { FileLog.e("WsProxy: StopProxy failed", t); return -101; }
    }

    public static void setPoolSize(int size) {
        Core c = core();
        if (c == null) return;
        try { c.SetPoolSize(size); } catch (Throwable t) { FileLog.e(t); }
    }

    public static void setCfProxyCacheDir(String cacheDir) {
        Core c = core();
        if (c == null || cacheDir == null) return;
        try { c.SetCfProxyCacheDir(cacheDir); } catch (Throwable t) { FileLog.e(t); }
    }

    public static void setCfProxyConfig(boolean enabled, boolean priority, String userDomain) {
        Core c = core();
        if (c == null) return;
        try { c.SetCfProxyConfig(enabled ? 1 : 0, priority ? 1 : 0, userDomain == null ? "" : userDomain); }
        catch (Throwable t) { FileLog.e(t); }
    }

    public static String getSecretWithPrefix() {
        Core c = core();
        if (c == null) return null;
        try {
            Pointer p = c.GetSecretWithPrefix();
            if (p == null) return null;
            String res = p.getString(0);
            c.FreeString(p);
            return res;
        } catch (Throwable t) { FileLog.e("WsProxy: GetSecretWithPrefix failed", t); return null; }
    }

    public static String getStats() {
        Core c = core();
        if (c == null) return null;
        try {
            Pointer p = c.GetStats();
            if (p == null) return null;
            String res = p.getString(0);
            c.FreeString(p);
            return res;
        } catch (Throwable t) { FileLog.e("WsProxy: GetStats failed", t); return null; }
    }
}
