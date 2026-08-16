package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramPremiumVisualActivity extends UniversalFragment {

    private static final int SWITCH_VISUAL_MODE = 1;
    private static final int BUTTON_VISUAL_STATUS = 2;
    private static final int SWITCH_BADGES = 3;
    private static final int SWITCH_GRADIENT = 4;
    private static final int SWITCH_GLOW = 5;
    private static final int SWITCH_ANIMATED = 6;
    private static final int RADIO_CLASSIC = 10;
    private static final int RADIO_NITRO = 11;
    private static final int RADIO_CRYSTAL = 12;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramPremiumVisual);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        int tabStyle = NitrogramConfig.getPremiumTabStyle();
        boolean visualModeEnabled = NitrogramConfig.isPremiumVisualModeEnabled();
        int enabledEffects = NitrogramConfig.getEnabledPremiumVisualEffectsCount();

        items.add(UItem.asHeader(getString(R.string.NitrogramPremiumVisualMode)));
        items.add(UItem.asSwitch(SWITCH_VISUAL_MODE, getString(R.string.NitrogramPremiumVisualEnabled)).setChecked(visualModeEnabled));
        items.add(UItem.asButton(BUTTON_VISUAL_STATUS, R.drawable.settings_premium, getString(R.string.NitrogramPremiumStatus), getPremiumVisualSummary()));
        items.add(UItem.asShadow(getString(visualModeEnabled ? R.string.NitrogramPremiumVisualEnabledInfo : R.string.NitrogramPremiumVisualDisabledInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramPremiumEffects)));
        items.add(UItem.asSwitch(SWITCH_BADGES, getString(R.string.NitrogramPremiumBadges)).setChecked(NitrogramConfig.isShowPremiumBadgesEnabled()));
        items.add(UItem.asSwitch(SWITCH_GRADIENT, getString(R.string.NitrogramPremiumGradient)).setChecked(NitrogramConfig.isShowPremiumGradientEnabled()));
        items.add(UItem.asSwitch(SWITCH_GLOW, getString(R.string.NitrogramProfileGlow)).setChecked(NitrogramConfig.isShowProfileGlowEnabled()));
        items.add(UItem.asSwitch(SWITCH_ANIMATED, getString(R.string.NitrogramAnimatedPremiumIcons)).setChecked(NitrogramConfig.isShowAnimatedPremiumIconsEnabled()));
        items.add(UItem.asShadow(LocaleController.formatString(R.string.NitrogramPremiumEffectsSummaryFormat, enabledEffects)));

        items.add(UItem.asHeader(getString(R.string.NitrogramPremiumTabStyle)));
        items.add(UItem.asRadio(RADIO_CLASSIC, getString(R.string.NitrogramPremiumTabClassic)).setChecked(tabStyle == NitrogramConfig.PREMIUM_TAB_CLASSIC));
        items.add(UItem.asRadio(RADIO_NITRO, getString(R.string.NitrogramPremiumTabNitro)).setChecked(tabStyle == NitrogramConfig.PREMIUM_TAB_NITRO));
        items.add(UItem.asRadio(RADIO_CRYSTAL, getString(R.string.NitrogramPremiumTabCrystal)).setChecked(tabStyle == NitrogramConfig.PREMIUM_TAB_CRYSTAL));
        if (tabStyle == NitrogramConfig.PREMIUM_TAB_CLASSIC) {
            items.add(UItem.asShadow(getString(R.string.NitrogramPremiumTabClassicInfo)));
        } else if (tabStyle == NitrogramConfig.PREMIUM_TAB_NITRO) {
            items.add(UItem.asShadow(getString(R.string.NitrogramPremiumTabNitroInfo)));
        } else {
            items.add(UItem.asShadow(getString(R.string.NitrogramPremiumTabCrystalInfo)));
        }
        items.add(UItem.asShadow(getString(R.string.NitrogramPremiumVisualInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == SWITCH_VISUAL_MODE || item.id == BUTTON_VISUAL_STATUS) {
            NitrogramConfig.setPremiumVisualModeEnabled(!NitrogramConfig.isPremiumVisualModeEnabled());
        } else if (item.id == SWITCH_BADGES) {
            NitrogramConfig.setShowPremiumBadgesEnabled(!NitrogramConfig.isShowPremiumBadgesEnabled());
        } else if (item.id == SWITCH_GRADIENT) {
            NitrogramConfig.setShowPremiumGradientEnabled(!NitrogramConfig.isShowPremiumGradientEnabled());
        } else if (item.id == SWITCH_GLOW) {
            NitrogramConfig.setShowProfileGlowEnabled(!NitrogramConfig.isShowProfileGlowEnabled());
        } else if (item.id == SWITCH_ANIMATED) {
            NitrogramConfig.setShowAnimatedPremiumIconsEnabled(!NitrogramConfig.isShowAnimatedPremiumIconsEnabled());
        } else if (item.id == RADIO_CLASSIC) {
            NitrogramConfig.setPremiumTabStyle(NitrogramConfig.PREMIUM_TAB_CLASSIC);
        } else if (item.id == RADIO_NITRO) {
            NitrogramConfig.setPremiumTabStyle(NitrogramConfig.PREMIUM_TAB_NITRO);
        } else if (item.id == RADIO_CRYSTAL) {
            NitrogramConfig.setPremiumTabStyle(NitrogramConfig.PREMIUM_TAB_CRYSTAL);
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
