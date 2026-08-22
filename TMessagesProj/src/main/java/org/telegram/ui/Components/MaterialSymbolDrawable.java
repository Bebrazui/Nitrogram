package org.telegram.ui.Components;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

import org.telegram.messenger.AndroidUtilities;

public class MaterialSymbolDrawable extends Drawable {

    private final Path originalPath;
    private final Path normalizedPath = new Path();
    private final Path transformedPath = new Path();
    private final Matrix transformMatrix = new Matrix();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int intrinsicWidth = AndroidUtilities.dp(24);
    private int intrinsicHeight = AndroidUtilities.dp(24);

    private ColorStateList tintList;
    private PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;
    private PorterDuffColorFilter tintFilter;
    private ColorFilter colorFilter;
    private int alpha = 255;
    private String pathData;

    public MaterialSymbolDrawable(String pathData, float viewportWidth, float viewportHeight) {
        this(pathData);
    }

    public MaterialSymbolDrawable(String pathData) {
        this.pathData = pathData;
        Path parsed;
        try {
            parsed = PathParser.createPathFromPathData(pathData);
        } catch (Throwable e) {
            parsed = new Path();
        }
        this.originalPath = parsed != null ? parsed : new Path();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        normalizePath();
    }

    private void normalizePath() {
        normalizedPath.set(originalPath);
    }

    public MaterialSymbolDrawable setSizeDp(int widthDp, int heightDp) {
        this.intrinsicWidth = AndroidUtilities.dp(widthDp);
        this.intrinsicHeight = AndroidUtilities.dp(heightDp);
        return this;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        updateTransform(bounds);
    }

    private void updateTransform(Rect bounds) {
        if (bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }
        transformMatrix.reset();
        float scale = Math.min((float) bounds.width() / 24f, (float) bounds.height() / 24f);
        float dx = bounds.left + (bounds.width() - 24f * scale) / 2f;
        float dy = bounds.top + (bounds.height() - 24f * scale) / 2f;

        transformMatrix.postScale(scale, scale);
        transformMatrix.postTranslate(dx, dy);

        transformedPath.reset();
        normalizedPath.transform(transformMatrix, transformedPath);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds == null || bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }
        if (transformedPath.isEmpty()) {
            updateTransform(bounds);
        }
        paint.setAlpha(alpha);
        if (colorFilter != null) {
            paint.setColorFilter(colorFilter);
        } else if (tintFilter != null) {
            paint.setColorFilter(tintFilter);
        } else {
            paint.setColorFilter(null);
        }
        canvas.drawPath(transformedPath, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public void setTint(int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        this.tintList = tint;
        updateTintFilter();
        invalidateSelf();
    }

    @Override
    public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
        this.tintMode = tintMode != null ? tintMode : PorterDuff.Mode.SRC_IN;
        updateTintFilter();
        invalidateSelf();
    }

    private void updateTintFilter() {
        if (tintList == null || tintMode == null) {
            tintFilter = null;
        } else {
            int color = tintList.getColorForState(getState(), tintList.getDefaultColor());
            tintFilter = new PorterDuffColorFilter(color, tintMode);
        }
    }

    @Override
    protected boolean onStateChange(@NonNull int[] state) {
        if (tintList != null && tintList.isStateful()) {
            updateTintFilter();
            invalidateSelf();
            return true;
        }
        return super.onStateChange(state);
    }

    @Override
    public boolean isStateful() {
        return (tintList != null && tintList.isStateful()) || super.isStateful();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return intrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return intrinsicHeight;
    }

    @NonNull
    @Override
    public Drawable mutate() {
        MaterialSymbolDrawable copy = new MaterialSymbolDrawable(pathData);
        copy.intrinsicWidth = intrinsicWidth;
        copy.intrinsicHeight = intrinsicHeight;
        copy.alpha = alpha;
        copy.tintList = tintList;
        copy.tintMode = tintMode;
        copy.colorFilter = colorFilter;
        copy.updateTintFilter();
        return copy;
    }
}
