package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.M3ShapeHelper;
import org.telegram.messenger.ModConfig;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.R;
import org.telegram.messenger.VoiceChanger;
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

public class ModCategoryActivity extends BaseFragment {

    public static final int CATEGORY_MAIN = 0;
    public static final int CATEGORY_APPEARANCE = 1;
    public static final int CATEGORY_CHATS = 2;
    public static final int CATEGORY_PLUGINS = 3;
    public static final int CATEGORY_OTHER = 4;

    private final int category;
    private RecyclerListView listView;
    private ListAdapter adapter;
    private final ArrayList<Item> items = new ArrayList<>();

    private static class Item {
        int id;
        int type; // 0 = check, 1 = setting, 2 = header, 3 = info
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

    public ModCategoryActivity(int category) {
        this.category = category;
    }

    @Override
    public boolean onFragmentCreate() {
        buildItems();
        return super.onFragmentCreate();
    }

    private void buildItems() {
        items.clear();
        switch (category) {
            case CATEGORY_MAIN:
                items.add(new Item(100, 2, "Telegram Stars"));
                long stars = NitrogramConfig.getFakeStarsAmount();
                items.add(new Item(1, 1, "Баланс Telegram Stars (Visual)", stars >= 0 ? (stars + " ⭐️") : "Реальный", false, true));
                items.add(new Item(101, 3, "Визуально меняет баланс Telegram Stars во всем приложении."));

                items.add(new Item(102, 2, "Подмена профиля"));
                items.add(new Item(2, 0, "Включить виртуальную подмену профиля", null, NitrogramConfig.isFakeIdentityEnabled(), true));
                items.add(new Item(3, 1, "Сбросить к реальным данным", null, false, true));
                items.add(new Item(4, 1, "Номер телефона", NitrogramConfig.getFakePhone(), false, true));
                items.add(new Item(5, 1, "Основной Username", "@" + NitrogramConfig.getFakeUsername(), false, true));
                items.add(new Item(6, 1, "Доп. Юзернеймы", NitrogramConfig.getFakeUsernamesExtra(), false, true));
                items.add(new Item(7, 1, "Имя", NitrogramConfig.getFakeFirstName().isEmpty() ? "Не задано" : NitrogramConfig.getFakeFirstName(), false, true));
                items.add(new Item(8, 1, "Фамилия", NitrogramConfig.getFakeLastName().isEmpty() ? "Не задана" : NitrogramConfig.getFakeLastName(), false, false));
                items.add(new Item(103, 3, "Подменяет имя, телефон и юзернеймы в интерфейсе клиента."));

                items.add(new Item(104, 2, "Основные"));
                items.add(new Item(9, 0, "Отключить округление чисел", "1.23K → 1,234", NitrogramConfig.isDisableNumberRounding(), true));
                items.add(new Item(10, 0, "Форматировать время с секундами", "12:34 → 12:34:56", NitrogramConfig.isShowSecondsInTime(), true));
                items.add(new Item(11, 0, "Вибрация в приложении", null, NitrogramConfig.isInAppVibrationEnabled(), true));
                items.add(new Item(12, 0, "Фильтр \"Zalgo\"", null, NitrogramConfig.isZalgoFilterEnabled(), false));
                items.add(new Item(105, 3, "Убирает искажающие текст символы \"Zalgo\" в именах и сообщениях."));
                break;

            case CATEGORY_APPEARANCE:
                items.add(new Item(200, 2, "Material 3 Оформление"));
                int shape = NitrogramConfig.getAvatarShape();
                items.add(new Item(21, 1, "Форма аватарок (Material Shape)", M3ShapeHelper.getShapeName(shape), false, true));
                items.add(new Item(22, 0, "Material 3 SearchBar", null, NitrogramConfig.isM3SearchBarEnabled(), true));
                items.add(new Item(23, 0, "Material секции", null, ModConfig.isMaterialSections(), true));
                items.add(new Item(24, 0, "Dynamic Colors (Monet)", null, ModConfig.isDynamicColor(), true));
                items.add(new Item(25, 0, "Material 3 Sliders & Switches", null, NitrogramConfig.isUseMaterial3Components(), true));
                items.add(new Item(27, 0, "Material загрузка", null, ModConfig.isMaterialLoading(), true));
                items.add(new Item(29, 0, "Иконки Material Symbols Rounded", null, NitrogramConfig.isMaterialSymbolsRoundedEnabled(), true));
                items.add(new Item(28, 1, "Анимации ввода текста (Text Animation)", "Настроить", false, false));
                items.add(new Item(201, 3, "Настройка визуального стиля Material Design 3, иконок и анимаций текста."));
                break;

            case CATEGORY_CHATS:
                items.add(new Item(300, 2, "Фильтрация и приватность"));
                items.add(new Item(31, 0, "Блокировка спонсорских постов", null, ModConfig.isBlockSponsored(), true));
                items.add(new Item(32, 0, "Блокировка #реклама сообщений", null, ModConfig.isBlockHashtagAds(), true));
                items.add(new Item(33, 0, "Ghost Mode (Невидимка)", null, NitrogramConfig.isGhostReadEnabled(), true));
                items.add(new Item(34, 0, "Скрывать статус печатания", null, NitrogramConfig.isHideTypingEnabled(), true));
                items.add(new Item(35, 0, "Безлимитные закрепы", null, NitrogramConfig.isUnlimitedPinsEnabled(), true));
                items.add(new Item(36, 0, "Безлимитные папки", null, NitrogramConfig.isUnlimitedFoldersEnabled(), true));
                items.add(new Item(37, 1, "Модулятор голоса (Voice Changer)", VoiceChanger.getEffectName(VoiceChanger.getEffect()), false, false));
                items.add(new Item(38, 1, "Ускорение отправки и скачивания", NitrogramConfig.getSpeedBoosterName(NitrogramConfig.getSpeedBoosterMode()), false, false));
                items.add(new Item(301, 3, "Управление чатами, приватностью, скрытием рекламы, эффектами голоса и ускорением передачи файлов."));
                break;

            case CATEGORY_PLUGINS:
                items.add(new Item(400, 2, "Сеть и расширения"));
                items.add(new Item(41, 1, "WebSocket / Shadowsocks прокси", "Настройки", false, true));
                items.add(new Item(42, 1, "Управление нативными модами (.so)", "Список", false, true));
                items.add(new Item(43, 1, "🧪 Проверить Mod API и Pine хуки", "Запуск теста", false, false));
                items.add(new Item(401, 3, "Тестирует нативный движок хуков Pine, перехват методов и выводит подробный отчёт в Logcat (tag: NitroModTest)."));
                break;

            case CATEGORY_OTHER:
                items.add(new Item(500, 2, "Дополнительно"));
                items.add(new Item(51, 1, "Очистить кэш модов", null, false, true));
                items.add(new Item(52, 1, "Сбросить все настройки", null, false, false));
                items.add(new Item(501, 3, "Служебные параметры клиента."));
                break;
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        switch (category) {
            case CATEGORY_MAIN:
                actionBar.setTitle("Основные");
                break;
            case CATEGORY_APPEARANCE:
                actionBar.setTitle("Внешний вид");
                break;
            case CATEGORY_CHATS:
                actionBar.setTitle("Чаты");
                break;
            case CATEGORY_PLUGINS:
                actionBar.setTitle("Плагины");
                break;
            case CATEGORY_OTHER:
                actionBar.setTitle("Другое");
                break;
        }

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
            onItemClicked(item, view, position);
        });

        fragmentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        this.fragmentView = fragmentView;
        return fragmentView;
    }

    private void onItemClicked(Item item, View view, int position) {
        if (item.id == 1) {
            long currentStars = NitrogramConfig.getFakeStarsAmount();
            String curStr = currentStars >= 0 ? String.valueOf(currentStars) : "";
            showInputDialog("Баланс Telegram Stars", "Введите число звёзд (или -1 для сброса)", curStr, text -> {
                try {
                    long val = Long.parseLong(text.trim().replaceAll("[^0-9-]", ""));
                    NitrogramConfig.setFakeStarsAmount(val);
                    org.telegram.ui.Stars.StarsController.getInstance(currentAccount).invalidateBalance();
                    buildItems();
                    if (adapter != null) adapter.notifyDataSetChanged();
                    Toast.makeText(getParentActivity(), "Баланс звёзд обновлен!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getParentActivity(), "Некорректное число", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (item.id == 2) {
            boolean v = !NitrogramConfig.isFakeIdentityEnabled();
            NitrogramConfig.setFakeIdentityEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
            NitrogramConfig.notifyIdentityChanged();
            buildItems();
            if (adapter != null) adapter.notifyDataSetChanged();
        } else if (item.id == 3) {
            NitrogramConfig.resetToRealAccount();
            buildItems();
            if (adapter != null) adapter.notifyDataSetChanged();
            Toast.makeText(getParentActivity(), "Сброшено к реальным данным аккаунта!", Toast.LENGTH_SHORT).show();
        } else if (item.id == 4) {
            showInputDialog("Номер телефона", "Любой номер", NitrogramConfig.getFakePhone(), text -> {
                NitrogramConfig.setFakePhone(text);
                NitrogramConfig.notifyIdentityChanged();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        } else if (item.id == 5) {
            showInputDialog("Основной Username", "Юзернейм", NitrogramConfig.getFakeUsername(), text -> {
                NitrogramConfig.setFakeUsername(text);
                NitrogramConfig.notifyIdentityChanged();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        } else if (item.id == 6) {
            showInputDialog("Доп. Юзернеймы", "Через запятую", NitrogramConfig.getFakeUsernamesExtra(), text -> {
                NitrogramConfig.setFakeUsernamesExtra(text);
                NitrogramConfig.notifyIdentityChanged();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        } else if (item.id == 7) {
            showInputDialog("Имя", "Имя", NitrogramConfig.getFakeFirstName(), text -> {
                NitrogramConfig.setFakeFirstName(text);
                NitrogramConfig.notifyIdentityChanged();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        } else if (item.id == 8) {
            showInputDialog("Фамилия", "Фамилия", NitrogramConfig.getFakeLastName(), text -> {
                NitrogramConfig.setFakeLastName(text);
                NitrogramConfig.notifyIdentityChanged();
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        } else if (item.id == 9) {
            boolean v = !NitrogramConfig.isDisableNumberRounding();
            NitrogramConfig.setDisableNumberRounding(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 10) {
            boolean v = !NitrogramConfig.isShowSecondsInTime();
            NitrogramConfig.setShowSecondsInTime(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 11) {
            boolean v = !NitrogramConfig.isInAppVibrationEnabled();
            NitrogramConfig.setInAppVibrationEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 12) {
            boolean v = !NitrogramConfig.isZalgoFilterEnabled();
            NitrogramConfig.setZalgoFilterEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 21) {
            presentFragment(new AvatarShapeActivity());
        } else if (item.id == 22) {
            boolean v = !NitrogramConfig.isM3SearchBarEnabled();
            NitrogramConfig.setM3SearchBarEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 23) {
            boolean v = !ModConfig.isMaterialSections();
            ModConfig.setMaterialSections(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 24) {
            boolean v = !ModConfig.isDynamicColor();
            ModConfig.setDynamicColor(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
            if (v) org.telegram.messenger.MonetColor.applyAccent();
            else org.telegram.messenger.MonetColor.restoreAccent();
            Theme.refreshThemeColors();
        } else if (item.id == 25) {
            boolean v = !NitrogramConfig.isUseMaterial3Components();
            NitrogramConfig.setUseMaterial3Components(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 26) {
            boolean v = !ModConfig.isMaterialNavigation();
            ModConfig.setMaterialNavigation(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 27) {
            boolean v = !ModConfig.isMaterialLoading();
            ModConfig.setMaterialLoading(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 29) {
            boolean v = !NitrogramConfig.isMaterialSymbolsRoundedEnabled();
            NitrogramConfig.setMaterialSymbolsRoundedEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 28) {
            presentFragment(new TextAnimationActivity());
        } else if (item.id == 31) {
            boolean v = !ModConfig.isBlockSponsored();
            ModConfig.setBlockSponsored(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 32) {
            boolean v = !ModConfig.isBlockHashtagAds();
            ModConfig.setBlockHashtagAds(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 33) {
            boolean v = !NitrogramConfig.isGhostReadEnabled();
            NitrogramConfig.setGhostReadEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 34) {
            boolean v = !NitrogramConfig.isHideTypingEnabled();
            NitrogramConfig.setHideTypingEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 35) {
            boolean v = !NitrogramConfig.isUnlimitedPinsEnabled();
            NitrogramConfig.setUnlimitedPinsEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 36) {
            boolean v = !NitrogramConfig.isUnlimitedFoldersEnabled();
            NitrogramConfig.setUnlimitedFoldersEnabled(v);
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(v);
        } else if (item.id == 37) {
            final String[] effects = new String[] {
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_NONE),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_CHIPMUNK),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_DEEP),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_ROBOT),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_RADIO),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_ECHO),
                VoiceChanger.getEffectName(VoiceChanger.EFFECT_ALIEN)
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Модулятор голоса");
            builder.setItems(effects, (dialog, which) -> {
                VoiceChanger.setEffect(which);
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
            showDialog(builder.create());
        } else if (item.id == 38) {
            final String[] modes = new String[] {
                NitrogramConfig.getSpeedBoosterName(NitrogramConfig.SPEED_BOOSTER_OFF),
                NitrogramConfig.getSpeedBoosterName(NitrogramConfig.SPEED_BOOSTER_FAST),
                NitrogramConfig.getSpeedBoosterName(NitrogramConfig.SPEED_BOOSTER_ULTRA)
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Ускорение отправки и скачивания");
            builder.setItems(modes, (dialog, which) -> {
                NitrogramConfig.setSpeedBoosterMode(which);
                buildItems();
                if (adapter != null) adapter.notifyDataSetChanged();
            });
            showDialog(builder.create());
        } else if (item.id == 41) {
            presentFragment(new WsProxySettingsActivity());
        } else if (item.id == 42) {
            presentFragment(new InstalledModsActivity());
        } else if (item.id == 43) {
            String testReport = org.telegram.messenger.HookManager.runApiSelfTest();
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("Тест Mod API & Pine Hooks");
            builder.setMessage(testReport);
            builder.setPositiveButton("OK", null);
            showDialog(builder.create());
            return;
        } else if (item.id == 51) {
            Toast.makeText(getParentActivity(), "Кэш модов очищен", Toast.LENGTH_SHORT).show();
        } else if (item.id == 52) {
            Toast.makeText(getParentActivity(), "Настройки сброшены", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputDialog(String title, String hint, String initialText, InputCallback callback) {
        if (getParentActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);

        final EditText editText = new EditText(getParentActivity());
        editText.setTextSize(16);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        editText.setHint(hint);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (initialText != null) {
            editText.setText(initialText);
            editText.setSelection(editText.getText().length());
        }

        FrameLayout container = new FrameLayout(getParentActivity());
        container.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(8), AndroidUtilities.dp(24), AndroidUtilities.dp(8));
        container.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        builder.setView(container);

        builder.setPositiveButton(LocaleController.getString("Save", R.string.Save), (dialog, which) -> {
            if (callback != null) {
                callback.onInput(editText.getText().toString());
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private interface InputCallback {
        void onInput(String text);
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
                    view.setBackground(Theme.getSelectorDrawable(true));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 1:
                default:
                    view = new TextSettingsCell(mContext);
                    view.setBackground(Theme.getSelectorDrawable(true));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Item item = items.get(position);
            switch (item.type) {
                case 0: {
                    TextCheckCell cell = (TextCheckCell) holder.itemView;
                    if (item.value != null) {
                        cell.setTextAndValueAndCheck(item.title, item.value, item.checked, false, item.divider);
                    } else {
                        cell.setTextAndCheck(item.title, item.checked, item.divider);
                    }
                    break;
                }
                case 1: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                    if (item.value != null) {
                        cell.setTextAndValue(item.title, item.value, item.divider);
                    } else {
                        cell.setText(item.title, item.divider);
                    }
                    break;
                }
                case 2: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                    cell.setText(item.title);
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    cell.setText(item.title);
                    break;
                }
            }
        }
    }

    @Override
    public int getNavigationBarColor() {
        return Theme.getColor(Theme.key_windowBackgroundGray, getResourceProvider());
    }
}
