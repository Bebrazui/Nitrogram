package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.nitrogram.modsdk.ModSettingsHost;
import org.nitrogram.modsdk.ModSettingsScreen;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ModManager;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Контейнер клиента для экрана настроек конкретного мода.
 * Сам экран описывается в коде мода (ModSettingsScreen) и хостится здесь.
 */
public class ModDetailSettingsActivity extends BaseFragment {

    private String modId;
    private String modName;

    public void setMod(ModManager.ModMeta m) {
        modId = m.id;
        modName = m.name;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(modName != null && !modName.isEmpty() ? modName : "Настройки мода");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        frameLayout.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8));
        scroll.addView(content, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        Object screenObj = ModManager.getSettingsScreen(modId);
        if (screenObj instanceof ModSettingsScreen) {
            ModSettingsHost host = new ModSettingsHost() {
                @Override
                public Object getValue(String key, Object def) {
                    return ModManager.getModSetting(modId, key, def);
                }

                @Override
                public void setValue(String key, Object value) {
                    ModManager.setModSetting(modId, key, value);
                }

                @Override
                public boolean getBool(String key, boolean def) {
                    Object o = getValue(key, def);
                    return o instanceof Boolean ? (Boolean) o : def;
                }

                @Override
                public int getInt(String key, int def) {
                    Object o = getValue(key, def);
                    return o instanceof Number ? ((Number) o).intValue() : def;
                }

                @Override
                public float getFloat(String key, float def) {
                    Object o = getValue(key, def);
                    return o instanceof Number ? ((Number) o).floatValue() : def;
                }

                @Override
                public String getString(String key, String def) {
                    Object o = getValue(key, def);
                    return o instanceof String ? (String) o : def;
                }
            };
            View screenView = ((ModSettingsScreen) screenObj).createView(context, host, content);
            if (screenView != null) {
                content.addView(screenView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        return fragmentView;
    }
}
