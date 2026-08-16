/*
 * Nitrogram "Secret Safe": hide selected chats behind a PIN.
 *
 * Safe chats are always removed from the visible chat list and produce no
 * notifications. They can only be reached by typing the PIN into the chat
 * search field, which opens a dedicated Safe screen.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SafeConfig {

    public static final String PREFS = "safeconfig";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_SALT = "pin_salt";
    private static final String KEY_IDS = "safe_ids";

    private static Set<Long> cache;

    private SafeConfig() {
    }

    public static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ----- PIN -----

    public static boolean hasPin() {
        return !TextUtils.isEmpty(prefs().getString(KEY_PIN_HASH, null));
    }

    public static void setPin(String pin) {
        String salt = randomHex(16);
        prefs().edit()
                .putString(KEY_SALT, salt)
                .putString(KEY_PIN_HASH, hash(salt, pin))
                .apply();
    }

    public static boolean checkPin(String pin) {
        if (TextUtils.isEmpty(pin) || !hasPin()) {
            return false;
        }
        String salt = prefs().getString(KEY_SALT, "");
        String expected = prefs().getString(KEY_PIN_HASH, "");
        return expected.equals(hash(salt, pin));
    }

    // ----- Safe membership -----

    private static synchronized Set<Long> ids() {
        if (cache == null) {
            cache = new HashSet<>();
            String raw = prefs().getString(KEY_IDS, "");
            if (!TextUtils.isEmpty(raw)) {
                for (String part : raw.split(",")) {
                    try {
                        cache.add(Long.parseLong(part.trim()));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return cache;
    }

    public static boolean isInSafe(long dialogId) {
        if (dialogId == 0) {
            return false;
        }
        return ids().contains(dialogId);
    }

    public static boolean isEmpty() {
        return ids().isEmpty();
    }

    public static synchronized Set<Long> getSafeIds() {
        return new HashSet<>(ids());
    }

    public static synchronized void addToSafe(Collection<Long> dialogIds) {
        ids().addAll(dialogIds);
        save();
    }

    public static synchronized void removeFromSafe(long dialogId) {
        ids().remove(dialogId);
        save();
    }

    public static synchronized void clearAll() {
        ids().clear();
        save();
    }

    private static void save() {
        StringBuilder sb = new StringBuilder();
        for (Long id : ids()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        prefs().edit().putString(KEY_IDS, sb.toString()).apply();
    }

    // ----- helpers -----

    private static String hash(String salt, String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            FileLog.e(e);
            return "";
        }
    }

    private static String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }
}
