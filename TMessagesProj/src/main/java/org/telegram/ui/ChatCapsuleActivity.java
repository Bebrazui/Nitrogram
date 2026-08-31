package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatCapsuleManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class ChatCapsuleActivity extends BaseFragment {

    private final String capsuleId;
    private ChatCapsuleManager.CapsuleData capsuleData;

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private MessagesAdapter adapter;
    private RadialProgressView progressBar;
    private FrameLayout bottomBar;

    private final static int MENU_DELETE = 1;

    public ChatCapsuleActivity(String capsuleId) {
        this.capsuleId = capsuleId;
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Снимок чата");
        actionBar.setSubtitle("Загрузка капсулы...");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == MENU_DELETE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle("Удалить снимок?");
                    builder.setMessage("Эта капсула чата и все сохраненные в ней медиафайлы будут безвозвратно удалены с устройства.");
                    builder.setPositiveButton("Удалить", (dialog, which) -> {
                        ChatCapsuleManager.getInstance().deleteCapsule(capsuleId);
                        finishFragment();
                    });
                    builder.setNegativeButton("Отмена", null);
                    showDialog(builder.create());
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(MENU_DELETE, R.drawable.msg_delete);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_chat_wallpaper));

        listView = new RecyclerListView(context);
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(context);
        listView.setAdapter(adapter);
        listView.setClipToPadding(false);
        listView.setPadding(0, dp(8), 0, dp(56));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        progressBar = new RadialProgressView(context);
        frameLayout.addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        // Bottom Read-Only banner
        bottomBar = new FrameLayout(context) {
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            {
                bgPaint.setColor(0xdd1a222c);
            }
            @Override
            protected void onDraw(Canvas canvas) {
                rect.set(0, 0, getWidth(), getHeight());
                canvas.drawRoundRect(rect, dp(18), dp(18), bgPaint);
                super.onDraw(canvas);
            }
        };
        bottomBar.setWillNotDraw(false);
        bottomBar.setPadding(dp(16), dp(8), dp(16), dp(8));

        TextView bottomText = new TextView(context);
        bottomText.setText("🔒 Замороженный снимок чата • Только для чтения");
        bottomText.setTextColor(0xeeffffff);
        bottomText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
        bottomText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        bottomText.setGravity(Gravity.CENTER);
        bottomBar.addView(bottomText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        frameLayout.addView(bottomBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 36, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 10));

        loadCapsule();

        return fragmentView;
    }

    private void loadCapsule() {
        ChatCapsuleManager.getInstance().loadCapsuleData(currentAccount, capsuleId, new ChatCapsuleManager.LoadCallback() {
            @Override
            public void onLoaded(ChatCapsuleManager.CapsuleData data) {
                capsuleData = data;
                progressBar.setVisibility(View.GONE);
                actionBar.setTitle(data.info.title);
                actionBar.setSubtitle("Снимок от " + data.info.getFormattedDate() + " • " + data.info.messagesCount + " сообщ.");
                adapter.notifyDataSetChanged();
                if (!data.messageObjects.isEmpty()) {
                    listView.scrollToPosition(data.messageObjects.size() - 1);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                actionBar.setTitle("Ошибка");
                actionBar.setSubtitle(error);
            }
        });
    }

    private class MessagesAdapter extends RecyclerListView.SelectionAdapter {

        private final Context context;

        public MessagesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public int getItemCount() {
            return capsuleData != null ? capsuleData.messageObjects.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ChatMessageCell cell = new ChatMessageCell(context, currentAccount);
            cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                @Override
                public void didPressImage(ChatMessageCell cell, float x, float y, boolean tag) {
                    MessageObject obj = cell.getMessageObject();
                    if (obj != null && (obj.isVideo() || obj.isPhoto())) {
                        PhotoViewer.getInstance().setParentActivity(getParentActivity());
                        PhotoViewer.getInstance().openPhoto(obj, null, 0, -1, 0, null);
                    }
                }

                @Override
                public void didPressSideButton(ChatMessageCell cell) {
                    MessageObject obj = cell.getMessageObject();
                    if (obj != null && (obj.isVoice() || obj.isRoundVideo() || obj.isMusic())) {
                        MediaController.getInstance().playMessage(obj);
                    }
                }
            });
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ChatMessageCell cell = (ChatMessageCell) holder.itemView;
            if (capsuleData != null && position >= 0 && position < capsuleData.messageObjects.size()) {
                MessageObject obj = capsuleData.messageObjects.get(position);
                cell.setMessageObject(obj, null, false, false, false);
            }
        }
    }
}
