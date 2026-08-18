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
    private static final String KEY_MATERIAL_SECTIONS = "material_sections";
    private static final String KEY_MATERIAL_SPACING = "material_spacing";
    private static final String KEY_DYNAMIC_COLOR = "material_monet";

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

    /** Material 3 section styling (rounded cards, gaps, styled headers). */
    public static boolean isMaterialSections() {
        return prefs().getBoolean(KEY_MATERIAL_SECTIONS, false);
    }

    public static void setMaterialSections(boolean value) {
        prefs().edit().putBoolean(KEY_MATERIAL_SECTIONS, value).apply();
    }

    /** 0 = normal, 1 = big, 2 = huge. */
    public static int getMaterialSectionsSpacing() {
        return prefs().getInt(KEY_MATERIAL_SPACING, 0);
    }

    public static void setMaterialSectionsSpacing(int value) {
        prefs().edit().putInt(KEY_MATERIAL_SPACING, value).apply();
    }

    /** Material You / monet: recolor the app accent from the wallpaper colors. */
    public static boolean isDynamicColor() {
        return prefs().getBoolean(KEY_DYNAMIC_COLOR, false);
    }

    public static void setDynamicColor(boolean value) {
        prefs().edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply();
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
