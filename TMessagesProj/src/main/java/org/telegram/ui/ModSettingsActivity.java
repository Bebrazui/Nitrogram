package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ModSettingsActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("");
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

        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(12), dp(16), dp(32));

        // App Logo & Header Info
        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        headerLayout.setPadding(0, dp(16), 0, dp(24));

        FrameLayout logoContainer = new FrameLayout(context);
        logoContainer.setBackground(Theme.createRoundRectDrawable(dp(22), Theme.getColor(Theme.key_avatar_backgroundGreen)));
        logoContainer.setClipToOutline(true);

        ImageView logoView = new ImageView(context);
        logoView.setImageResource(R.drawable.nitro_mod_logo);
        logoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logoContainer.addView(logoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        headerLayout.addView(logoContainer, LayoutHelper.createLinear(72, 72, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        TextView titleView = new TextView(context);
        titleView.setText("Nitrogram");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setGravity(Gravity.CENTER);
        headerLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

        TextView versionView = new TextView(context);
        try {
            String versionName = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionName;
            int versionCode = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode;
            versionView.setText(versionName + " (" + versionCode + ")");
        } catch (Exception ignore) {
            versionView.setText("1.0.0 (70079)");
        }
        versionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        versionView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        versionView.setGravity(Gravity.CENTER);
        headerLayout.addView(versionView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

        contentLayout.addView(headerLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Section: Категории
        TextView categoriesHeader = createSectionHeader(context, "Категории");
        contentLayout.addView(categoriesHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 8, 8, 8, 8));

        LinearLayout categoriesCard = createCardContainer(context);

        categoriesCard.addView(createRowItem(context, R.drawable.msg_settings, "Основные", null, true, v -> {
            presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_MAIN));
        }));

        categoriesCard.addView(createRowItem(context, R.drawable.msg_theme, "Внешний вид", null, true, v -> {
            presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_APPEARANCE));
        }));

        categoriesCard.addView(createRowItem(context, R.drawable.msg_discussion, "Чаты", null, true, v -> {
            presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_CHATS));
        }));

        categoriesCard.addView(createRowItem(context, R.drawable.msg_customize, "Плагины", null, true, v -> {
            presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_PLUGINS));
        }));

        categoriesCard.addView(createRowItem(context, R.drawable.msg_info, "Другое", null, false, v -> {
            presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_OTHER));
        }));

        contentLayout.addView(categoriesCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Section: Ссылки
        TextView linksHeader = createSectionHeader(context, "Ссылки");
        contentLayout.addView(linksHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 8, 8, 8, 8));

        LinearLayout linksCard = createCardContainer(context);

        linksCard.addView(createRowItem(context, R.drawable.msg_channel, "Канал", "@nitrogram_offc", true, v -> {
            Browser.openUrl(getParentActivity(), "https://t.me/nitrogram_offc");
        }));

        linksCard.addView(createRowItem(context, R.drawable.msg_groups, "Моды", "@nitromod", true, v -> {
            Browser.openUrl(getParentActivity(), "https://t.me/nitromod");
        }));

        linksCard.addView(createRowItem(context, R.drawable.msg_language, "GitHub", "Bebrazui/Nitrogram", false, v -> {
            Browser.openUrl(getParentActivity(), "https://github.com/Bebrazui/Nitrogram");
        }));

        contentLayout.addView(linksCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        scrollView.addView(contentLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        this.fragmentView = fragmentView;
        return fragmentView;
    }

    private TextView createSectionHeader(Context context, String title) {
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        return textView;
    }

    private LinearLayout createCardContainer(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Theme.createRoundRectDrawable(dp(16), Theme.getColor(Theme.key_windowBackgroundWhite)));
        return card;
    }

    private View createRowItem(Context context, int iconRes, String title, String value, boolean divider, View.OnClickListener onClick) {
        FrameLayout itemLayout = new FrameLayout(context);
        itemLayout.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 2));
        itemLayout.setPadding(dp(16), dp(14), dp(16), dp(14));
        itemLayout.setOnClickListener(onClick);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        if (iconRes != 0) {
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(iconRes);
            iconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            row.addView(iconView, LayoutHelper.createLinear(24, 24, Gravity.CENTER_VERTICAL, 0, 0, 16, 0));
        }

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f, Gravity.CENTER_VERTICAL));

        if (value != null) {
            TextView valueView = new TextView(context);
            valueView.setText(value);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            valueView.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            row.addView(valueView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 8, 0, 0, 0));
        }

        itemLayout.addView(row, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (divider) {
            View dividerView = new View(context);
            dividerView.setBackgroundColor(Theme.getColor(Theme.key_divider));
            itemLayout.addView(dividerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM, dp(56), 0, 0, 0));
        }

        return itemLayout;
    }
}
