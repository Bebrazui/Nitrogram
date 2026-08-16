package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramSettingsActivity extends UniversalFragment {

    private static final int BUTTON_VERSION = 1;
    private static final int BUTTON_BUILD = 2;
    private static final int BUTTON_ADS = 3;
    private static final int BUTTON_SORTING = 4;
    private static final int BUTTON_APPEARANCE = 5;
    private static final int BUTTON_PREMIUM = 6;
    private static final int BUTTON_PRIVACY = 7;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramSettings);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.NitrogramSettingsAbout)));
        items.add(UItem.asButton(BUTTON_VERSION, R.drawable.msg_recent, getString(R.string.NitrogramVersion), getVersionName()));
        items.add(UItem.asButton(BUTTON_BUILD, R.drawable.msg_customize, getString(R.string.NitrogramBuild), Build.MANUFACTURER + " / Android " + Build.VERSION.RELEASE));
        items.add(UItem.asShadow(getString(R.string.NitrogramSettingsAboutInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramSettingsTweaks)));
        items.add(UItem.asButton(BUTTON_ADS, R.drawable.msg_channel, getString(R.string.NitrogramAdsRemoved), getString(R.string.NitrogramEnabled)));
        items.add(UItem.asShadow(getString(R.string.NitrogramAdsRemovedInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramSettingsControl)));
        items.add(UItem.asButton(BUTTON_SORTING, R.drawable.settings_folders, getString(R.string.NitrogramSorting), getString(R.string.NitrogramSortingInfo)));
        items.add(UItem.asButton(BUTTON_APPEARANCE, R.drawable.settings_chat, getString(R.string.NitrogramCustomization), getString(R.string.NitrogramCustomizationInfo)));
        items.add(UItem.asButton(BUTTON_PRIVACY, R.drawable.msg_permissions, getString(R.string.NitrogramPrivacyLimits), getString(R.string.NitrogramPrivacyLimitsInfo)));
        items.add(UItem.asButton(BUTTON_PREMIUM, R.drawable.settings_premium, getString(R.string.NitrogramPremiumVisual), getPremiumVisualSummary()));
        items.add(UItem.asShadow(getString(R.string.NitrogramSettingsControlInfo)));
    }

    @Override
    protected void onClick(UItem item, android.view.View view, int position, float x, float y) {
        if (item.id == BUTTON_SORTING) {
            presentFragment(new NitrogramSortingActivity());
        } else if (item.id == BUTTON_APPEARANCE) {
            presentFragment(new NitrogramCustomizationActivity());
        } else if (item.id == BUTTON_PRIVACY) {
            presentFragment(new NitrogramPrivacyActivity());
        } else if (item.id == BUTTON_PREMIUM) {
            presentFragment(new NitrogramPremiumVisualActivity());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, android.view.View view, int position, float x, float y) {
        return false;
    }

    private String getVersionName() {
        try {
            PackageManager pm = ApplicationLoader.applicationContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return info.versionName + " (" + info.versionCode + ")";
        } catch (Exception ignore) {
            return BuildVars.BUILD_VERSION_STRING;
        }
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
