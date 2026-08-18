package org.telegram.messenger;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.SparseIntArray;

import androidx.core.graphics.ColorUtils;

import android.graphics.BitmapFactory;

import org.telegram.ui.ActionBar.Theme;

import java.util.HashMap;

public final class MonetColor {

    private MonetColor() {
    }

    private static final int MONET_ACCENT_ID = 0x4D6F6E74;
    private static final HashMap<String, Integer> originalAccents = new HashMap<>();
    private static int lastAppliedColor = 0;

    public static void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(ApplicationLoader.applicationContext);
                android.app.WallpaperManager.OnColorsChangedListener listener = (colors, which) -> {
                    if (ModConfig.isDynamicColor()) {
                        Theme.refreshThemeColors();
                    }
                };
                wm.addOnColorsChangedListener(listener, new Handler(Looper.getMainLooper()));
            } catch (Throwable ignore) {
            }
        }
    }

    public static void applyIfEnabled() {
        applyAccent();
    }

    public static void applyAccent() {
        if (!ModConfig.isDynamicColor()) {
            return;
        }
        int[] c = getMonetColors();
        if (c == null) {
            return;
        }
        int primary = c[0];
        boolean changed = ensureAccent((Theme.ThemeInfo) Theme.getCurrentTheme(), primary)
                | ensureAccent((Theme.ThemeInfo) Theme.getCurrentNightTheme(), primary);
        if (changed && primary != lastAppliedColor) {
            lastAppliedColor = primary;
            AndroidUtilities.runOnUIThread(Theme::refreshThemeColors);
        } else if (changed) {
            lastAppliedColor = primary;
        }
    }

    public static void restoreAccent() {
        restoreTheme((Theme.ThemeInfo) Theme.getCurrentTheme());
        restoreTheme((Theme.ThemeInfo) Theme.getCurrentNightTheme());
        lastAppliedColor = 0;
    }

    private static boolean ensureAccent(Theme.ThemeInfo theme, int color) {
        if (theme == null || theme.accentBaseColor == 0) {
            return false;
        }
        String key = theme.getKey();
        if (!originalAccents.containsKey(key)) {
            originalAccents.put(key, theme.currentAccentId);
        }
        Theme.ThemeAccent accent = theme.themeAccentsMap.get(MONET_ACCENT_ID);
        boolean changed = false;
        if (accent == null) {
            accent = new Theme.ThemeAccent();
            accent.id = MONET_ACCENT_ID;
            accent.parentTheme = theme;
            theme.themeAccentsMap.put(MONET_ACCENT_ID, accent);
            theme.themeAccents.add(0, accent);
            changed = true;
        }
        if (accent.accentColor != color) {
            accent.accentColor = color;
            changed = true;
        }
        accent.accentColor2 = color;
        accent.myMessagesAccentColor = color;
        accent.myMessagesGradientAccentColor1 = 0;
        accent.myMessagesGradientAccentColor2 = 0;
        accent.myMessagesGradientAccentColor3 = 0;
        accent.myMessagesAnimated = false;
        accent.backgroundOverrideColor = 0;
        accent.backgroundGradientOverrideColor1 = 0;
        accent.backgroundGradientOverrideColor2 = 0;
        accent.backgroundGradientOverrideColor3 = 0;
        if (theme.currentAccentId != MONET_ACCENT_ID) {
            theme.setCurrentAccentId(MONET_ACCENT_ID);
            changed = true;
        }
        return changed;
    }

    private static void restoreTheme(Theme.ThemeInfo theme) {
        if (theme == null) {
            return;
        }
        String key = theme.getKey();
        Integer orig = originalAccents.get(key);
        if (orig == null) {
            return;
        }
        theme.themeAccentsMap.remove(MONET_ACCENT_ID);
        for (int i = 0; i < theme.themeAccents.size(); i++) {
            if (theme.themeAccents.get(i).id == MONET_ACCENT_ID) {
                theme.themeAccents.remove(i);
                break;
            }
        }
        theme.setCurrentAccentId(orig);
        originalAccents.remove(key);
    }

    private static String lastSource = "none";

    public static String getLastSource() {
        return lastSource;
    }

    public static int[] getMonetColors() {
        lastSource = "none";
        try {
            int fw = getFrameworkAccent();
            if (fw != 0) {
                lastSource = "framework";
                return new int[]{fw, AndroidUtilities.getAverageColor(fw, 0xffffffff), AndroidUtilities.getAverageColor(fw, 0xff000000)};
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    android.content.res.Resources sys = android.content.res.Resources.getSystem();
                    int primary = sys.getColor(android.R.color.system_accent1_500, null);
                    int secondary = sys.getColor(android.R.color.system_accent1_200, null);
                    int tertiary = sys.getColor(android.R.color.system_accent3_500, null);
                    if (primary != 0) {
                        lastSource = "sysmonet";
                        return new int[]{primary, secondary, tertiary};
                    }
                } catch (Throwable ignore) {
                }
            }
            WallpaperManager wm = WallpaperManager.getInstance(ApplicationLoader.applicationContext);
            int dom = dominantFromDrawable(wm);
            if (dom != 0) {
                lastSource = "drawable";
                return new int[]{dom, AndroidUtilities.getAverageColor(dom, 0xffffffff), AndroidUtilities.getAverageColor(dom, 0xff000000)};
            }
            dom = dominantFromFile(wm);
            if (dom != 0) {
                lastSource = "file";
                return new int[]{dom, AndroidUtilities.getAverageColor(dom, 0xffffffff), AndroidUtilities.getAverageColor(dom, 0xff000000)};
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                android.app.WallpaperColors wc = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
                if (wc == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    wc = wm.getWallpaperColors(WallpaperManager.FLAG_LOCK);
                }
                if (wc != null && wc.getPrimaryColor() != null) {
                    int primary = wc.getPrimaryColor().toArgb();
                    int secondary = wc.getSecondaryColor() != null ? wc.getSecondaryColor().toArgb() : primary;
                    int tertiary = wc.getTertiaryColor() != null ? wc.getTertiaryColor().toArgb() : primary;
                    if (primary != 0) {
                        lastSource = "sys";
                        return new int[]{primary, secondary, tertiary};
                    }
                }
            }
        } catch (Throwable ignore) {
        }
        return null;
    }

    private static int getFrameworkAccent() {
        try {
            android.content.ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
            String raw = android.provider.Settings.Secure.getString(cr, "theme_customization_overlay_packages");
            if (raw != null && !raw.isEmpty()) {
                org.json.JSONObject obj = new org.json.JSONObject(raw);
                String acc = obj.optString("android.theme.customization.accent_color", null);
                if (acc == null) {
                    acc = obj.optString("android.theme.customization.system_palette", null);
                }
                if (acc != null && !acc.isEmpty()) {
                    if (acc.startsWith("#")) {
                        acc = acc.substring(1);
                    }
                    if (acc.length() == 8) {
                        acc = acc.substring(2);
                    }
                    int c = Integer.parseInt(acc, 16);
                    c = 0xff000000 | c;
                    if (c != 0) {
                        return c;
                    }
                }
            }
        } catch (Throwable ignore) {
        }
        return 0;
    }

    private static int dominantFromDrawable(WallpaperManager wm) {
        try {
            Drawable d = wm.getDrawable();
            if (d == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                d = wm.peekDrawable();
            }
            if (d != null) {
                return AndroidUtilities.getDominantColor(drawableToBitmap(d));
            }
        } catch (Throwable ignore) {
        }
        return 0;
    }

    private static int dominantFromFile(WallpaperManager wm) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.os.ParcelFileDescriptor pfd = wm.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                if (pfd != null) {
                    Bitmap bmp = android.graphics.BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    if (bmp != null) {
                        int c = AndroidUtilities.getDominantColor(bmp);
                        bmp.recycle();
                        return c;
                    }
                }
            }
        } catch (Throwable ignore) {
        }
        return 0;
    }

    private static Bitmap drawableToBitmap(Drawable d) {
        int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : 512;
        int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : 512;
        if (w > 512) {
            float scale = 512f / w;
            w = 512;
            h = (int) (h * scale);
        }
        if (h <= 0) {
            h = 512;
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        d.setBounds(0, 0, w, h);
        d.draw(canvas);
        return bmp;
    }
}
