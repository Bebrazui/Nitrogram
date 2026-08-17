package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ModManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.Switch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstalledModsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private FrameLayout emptyView;
    private List<ModManager.ModMeta> mods = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        mods = ModManager.getInstalledMods();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Установленные моды");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context, LinearLayout.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        emptyView = new FrameLayout(context);
        TextView tv = new TextView(context);
        tv.setText("Модов пока нет.\nОтправьте .so файл в чат и нажмите на него, чтобы установить.");
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        tv.setTextSize(15);
        emptyView.addView(tv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 40, 0, 40, 0));
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        updateVisibility();

        return fragmentView;
    }

    private void updateVisibility() {
        boolean empty = mods.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    void onToggle(ModManager.ModMeta m, boolean checked) {
        ModManager.setEnabled(m.id, checked);
        m.enabled = checked;
        if (checked) {
            File f = ModManager.getModFile(m.id);
            ModManager.loadNative(f);
            m.loaded = true;
            Toast.makeText(getParentActivity(), "Мод включён.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getParentActivity(), "Мод отключён. Изменения применятся после перезапуска.", Toast.LENGTH_LONG).show();
        }
    }

    void onDelete(ModManager.ModMeta m) {
        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
        b.setTitle("Удалить мод?");
        b.setMessage(m.name + (m.version.isEmpty() ? "" : " v" + m.version));
        b.setPositiveButton("Удалить", (d, w) -> {
            ModManager.deleteMod(m.id);
            mods.remove(m);
            listAdapter.notifyDataSetChanged();
            updateVisibility();
        });
        b.setNegativeButton("Отмена", null);
        showDialog(b.create());
    }

    private static class ModCard extends FrameLayout {
        ImageView avatar;
        TextView name;
        TextView ver;
        TextView desc;
        TextView status;
        TextView deleteBtn;
        Switch sw;
        ModManager.ModMeta meta;

        public ModCard(Context context) {
            super(context);
            setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(4), AndroidUtilities.dp(10), AndroidUtilities.dp(4));

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(14), Theme.getColor(Theme.key_windowBackgroundWhite)));
            card.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12));
            addView(card, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            LinearLayout top = new LinearLayout(context);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(top, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0));

            avatar = new ImageView(context);
            int av = AndroidUtilities.dp(46);
            avatar.setBackground(Theme.createRoundRectDrawable(av / 2, Theme.getColor(Theme.key_chats_actionBackground)));
            avatar.setClipToOutline(true);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            top.addView(avatar, LayoutHelper.createFrame(av, av));

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(8), 0);
            name = new TextView(context);
            name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            name.setTextSize(16);
            name.setTypeface(AndroidUtilities.bold());
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
            info.addView(name, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            ver = new TextView(context);
            ver.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            ver.setTextSize(13);
            info.addView(ver, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));
            top.addView(info, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

            sw = new Switch(context);
            sw.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            top.addView(sw, LayoutHelper.createFrame(37, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

            desc = new TextView(context);
            desc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            desc.setTextSize(14);
            desc.setMaxLines(2);
            desc.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(desc, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

            View divider = new View(context);
            divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
            card.addView(divider, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 0, 12, 0, 0));

            LinearLayout actions = new LinearLayout(context);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.CENTER_VERTICAL);
            status = new TextView(context);
            status.setTextSize(13);
            actions.addView(status, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
            deleteBtn = new TextView(context);
            deleteBtn.setText("Удалить");
            deleteBtn.setTextSize(14);
            deleteBtn.setTypeface(AndroidUtilities.bold());
            deleteBtn.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
            deleteBtn.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(6));
            actions.addView(deleteBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            card.addView(actions, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 0));
        }

        void bind(ModManager.ModMeta m, InstalledModsActivity a) {
            meta = m;
            name.setText(m.name);
            if (m.version != null && !m.version.isEmpty()) {
                ver.setVisibility(View.VISIBLE);
                ver.setText("Версия " + m.version);
            } else {
                ver.setVisibility(View.GONE);
            }
            if (m.description != null && !m.description.isEmpty()) {
                desc.setVisibility(View.VISIBLE);
                desc.setText(m.description);
            } else {
                desc.setVisibility(View.GONE);
            }
            if (m.icon != null) {
                avatar.setImageBitmap(m.icon);
            } else {
                avatar.setImageBitmap(null);
            }
            sw.setClickable(true);
            sw.setChecked(m.enabled, false);
            sw.setOnClickListener(v -> {
                boolean newState = !sw.isChecked();
                sw.setChecked(newState, true);
                m.enabled = newState;
                a.onToggle(m, newState);
                updateStatus();
            });
            deleteBtn.setOnClickListener(v -> a.onDelete(m));
            updateStatus();
        }

        void updateStatus() {
            if (meta.enabled) {
                status.setText("Активен");
                status.setTextColor(0xff4caf50);
            } else {
                status.setText("Отключён");
                status.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return mods.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ModCard view = new ModCard(mContext);
            view.setClickable(false);
            view.setFocusable(false);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ModCard) holder.itemView).bind(mods.get(position), InstalledModsActivity.this);
        }
    }
}
