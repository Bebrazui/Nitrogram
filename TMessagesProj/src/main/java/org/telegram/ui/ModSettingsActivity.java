package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class ModSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;
    private final ArrayList<Item> items = new ArrayList<>();

    private static class Item {
        int id;
        int viewType;
        int iconRes;
        String title;
        String value;
        boolean divider;

        Item(int id, int viewType, int iconRes, String title, String value, boolean divider) {
            this.id = id;
            this.viewType = viewType;
            this.iconRes = iconRes;
            this.title = title;
            this.value = value;
            this.divider = divider;
        }

        Item(int id, int viewType, String title) {
            this.id = id;
            this.viewType = viewType;
            this.title = title;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        buildItems();
        return super.onFragmentCreate();
    }

    private void buildItems() {
        items.clear();
        items.add(new Item(0, 0, null));

        items.add(new Item(100, 1, "Категории"));
        items.add(new Item(1, 2, R.drawable.msg_settings, "Основные", null, true));
        items.add(new Item(2, 2, R.drawable.msg_theme, "Внешний вид", null, true));
        items.add(new Item(3, 2, R.drawable.msg_discussion, "Чаты", null, true));
        items.add(new Item(4, 2, R.drawable.msg_customize, "Плагины", null, true));
        items.add(new Item(5, 2, R.drawable.msg_info, "Другое", null, false));
        items.add(new Item(101, 3, ""));

        items.add(new Item(200, 1, "Ссылки"));
        items.add(new Item(6, 2, R.drawable.msg_channel, "Канал", "@nitrogram_offc", true));
        items.add(new Item(7, 2, R.drawable.msg_groups, "Моды", "@nitromod", true));
        items.add(new Item(8, 2, R.drawable.msg_language, "GitHub", "Bebrazui/Nitrogram", false));
        items.add(new Item(201, 3, "Nitrogram Mod Client"));
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Nitrogram Mods");
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

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        adapter = new ListAdapter(context);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) return;
            Item item = items.get(position);
            if (item.id == 1) {
                presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_MAIN));
            } else if (item.id == 2) {
                presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_APPEARANCE));
            } else if (item.id == 3) {
                presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_CHATS));
            } else if (item.id == 4) {
                presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_PLUGINS));
            } else if (item.id == 5) {
                presentFragment(new ModCategoryActivity(ModCategoryActivity.CATEGORY_OTHER));
            } else if (item.id == 6) {
                Browser.openUrl(getParentActivity(), "https://t.me/nitrogram_offc");
            } else if (item.id == 7) {
                Browser.openUrl(getParentActivity(), "https://t.me/nitromod");
            } else if (item.id == 8) {
                Browser.openUrl(getParentActivity(), "https://github.com/Bebrazui/Nitrogram");
            }
        });

        fragmentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        this.fragmentView = fragmentView;
        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            if (pos >= 0 && pos < items.size()) {
                return items.get(pos).viewType == 2;
            }
            return false;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).viewType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    LinearLayout headerLayout = new LinearLayout(mContext);
                    headerLayout.setOrientation(LinearLayout.VERTICAL);
                    headerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    headerLayout.setPadding(0, dp(24), 0, dp(16));

                    FrameLayout logoContainer = new FrameLayout(mContext);
                    logoContainer.setBackground(Theme.createRoundRectDrawable(dp(22), Theme.getColor(Theme.key_avatar_backgroundGreen)));
                    logoContainer.setClipToOutline(true);

                    ImageView logoView = new ImageView(mContext);
                    logoView.setImageResource(R.drawable.nitro_mod_logo);
                    logoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    logoContainer.addView(logoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

                    headerLayout.addView(logoContainer, LayoutHelper.createLinear(72, 72, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

                    TextView titleView = new TextView(mContext);
                    titleView.setText("Nitrogram");
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                    titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    titleView.setGravity(Gravity.CENTER);
                    headerLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

                    TextView versionView = new TextView(mContext);
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

                    view = headerLayout;
                    break;
                }
                case 1: {
                    HeaderCell headerCell = new HeaderCell(mContext);
                    headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    view = headerCell;
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell infoCell = new TextInfoPrivacyCell(mContext);
                    view = infoCell;
                    break;
                }
                case 2:
                default: {
                    TextCell textCell = new TextCell(mContext);
                    textCell.setBackground(Theme.getSelectorDrawable(true));
                    view = textCell;
                    break;
                }
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Item item = items.get(position);
            switch (item.viewType) {
                case 1: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(item.title);
                    break;
                }
                case 2: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (item.value != null) {
                        textCell.setTextAndValueAndIcon(item.title, item.value, item.iconRes, item.divider);
                    } else {
                        textCell.setTextAndIcon(item.title, item.iconRes, item.divider);
                    }
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    infoCell.setText(item.title);
                    break;
                }
            }
        }
    }
}
