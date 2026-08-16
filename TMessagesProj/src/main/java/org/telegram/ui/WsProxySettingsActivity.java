/*
 * Settings screen for the embedded TG WS Proxy.
 */

package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.wsproxy.WsProxyController;
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

public class WsProxySettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int enableRow;
    private int enableInfoRow;
    private int connectionHeaderRow;
    private int cloudflareRow;
    private int cloudflareInfoRow;
    private int secretRow;
    private int statusRow;
    private int secretInfoRow;

    private static final int VIEW_TYPE_CHECK = 0;
    private static final int VIEW_TYPE_INFO = 1;
    private static final int VIEW_TYPE_HEADER = 2;
    private static final int VIEW_TYPE_SETTING = 3;
    private static final int VIEW_TYPE_SHADOW = 4;

    private void updateRows() {
        rowCount = 0;
        enableRow = rowCount++;
        enableInfoRow = rowCount++;
        boolean on = WsProxyController.isEnabled();
        if (on) {
            connectionHeaderRow = rowCount++;
            cloudflareRow = rowCount++;
            cloudflareInfoRow = rowCount++;
            statusRow = rowCount++;
            secretRow = rowCount++;
            secretInfoRow = rowCount++;
        } else {
            connectionHeaderRow = -1;
            cloudflareRow = -1;
            cloudflareInfoRow = -1;
            statusRow = -1;
            secretRow = -1;
            secretInfoRow = -1;
        }
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
        actionBar.setTitle("WS Proxy");
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
        ((DefaultItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position) -> {
            if (!WsProxyController.isAvailable()) {
                Toast.makeText(context, "Proxy core not available for this device", Toast.LENGTH_SHORT).show();
                return;
            }
            if (position == enableRow) {
                boolean newState = !WsProxyController.isEnabled();
                WsProxyController.setEnabled(newState);
                ((TextCheckCell) view).setChecked(newState);
                updateRows();
                listAdapter.notifyDataSetChanged();
            } else if (position == cloudflareRow) {
                boolean newState = !WsProxyController.isCloudflareEnabled();
                WsProxyController.setCloudflareEnabled(newState);
                ((TextCheckCell) view).setChecked(newState);
                // restart to apply CloudFlare change
                if (WsProxyController.isEnabled()) {
                    WsProxyController.stop();
                    WsProxyController.start();
                }
                if (statusRow != -1) {
                    listAdapter.notifyItemChanged(statusRow);
                }
            } else if (position == secretRow) {
                String secret = WsProxyController.getMtprotoSecret();
                if (secret != null) {
                    AndroidUtilities.addToClipboard(secret);
                    Toast.makeText(context, "Secret copied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
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
            return position == enableRow || position == cloudflareRow || position == secretRow;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == enableRow || position == cloudflareRow) {
                return VIEW_TYPE_CHECK;
            } else if (position == enableInfoRow || position == cloudflareInfoRow || position == secretInfoRow) {
                return VIEW_TYPE_INFO;
            } else if (position == connectionHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == statusRow || position == secretRow) {
                return VIEW_TYPE_SETTING;
            }
            return VIEW_TYPE_SHADOW;
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
                    if (position == enableRow) {
                        cell.setTextAndCheck("Enable WS Proxy", WsProxyController.isEnabled(), cloudflareRow != -1);
                    } else if (position == cloudflareRow) {
                        cell.setTextAndCheck("Route via CloudFlare", WsProxyController.isCloudflareEnabled(), false);
                    }
                    break;
                }
                case VIEW_TYPE_HEADER: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                    if (position == connectionHeaderRow) {
                        cell.setText("Connection");
                    }
                    break;
                }
                case VIEW_TYPE_SETTING: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                    if (position == statusRow) {
                        cell.setTextAndValue("Status", WsProxyController.isRunning() ? "Running" : "Stopped", true);
                    } else if (position == secretRow) {
                        String secret = WsProxyController.getMtprotoSecret();
                        cell.setTextAndValue("Secret (tap to copy)", secret == null ? "—" : secret, false);
                    }
                    break;
                }
                case VIEW_TYPE_INFO: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == enableInfoRow) {
                        if (!WsProxyController.isAvailable()) {
                            cell.setText("Native proxy core is not available for this device architecture.");
                        } else {
                            cell.setText("Runs a local MTProto proxy (127.0.0.1:" + WsProxyController.getBoundPort() + ") and routes Telegram through secure WebSocket connections to the datacenters.");
                        }
                    } else if (position == cloudflareInfoRow) {
                        cell.setText("Tunnel traffic through CloudFlare WebSocket endpoints. Disable for a direct WSS connection to the datacenters.");
                    } else if (position == secretInfoRow) {
                        cell.setText("This proxy is applied to Telegram automatically while enabled.");
                    }
                    break;
                }
            }
        }
    }
}
