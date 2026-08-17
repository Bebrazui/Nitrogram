package org.nitrogram.mod;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import org.nitrogram.modsdk.ModSettingsHost;
import org.nitrogram.modsdk.ModSettingsScreen;

/**
 * Скелет мода Nitrogram. Замени заглушки своей логикой.
 *
 * Контракт (клиент вызывает эти методы):
 *   - apply()              — один раз при загрузке мода (здесь вешаем хуки на UI)
 *   - applySettings(json)  — при старте и при изменении настроек (JSON-объект)
 *   - createSettingsScreen — экран настроек (ModSettingsScreen) либо null
 *
 * Не забудь выставить "settings":true в метаданных mod.c, если есть экран настроек.
 */
public final class ModEntry {

    private static volatile boolean applied = false;

    // --- настройки (меняются из экрана настроек) ---
    private static boolean enabled = true;

    public static void apply() {
        if (applied || Build.VERSION.SDK_INT < 31) {
            return;
        }
        applied = true;
        try {
            Application app = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null);
            if (app != null) {
                // TODO: здесь подключи хуки к UI (Window.Callback, RecyclerView и т.д.)
                Toast.makeText(app, "Мод загружен", Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** Вызывается клиентом при загрузке и при каждом изменении настроек. */
    public static void applySettings(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        // TODO: разбери json и примени значения (см. пример motion-blur-fx)
        android.util.Log.i("MyMod", "applySettings: " + json);
    }

    /** Верни экран настроек либо null. */
    public static Object createSettingsScreen() {
        return new SettingsScreen();
    }

    // --- пример минимального экрана настроек ---
    private static final class SettingsScreen implements ModSettingsScreen {
        @Override
        public android.view.View createView(Context context, ModSettingsHost host, android.view.ViewGroup parent) {
            TextView tv = new TextView(context);
            tv.setText("Настройки моего мода");
            tv.setTextSize(16);
            // TODO: добавь SeekBar/Switch и вызывай host.setValue(...) при изменении
            return tv;
        }
    }
}
