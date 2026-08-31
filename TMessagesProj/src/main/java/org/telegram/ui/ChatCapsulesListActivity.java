package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatCapsuleManager;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class ChatCapsulesListActivity extends BaseFragment {

    private final long dialogId;
    private List<ChatCapsuleManager.CapsuleInfo> capsules = new ArrayList<>();
    private RecyclerListView listView;
    private CapsulesAdapter adapter;
    private TextView emptyView;

    public ChatCapsulesListActivity(long dialogId) {
        this.dialogId = dialogId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(dialogId != 0 ? "Снимки этого чата" : "Капсулы чатов");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new CapsulesAdapter(context);
        listView.setAdapter(adapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new TextView(context);
        emptyView.setText("Нет сохраненных снимков чата");
        emptyView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        listView.setOnItemClickListener((view, position) -> {
            if (position >= 0 && position < capsules.size()) {
                presentFragment(new ChatCapsuleActivity(capsules.get(position).id));
            }
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= 0 && position < capsules.size()) {
                ChatCapsuleManager.CapsuleInfo info = capsules.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Удалить снимок?");
                builder.setMessage("Удалить капсулу «" + info.title + "» от " + info.getFormattedDate() + "?");
                builder.setPositiveButton("Удалить", (dialog, which) -> {
                    ChatCapsuleManager.getInstance().deleteCapsule(info.id);
                    loadCapsules();
                });
                builder.setNegativeButton("Отмена", null);
                showDialog(builder.create());
                return true;
            }
            return false;
        });

        loadCapsules();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCapsules();
    }

    private void loadCapsules() {
        capsules = ChatCapsuleManager.getInstance().getCapsules(dialogId);
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(capsules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class CapsulesAdapter extends RecyclerListView.SelectionAdapter {

        private final Context context;

        public CapsulesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return capsules.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout itemLayout = new FrameLayout(context);
            itemLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            itemLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            itemLayout.setPadding(dp(18), dp(12), dp(18), dp(12));

            LinearLayout textLayout = new LinearLayout(context);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            TextView titleTv = new TextView(context);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleTv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleTv.setTag("title");
            textLayout.addView(titleTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            TextView subtitleTv = new TextView(context);
            subtitleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            subtitleTv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            subtitleTv.setTag("subtitle");
            textLayout.addView(subtitleTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 3, 0, 0));

            itemLayout.addView(textLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

            return new RecyclerListView.Holder(itemLayout);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            FrameLayout root = (FrameLayout) holder.itemView;
            TextView titleTv = root.findViewWithTag("title");
            TextView subtitleTv = root.findViewWithTag("subtitle");

            ChatCapsuleManager.CapsuleInfo info = capsules.get(position);
            titleTv.setText(info.title);
            subtitleTv.setText("Зафиксирован: " + info.getFormattedDate() + " • " + info.messagesCount + " сообщ. • " + info.getFormattedSize());
        }
    }
}
