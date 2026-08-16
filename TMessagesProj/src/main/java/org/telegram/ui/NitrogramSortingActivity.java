package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.view.View;

import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class NitrogramSortingActivity extends UniversalFragment {

    private static final int RADIO_ACTIVITY = 1;
    private static final int RADIO_UNREAD = 2;
    private static final int RADIO_TYPE = 3;
    private static final int SWITCH_UNREAD_FIRST = 10;
    private static final int SWITCH_FOLDERS_FIRST = 11;
    private static final int SWITCH_CHANNELS_FIRST = 12;
    private static final int SWITCH_CONTACTS_FIRST = 13;

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.NitrogramSorting);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        int mode = NitrogramConfig.getSortMode();

        items.add(UItem.asHeader(getString(R.string.NitrogramSortingMode)));
        items.add(UItem.asRadio(RADIO_ACTIVITY, getString(R.string.NitrogramSortByActivity)).setChecked(mode == NitrogramConfig.SORT_BY_ACTIVITY));
        items.add(UItem.asRadio(RADIO_UNREAD, getString(R.string.NitrogramSortByUnread)).setChecked(mode == NitrogramConfig.SORT_BY_UNREAD));
        items.add(UItem.asRadio(RADIO_TYPE, getString(R.string.NitrogramSortByType)).setChecked(mode == NitrogramConfig.SORT_BY_TYPE));
        if (mode == NitrogramConfig.SORT_BY_ACTIVITY) {
            items.add(UItem.asShadow(getString(R.string.NitrogramSortByActivityInfo)));
        } else if (mode == NitrogramConfig.SORT_BY_UNREAD) {
            items.add(UItem.asShadow(getString(R.string.NitrogramSortByUnreadInfo)));
        } else {
            items.add(UItem.asShadow(getString(R.string.NitrogramSortByTypeInfo)));
        }
        items.add(UItem.asShadow(getString(R.string.NitrogramSortingModeHint)));

        items.add(UItem.asHeader(getString(R.string.NitrogramSortingRules)));
        items.add(UItem.asSwitch(SWITCH_UNREAD_FIRST, getString(R.string.NitrogramUnreadFirst)).setChecked(NitrogramConfig.isSortUnreadFirstEnabled()));
        items.add(UItem.asSwitch(SWITCH_FOLDERS_FIRST, getString(R.string.NitrogramFoldersFirst)).setChecked(NitrogramConfig.isSortFoldersFirstEnabled()));
        items.add(UItem.asSwitch(SWITCH_CHANNELS_FIRST, getString(R.string.NitrogramChannelsFirst)).setChecked(NitrogramConfig.isSortChannelsFirstEnabled()));
        items.add(UItem.asSwitch(SWITCH_CONTACTS_FIRST, getString(R.string.NitrogramContactsFirst)).setChecked(NitrogramConfig.isSortContactsFirstEnabled()));
        items.add(UItem.asShadow(getString(R.string.NitrogramSortingRulesInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == RADIO_ACTIVITY) {
            NitrogramConfig.setSortMode(NitrogramConfig.SORT_BY_ACTIVITY);
        } else if (item.id == RADIO_UNREAD) {
            NitrogramConfig.setSortMode(NitrogramConfig.SORT_BY_UNREAD);
        } else if (item.id == RADIO_TYPE) {
            NitrogramConfig.setSortMode(NitrogramConfig.SORT_BY_TYPE);
        } else if (item.id == SWITCH_UNREAD_FIRST) {
            NitrogramConfig.setSortUnreadFirstEnabled(!NitrogramConfig.isSortUnreadFirstEnabled());
        } else if (item.id == SWITCH_FOLDERS_FIRST) {
            NitrogramConfig.setSortFoldersFirstEnabled(!NitrogramConfig.isSortFoldersFirstEnabled());
        } else if (item.id == SWITCH_CHANNELS_FIRST) {
            NitrogramConfig.setSortChannelsFirstEnabled(!NitrogramConfig.isSortChannelsFirstEnabled());
        } else if (item.id == SWITCH_CONTACTS_FIRST) {
            NitrogramConfig.setSortContactsFirstEnabled(!NitrogramConfig.isSortContactsFirstEnabled());
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
