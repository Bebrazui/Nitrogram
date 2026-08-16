package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ModManager;
import org.telegram.ui.ActionBar.Theme;

import java.io.File;

/**
 * Shown when a chat document ending with ".so" is tapped. Parses the embedded
 * mod metadata and lets the user install/apply it.
 */
public class ModPreviewActivity extends Activity {

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_NAME = "name";

    private File soFile;
    private ModManager.ModMeta meta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String path = intent.getStringExtra(EXTRA_PATH);
        soFile = path != null ? new File(path) : null;
        if (soFile == null || !soFile.exists()) {
            Toast.makeText(this, "Файл мода не найден. Сначала скачайте его.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        meta = ModManager.parseMeta(soFile);
        buildUI();
    }

    private void buildUI() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = AndroidUtilities.dp(16);
        layout.setPadding(pad, pad, pad, pad);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        if (meta == null) {
            TextView warn = new TextView(this);
            warn.setText("Этот .so не содержит метаданных Nitrogram-мода.\n\nОжидается блок:\nNITROGRAM_MOD_META_START\n{ ... }\nNITROGRAM_MOD_META_END");
            warn.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            warn.setTextSize(16);
            layout.addView(warn);
        } else {
            if (meta.icon != null) {
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(meta.icon);
                int sz = AndroidUtilities.dp(96);
                layout.addView(iv, new LinearLayout.LayoutParams(sz, sz));
            }

            TextView name = new TextView(this);
            name.setText(meta.name);
            name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            name.setTextSize(22);
            name.setGravity(Gravity.CENTER);
            layout.addView(name);

            if (!meta.version.isEmpty()) {
                TextView ver = new TextView(this);
                ver.setText("Версия: " + meta.version);
                ver.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                ver.setTextSize(14);
                layout.addView(ver);
            }

            if (!meta.description.isEmpty()) {
                TextView desc = new TextView(this);
                desc.setText(meta.description);
                desc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                desc.setTextSize(16);
                desc.setPadding(0, AndroidUtilities.dp(8), 0, 0);
                layout.addView(desc);
            }

            if (!meta.extra.isEmpty()) {
                TextView extra = new TextView(this);
                extra.setText("Доп. сведения: " + meta.extra);
                extra.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                extra.setTextSize(13);
                extra.setPadding(0, AndroidUtilities.dp(8), 0, 0);
                layout.addView(extra);
            }

            Button install = new Button(this);
            install.setText("Установить и применить");
            install.setOnClickListener(v -> {
                boolean ok = ModManager.installMod(soFile, meta);
                Toast.makeText(ModPreviewActivity.this,
                        ok ? "Мод установлен. Перезапустите приложение для полного применения."
                                : "Не удалось установить мод", Toast.LENGTH_LONG).show();
                if (ok) {
                    finish();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, AndroidUtilities.dp(16), 0, 0);
            layout.addView(install, lp);
        }

        scroll.addView(layout);
        root.addView(scroll);
        setContentView(root);
        getWindow().setBackgroundDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)));
    }
}
