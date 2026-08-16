/*
 * Nitrogram mod settings (ad blocking, etc.).
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public final class ModConfig {

    public static final String PREFS = "modconfig";
    private static final String KEY_BLOCK_SPONSORED = "block_sponsored";
    private static final String KEY_BLOCK_HASHTAG_ADS = "block_hashtag_ads";

    private ModConfig() {
    }

    public static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isBlockSponsored() {
        return prefs().getBoolean(KEY_BLOCK_SPONSORED, true);
    }

    public static void setBlockSponsored(boolean value) {
        prefs().edit().putBoolean(KEY_BLOCK_SPONSORED, value).apply();
    }

    public static boolean isBlockHashtagAds() {
        return prefs().getBoolean(KEY_BLOCK_HASHTAG_ADS, true);
    }

    public static void setBlockHashtagAds(boolean value) {
        prefs().edit().putBoolean(KEY_BLOCK_HASHTAG_ADS, value).apply();
    }

    /** True if the message looks like an advertisement (contains an #реклама / #ad hashtag). */
    public static boolean isAdMessage(MessageObject mo) {
        if (mo == null || mo.messageOwner == null) {
            return false;
        }
        String text = mo.messageOwner.message;
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lower = text.toLowerCase();
        if (lower.contains("#реклама") || lower.contains("#реклам")) {
            return true;
        }
        int idx = lower.indexOf("#ad");
        while (idx >= 0) {
            int end = idx + 3;
            char after = end < lower.length() ? lower.charAt(end) : ' ';
            if (!Character.isLetterOrDigit(after)) {
                return true;
            }
            idx = lower.indexOf("#ad", idx + 1);
        }
        return false;
    }
}
