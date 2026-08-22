package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.InMemoryDexClassLoader;

public class TextAnimationManager {

    private static final String PREFS_NAME = "text_animation_config";
    private static Class<?> coreClass = null;
    private static boolean isLoaded = false;

    public static void init() {
        if (isLoaded || Build.VERSION.SDK_INT < 26) {
            return;
        }
        try {
            Context context = ApplicationLoader.applicationContext;
            if (context == null) return;

            InputStream is = context.getAssets().open("text_animation_core.dex");
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            is.close();
            byte[] dexBytes = baos.toByteArray();

            ByteBuffer byteBuffer = ByteBuffer.wrap(dexBytes);
            InMemoryDexClassLoader classLoader = new InMemoryDexClassLoader(byteBuffer, context.getClassLoader());
            coreClass = classLoader.loadClass("com.textanimation.TextAnimationCore");

            Method startMethod = coreClass.getDeclaredMethod("start");
            startMethod.setAccessible(true);
            startMethod.invoke(null);

            isLoaded = true;
            syncSettings();
            android.util.Log.d("NITROGRAM_ANIM", "TextAnimationCore successfully started natively! Bytes: " + dexBytes.length);
        } catch (Throwable e) {
            android.util.Log.e("NITROGRAM_ANIM", "Failed to initialize TextAnimationCore: ", e);
        }
    }

    public static SharedPreferences getPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void syncSettings() {
        if (!isLoaded || coreClass == null) return;
        try {
            SharedPreferences prefs = getPrefs();
            JSONObject json = new JSONObject();

            json.put("enabled", prefs.getBoolean("enabled", true));
            json.put("duration", prefs.getString("duration", "300"));
            json.put("blur_enabled", prefs.getBoolean("blur_enabled", true));
            json.put("blur_duration", prefs.getString("blur_duration", "300"));
            json.put("blur_radius", prefs.getString("blur_radius", "10"));
            json.put("blur_text_delay", prefs.getString("blur_text_delay", "20"));
            json.put("slide_enabled", prefs.getBoolean("slide_enabled", true));
            json.put("slide_dist", prefs.getString("slide_dist", "20"));
            json.put("scale_enabled", prefs.getBoolean("scale_enabled", false));
            json.put("scale_start", prefs.getString("scale_start", "0.0"));
            json.put("rotate_enabled", prefs.getBoolean("rotate_enabled", false));
            json.put("rotate_angle", prefs.getString("rotate_angle", "-15"));
            json.put("delete_anim_enabled", prefs.getBoolean("delete_anim_enabled", true));
            json.put("particle_style", prefs.getInt("particle_style", 0));
            json.put("particle_count", prefs.getString("particle_count", "5"));
            json.put("particle_speed", prefs.getString("particle_speed", "50"));
            json.put("particle_spread", prefs.getString("particle_spread", "50"));
            json.put("particle_size", prefs.getString("particle_size", "50"));
            json.put("cursor_enabled", prefs.getBoolean("cursor_enabled", true));
            json.put("cursor_speed", prefs.getString("cursor_speed", "25"));
            json.put("cursor_width", prefs.getString("cursor_width", "5"));
            json.put("liquid_cursor_enabled", prefs.getBoolean("liquid_cursor_enabled", false));
            json.put("liquid_scale_factor", prefs.getString("liquid_scale_factor", "15"));
            json.put("selection_cursor_effect", prefs.getInt("selection_cursor_effect", 0));
            json.put("selection_liquid_stretch", prefs.getString("selection_liquid_stretch", "60"));
            json.put("selection_liquid_side", prefs.getString("selection_liquid_side", "50"));
            json.put("ignore_spaces", prefs.getBoolean("ignore_spaces", true));
            json.put("animate_all_lines", prefs.getBoolean("animate_all_lines", false));
            json.put("debug_mode", prefs.getBoolean("debug_mode", false));

            Method updateMethod = coreClass.getDeclaredMethod("updateSettings", String.class);
            updateMethod.setAccessible(true);
            updateMethod.invoke(null, json.toString());
        } catch (Throwable e) {
            FileLog.e("Failed to sync TextAnimation settings: ", e);
        }
    }
}
