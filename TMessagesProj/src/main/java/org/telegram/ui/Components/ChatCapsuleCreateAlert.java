package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatCapsuleManager;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatCapsuleActivity;

public class ChatCapsuleCreateAlert extends BottomSheet {

    private final BaseFragment parentFragment;
    private final long dialogId;
    private final int currentAccount;

    private int selectedPeriodDays = 0; // 0 = all time, 7, 30, 90
    private boolean includePhotos = true;
    private boolean includeVideos = true;
    private boolean includeVoice = true;
    private boolean includeDocs = true;

    private LinearLayout configLayout;
    private LinearLayout progressLayout;
    private TextView progressTitle;
    private ProgressBar progressBar;
    private TextView createButton;

    public ChatCapsuleCreateAlert(BaseFragment fragment, long dialogId) {
        super(fragment.getParentActivity(), false);
        this.parentFragment = fragment;
        this.dialogId = dialogId;
        this.currentAccount = fragment.getCurrentAccount();

        Context context = getContext();

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(20));

        // Header Title
        TextView titleView = new TextView(context);
        titleView.setText("Снимок чата (Капсула времени)");
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        container.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        TextView subtitleView = new TextView(context);
        subtitleView.setText("Создает автономную копию чата, в которую можно зайти позже и просматривать переписку и медиа даже офлайн.");
        subtitleView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.5f);
        container.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        configLayout = new LinearLayout(context);
        configLayout.setOrientation(LinearLayout.VERTICAL);

        // Section: Period
        TextView periodHeader = createSectionHeader(context, "ВРЕМЕННОЙ ПЕРИОД");
        configLayout.addView(periodHeader);

        LinearLayout periodButtons = new LinearLayout(context);
        periodButtons.setOrientation(LinearLayout.HORIZONTAL);

        final TextView[] periodTvs = new TextView[4];
        final int[] periodValues = new int[]{0, 7, 30, 90};
        final String[] periodLabels = new String[]{"Вся история", "7 дней", "30 дней", "90 дней"};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            final TextView tv = new TextView(context);
            tv.setText(periodLabels[i]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(8), dp(8), dp(8), dp(8));
            tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            periodTvs[i] = tv;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(36), 1f);
            if (i > 0) lp.leftMargin = dp(6);
            periodButtons.addView(tv, lp);

            tv.setOnClickListener(v -> {
                selectedPeriodDays = periodValues[idx];
                for (int j = 0; j < 4; j++) {
                    boolean sel = (j == idx);
                    periodTvs[j].setBackground(Theme.createRoundRectDrawable(dp(8), sel ? Theme.getColor(Theme.key_featuredStickers_addButton) : Theme.getColor(Theme.key_dialogSearchBackground)));
                    periodTvs[j].setTextColor(sel ? 0xffffffff : Theme.getColor(Theme.key_dialogTextBlack));
                }
            });
        }

        // Default selected: all time
        for (int j = 0; j < 4; j++) {
            boolean sel = (j == 0);
            periodTvs[j].setBackground(Theme.createRoundRectDrawable(dp(8), sel ? Theme.getColor(Theme.key_featuredStickers_addButton) : Theme.getColor(Theme.key_dialogSearchBackground)));
            periodTvs[j].setTextColor(sel ? 0xffffffff : Theme.getColor(Theme.key_dialogTextBlack));
        }
        configLayout.addView(periodButtons, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Section: Media Content
        TextView mediaHeader = createSectionHeader(context, "ЧТО ВКЛЮЧИТЬ В СНИМОК");
        configLayout.addView(mediaHeader);

        configLayout.addView(createCheckboxRow(context, "Фотографии", true, checked -> includePhotos = checked));
        configLayout.addView(createCheckboxRow(context, "Видеозаписи", true, checked -> includeVideos = checked));
        configLayout.addView(createCheckboxRow(context, "Голосовые и видеосообщения (кружочки)", true, checked -> includeVoice = checked));
        configLayout.addView(createCheckboxRow(context, "Файлы и документы", true, checked -> includeDocs = checked));

        container.addView(configLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Progress layout (hidden initially)
        progressLayout = new LinearLayout(context);
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setVisibility(View.GONE);
        progressLayout.setPadding(0, dp(16), 0, dp(16));

        progressTitle = new TextView(context);
        progressTitle.setText("Создание снимка чата...");
        progressTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        progressTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        progressTitle.setGravity(Gravity.CENTER);
        progressTitle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        progressLayout.addView(progressTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton), PorterDuff.Mode.SRC_IN));
        progressLayout.addView(progressBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, dp(8), 0, 0, 0, 16));

        container.addView(progressLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Create Button
        createButton = new TextView(context);
        createButton.setText("Создать снимок");
        createButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        createButton.setTextColor(0xffffffff);
        createButton.setGravity(Gravity.CENTER);
        createButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        createButton.setBackground(Theme.createRoundRectDrawable(dp(10), Theme.getColor(Theme.key_featuredStickers_addButton)));
        createButton.setOnClickListener(v -> startCreation());

        container.addView(createButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, dp(48), 0, 12, 0, 0));

        root.addView(container);
        setCustomView(root);
    }

    private TextView createSectionHeader(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        tv.setPadding(0, 0, 0, dp(6));
        return tv;
    }

    private interface OnCheckChanged {
        void onCheck(boolean checked);
    }

    private View createCheckboxRow(Context context, String title, boolean initial, OnCheckChanged callback) {
        FrameLayout row = new FrameLayout(context);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        row.addView(tv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        final CheckBoxSquare checkBox = new CheckBoxSquare(context, false);
        checkBox.setChecked(initial, false);
        row.addView(checkBox, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        row.setOnClickListener(v -> {
            boolean next = !checkBox.isChecked();
            checkBox.setChecked(next, true);
            callback.onCheck(next);
        });

        return row;
    }

    private void startCreation() {
        configLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        createButton.setVisibility(View.GONE);
        setCancelable(false);

        ChatCapsuleManager.getInstance().createCapsule(
                currentAccount,
                dialogId,
                selectedPeriodDays,
                includePhotos,
                includeVideos,
                includeVoice,
                includeDocs,
                new ChatCapsuleManager.CreateCallback() {
                    @Override
                    public void onProgress(int current, int total, String status) {
                        if (total > 0) {
                            int pct = (int) ((current / (float) total) * 100);
                            progressBar.setProgress(pct);
                        }
                        progressTitle.setText(status);
                    }

                    @Override
                    public void onSuccess(ChatCapsuleManager.CapsuleInfo info) {
                        progressBar.setProgress(100);
                        progressTitle.setText("Снимок готов! Сохранено " + info.messagesCount + " сообщений (" + info.getFormattedSize() + ")");
                        createButton.setText("Открыть капсулу");
                        createButton.setVisibility(View.VISIBLE);
                        createButton.setOnClickListener(v -> {
                            dismiss();
                            parentFragment.presentFragment(new ChatCapsuleActivity(info.id));
                        });
                        setCancelable(true);
                    }

                    @Override
                    public void onError(String error) {
                        progressTitle.setText(error);
                        createButton.setText("Закрыть");
                        createButton.setVisibility(View.VISIBLE);
                        createButton.setOnClickListener(v -> dismiss());
                        setCancelable(true);
                    }
                }
        );
    }
}
