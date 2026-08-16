package org.telegram.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LauncherIconController;

import java.util.ArrayList;

public class NitrogramCustomizationActivity extends UniversalFragment {

    private static final int RADIO_THEME_SYSTEM = 1;
    private static final int RADIO_THEME_NITRO = 2;
    private static final int RADIO_THEME_MIDNIGHT = 3;
    private static final int BUTTON_CREATE_THEME = 4;
    private static final int BUTTON_EDIT_THEME = 5;
    private static final int BUTTON_THEME_STUDIO = 6;
    private static final int BUTTON_DEVELOPER = 7;
    private static final int RADIO_ICON_DEFAULT = 10;
    private static final int RADIO_ICON_VINTAGE = 11;
    private static final int RADIO_ICON_AQUA = 12;
    private static final int RADIO_ICON_PREMIUM = 13;
    private static final int RADIO_ICON_TURBO = 14;
    private static final int RADIO_ICON_NOX = 15;
    private static final int SLIDER_CHAT_DENSITY = 30;
    private static final int RADIO_SWIPE_ARCHIVE = 40;
    private static final int RADIO_SWIPE_READ = 41;
    private static final int RADIO_SWIPE_MUTE = 42;
    private static final int SWITCH_HIDE_STORIES = 50;
    private static final int SWITCH_HIDE_REACTIONS = 51;
    private static final int BUTTON_PREMIUM = 60;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramCustomization);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        int themeMode = NitrogramConfig.getThemeMode();
        int density = NitrogramConfig.getChatDensity();
        int swipeAction = NitrogramConfig.getSwipeAction();
        LauncherIconController.LauncherIcon currentIcon = NitrogramConfig.getCurrentLauncherIcon();

        items.add(UItem.asHeader(getString(R.string.NitrogramThemes)));
        items.add(UItem.asRadio(RADIO_THEME_SYSTEM, getString(R.string.NitrogramThemeSystem)).setChecked(themeMode == NitrogramConfig.THEME_SYSTEM));
        items.add(UItem.asRadio(RADIO_THEME_NITRO, getString(R.string.NitrogramThemeNitro)).setChecked(themeMode == NitrogramConfig.THEME_NITRO));
        items.add(UItem.asRadio(RADIO_THEME_MIDNIGHT, getString(R.string.NitrogramThemeMidnight)).setChecked(themeMode == NitrogramConfig.THEME_MIDNIGHT));
        items.add(UItem.asButton(BUTTON_THEME_STUDIO, R.drawable.msg_palette, getString(R.string.NitrogramThemeStudio), getString(R.string.NitrogramThemeStudioInfo)));
        items.add(UItem.asButton(BUTTON_CREATE_THEME, R.drawable.msg_add, getString(R.string.NitrogramCreateTheme), getString(R.string.NitrogramCreateThemeInfo)));
        items.add(UItem.asButton(BUTTON_EDIT_THEME, R.drawable.msg_customize, getString(R.string.NitrogramEditCurrentTheme), Theme.getCurrentThemeName()));
        if (themeMode == NitrogramConfig.THEME_SYSTEM) {
            items.add(UItem.asShadow(getString(R.string.NitrogramThemeSystemInfo)));
        } else if (themeMode == NitrogramConfig.THEME_NITRO) {
            items.add(UItem.asShadow(getString(R.string.NitrogramThemeNitroInfo)));
        } else {
            items.add(UItem.asShadow(getString(R.string.NitrogramThemeMidnightInfo)));
        }
        items.add(UItem.asShadow(getString(R.string.NitrogramThemesInfo)));

        items.add(UItem.asHeader(getString(R.string.AppIcon)));
        items.add(UItem.asRadio(RADIO_ICON_DEFAULT, getString(R.string.AppIconDefault)).setChecked(currentIcon == LauncherIconController.LauncherIcon.DEFAULT));
        items.add(UItem.asRadio(RADIO_ICON_VINTAGE, getString(R.string.AppIconVintage)).setChecked(currentIcon == LauncherIconController.LauncherIcon.VINTAGE));
        items.add(UItem.asRadio(RADIO_ICON_AQUA, getString(R.string.AppIconAqua)).setChecked(currentIcon == LauncherIconController.LauncherIcon.AQUA));
        items.add(UItem.asRadio(RADIO_ICON_PREMIUM, getString(R.string.AppIconPremium)).setChecked(currentIcon == LauncherIconController.LauncherIcon.PREMIUM));
        items.add(UItem.asRadio(RADIO_ICON_TURBO, getString(R.string.AppIconTurbo)).setChecked(currentIcon == LauncherIconController.LauncherIcon.TURBO));
        items.add(UItem.asRadio(RADIO_ICON_NOX, getString(R.string.AppIconNox)).setChecked(currentIcon == LauncherIconController.LauncherIcon.NOX));
        items.add(UItem.asShadow(getString(R.string.NitrogramAppIconInfo)));

        items.add(UItem.asHeader(getString(R.string.BubbleRadius)));
        items.add(UItem.asIntSlideView(0, 8, NitrogramConfig.getBubbleRadius(), 32, value -> formatString(R.string.NitrogramCornersValue, value), NitrogramConfig::setBubbleRadius));
        items.add(UItem.asShadow(getString(R.string.NitrogramCornersInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramChatDensity)));
        items.add(UItem.asIntSlideView(
            SLIDER_CHAT_DENSITY,
            NitrogramConfig.MIN_CHAT_DENSITY,
            density,
            NitrogramConfig.MAX_CHAT_DENSITY,
            value -> formatString(R.string.NitrogramDensityValue, value),
            NitrogramConfig::setChatDensity
        ));
        items.add(UItem.asShadow(getString(R.string.NitrogramDensitySliderInfo)));
        items.add(UItem.asShadow(getString(R.string.NitrogramCustomizationPanelInfo)));

        items.add(UItem.asHeader(getString(R.string.ChatListSwipeGesture)));
        items.add(UItem.asRadio(RADIO_SWIPE_ARCHIVE, getString(R.string.NitrogramSwipeArchive)).setChecked(swipeAction == NitrogramConfig.SWIPE_ARCHIVE));
        items.add(UItem.asRadio(RADIO_SWIPE_READ, getString(R.string.NitrogramSwipeRead)).setChecked(swipeAction == NitrogramConfig.SWIPE_READ));
        items.add(UItem.asRadio(RADIO_SWIPE_MUTE, getString(R.string.NitrogramSwipeMute)).setChecked(swipeAction == NitrogramConfig.SWIPE_MUTE));
        if (swipeAction == NitrogramConfig.SWIPE_ARCHIVE) {
            items.add(UItem.asShadow(getString(R.string.NitrogramSwipeArchiveInfo)));
        } else if (swipeAction == NitrogramConfig.SWIPE_READ) {
            items.add(UItem.asShadow(getString(R.string.NitrogramSwipeReadInfo)));
        } else {
            items.add(UItem.asShadow(getString(R.string.NitrogramSwipeMuteInfo)));
        }
        items.add(UItem.asShadow(getString(R.string.NitrogramSwipeInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramMinimalUi)));
        items.add(UItem.asSwitch(SWITCH_HIDE_STORIES, getString(R.string.NitrogramHideStories)).setChecked(NitrogramConfig.isHideStoriesEnabled()));
        items.add(UItem.asSwitch(SWITCH_HIDE_REACTIONS, getString(R.string.NitrogramHideReactions)).setChecked(NitrogramConfig.isHideReactionsEnabled()));
        items.add(UItem.asShadow(getString(R.string.NitrogramMinimalUiInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramPremiumVisual)));
        items.add(UItem.asButton(BUTTON_PREMIUM, R.drawable.settings_premium, getString(R.string.NitrogramPremiumVisual), getPremiumVisualSummary()));
        items.add(UItem.asShadow(getString(R.string.NitrogramPremiumPanelInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramDeveloperSettings)));
        items.add(UItem.asButton(BUTTON_DEVELOPER, R.drawable.msg_permissions, getString(R.string.NitrogramDeveloperSettings), getString(R.string.NitrogramDeveloperSettingsInfo)));
        items.add(UItem.asShadow(getString(R.string.NitrogramDeveloperSettingsInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == RADIO_THEME_SYSTEM) {
            NitrogramConfig.setThemeMode(NitrogramConfig.THEME_SYSTEM);
        } else if (item.id == RADIO_THEME_NITRO) {
            NitrogramConfig.setThemeMode(NitrogramConfig.THEME_NITRO);
        } else if (item.id == RADIO_THEME_MIDNIGHT) {
            NitrogramConfig.setThemeMode(NitrogramConfig.THEME_MIDNIGHT);
        } else if (item.id == BUTTON_THEME_STUDIO) {
            presentFragment(new NitrogramThemeStudioActivity());
        } else if (item.id == BUTTON_CREATE_THEME) {
            AlertsCreator.createThemeCreateDialog(this, 0, null, null);
        } else if (item.id == BUTTON_EDIT_THEME) {
            boolean night = Theme.isCurrentThemeNight();
            Theme.ThemeInfo currentTheme = night ? Theme.getCurrentNightTheme() : Theme.getCurrentTheme();
            if (currentTheme == null) {
                return;
            }
            Theme.ThemeAccent accent = currentTheme.getAccent(false);
            presentFragment(new ThemePreviewActivity(currentTheme, false, ThemePreviewActivity.SCREEN_TYPE_ACCENT_COLOR, accent != null && accent.id >= 100, night));
        } else if (item.id == BUTTON_DEVELOPER) {
            presentFragment(new NitrogramDeveloperSettingsActivity());
        } else if (item.id == RADIO_ICON_DEFAULT) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.DEFAULT);
        } else if (item.id == RADIO_ICON_VINTAGE) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.VINTAGE);
        } else if (item.id == RADIO_ICON_AQUA) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.AQUA);
        } else if (item.id == RADIO_ICON_PREMIUM) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.PREMIUM);
        } else if (item.id == RADIO_ICON_TURBO) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.TURBO);
        } else if (item.id == RADIO_ICON_NOX) {
            NitrogramConfig.setCurrentLauncherIcon(LauncherIconController.LauncherIcon.NOX);
        } else if (item.id == RADIO_SWIPE_ARCHIVE) {
            NitrogramConfig.setSwipeAction(NitrogramConfig.SWIPE_ARCHIVE);
        } else if (item.id == RADIO_SWIPE_READ) {
            NitrogramConfig.setSwipeAction(NitrogramConfig.SWIPE_READ);
        } else if (item.id == RADIO_SWIPE_MUTE) {
            NitrogramConfig.setSwipeAction(NitrogramConfig.SWIPE_MUTE);
        } else if (item.id == SWITCH_HIDE_STORIES) {
            NitrogramConfig.setHideStoriesEnabled(!NitrogramConfig.isHideStoriesEnabled());
        } else if (item.id == SWITCH_HIDE_REACTIONS) {
            NitrogramConfig.setHideReactionsEnabled(!NitrogramConfig.isHideReactionsEnabled());
        } else if (item.id == BUTTON_PREMIUM) {
            presentFragment(new NitrogramPremiumVisualActivity());
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

    private String getPremiumVisualSummary() {
        if (!NitrogramConfig.isPremiumVisualModeEnabled()) {
            return getString(R.string.NitrogramPremiumStatusDisabled);
        }
        return LocaleController.formatString(
            R.string.NitrogramPremiumSummaryFormat,
            getPremiumStyleLabel(),
            NitrogramConfig.getEnabledPremiumVisualEffectsCount()
        );
    }

    private String getPremiumStyleLabel() {
        int style = NitrogramConfig.getPremiumTabStyle();
        if (style == NitrogramConfig.PREMIUM_TAB_CLASSIC) {
            return getString(R.string.NitrogramPremiumTabClassic);
        } else if (style == NitrogramConfig.PREMIUM_TAB_CRYSTAL) {
            return getString(R.string.NitrogramPremiumTabCrystal);
        }
        return getString(R.string.NitrogramPremiumTabNitro);
    }
}
