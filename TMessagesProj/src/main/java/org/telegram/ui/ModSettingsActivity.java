/*
 * Nitrogram mod settings screen.
 */

package org.telegram.ui;

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
import org.telegram.messenger.ModConfig;
import org.telegram.messenger.ModManager;
import org.telegram.messenger.NitrogramConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.List;

public class ModSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int fakeHeaderRow;
    private int fakeStarsRow;
    private int fakeSwitchRow;
    private int fakeResetRow;
    private int fakePhoneRow;
    private int fakeUsernameRow;
    private int fakeUsernamesExtraRow;
    private int fakeFirstNameRow;
    private int fakeLastNameRow;
    private int fakeInfoRow;

    private int m3HeaderRow;
    private int m3SwitchRow;
    private int m3InfoRow;

    private int adsHeaderRow;
    private int blockSponsoredRow;
    private int blockHashtagRow;
    private int adsInfoRow;
    private int networkHeaderRow;
    private int proxyRow;
    private int proxyInfoRow;
    private int modsHeaderRow;
    private int installedModsRow;
    private int modsInfoRow;
    private int materialHeaderRow;
    private int materialSwitchRow;
    private int materialSpacingRow;
    private int materialInfoRow;
    private int materialMonetRow;
    private int materialMonetInfoRow;
    private int materialNavRow;
    private int materialLoadingRow;

    private static final int VIEW_TYPE_CHECK = 0;
    private static final int VIEW_TYPE_INFO = 1;
    private static final int VIEW_TYPE_HEADER = 2;
    private static final int VIEW_TYPE_SETTING = 3;

    private void updateRows() {
        rowCount = 0;

        // Fake Identity Section
        fakeHeaderRow = rowCount++;
        fakeStarsRow = rowCount++;
        fakeSwitchRow = rowCount++;
        fakeResetRow = rowCount++;
        fakePhoneRow = rowCount++;
        fakeUsernameRow = rowCount++;
        fakeUsernamesExtraRow = rowCount++;
        fakeFirstNameRow = rowCount++;
        fakeLastNameRow = rowCount++;
        fakeInfoRow = rowCount++;

        // Material 3 Components Section
        m3HeaderRow = rowCount++;
        m3SwitchRow = rowCount++;
        m3InfoRow = rowCount++;

        // Native Mods & Tools
        modsHeaderRow = rowCount++;
        installedModsRow = rowCount++;
        modsInfoRow = rowCount++;

        // Material Theme & UI
        materialHeaderRow = rowCount++;
        materialSwitchRow = rowCount++;
        materialSpacingRow = rowCount++;
        materialInfoRow = rowCount++;
        materialMonetRow = rowCount++;
        materialMonetInfoRow = rowCount++;
        materialNavRow = rowCount++;
        materialLoadingRow = rowCount++;

        // Ads & Filtering
        adsHeaderRow = rowCount++;
        blockSponsoredRow = rowCount++;
        blockHashtagRow = rowCount++;
        adsInfoRow = rowCount++;

        // Network
        networkHeaderRow = rowCount++;
        proxyRow = rowCount++;
        proxyInfoRow = rowCount++;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Настройки Nitrogram");
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
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position) -> {
            if (position == fakeStarsRow) {
                long currentStars = NitrogramConfig.getFakeStarsAmount();
                String curStr = currentStars >= 0 ? String.valueOf(currentStars) : "";
                showInputDialog("Баланс Telegram Stars (Visual)", "Введите число звёзд (или -1 для сброса)", curStr, text -> {
                    try {
                        long val = Long.parseLong(text.trim().replaceAll("[^0-9-]", ""));
                        NitrogramConfig.setFakeStarsAmount(val);
                        org.telegram.ui.Stars.StarsController.getInstance(currentAccount).invalidateBalance();
                        if (listAdapter != null) listAdapter.notifyItemChanged(fakeStarsRow);
                        Toast.makeText(getParentActivity(), "Баланс звёзд обновлен!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getParentActivity(), "Некорректное число", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (position == fakeSwitchRow) {
                boolean v = !NitrogramConfig.isFakeIdentityEnabled();
                NitrogramConfig.setFakeIdentityEnabled(v);
                ((TextCheckCell) view).setChecked(v);
                if (listAdapter != null) listAdapter.notifyDataSetChanged();
            } else if (position == fakeResetRow) {
                NitrogramConfig.resetToRealAccount();
                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                Toast.makeText(getParentActivity(), "Сброшено к реальным данным аккаунта!", Toast.LENGTH_SHORT).show();
            } else if (position == fakePhoneRow) {
                showInputDialog("Номер телефона", "Любой номер (например, +7 (999) 777-77-77, +1337)", NitrogramConfig.getFakePhone(), text -> {
                    NitrogramConfig.setFakePhone(text);
                    if (listAdapter != null) listAdapter.notifyItemChanged(fakePhoneRow);
                });
            } else if (position == fakeUsernameRow) {
                showInputDialog("Основной Username", "Юзернейм (например, durov)", NitrogramConfig.getFakeUsername(), text -> {
                    NitrogramConfig.setFakeUsername(text);
                    if (listAdapter != null) listAdapter.notifyItemChanged(fakeUsernameRow);
                });
            } else if (position == fakeUsernamesExtraRow) {
                showInputDialog("Дополнительные Юзернеймы", "Через запятую (например, nitro_master, vip_user)", NitrogramConfig.getFakeUsernamesExtra(), text -> {
                    NitrogramConfig.setFakeUsernamesExtra(text);
                    if (listAdapter != null) listAdapter.notifyItemChanged(fakeUsernamesExtraRow);
                });
            } else if (position == fakeFirstNameRow) {
                showInputDialog("Имя", "Введите имя", NitrogramConfig.getFakeFirstName(), text -> {
                    NitrogramConfig.setFakeFirstName(text);
                    if (listAdapter != null) listAdapter.notifyItemChanged(fakeFirstNameRow);
                });
            } else if (position == fakeLastNameRow) {
                showInputDialog("Фамилия", "Введите фамилию", NitrogramConfig.getFakeLastName(), text -> {
                    NitrogramConfig.setFakeLastName(text);
                    if (listAdapter != null) listAdapter.notifyItemChanged(fakeLastNameRow);
                });
            } else if (position == m3SwitchRow) {
                boolean v = !NitrogramConfig.isUseMaterial3Components();
                NitrogramConfig.setUseMaterial3Components(v);
                ((TextCheckCell) view).setChecked(v);
                Toast.makeText(getParentActivity(), "Переключены компоненты Material 3", Toast.LENGTH_SHORT).show();
            } else if (position == blockSponsoredRow) {
                boolean v = !ModConfig.isBlockSponsored();
                ModConfig.setBlockSponsored(v);
                ((TextCheckCell) view).setChecked(v);
            } else if (position == blockHashtagRow) {
                boolean v = !ModConfig.isBlockHashtagAds();
                ModConfig.setBlockHashtagAds(v);
                ((TextCheckCell) view).setChecked(v);
            } else if (position == proxyRow) {
                presentFragment(new WsProxySettingsActivity());
            } else if (position == installedModsRow) {
                presentFragment(new InstalledModsActivity());
            } else if (position == materialSwitchRow) {
                boolean v = !ModConfig.isMaterialSections();
                ModConfig.setMaterialSections(v);
                ((TextCheckCell) view).setChecked(v);
            } else if (position == materialSpacingRow) {
                showSpacingSelector();
            } else if (position == materialMonetRow) {
                boolean v = !ModConfig.isDynamicColor();
                ModConfig.setDynamicColor(v);
                ((TextCheckCell) view).setChecked(v);
                if (v) {
                    org.telegram.messenger.MonetColor.applyAccent();
                } else {
                    org.telegram.messenger.MonetColor.restoreAccent();
                }
                Theme.refreshThemeColors();
            } else if (position == materialNavRow) {
                boolean v = !ModConfig.isMaterialNavigation();
                ModConfig.setMaterialNavigation(v);
                ((TextCheckCell) view).setChecked(v);
                getParentActivity().recreate();
            } else if (position == materialLoadingRow) {
                boolean v = !ModConfig.isMaterialLoading();
                ModConfig.setMaterialLoading(v);
                ((TextCheckCell) view).setChecked(v);
                getParentActivity().recreate();
            }
        });

        return fragmentView;
    }

    private interface StringCallback {
        void onResult(String text);
    }

    private void showInputDialog(String title, String hint, String currentValue, StringCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);

        LinearLayout layout = new LinearLayout(getParentActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(10), AndroidUtilities.dp(20), AndroidUtilities.dp(10));

        final EditText editText = new EditText(getParentActivity());
        editText.setHint(hint);
        editText.setText(currentValue);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setSelection(editText.getText().length());
        layout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            if (callback != null) {
                callback.onResult(editText.getText().toString());
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showSpacingSelector() {
        CharSequence[] items = {"Обычный", "Большой", "Огромный"};
        int current = ModConfig.getMaterialSectionsSpacing();
        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
        b.setTitle("Отступы");
        b.setItems(items, (dialog, which) -> {
            ModConfig.setMaterialSectionsSpacing(which);
            if (listAdapter != null) {
                listAdapter.notifyItemChanged(materialSpacingRow);
            }
            dialog.dismiss();
        });
        b.show();
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == fakeSwitchRow || position == fakePhoneRow || position == fakeUsernameRow || position == fakeUsernamesExtraRow || position == fakeFirstNameRow || position == fakeLastNameRow
                    || position == m3SwitchRow || position == blockSponsoredRow || position == blockHashtagRow || position == proxyRow || position == installedModsRow
                    || position == materialSwitchRow || position == materialSpacingRow || position == materialMonetRow || position == materialNavRow || position == materialLoadingRow;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == fakeSwitchRow || position == m3SwitchRow || position == blockSponsoredRow || position == blockHashtagRow || position == materialSwitchRow || position == materialMonetRow || position == materialNavRow || position == materialLoadingRow) {
                return VIEW_TYPE_CHECK;
            } else if (position == fakeInfoRow || position == m3InfoRow || position == adsInfoRow || position == proxyInfoRow || position == materialInfoRow || position == materialMonetInfoRow || position == modsInfoRow) {
                return VIEW_TYPE_INFO;
            } else if (position == fakeHeaderRow || position == m3HeaderRow || position == adsHeaderRow || position == networkHeaderRow || position == modsHeaderRow || position == materialHeaderRow) {
                return VIEW_TYPE_HEADER;
            }
            return VIEW_TYPE_SETTING;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SETTING:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_INFO:
                default:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_CHECK: {
                    TextCheckCell cell = (TextCheckCell) holder.itemView;
                    if (position == fakeSwitchRow) {
                        cell.setTextAndCheck("Включить виртуальную подмену профиля", NitrogramConfig.isFakeIdentityEnabled(), true);
                    } else if (position == m3SwitchRow) {
                        cell.setTextAndCheck("Material 3 Sliders & Switches", NitrogramConfig.isUseMaterial3Components(), false);
                    } else if (position == blockSponsoredRow) {
                        cell.setTextAndCheck("Блокировка спонсорских постов", ModConfig.isBlockSponsored(), true);
                    } else if (position == blockHashtagRow) {
                        cell.setTextAndCheck("Блокировка #реклама сообщений", ModConfig.isBlockHashtagAds(), false);
                    } else if (position == materialSwitchRow) {
                        cell.setTextAndCheck("Material секции", ModConfig.isMaterialSections(), true);
                    } else if (position == materialMonetRow) {
                        cell.setTextAndCheck("Dynamic Colors (Monet)", ModConfig.isDynamicColor(), true);
                    } else if (position == materialNavRow) {
                        cell.setTextAndCheck("Material навигация", ModConfig.isMaterialNavigation(), true);
                    } else if (position == materialLoadingRow) {
                        cell.setTextAndCheck("Material загрузка", ModConfig.isMaterialLoading(), false);
                    }
                    break;
                }
                case VIEW_TYPE_HEADER: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                    if (position == fakeHeaderRow) {
                        cell.setText("Подмена личных данных");
                    } else if (position == m3HeaderRow) {
                        cell.setText("Компоненты Material Design 3");
                    } else if (position == modsHeaderRow) {
                        cell.setText("Нативные моды");
                    } else if (position == materialHeaderRow) {
                        cell.setText("Material 3 оформление");
                    } else if (position == adsHeaderRow) {
                        cell.setText("Фильтрация рекламы");
                    } else if (position == networkHeaderRow) {
                        cell.setText("Сеть и Прокси");
                    }
                    break;
                }
                case VIEW_TYPE_SETTING: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                    if (position == fakeStarsRow) {
                        long stars = NitrogramConfig.getFakeStarsAmount();
                        cell.setTextAndValue("Баланс Telegram Stars (Visual)", stars >= 0 ? (stars + " ⭐️") : "Реальный", true);
                    } else if (position == fakeResetRow) {
                        cell.setText("Сбросить к реальным данным аккаунта", false);
                        cell.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                    } else if (position == fakePhoneRow) {
                        cell.setTextAndValue("Номер телефона", NitrogramConfig.getFakePhone(), true);
                    } else if (position == fakeUsernameRow) {
                        cell.setTextAndValue("Основной Username", "@" + NitrogramConfig.getFakeUsername(), true);
                    } else if (position == fakeUsernamesExtraRow) {
                        cell.setTextAndValue("Доп. Юзернеймы", NitrogramConfig.getFakeUsernamesExtra(), true);
                    } else if (position == fakeFirstNameRow) {
                        cell.setTextAndValue("Имя", NitrogramConfig.getFakeFirstName().isEmpty() ? "Не задано" : NitrogramConfig.getFakeFirstName(), true);
                    } else if (position == fakeLastNameRow) {
                        cell.setTextAndValue("Фамилия", NitrogramConfig.getFakeLastName().isEmpty() ? "Не задана" : NitrogramConfig.getFakeLastName(), false);
                    } else if (position == proxyRow) {
                        cell.setTextAndValue("WebSocket / Shadowsocks прокси", "Настройки", false);
                    } else if (position == installedModsRow) {
                        cell.setTextAndValue("Управление нативными модами (.so)", "Список модов", false);
                    } else if (position == materialSpacingRow) {
                        String v = "Обычный";
                        int s = ModConfig.getMaterialSectionsSpacing();
                        if (s == 1) v = "Большой";
                        else if (s == 2) v = "Огромный";
                        cell.setTextAndValue("Отступы секций", v, true);
                    }
                    break;
                }
                case VIEW_TYPE_INFO: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == fakeInfoRow) {
                        cell.setText("Визуально подменяет номер телефона (любой формат), юзернеймы и имя в профиле и интерфейсе клиента.");
                    } else if (position == m3InfoRow) {
                        cell.setText("Заменяет стандартные ползунки и переключатели приложения на компоненты Material Design 3.");
                    } else if (position == modsInfoRow) {
                        cell.setText("Управление сторонними .so модами, хуками и их параметрами.");
                    } else if (position == adsInfoRow) {
                        cell.setText("Автоматически скрывает рекламные посты в каналах и сообщения с хештегами рекламы.");
                    } else if (position == proxyInfoRow) {
                        cell.setText("Настройка встроенных обходов и прокси-протоколов.");
                    } else if (position == materialInfoRow) {
                        cell.setText("Настройки визуала Material 3.");
                    }
                    break;
                }
            }
        }
    }
}
