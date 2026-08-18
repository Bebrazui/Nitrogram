/*
 * Material Containers — native port of the "material_containers" plugin.
 * Renders list sections in Material 3 style (rounded cards, gaps, styled headers).
 * Original plugin used Xposed hooks; here the same logic is wired directly into
 * the client's own classes via MaterialContainers.* calls.
 */

package org.telegram.messenger;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.WeakHashMap;

public class MaterialContainers {

    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path path = new Path();
    private static final RectF rect = new RectF();
    private static final float[] radii = new float[8];

    private static final WeakHashMap<View, Boolean> mergedPrev = new WeakHashMap<>();
    private static final WeakHashMap<View, Boolean> mergedNext = new WeakHashMap<>();
    private static final WeakHashMap<View, Boolean> runIsSectionCache = new WeakHashMap<>();

    static {
        paint.setStyle(Paint.Style.FILL);
    }

    public static boolean isEnabled() {
        return ModConfig.isMaterialSections();
    }

    public static int getSpacingMode() {
        return ModConfig.getMaterialSectionsSpacing();
    }

    public static void syncSettings() {
        // Settings are read live from ModConfig; nothing to cache.
    }

    public static void log(String str) {
        android.util.Log.e("MaterialContainers", str);
    }

    // region radii computation

    private static float getOuterR() {
        return AndroidUtilities.dp(20.0f);
    }

    private static float getInnerR() {
        return AndroidUtilities.dp(4.0f);
    }

    private static int getGap() {
        float f;
        int mode = getSpacingMode();
        if (mode == 1) {
            f = 4.0f;
        } else if (mode == 2) {
            f = 6.0f;
        } else {
            f = 2.0f;
        }
        return AndroidUtilities.dp(f);
    }

    private static float outerRForChild(View view) {
        return getOuterR();
    }

    private static boolean isMergedWithPrev(View view) {
        if (view == null) {
            return false;
        }
        Boolean b = mergedPrev.get(view);
        return b != null && b;
    }

    private static boolean isMergedWithNext(View view) {
        if (view == null) {
            return false;
        }
        Boolean b = mergedNext.get(view);
        return b != null && b;
    }

    public static void markMerged(View view, boolean z, boolean z2) {
        if (isEnabled()) {
            mergedPrev.put(view, z ? Boolean.TRUE : null);
            mergedNext.put(view, z2 ? Boolean.TRUE : null);
        }
    }

    private static float[] m3Radii(View view, boolean z, boolean z2) {
        float f;
        float fOuterRForChild = outerRForChild(view);
        float innerR = getInnerR();
        if (isMergedWithPrev(view)) {
            f = 0.0f;
        } else if (z) {
            f = innerR;
        } else {
            f = fOuterRForChild;
        }
        if (isMergedWithNext(view)) {
            fOuterRForChild = 0.0f;
        } else if (z2) {
            fOuterRForChild = innerR;
        }
        return new float[]{f, fOuterRForChild};
    }

    private static View visualSibling(ViewGroup viewGroup, int i, boolean z) {
        int i2 = z ? 1 : -1;
        int childCount = viewGroup.getChildCount();
        for (int i3 = i + i2; i3 >= 0 && i3 < childCount; i3 += i2) {
            View childAt = viewGroup.getChildAt(i3);
            if (childAt != null && childAt.getVisibility() == View.VISIBLE && childAt.getAlpha() > 0.01f) {
                return childAt;
            }
        }
        return null;
    }

    private static float[] computeRadii(ViewGroup viewGroup, View view, int i, Utilities.CallbackReturn<View, Boolean> isSectionItem) {
        View viewVisualSibling = null;
        boolean z = false;
        View viewVisualSibling2 = i >= 0 ? visualSibling(viewGroup, i, false) : null;
        if (i >= 0) {
            viewVisualSibling = visualSibling(viewGroup, i, true);
        }
        boolean z2 = viewVisualSibling2 != null && runIsSectionChecked(isSectionItem, viewVisualSibling2);
        if (viewVisualSibling != null && runIsSectionChecked(isSectionItem, viewVisualSibling)) {
            z = true;
        }
        return m3Radii(view, z2, z);
    }

    private static float[] sectionRadiiFor(RecyclerListView rlv, View view) {
        try {
            RecyclerListView.ListSectionsDecoration deco = rlv.getSectionsItemDecoration();
            if (deco == null || deco.isSectionItem == null || !runIsSectionChecked(deco.isSectionItem, view)) {
                return null;
            }
            int index = ((ViewGroup) rlv).indexOfChild(view);
            if (index < 0) {
                return null;
            }
            return computeRadii(rlv, view, index, deco.isSectionItem);
        } catch (Exception e) {
            return null;
        }
    }

    private static void setRadii(float f, float f2) {
        radii[0] = f;
        radii[1] = f;
        radii[2] = f;
        radii[3] = f;
        radii[4] = f2;
        radii[5] = f2;
        radii[6] = f2;
        radii[7] = f2;
    }

    private static int multAlpha(int i, float f) {
        int i2 = (int) (((i >>> 24) & 255) * f);
        if (i2 < 0) {
            i2 = 0;
        }
        if (i2 > 255) {
            i2 = 255;
        }
        return (i & 0x00FFFFFF) | ((i2 <= 255 ? i2 : 255) << 24);
    }

    private static float getRecyclerListViewTop(View view) {
        return view.getTop();
    }

    private static float getRecyclerListViewBottom(View view) {
        return view.getBottom();
    }

    // endregion

    // region section item detection

    @SuppressWarnings("unchecked")
    private static boolean runIsSection(Utilities.CallbackReturn<View, Boolean> cb, View view) {
        try {
            Boolean res = cb.run(view);
            return res != null && res;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean runIsSectionChecked(Utilities.CallbackReturn<View, Boolean> cb, View view) {
        if (!isEnabled() || view instanceof HeaderCell) {
            return false;
        }
        Boolean cached = runIsSectionCache.get(view);
        if (cached != null) {
            return cached;
        }
        boolean res = runIsSection(cb, view);
        runIsSectionCache.put(view, res);
        return res;
    }

    // endregion

    // region drawing

    public static void drawSectionsBackgrounds(Canvas canvas, RecyclerListView rlv) {
        try {
            runIsSectionCache.clear();
            ViewGroup viewGroup = rlv;
            RecyclerListView.ListSectionsDecoration deco = rlv.getSectionsItemDecoration();
            if (deco == null || deco.isSectionItem == null) {
                return;
            }
            int color = Theme.getColor(Theme.key_windowBackgroundWhite, rlv.getResourcesProvider());
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt != null && childAt.getVisibility() == View.VISIBLE && childAt.getAlpha() > 0.0f
                        && runIsSectionChecked(deco.isSectionItem, childAt)) {
                    float[] fArr = computeRadii(viewGroup, childAt, i, deco.isSectionItem);
                    float f = fArr[0];
                    float f2 = fArr[1];
                    rect.set(childAt.getLeft(), getRecyclerListViewTop(childAt), childAt.getRight(), getRecyclerListViewBottom(childAt));
                    setRadii(f, f2);
                    path.rewind();
                    path.addRoundRect(rect, radii, Path.Direction.CW);
                    paint.setColor(multAlpha(color, childAt.getAlpha()));
                    canvas.drawPath(path, paint);
                }
            }
        } catch (Exception e) {
            log("drawSectionsBackgrounds failed: " + e);
        }
    }

    public static void clipChild(Canvas canvas, View view, RecyclerListView rlv) {
        try {
            RecyclerListView.ListSectionsDecoration deco = rlv.getSectionsItemDecoration();
            if (deco == null || deco.isSectionItem == null || !runIsSectionChecked(deco.isSectionItem, view)) {
                return;
            }
            float[] fArr = computeRadii(rlv, view, ((ViewGroup) rlv).indexOfChild(view), deco.isSectionItem);
            float f = fArr[0];
            float f2 = fArr[1];
            rect.set(view.getX(), getRecyclerListViewTop(view), view.getX() + view.getWidth(), getRecyclerListViewBottom(view));
            setRadii(f, f2);
            path.rewind();
            path.addRoundRect(rect, radii, Path.Direction.CW);
            canvas.clipPath(path);
        } catch (Exception e) {
            log("clipChild failed: " + e);
        }
    }

    public static Drawable makeClipBackground(RecyclerListView rlv, View view) {
        try {
            float[] fArr = sectionRadiiFor(rlv, view);
            if (fArr == null) {
                return null;
            }
            float f = fArr[0];
            float f2 = fArr[1];
            final int color = Theme.getColor(Theme.key_windowBackgroundWhite, rlv.getResourcesProvider());
            final int width = view.getWidth();
            final int height = view.getHeight();
            final float[] fArrFinal = {f, f, f, f, f2, f2, f2, f2};
            return new Drawable() {
                private final Paint dPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                private final Path dClipPath = new Path();
                private final RectF dTmp = new RectF();

                @Override
                public void draw(Canvas canvas) {
                    canvas.save();
                    dTmp.set(0.0f, 0.0f, width, height);
                    dClipPath.rewind();
                    dClipPath.addRoundRect(dTmp, fArrFinal, Path.Direction.CW);
                    canvas.clipPath(dClipPath);
                    dPaint.setColor(ColorUtils.setAlphaComponent(color, dPaint.getAlpha()));
                    canvas.drawRect(dTmp, dPaint);
                    canvas.restore();
                }

                @Override
                public void setAlpha(int i) {
                    dPaint.setAlpha(i);
                }

                @Override
                public void setColorFilter(ColorFilter colorFilter) {
                }

                @Override
                public int getOpacity() {
                    return -2;
                }
            };
        } catch (Exception e) {
            return null;
        }
    }

    public static void applyScrimClip(Canvas canvas, View view) {
        try {
            ViewParent parent = view.getParent();
            if (parent instanceof RecyclerListView) {
                RecyclerListView rlv = (RecyclerListView) parent;
                if (rlv.hasSections()) {
                    rect.set(0.0f, 0.0f, view.getWidth(), view.getHeight());
                    path.rewind();
                    float[] fArr = sectionRadiiFor(rlv, view);
                    if (fArr == null) {
                        return;
                    }
                    setRadii(fArr[0], fArr[1]);
                    path.addRoundRect(rect, radii, Path.Direction.CW);
                    canvas.clipPath(path);
                }
            }
        } catch (Exception e) {
            log("applyScrimClip failed: " + e);
        }
    }

    // endregion

    // region offsets / styling

    public static void augmentItemOffsets(Rect rect2, View view, int i) {
        if (i > 0 && !isMergedWithPrev(view)) {
            rect2.top += getGap();
        }
    }

    public static int shadowHeightDp(Object obj, int i) {
        if (isEnabled()) {
            return (obj == null || !isHeaderViewType(obj)) ? 16 : 10;
        }
        return i;
    }

    public static boolean isHeaderViewType(int i) {
        return i == 0 || i == 26 || i == 1 || i == 42;
    }

    public static boolean isHeaderViewType(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            return isHeaderViewType(((UItem) obj).viewType);
        } catch (Exception e) {
            return false;
        }
    }

    public static void styleHeaderCell(View view) {
        try {
            if (!(view instanceof HeaderCell)) {
                return;
            }
            HeaderCell cell = (HeaderCell) view;
            cell.setBackgroundColor(0);
            cell.setBottomMargin(4);
            java.lang.reflect.Field tvField = HeaderCell.class.getDeclaredField("textView");
            tvField.setAccessible(true);
            View textView = (View) tvField.get(cell);
            if (textView instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) textView;
                tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14.0f);
                tv.setTypeface(AndroidUtilities.bold());
                tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            }
        } catch (Exception e) {
            log("styleHeaderCell failed: " + e);
        }
    }

    public static void setTextInfoPrivacyCellFixedSize(View view, int i) {
        try {
            if (view instanceof TextInfoPrivacyCell) {
                ((TextInfoPrivacyCell) view).setFixedSize(i);
            }
        } catch (Exception e) {
            log("setTextInfoPrivacyCellFixedSize failed: " + e);
        }
    }

    public static void onUniversalBind(UniversalAdapter adapter, RecyclerView.ViewHolder holder, int position) {
        if (!isEnabled()) {
            return;
        }
        try {
            RecyclerListView listView = adapter.getListView();
            if (listView == null || !listView.hasSections()) {
                return;
            }
            UItem item = adapter.getItem(position);
            UItem nextItem = adapter.getItem(position + 1);
            if (item != null) {
                int i = item.pad;
                int i2 = nextItem != null ? nextItem.pad : 0;
                View view = holder.itemView;
                boolean mergeWithPrev = i > 0;
                boolean mergeWithNext = i2 > 0;
                markMerged(view, mergeWithPrev, mergeWithNext);
            }
            int itemViewType = holder.getItemViewType();
            if (isHeaderViewType(itemViewType)) {
                styleHeaderCell(holder.itemView);
            } else if (itemViewType == 7 || itemViewType == 8) {
                if (holder.itemView instanceof TextInfoPrivacyCell) {
                    setTextInfoPrivacyCellFixedSize(holder.itemView, itemViewType == 8 ? 220 : shadowHeightDp(nextItem, 12));
                }
            }
        } catch (Exception e) {
            log("onUniversalBind failed: " + e);
        }
    }
}
