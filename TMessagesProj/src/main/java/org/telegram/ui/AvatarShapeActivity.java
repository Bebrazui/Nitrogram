package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.M3ShapeHelper;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class AvatarShapeActivity extends BaseFragment {

    private BackupImageView bigAvatarView;
    private TextView shapeNameText;
    private ShapeAdapter adapter;
    private int selectedShape;

    @Override
    public boolean onFragmentCreate() {
        selectedShape = NitrogramConfig.getAvatarShape();
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Форма аватарок");
        actionBar.setSubtitle("Material Design 3 Shape");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        // Top Live Preview Card
        FrameLayout previewCard = new FrameLayout(context);
        previewCard.setBackground(Theme.createRoundRectDrawable(dp(16), Theme.getColor(Theme.key_windowBackgroundWhite)));
        previewCard.setPadding(dp(16), dp(20), dp(16), dp(20));

        LinearLayout previewContent = new LinearLayout(context);
        previewContent.setOrientation(LinearLayout.VERTICAL);
        previewContent.setGravity(Gravity.CENTER_HORIZONTAL);

        bigAvatarView = new BackupImageView(context);
        bigAvatarView.setRoundRadius(dp(60));

        TLRPC.User currentUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        if (currentUser != null) {
            avatarDrawable.setInfo(currentAccount, currentUser);
            bigAvatarView.setForUserOrChat(currentUser, avatarDrawable);
        } else {
            avatarDrawable.setInfo(1, "N", "G");
            bigAvatarView.setImageDrawable(avatarDrawable);
        }

        previewContent.addView(bigAvatarView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        shapeNameText = new TextView(context);
        shapeNameText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        shapeNameText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        shapeNameText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        shapeNameText.setGravity(Gravity.CENTER);
        shapeNameText.setText(M3ShapeHelper.getShapeName(selectedShape));
        previewContent.addView(shapeNameText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

        TextView hintText = new TextView(context);
        hintText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        hintText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        hintText.setGravity(Gravity.CENTER);
        hintText.setText("Форма применяется ко всем аватаркам в чатах, группах и профиле");
        previewContent.addView(hintText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        previewCard.addView(previewContent, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        contentLayout.addView(previewCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 12, 12, 12));

        // Grid of Shapes
        RecyclerListView listView = new RecyclerListView(context);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        listView.setLayoutManager(layoutManager);
        adapter = new ShapeAdapter(context);
        listView.setAdapter(adapter);
        listView.setClipToPadding(false);
        listView.setPadding(dp(8), dp(4), dp(8), dp(24));
        listView.setOnItemClickListener((view, position) -> {
            M3ShapeHelper.ShapeInfo shapeInfo = M3ShapeHelper.ALL_SHAPES[position];
            selectedShape = shapeInfo.id;
            NitrogramConfig.setAvatarShape(selectedShape);
            shapeNameText.setText(shapeInfo.name);
            bigAvatarView.invalidate();
            adapter.notifyDataSetChanged();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
        });

        contentLayout.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView.addView(contentLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        this.fragmentView = fragmentView;
        return fragmentView;
    }

    private class ShapeAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ShapeAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return M3ShapeHelper.ALL_SHAPES.length;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ShapeCell cell = new ShapeCell(mContext);
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ShapeCell cell = (ShapeCell) holder.itemView;
            M3ShapeHelper.ShapeInfo shapeInfo = M3ShapeHelper.ALL_SHAPES[position];
            cell.setShape(shapeInfo, shapeInfo.id == selectedShape);
        }
    }

    private static class ShapeCell extends FrameLayout {

        private final ShapeIconView iconView;
        private final TextView titleView;
        private final FrameLayout cardLayout;

        public ShapeCell(Context context) {
            super(context);

            cardLayout = new FrameLayout(context);
            cardLayout.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite)));
            cardLayout.setPadding(dp(8), dp(12), dp(8), dp(12));

            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(Gravity.CENTER_HORIZONTAL);

            iconView = new ShapeIconView(context);
            content.addView(iconView, LayoutHelper.createLinear(52, 52, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 8));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setGravity(Gravity.CENTER);
            titleView.setMaxLines(2);
            content.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            cardLayout.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            addView(cardLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.NO_GRAVITY, 4, 4, 4, 4));
        }

        public void setShape(M3ShapeHelper.ShapeInfo shapeInfo, boolean isSelected) {
            iconView.setShape(shapeInfo.id, isSelected);
            String name = shapeInfo.name;
            int idx = name.indexOf(" (");
            if (idx > 0) {
                name = name.substring(0, idx) + "\n" + name.substring(idx + 1).replace(")", "");
            }
            titleView.setText(name);

            if (isSelected) {
                cardLayout.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_featuredStickers_addButton)));
            } else {
                cardLayout.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundWhite)));
            }
        }
    }

    private static class ShapeIconView extends View {

        private int shapeId;
        private boolean isSelected;
        private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF drawRect = new RectF();

        public ShapeIconView(Context context) {
            super(context);
        }

        public void setShape(int shapeId, boolean isSelected) {
            this.shapeId = shapeId;
            this.isSelected = isSelected;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            drawRect.set(dp(2), dp(2), w - dp(2), h - dp(2));

            if (isSelected) {
                shapePaint.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            } else {
                shapePaint.setColor(Theme.getColor(Theme.key_avatar_backgroundBlue));
            }

            Path path = M3ShapeHelper.getShapePath(shapeId, drawRect);
            canvas.drawPath(path, shapePaint);
        }
    }
}
