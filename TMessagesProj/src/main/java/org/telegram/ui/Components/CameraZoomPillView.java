package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.camera.Camera2Session;

import java.util.Locale;

public class CameraZoomPillView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF bgRect = new RectF();
    private final RectF activeCircleRect = new RectF();

    private boolean isFront = false;
    private boolean hasUltraWide = true;

    private float[] levels = new float[]{0.6f, 1.0f, 2.0f};
    private String[] labels = new String[]{"0.6", "1x", "2x"};

    private float currentZoom = 1.0f;
    private float animatedZoom = 1.0f;
    private ValueAnimator animator;

    private boolean isDragging = false;
    private float touchStartX = 0;
    private int lastHapticIndex = -1;

    public interface Delegate {
        void onZoomChanged(float zoom);
    }
    private Delegate delegate;

    public CameraZoomPillView(Context context) {
        super(context);

        bgPaint.setColor(0x80000000);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(0x2affffff);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));

        activeCirclePaint.setColor(0xffffffff);
        activeCirclePaint.setStyle(Paint.Style.FILL);
        activeCirclePaint.setShadowLayer(dp(3), 0, dp(1), 0x55000000);

        labelPaint.setColor(0xddffffff);
        labelPaint.setTextSize(dp(11.5f));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        activeLabelPaint.setColor(0xff121212);
        activeLabelPaint.setTextSize(dp(11.5f));
        activeLabelPaint.setTextAlign(Paint.Align.CENTER);
        activeLabelPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        activeLabelPaint.setFakeBoldText(true);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        updateLevels();
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void updateLevels() {
        hasUltraWide = !isFront && Camera2Session.hasUltraWide(isFront);
        if (hasUltraWide) {
            levels = new float[]{0.6f, 1.0f, 2.0f};
            labels = new String[]{"0.6", "1x", "2x"};
        } else {
            levels = new float[]{1.0f, 2.0f, 3.0f};
            labels = new String[]{"1x", "2x", "3x"};
        }
        invalidate();
    }

    public void setIsFront(boolean front) {
        if (isFront != front) {
            isFront = front;
            updateLevels();
            setZoom(1.0f, false);
        }
    }

    public void setZoom(float zoom, boolean notify) {
        float min = levels[0];
        float max = levels[levels.length - 1] * 1.75f;
        currentZoom = Utilities.clamp(zoom, max, min);
        animatedZoom = currentZoom;
        invalidate();
        if (notify && delegate != null) {
            delegate.onZoomChanged(currentZoom);
        }
    }

    public float getZoom() {
        return currentZoom;
    }

    private float getFractionForZoom(float zoom) {
        if (zoom <= levels[0]) return 0f;
        if (zoom <= levels[1]) {
            return (zoom - levels[0]) / (levels[1] - levels[0]) * 0.5f;
        }
        if (zoom <= levels[2]) {
            return 0.5f + (zoom - levels[1]) / (levels[2] - levels[1]) * 0.5f;
        }
        return Math.min(1f, 1f + (zoom - levels[2]) / (levels[2] * 0.75f) * 0.15f);
    }

    private float getZoomForFraction(float frac) {
        float f = Utilities.clamp(frac, 1f, 0f);
        if (f <= 0.5f) {
            return levels[0] + (f / 0.5f) * (levels[1] - levels[0]);
        } else {
            return levels[1] + ((f - 0.5f) / 0.5f) * (levels[2] - levels[1]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = dp(160);
        int h = dp(38);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float r = h / 2f;

        bgRect.set(0, 0, w, h);
        canvas.drawRoundRect(bgRect, r, r, bgPaint);
        canvas.drawRoundRect(bgRect, r, r, borderPaint);

        float padX = dp(24);
        float usableW = w - padX * 2f;
        float frac = getFractionForZoom(animatedZoom);
        float activeCenterX = padX + frac * usableW;

        // Draw inactive labels
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float textY = h / 2f - (fm.ascent + fm.descent) / 2f;

        for (int i = 0; i < levels.length; i++) {
            float nodeX = padX + (i == 0 ? 0f : (i == 1 ? 0.5f : 1.0f)) * usableW;
            float dist = Math.abs(nodeX - activeCenterX);
            if (dist > dp(16)) {
                float alpha = Math.min(1f, (dist - dp(16)) / dp(10));
                labelPaint.setAlpha((int) (220 * alpha));
                canvas.drawText(labels[i], nodeX, textY, labelPaint);
            }
        }

        // Draw active circular badge
        float circleD = dp(30);
        activeCircleRect.set(activeCenterX - circleD / 2f, h / 2f - circleD / 2f, activeCenterX + circleD / 2f, h / 2f + circleD / 2f);
        canvas.drawRoundRect(activeCircleRect, circleD / 2f, circleD / 2f, activeCirclePaint);

        // Format active text
        String activeStr;
        if (Math.abs(animatedZoom - 0.6f) < 0.05f) {
            activeStr = "0.6";
        } else if (Math.abs(animatedZoom - Math.round(animatedZoom)) < 0.05f) {
            activeStr = String.format(Locale.US, "%dx", Math.round(animatedZoom));
        } else {
            activeStr = String.format(Locale.US, "%.1fx", animatedZoom);
        }

        Paint.FontMetrics afm = activeLabelPaint.getFontMetrics();
        float activeTextY = h / 2f - (afm.ascent + afm.descent) / 2f;
        canvas.drawText(activeStr, activeCenterX, activeTextY, activeLabelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float padX = dp(24);
        float usableW = getWidth() - padX * 2f;
        float fraction = Utilities.clamp((x - padX) / usableW, 1f, 0f);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                touchStartX = x;
                isDragging = false;
                if (animator != null) animator.cancel();

                // Check tap on nodes
                int tappedNode = getNearestNode(fraction);
                float nodeFrac = tappedNode == 0 ? 0f : (tappedNode == 1 ? 0.5f : 1.0f);
                if (Math.abs(fraction - nodeFrac) < 0.18f) {
                    animateToZoom(levels[tappedNode]);
                } else {
                    float newZoom = getZoomForFraction(fraction);
                    setZoom(newZoom, true);
                }
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - touchStartX) > dp(4)) {
                    isDragging = true;
                }
                if (isDragging) {
                    float newZoom = getZoomForFraction(fraction);
                    setZoom(newZoom, true);

                    int nearest = getNearestNode(fraction);
                    if (nearest != lastHapticIndex) {
                        float nearestF = nearest == 0 ? 0f : (nearest == 1 ? 0.5f : 1.0f);
                        if (Math.abs(fraction - nearestF) < 0.05f) {
                            lastHapticIndex = nearest;
                            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                        }
                    } else {
                        float nearestF = nearest == 0 ? 0f : (nearest == 1 ? 0.5f : 1.0f);
                        if (Math.abs(fraction - nearestF) >= 0.08f) {
                            lastHapticIndex = -1;
                        }
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                lastHapticIndex = -1;
                int nearest = getNearestNode(fraction);
                float nearestF = nearest == 0 ? 0f : (nearest == 1 ? 0.5f : 1.0f);
                if (Math.abs(fraction - nearestF) < 0.10f) {
                    animateToZoom(levels[nearest]);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int getNearestNode(float fraction) {
        if (fraction < 0.25f) return 0;
        if (fraction < 0.75f) return 1;
        return 2;
    }

    public void animateToZoom(float targetZoom) {
        if (animator != null) animator.cancel();
        float start = animatedZoom;
        float end = targetZoom;
        animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(180);
        animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animator.addUpdateListener(animation -> {
            animatedZoom = (float) animation.getAnimatedValue();
            currentZoom = animatedZoom;
            invalidate();
            if (delegate != null) {
                delegate.onZoomChanged(animatedZoom);
            }
        });
        animator.start();
    }
}
