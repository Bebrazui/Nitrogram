package org.telegram.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.ColorPicker;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramDeveloperSettingsActivity extends UniversalFragment {

    private static final int SWITCH_HIDE_AVATARS = 1;
    private static final int SWITCH_HIDE_STATUS = 2;
    private static final int SWITCH_HIDE_PINS = 3;
    private static final int SLIDER_AVATAR_RADIUS = 4;
    private static final int SLIDER_DIALOG_PADDING = 5;
    private static final int SLIDER_DIALOG_TEXT_SPACING = 6;
    private static final int SLIDER_UNREAD_BADGE_SCALE = 7;
    private static final int SLIDER_BUBBLE_TOP_RADIUS = 8;
    private static final int SLIDER_BUBBLE_BOTTOM_RADIUS = 9;
    private static final int BUTTON_OUTGOING_BUBBLE_COLOR = 10;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramDeveloperSettings);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.NitrogramDeveloperSettings)));
        items.add(UItem.asSwitch(SWITCH_HIDE_AVATARS, getString(R.string.NitrogramDevHideDialogAvatars)).setChecked(NitrogramConfig.isDeveloperHideDialogAvatarsEnabled()));
        items.add(UItem.asSwitch(SWITCH_HIDE_STATUS, getString(R.string.NitrogramDevHideStatusIcons)).setChecked(NitrogramConfig.isDeveloperHideStatusIconsEnabled()));
        items.add(UItem.asSwitch(SWITCH_HIDE_PINS, getString(R.string.NitrogramDevHidePinnedMarkers)).setChecked(NitrogramConfig.isDeveloperHidePinnedMarkersEnabled()));
        items.add(UItem.asIntSlideView(
            SLIDER_AVATAR_RADIUS,
            NitrogramConfig.MIN_DIALOG_AVATAR_RADIUS,
            NitrogramConfig.getDeveloperDialogAvatarRadius(),
            NitrogramConfig.MAX_DIALOG_AVATAR_RADIUS,
            value -> formatString(R.string.NitrogramDevAvatarRadiusValue, value),
            NitrogramConfig::setDeveloperDialogAvatarRadius
        ));
        items.add(UItem.asIntSlideView(
            SLIDER_DIALOG_PADDING,
            NitrogramConfig.MIN_DIALOG_PADDING,
            NitrogramConfig.getDeveloperDialogPadding(),
            NitrogramConfig.MAX_DIALOG_PADDING,
            value -> formatString(R.string.NitrogramDevDialogPaddingValue, value),
            NitrogramConfig::setDeveloperDialogPadding
        ));
        items.add(UItem.asIntSlideView(
            SLIDER_DIALOG_TEXT_SPACING,
            NitrogramConfig.MIN_DIALOG_TEXT_SPACING,
            NitrogramConfig.getDeveloperDialogTextSpacing(),
            NitrogramConfig.MAX_DIALOG_TEXT_SPACING,
            value -> formatString(R.string.NitrogramDevDialogTextSpacingValue, value),
            NitrogramConfig::setDeveloperDialogTextSpacing
        ));
        items.add(UItem.asIntSlideView(
            SLIDER_UNREAD_BADGE_SCALE,
            NitrogramConfig.MIN_UNREAD_BADGE_SCALE,
            NitrogramConfig.getDeveloperUnreadBadgeScale(),
            NitrogramConfig.MAX_UNREAD_BADGE_SCALE,
            value -> formatString(R.string.NitrogramDevUnreadBadgeScaleValue, value),
            NitrogramConfig::setDeveloperUnreadBadgeScale
        ));
        items.add(UItem.asIntSlideView(
            SLIDER_BUBBLE_TOP_RADIUS,
            NitrogramConfig.MIN_DEV_BUBBLE_RADIUS,
            NitrogramConfig.getDeveloperBubbleTopRadius(),
            NitrogramConfig.MAX_DEV_BUBBLE_RADIUS,
            value -> formatString(R.string.NitrogramDevBubbleRadiusValue, value),
            NitrogramConfig::setDeveloperBubbleTopRadius
        ));
        items.add(UItem.asIntSlideView(
            SLIDER_BUBBLE_BOTTOM_RADIUS,
            NitrogramConfig.MIN_DEV_BUBBLE_RADIUS,
            NitrogramConfig.getDeveloperBubbleBottomRadius(),
            NitrogramConfig.MAX_DEV_BUBBLE_RADIUS,
            value -> formatString(R.string.NitrogramDevBubbleRadiusValue, value),
            NitrogramConfig::setDeveloperBubbleBottomRadius
        ));
        items.add(UItem.asButton(
            BUTTON_OUTGOING_BUBBLE_COLOR,
            R.drawable.msg_palette,
            getString(R.string.NitrogramDevOutgoingBubbleColor),
            NitrogramConfig.hasDeveloperOutgoingBubbleColorOverride()
                ? colorToHex(NitrogramConfig.getDeveloperOutgoingBubbleColor())
                : getString(R.string.NitrogramDefaultValue)
        ));
        items.add(UItem.asShadow(getString(R.string.NitrogramDeveloperSettingsInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == SWITCH_HIDE_AVATARS) {
            NitrogramConfig.setDeveloperHideDialogAvatarsEnabled(!NitrogramConfig.isDeveloperHideDialogAvatarsEnabled());
        } else if (item.id == SWITCH_HIDE_STATUS) {
            NitrogramConfig.setDeveloperHideStatusIconsEnabled(!NitrogramConfig.isDeveloperHideStatusIconsEnabled());
        } else if (item.id == SWITCH_HIDE_PINS) {
            NitrogramConfig.setDeveloperHidePinnedMarkersEnabled(!NitrogramConfig.isDeveloperHidePinnedMarkersEnabled());
        } else if (item.id == BUTTON_OUTGOING_BUBBLE_COLOR) {
            openOutgoingBubbleColorPicker();
        } else {
            return;
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == BUTTON_OUTGOING_BUBBLE_COLOR) {
            NitrogramConfig.clearDeveloperOutgoingBubbleColor();
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
            return true;
        }
        return false;
    }

    private void openOutgoingBubbleColorPicker() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        ColorPicker picker = new ColorPicker(context, false, new ColorPicker.ColorPickerDelegate() {
            @Override
            public void setColor(int color, int num, boolean applyNow) {
                NitrogramConfig.setDeveloperOutgoingBubbleColor(color);
                if (applyNow && listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
            }
        });
        picker.setType(1, false, 1, 1, true, 0, false);
        picker.setColor(
            NitrogramConfig.hasDeveloperOutgoingBubbleColorOverride()
                ? NitrogramConfig.getDeveloperOutgoingBubbleColor()
                : Color.parseColor("#4A8DFF"),
            0
        );

        FrameLayout container = new FrameLayout(context);
        container.addView(picker, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(0, AndroidUtilities.dp(8), 0, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.NitrogramDevOutgoingBubbleColor));
        builder.setView(container);
        builder.setPositiveButton(getString(R.string.Done), null);
        showDialog(builder.create());
    }

    private String colorToHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
