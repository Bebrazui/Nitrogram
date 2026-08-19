/*
 * Nitrogram mod settings screen.
 */

package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.ModConfig;
import org.telegram.messenger.ModManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ActionBar.AlertDialog;
import java.util.List;

public class ModSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
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
        adsHeaderRow = rowCount++;
        blockSponsoredRow = rowCount++;
        blockHashtagRow = rowCount++;
        adsInfoRow = rowCount++;
        networkHeaderRow = rowCount++;
        proxyRow = rowCount++;
        proxyInfoRow = rowCount++;
        modsHeaderRow = rowCount++;
        installedModsRow = rowCount++;
        modsInfoRow = rowCount++;
        materialHeaderRow = rowCount++;
        materialSwitchRow = rowCount++;
        materialSpacingRow = rowCount++;
        materialInfoRow = rowCount++;
        materialMonetRow = rowCount++;
        materialMonetInfoRow = rowCount++;
        materialNavRow = rowCount++;
        materialLoadingRow = rowCount++;
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
        actionBar.setTitle("Nitrogram");
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
            if (position == blockSponsoredRow) {
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
                org.telegram.ui.ActionBar.Theme.refreshThemeColors();
                if (v) {
                    int[] c = org.telegram.messenger.MonetColor.getMonetColors();
                    String src = org.telegram.messenger.MonetColor.getLastSource();
                    android.widget.Toast toast = android.widget.Toast.makeText(getParentActivity(),
                            c != null ? "Monet [" + src + "]: #" + String.format("%06X", c[0] & 0xFFFFFF) : "Monet: обои недоступны",
                            android.widget.Toast.LENGTH_LONG);
                    toast.show();
                }
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
            return position == blockSponsoredRow || position == blockHashtagRow || position == proxyRow || position == installedModsRow
                    || position == materialSwitchRow || position == materialSpacingRow || position == materialMonetRow || position == materialNavRow || position == materialLoadingRow;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == blockSponsoredRow || position == blockHashtagRow || position == materialSwitchRow || position == materialMonetRow || position == materialNavRow || position == materialLoadingRow) {
                return VIEW_TYPE_CHECK;
            } else if (position == adsInfoRow || position == proxyInfoRow || position == materialInfoRow || position == materialMonetInfoRow) {
                return VIEW_TYPE_INFO;
            } else if (position == adsHeaderRow || position == networkHeaderRow || position == modsHeaderRow || position == materialHeaderRow) {
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
                if (position == blockSponsoredRow) {
                    cell.setTextAndCheck("Block sponsored ads", ModConfig.isBlockSponsored(), true);
                } else if (position == blockHashtagRow) {
                    cell.setTextAndCheck("Block #реклама / #ad messages", ModConfig.isBlockHashtagAds(), false);
                } else if (position == materialSwitchRow) {
                    cell.setTextAndCheck("Material секции", ModConfig.isMaterialSections(), true);
                } else if (position == materialMonetRow) {
                    cell.setTextAndCheck("Material You (monet)", ModConfig.isDynamicColor(), true);
                } else if (position == materialNavRow) {
                    cell.setTextAndCheck("Material нижняя навигация", ModConfig.isMaterialNavigation(), true);
                } else if (position == materialLoadingRow) {
                    cell.setTextAndCheck("Material 3 Expressive загрузка", ModConfig.isMaterialLoading(), true);
                }
                break;
                }
                case VIEW_TYPE_HEADER: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == adsHeaderRow) {
                    cell.setText("Ad blocking");
                } else if (position == networkHeaderRow) {
                    cell.setText("Network");
                } else if (position == modsHeaderRow) {
                    cell.setText("Mods");
                } else if (position == materialHeaderRow) {
                    cell.setText("Оформление");
                }
                break;
                }
                case VIEW_TYPE_SETTING: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == proxyRow) {
                    cell.setText("WS Proxy", false);
                } else if (position == installedModsRow) {
                    cell.setText("Установленные моды (" + ModManager.getInstalledMods().size() + ")", false);
                } else if (position == materialSpacingRow) {
                    int sp = ModConfig.getMaterialSectionsSpacing();
                    cell.setTextAndValue("Отступы", sp == 0 ? "Обычный" : sp == 1 ? "Большой" : "Огромный", true);
                }
                break;
                }
                case VIEW_TYPE_INFO: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                if (position == adsInfoRow) {
                    cell.setText("Hides Telegram's official sponsored messages and channel posts tagged with #реклама or #ad.");
                } else if (position == proxyInfoRow) {
                    cell.setText("Built-in WebSocket proxy settings.");
                } else if (position == modsInfoRow) {
                    cell.setText("Список загруженных .so модов. Установка выполняется через .so файл, отправленный в чате.");
                } else if (position == materialInfoRow) {
                    cell.setText("Стилизует секции списков в стиле Material 3: скруглённые карточки, отступы между секциями и стиль заголовков.");
                } else if (position == materialMonetInfoRow) {
                    cell.setText("Перекрашивает акцентные цвета приложения (ссылки, переключатели, кнопки, шапка) в цвета системных обоев (Android 12+).");
                }
                break;
                }
            }
        }
    }
}
