package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramPrivacyActivity extends UniversalFragment {

    private static final int SWITCH_HIDE_TYPING = 1;
    private static final int SWITCH_GHOST_READ = 2;
    private static final int SWITCH_UNLIMITED_PINS = 3;
    private static final int SWITCH_UNLIMITED_FOLDERS = 4;
    private static final int SWITCH_UNLIMITED_FOLDER_CHATS = 5;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramPrivacyLimits);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.NitrogramAnonymity)));
        items.add(UItem.asSwitch(SWITCH_HIDE_TYPING, getString(R.string.NitrogramHideTyping)).setChecked(NitrogramConfig.isHideTypingEnabled()));
        items.add(UItem.asSwitch(SWITCH_GHOST_READ, getString(R.string.NitrogramGhostRead)).setChecked(NitrogramConfig.isGhostReadEnabled()));
        items.add(UItem.asShadow(getString(R.string.NitrogramAnonymityInfo)));

        items.add(UItem.asHeader(getString(R.string.NitrogramLimits)));
        items.add(UItem.asSwitch(SWITCH_UNLIMITED_PINS, getString(R.string.NitrogramUnlimitedPins)).setChecked(NitrogramConfig.isUnlimitedPinsEnabled()));
        items.add(UItem.asSwitch(SWITCH_UNLIMITED_FOLDERS, getString(R.string.NitrogramUnlimitedFolders)).setChecked(NitrogramConfig.isUnlimitedFoldersEnabled()));
        items.add(UItem.asSwitch(SWITCH_UNLIMITED_FOLDER_CHATS, getString(R.string.NitrogramUnlimitedFolderChats)).setChecked(NitrogramConfig.isUnlimitedFolderChatsEnabled()));
        items.add(UItem.asButton(100, R.drawable.msg_permissions, getString(R.string.NitrogramAccountsExpanded), String.valueOf(NitrogramConfig.MAX_ACCOUNT_SLOTS)));
        items.add(UItem.asShadow(getString(R.string.NitrogramLimitsInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == SWITCH_HIDE_TYPING) {
            NitrogramConfig.setHideTypingEnabled(!NitrogramConfig.isHideTypingEnabled());
        } else if (item.id == SWITCH_GHOST_READ) {
            NitrogramConfig.setGhostReadEnabled(!NitrogramConfig.isGhostReadEnabled());
        } else if (item.id == SWITCH_UNLIMITED_PINS) {
            NitrogramConfig.setUnlimitedPinsEnabled(!NitrogramConfig.isUnlimitedPinsEnabled());
        } else if (item.id == SWITCH_UNLIMITED_FOLDERS) {
            NitrogramConfig.setUnlimitedFoldersEnabled(!NitrogramConfig.isUnlimitedFoldersEnabled());
        } else if (item.id == SWITCH_UNLIMITED_FOLDER_CHATS) {
            NitrogramConfig.setUnlimitedFolderChatsEnabled(!NitrogramConfig.isUnlimitedFolderChatsEnabled());
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
}
