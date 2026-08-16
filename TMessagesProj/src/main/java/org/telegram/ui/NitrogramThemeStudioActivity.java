package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColorPicker;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramThemeStudioActivity extends UniversalFragment {

    private static final int BUTTON_ACCENT = 1;
    private static final int BUTTON_BACKGROUND = 2;
    private static final int BUTTON_FULL_EDITOR = 3;
    private static final int BUTTON_CREATE_THEME = 4;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramThemeStudio);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.NitrogramThemeStudio)));
        items.add(UItem.asButton(BUTTON_ACCENT, R.drawable.msg_palette, getString(R.string.NitrogramAccentColor), colorToHex(getCurrentAccentColor())));
        items.add(UItem.asButton(BUTTON_BACKGROUND, R.drawable.msg_background, getString(R.string.NitrogramBackgroundColor), colorToHex(getCurrentBackgroundColor())));
        items.add(UItem.asButton(BUTTON_FULL_EDITOR, R.drawable.msg_customize, getString(R.string.NitrogramOpenFullEditor), Theme.getCurrentThemeName()));
        items.add(UItem.asButton(BUTTON_CREATE_THEME, R.drawable.msg_add, getString(R.string.NitrogramCreateTheme), getString(R.string.NitrogramCreateThemeInfo)));
        items.add(UItem.asShadow(getString(R.string.NitrogramThemeStudioInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == BUTTON_ACCENT) {
            openColorPicker(false);
        } else if (item.id == BUTTON_BACKGROUND) {
            openColorPicker(true);
        } else if (item.id == BUTTON_FULL_EDITOR) {
            boolean night = Theme.isCurrentThemeNight();
            Theme.ThemeInfo currentTheme = night ? Theme.getCurrentNightTheme() : Theme.getCurrentTheme();
            if (currentTheme == null) {
                return;
            }
            Theme.ThemeAccent accent = currentTheme.getAccent(false);
            presentFragment(new ThemePreviewActivity(currentTheme, false, ThemePreviewActivity.SCREEN_TYPE_ACCENT_COLOR, accent != null && accent.id >= 100, night));
        } else if (item.id == BUTTON_CREATE_THEME) {
            org.telegram.ui.Components.AlertsCreator.createThemeCreateDialog(this, 0, null, null);
        } else {
            return;
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void openColorPicker(boolean background) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        ColorPicker picker = new ColorPicker(context, false, new ColorPicker.ColorPickerDelegate() {
            @Override
            public void setColor(int color, int num, boolean applyNow) {
                if (background) {
                    applyBackgroundColor(color);
                } else {
                    applyAccentColor(color);
                }
                if (applyNow && listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
            }
        });
        picker.setType(background ? 2 : 1, false, 1, 1, false, 0, false);
        picker.setColor(background ? getCurrentBackgroundColor() : getCurrentAccentColor(), 0);

        FrameLayout container = new FrameLayout(context);
        container.addView(picker, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(0, AndroidUtilities.dp(8), 0, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(background ? R.string.NitrogramBackgroundColor : R.string.NitrogramAccentColor));
        builder.setView(container);
        builder.setPositiveButton(getString(R.string.Done), null);
        showDialog(builder.create());
    }

    private void applyAccentColor(int color) {
        Theme.ThemeInfo themeInfo = Theme.getCurrentTheme();
        Theme.ThemeAccent accent = getEditableAccent(themeInfo);
        if (accent == null) {
            return;
        }
        accent.accentColor = color;
        Theme.saveThemeAccents(themeInfo, true, false, false, false);
        Theme.refreshThemeColors();
    }

    private void applyBackgroundColor(int color) {
        Theme.ThemeInfo themeInfo = Theme.getCurrentTheme();
        Theme.ThemeAccent accent = getEditableAccent(themeInfo);
        if (accent == null) {
            return;
        }
        accent.backgroundOverrideColor = color;
        accent.backgroundGradientOverrideColor1 = 0;
        accent.backgroundGradientOverrideColor2 = 0;
        accent.backgroundGradientOverrideColor3 = 0;
        accent.backgroundRotation = 0;
        accent.patternSlug = Theme.COLOR_BACKGROUND_SLUG;
        accent.patternIntensity = 0f;
        accent.patternMotion = false;
        Theme.saveThemeAccents(themeInfo, true, false, false, false);
        Theme.refreshThemeColors(true, false);
    }

    private Theme.ThemeAccent getEditableAccent(Theme.ThemeInfo themeInfo) {
        if (themeInfo == null) {
            return null;
        }
        Theme.ThemeAccent accent = themeInfo.getAccent(false);
        if (accent == null) {
            return null;
        }
        if (accent.id < 100) {
            accent = themeInfo.getAccent(true);
            Theme.saveThemeAccents(themeInfo, true, false, false, false);
        }
        return accent;
    }

    private int getCurrentAccentColor() {
        Theme.ThemeAccent accent = Theme.getCurrentTheme().getAccent(false);
        if (accent != null && accent.accentColor != 0) {
            return accent.accentColor;
        }
        return Theme.getDefaultAccentColor(Theme.key_windowBackgroundWhiteBlueText4);
    }

    private int getCurrentBackgroundColor() {
        Theme.ThemeAccent accent = Theme.getCurrentTheme().getAccent(false);
        if (accent != null && (int) accent.backgroundOverrideColor != 0) {
            return (int) accent.backgroundOverrideColor;
        }
        return Theme.getDefaultAccentColor(Theme.key_chat_wallpaper);
    }

    private String colorToHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
