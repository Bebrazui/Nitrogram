/*
 * Nitrogram Secret Safe screen: lists the hidden ("safe") chats with real
 * chat rows (avatar, last message, time). Reachable only by typing the Safe
 * PIN into the chat search field.
 */

package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SafeConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class SafeChatsActivity extends BaseFragment {

    private static final int menu_change_pin = 1;
    private static final int menu_clear = 2;

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private final ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();

    private void reload() {
        dialogs.clear();
        for (long id : SafeConfig.getSafeIds()) {
            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(id);
            if (dialog != null) {
                dialogs.add(dialog);
            }
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        reload();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Safe");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == menu_change_pin) {
                    showChangePinDialog();
                } else if (id == menu_clear) {
                    showClearDialog();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem other = menu.addItem(0, R.drawable.ic_ab_other);
        other.addSubItem(menu_change_pin, R.drawable.msg_secret, "Сменить PIN");
        other.addSubItem(menu_clear, R.drawable.msg_delete, "Очистить сейф");

        listAdapter = new ListAdapter(context);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        fragmentView = frameLayout;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= dialogs.size()) {
                return;
            }
            long dialogId = dialogs.get(position).id;
            Bundle args = new Bundle();
            if (dialogId > 0) {
                args.putLong("user_id", dialogId);
            } else {
                args.putLong("chat_id", -dialogId);
            }
            if (getMessagesController().checkCanOpenChat(args, this)) {
                presentFragment(new ChatActivity(args));
            }
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (position < 0 || position >= dialogs.size()) {
                return false;
            }
            long dialogId = dialogs.get(position).id;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Убрать из сейфа");
            builder.setMessage("Чат вернётся в обычный список чатов.");
            builder.setPositiveButton("Убрать", (d, w) -> {
                SafeConfig.removeFromSafe(dialogId);
                reload();
                listAdapter.notifyDataSetChanged();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogsNeedReload);
                if (dialogs.isEmpty()) {
                    finishFragment();
                }
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder.create());
            return true;
        });

        return fragmentView;
    }

    private void showClearDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Очистить сейф");
        builder.setMessage("Все чаты вернутся в обычный список. PIN сохранится.");
        builder.setPositiveButton("Очистить", (d, w) -> {
            SafeConfig.clearAll();
            getNotificationCenter().postNotificationName(NotificationCenter.dialogsNeedReload);
            finishFragment();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showChangePinDialog() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }
        final android.widget.EditText editText = new android.widget.EditText(context);
        editText.setHint("Новый PIN");
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        FrameLayout container = new FrameLayout(context);
        int pad = AndroidUtilities.dp(22);
        container.setPadding(pad, AndroidUtilities.dp(4), pad, 0);
        container.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Сменить PIN");
        builder.setView(container);
        builder.setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> {
            String pin = editText.getText().toString();
            if (pin.length() < 1) {
                return;
            }
            SafeConfig.setPin(pin);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
        AndroidUtilities.runOnUIThread(() -> {
            editText.requestFocus();
            AndroidUtilities.showKeyboard(editText);
        }, 100);
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return dialogs.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DialogCell cell = new DialogCell(null, mContext, false, false, currentAccount, null);
            cell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            DialogCell cell = (DialogCell) holder.itemView;
            TLRPC.Dialog dialog = dialogs.get(position);
            cell.useSeparator = position != dialogs.size() - 1;
            cell.setDialog(dialog, DialogsActivity.DIALOGS_TYPE_DEFAULT, 0);
        }
    }
}
