package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.TextAnimationManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class TextAnimationActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;
    private final ArrayList<Item> items = new ArrayList<>();

    private static class Item {
        int id;
        int type; // 0 = check, 1 = setting, 2 = header, 3 = info, 4 = preview
        String title;
        String value;
        boolean checked;
        boolean divider;

        Item(int id, int type, String title, String value, boolean checked, boolean divider) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.value = value;
            this.checked = checked;
            this.divider = divider;
        }

        Item(int id, int type, String title) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.divider = true;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        buildItems();
        return super.onFragmentCreate();
    }

    private void buildItems() {
        items.clear();
        SharedPreferences p = TextAnimationManager.getPrefs();

        items.add(new Item(100, 4, "Preview"));

        items.add(new Item(101, 2, "Состояние"));
        items.add(new Item(1, 0, "Включить анимации ввода", null, p.getBoolean("enabled", true), true));
        items.add(new Item(3, 0, "Игнорировать пробелы", null, p.getBoolean("ignore_spaces", true), true));
        items.add(new Item(4, 1, "Темп появления", p.getString("duration", "300") + " мс", false, false));

        items.add(new Item(102, 2, "Кривая анимации (Интерполятор)"));
        int easing = p.getInt("easing_type", 0);
        String[] easings = new String[]{"Ease Out (Плавное замедление)", "Ease In (Плавный разгон)", "Ease In-Out (Разгон и замедление)", "Spring (Пружинистый отскок)", "Linear (Линейно)"};
        items.add(new Item(16, 1, "Тип кривой анимации", easings[Math.max(0, Math.min(easings.length - 1, easing))], false, false));

        items.add(new Item(103, 2, "Мягкое появление (Размытие)"));
        items.add(new Item(5, 0, "Появляться из размытия (Blur)", null, p.getBoolean("blur_enabled", true), false));

        items.add(new Item(104, 2, "Движение символов"));
        items.add(new Item(8, 0, "Лёгкий въезд сверху", null, p.getBoolean("slide_enabled", true), true));
        items.add(new Item(9, 1, "Дистанция въезда", p.getString("slide_dist", "20") + " dp", false, true));
        items.add(new Item(10, 0, "Добавить масштаб", null, p.getBoolean("scale_enabled", false), true));
        items.add(new Item(11, 0, "Чуть поворачивать буквы", null, p.getBoolean("rotate_enabled", false), false));

        items.add(new Item(105, 2, "Удаление текста (Частицы)"));
        items.add(new Item(12, 0, "Рассыпать удаляемые символы", null, p.getBoolean("delete_anim_enabled", true), true));
        int style = p.getInt("particle_style", 0);
        String[] styles = new String[]{"Пыль", "Искры", "Снежинки", "Лепестки", "Буквы"};
        items.add(new Item(13, 1, "Стиль частиц", styles[Math.max(0, Math.min(styles.length - 1, style))], false, true));
        int palette = p.getInt("particle_palette", 0);
        String[] palettes = new String[]{"Цвет текста / Темы", "Радужный микс", "Неоновый киберпанк", "Золотые искры", "Пастельные тона", "Монохром"};
        items.add(new Item(19, 1, "Цветовая палитра", palettes[Math.max(0, Math.min(palettes.length - 1, palette))], false, true));
        items.add(new Item(17, 1, "Размер частиц", p.getString("particle_size", "3") + " dp", false, true));
        items.add(new Item(18, 1, "Количество частиц", p.getString("particle_count", "8"), false, false));

        items.add(new Item(106, 2, "Курсор"));
        items.add(new Item(14, 0, "Сделать курсор плавным", null, p.getBoolean("cursor_enabled", true), true));
        items.add(new Item(15, 0, "Жидкий курсор", null, p.getBoolean("liquid_cursor_enabled", false), false));
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Анимации ввода текста");
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
        adapter = new ListAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((view, position) -> {
            Item item = items.get(position);
            onItemClicked(item, view);
        });

        fragmentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        this.fragmentView = fragmentView;
        return fragmentView;
    }

    private void onItemClicked(Item item, View view) {
        SharedPreferences.Editor edit = TextAnimationManager.getPrefs().edit();
        if (item.id == 1) {
            boolean v = !item.checked;
            edit.putBoolean("enabled", v).apply();
        } else if (item.id == 3) {
            boolean v = !item.checked;
            edit.putBoolean("ignore_spaces", v).apply();
        } else if (item.id == 4) {
            showNumberDialog("Темп появления (мс)", "duration", "300");
            return;
        } else if (item.id == 16) {
            String[] easings = new String[]{"Ease Out (Плавное замедление)", "Ease In (Плавный разгон)", "Ease In-Out (Разгон и замедление)", "Spring (Пружинистый отскок)", "Linear (Линейно)"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Кривая анимации");
            builder.setItems(easings, (dialog, which) -> {
                TextAnimationManager.getPrefs().edit().putInt("easing_type", which).apply();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
            showDialog(builder.create());
            return;
        } else if (item.id == 5) {
            boolean v = !item.checked;
            edit.putBoolean("blur_enabled", v).apply();
        } else if (item.id == 8) {
            boolean v = !item.checked;
            edit.putBoolean("slide_enabled", v).apply();
        } else if (item.id == 9) {
            showNumberDialog("Дистанция въезда (dp)", "slide_dist", "20");
            return;
        } else if (item.id == 10) {
            boolean v = !item.checked;
            edit.putBoolean("scale_enabled", v).apply();
        } else if (item.id == 11) {
            boolean v = !item.checked;
            edit.putBoolean("rotate_enabled", v).apply();
        } else if (item.id == 12) {
            boolean v = !item.checked;
            edit.putBoolean("delete_anim_enabled", v).apply();
        } else if (item.id == 13) {
            String[] styles = new String[]{"Пыль", "Искры", "Снежинки", "Лепестки", "Буквы"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Стиль частиц");
            builder.setItems(styles, (dialog, which) -> {
                TextAnimationManager.getPrefs().edit().putInt("particle_style", which).apply();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
            showDialog(builder.create());
            return;
        } else if (item.id == 19) {
            String[] palettes = new String[]{"Цвет текста / Темы", "Радужный микс", "Неоновый киберпанк", "Золотые искры", "Пастельные тона", "Монохром"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Цветовая палитра частиц");
            builder.setItems(palettes, (dialog, which) -> {
                TextAnimationManager.getPrefs().edit().putInt("particle_palette", which).apply();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
            showDialog(builder.create());
            return;
        } else if (item.id == 17) {
            showNumberDialog("Размер частиц (1-15 dp)", "particle_size", "3");
            return;
        } else if (item.id == 18) {
            showNumberDialog("Количество частиц (3-24)", "particle_count", "8");
            return;
        } else if (item.id == 14) {
            boolean v = !item.checked;
            edit.putBoolean("cursor_enabled", v).apply();
        } else if (item.id == 15) {
            boolean v = !item.checked;
            edit.putBoolean("liquid_cursor_enabled", v).apply();
        }

        buildItems();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showNumberDialog(String title, String key, String defVal) {
        if (getParentActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        final EditText editText = new EditText(getParentActivity());
        editText.setText(TextAnimationManager.getPrefs().getString(key, defVal));
        editText.setSelection(editText.getText().length());
        FrameLayout container = new FrameLayout(getParentActivity());
        container.setPadding(dp(24), dp(8), dp(24), dp(8));
        container.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        builder.setView(container);
        builder.setPositiveButton(LocaleController.getString("Save", R.string.Save), (dialog, which) -> {
            String txt = editText.getText().toString().trim();
            TextAnimationManager.getPrefs().edit().putString(key, txt.isEmpty() ? defVal : txt).apply();
            buildItems();
            if (adapter != null) adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
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
                int type = items.get(pos).type;
                return type == 0 || type == 1;
            }
            return false;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 4:
                    LinearLayout previewLayout = new LinearLayout(mContext);
                    previewLayout.setOrientation(LinearLayout.VERTICAL);
                    previewLayout.setPadding(dp(18), dp(16), dp(18), dp(16));
                    previewLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                    TextView title = new TextView(mContext);
                    title.setText("Попробуйте ввод текста:");
                    title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    title.setTypeface(AndroidUtilities.bold());
                    title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    previewLayout.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

                    org.telegram.ui.Components.EditTextBoldCursor input = new org.telegram.ui.Components.EditTextBoldCursor(mContext);
                    input.setHint("Печатайте здесь для проверки анимаций...");
                    input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    input.setTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
                    input.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));
                    input.setBackground(Theme.createRoundRectDrawable(dp(14), Theme.getColor(Theme.key_chat_messagePanelBackground)));
                    input.setPadding(dp(14), dp(10), dp(14), dp(10));
                    previewLayout.addView(input, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                    view = previewLayout;
                    break;
                case 1:
                default:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Item item = items.get(position);
            switch (holder.getItemViewType()) {
                case 0:
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    checkCell.setTextAndCheck(item.title, item.checked, item.divider);
                    break;
                case 1:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    settingsCell.setTextAndValue(item.title, item.value, item.divider);
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(item.title);
                    break;
                case 3:
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    infoCell.setText(item.title);
                    break;
            }
        }
    }

    @Override
    public int getNavigationBarColor() {
        return Theme.getColor(Theme.key_windowBackgroundGray, getResourceProvider());
    }
}
