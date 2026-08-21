package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ModManager;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;

/**
 * Shown when a chat document ending with ".so" is tapped. Parses the embedded
 * mod metadata and lets the user install/apply it. Presented as a bottom sheet
 * that slides up, dims + blurs the backdrop and occupies a fixed share of the
 * screen height (rounded on top only). All content is compact and fits without
 * scrolling.
 */
public class ModPreviewActivity extends BottomSheet {

    private final Context ctx;
    private final File soFile;
    private final ModManager.ModMeta meta;
    private boolean applyNow = true;

    public ModPreviewActivity(Context context, String path, String name) {
        super(context, false);
        this.ctx = context;
        this.soFile = path != null ? new File(path) : null;
        this.meta = soFile != null && soFile.exists() ? ModManager.parseMeta(soFile) : null;

        setApplyTopPadding(false);
        setApplyBottomPadding(false);
        setDimBehindAlpha(38);

        // Make the sheet's own panel background transparent so our rounded
        // content view is the only visible panel.
        shadowDrawable = new ColorDrawable(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 31) {
            try {
                java.lang.reflect.Method m = android.view.Window.class.getMethod("setBlurBehindRadius", int.class);
                m.invoke(getWindow(), AndroidUtilities.dp(18));
            } catch (Throwable ignored) {
            }
        }

        int sheetH = (int) (AndroidUtilities.displaySize.y * 0.42f);

        SheetLayout root = new SheetLayout(context, sheetH);
        root.setOrientation(LinearLayout.VERTICAL);
        int r = AndroidUtilities.dp(28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_dialogBackground));
        bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        root.setBackground(bg);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(14));

        // Drag handle
        View handle = new View(context);
        handle.setBackgroundColor(Theme.getColor(Theme.key_sheet_scrollUp));
        root.addView(handle, LayoutHelper.createLinear(36, 4, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 6));

        if (meta == null) {
            TextView warn = new TextView(context);
            warn.setText("Этот .so не содержит метаданных Nitrogram-мода.\n\nОжидается блок:\nNITROGRAM_MOD_META_START\n{ ... }\nNITROGRAM_MOD_META_END");
            warn.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            warn.setTextSize(14);
            root.addView(warn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 6, 0, 0));
            setCustomView(root);
            return;
        }

        int accent = Theme.getColor(Theme.key_chats_actionBackground);

        // Header: small icon + name/version
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(context);
        int iconSize = AndroidUtilities.dp(36);
        if (meta.icon != null) {
            icon.setImageBitmap(meta.icon);
        } else {
            GradientDrawable cd = new GradientDrawable();
            cd.setShape(GradientDrawable.OVAL);
            cd.setColor(accent);
            icon.setBackground(cd);
        }
        header.addView(icon, LayoutHelper.createLinear(iconSize, iconSize, Gravity.CENTER_VERTICAL));

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(AndroidUtilities.dp(10), 0, 0, 0);

        TextView nameView = new TextView(context);
        nameView.setText(meta.name);
        nameView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameView.setTextSize(17);
        nameView.setTypeface(AndroidUtilities.bold());
        info.addView(nameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (!meta.version.isEmpty()) {
            TextView ver = new TextView(context);
            ver.setText("Версия " + meta.version);
            ver.setTextColor(Theme.getColor(Theme.key_dialogTextHint));
            ver.setTextSize(12);
            info.addView(ver, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));
        }
        header.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        root.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

        // Description (clamped to 2 lines, no scroll)
        if (!meta.description.isEmpty()) {
            TextView desc = new TextView(context);
            desc.setText(meta.description);
            desc.setTextColor(Theme.getColor(Theme.key_dialogTextHint));
            desc.setTextSize(13);
            desc.setMaxLines(2);
            desc.setEllipsize(android.text.TextUtils.TruncateAt.END);
            root.addView(desc, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 0));
        }

        // Spacer pushes the controls to the bottom
        View spacer = new View(context);
        root.addView(spacer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f, 0, 4, 0, 0));

        // Apply-after-install checkbox
        TextCheckCell check = new TextCheckCell(context);
        check.setTextAndCheck("Применить и запустить сразу", applyNow, false);
        check.setOnClickListener(v -> {
            applyNow = !applyNow;
            check.setChecked(applyNow);
        });
        root.addView(check, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

        boolean isInstalled = ModManager.isModInstalled(meta.id);

        // Install button (always at the bottom)
        TextView install = new TextView(context);
        install.setGravity(Gravity.CENTER);
        install.setText(isInstalled ? "Обновить и запустить мод" : "Установить и запустить мод");
        install.setTextColor(0xffffffff);
        install.setTextSize(15);
        install.setTypeface(AndroidUtilities.bold());
        install.setBackground(Theme.createSimpleSelectorRoundRectDrawable(
                AndroidUtilities.dp(10), accent, ColorUtils.setAlphaComponent(accent, 200)));
        install.setOnClickListener(v -> installMod());
        root.addView(install, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, 0, 8, 0, 0));

        setCustomView(root);
    }

    private void installMod() {
        try {
            File dest = ModManager.installMod(soFile, meta);
            if (dest == null) {
                Toast.makeText(ctx, "Не удалось установить мод", Toast.LENGTH_SHORT).show();
                return;
            }
            ModManager.setEnabled(meta.id, true);
            boolean loaded = ModManager.loadNative(dest);
            if (loaded) {
                ModManager.registerLoadedMod(meta.id);
                Toast.makeText(ctx, "Мод успешно установлен и запущен!", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                String err = ModManager.getLastError();
                showModErrorDialog(err != null ? err : "Не удалось загрузить бинарный файл мода (.so). Убедитесь, что архитектура устройства поддерживает ARM64.");
            }
        } catch (Throwable t) {
            FileLog.e(t);
            showModErrorDialog(android.util.Log.getStackTraceString(t));
        }
    }

    private void showModErrorDialog(String errorLog) {
        if (ctx == null) return;
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(ctx);
        builder.setTitle("Ошибка запуска мода");
        builder.setMessage(errorLog);
        builder.setPositiveButton("Скопировать лог ошибки", (dialog, which) -> {
            try {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Mod Error Log", errorLog);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(ctx, "Лог ошибки скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
            } catch (Throwable ignore) {
            }
        });
        builder.setNegativeButton("Закрыть", null);
        builder.show();
    }

    private static class SheetLayout extends LinearLayout {
        private final int fixedH;

        public SheetLayout(Context context, int h) {
            super(context);
            fixedH = h;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // Force a fixed height so the weight-spacer can never stretch the
            // sheet to fill the screen. Children are measured against this exact
            // height, keeping the install button pinned at the bottom.
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(fixedH, View.MeasureSpec.AT_MOST));
            if (getMeasuredHeight() != fixedH) {
                setMeasuredDimension(getMeasuredWidth(), fixedH);
            }
        }
    }
}
