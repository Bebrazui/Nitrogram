package org.telegram.messenger;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class NitrogramResources extends Resources {

    private final Resources base;

    @SuppressWarnings("deprecation")
    public NitrogramResources(Resources base) {
        super(base.getAssets(), base.getDisplayMetrics(), base.getConfiguration());
        this.base = base;
    }

    @Override
    public Drawable getDrawable(int id, @Nullable Theme theme) throws NotFoundException {
        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
            Drawable d = MaterialSymbolsHelper.get(id);
            if (d != null) {
                return d.mutate();
            }
        }
        return super.getDrawable(id, theme);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
            Drawable d = MaterialSymbolsHelper.get(id);
            if (d != null) {
                return d.mutate();
            }
        }
        return super.getDrawable(id);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
            Drawable d = MaterialSymbolsHelper.get(id);
            if (d != null) {
                return d.mutate();
            }
        }
        return super.getDrawableForDensity(id, density);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, @Nullable Theme theme) {
        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
            Drawable d = MaterialSymbolsHelper.get(id);
            if (d != null) {
                return d.mutate();
            }
        }
        return super.getDrawableForDensity(id, density, theme);
    }
}
