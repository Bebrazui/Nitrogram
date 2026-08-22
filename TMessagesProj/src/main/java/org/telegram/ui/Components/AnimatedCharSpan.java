package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.style.ReplacementSpan;
import android.widget.EditText;

public class AnimatedCharSpan extends ReplacementSpan {

    private final EditText editText;
    private final char character;
    private final long startTime;
    private final long duration;
    private final long blurDuration;
    private final float slideDist;
    private final float scaleStart;
    private final float rotateAngle;
    private final float blurRadius;
    private final boolean blur;
    private final int easingType;

    public AnimatedCharSpan(EditText editText, char character, long duration, long blurDuration, float slideDist, float scaleStart, float rotateAngle, float blurRadius, boolean blur, int easingType) {
        this.editText = editText;
        this.character = character;
        this.startTime = SystemClock.uptimeMillis();
        this.duration = Math.max(30, duration);
        this.blurDuration = Math.max(30, blurDuration);
        this.slideDist = slideDist;
        this.scaleStart = scaleStart;
        this.rotateAngle = rotateAngle;
        this.blurRadius = blurRadius;
        this.blur = blur;
        this.easingType = easingType;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            Paint.FontMetricsInt target = paint.getFontMetricsInt();
            fm.ascent = target.ascent;
            fm.descent = target.descent;
            fm.top = target.top;
            fm.bottom = target.bottom;
            fm.leading = target.leading;
        }
        return (int) Math.ceil(paint.measureText(text, start, end));
    }

    private float easeOutQuint(float x) {
        float f = 1.0f - x;
        return 1.0f - f * f * f * f * f;
    }

    private float interpolate(float progress) {
        switch (easingType) {
            case 1: // Ease In (Cubic)
                return (float) Math.pow(progress, 3);
            case 2: // Ease In-Out
                return progress < 0.5f
                        ? (float) (4 * Math.pow(progress, 3))
                        : (float) (1 - Math.pow(-2 * progress + 2, 3) / 2.0);
            case 3: // Spring / Elastic Bounce
                return (float) (Math.pow(2, -10 * progress) * Math.sin((progress - 0.075f) * (2 * Math.PI) / 0.3f) + 1);
            case 4: // Linear
                return progress;
            case 0: // Quintic Ease Out
            default:
                return easeOutQuint(progress);
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        long now = SystemClock.uptimeMillis();
        long elapsed = now - startTime;
        float rawProgress = Math.min(1.0f, elapsed / (float) duration);

        if (rawProgress >= 1.0f) {
            paint.setColor(editText.getCurrentTextColor());
            paint.setAlpha(255);
            canvas.drawText(text, start, end, x, y, paint);
            return;
        }

        float eased = interpolate(rawProgress);
        float blurProgress = easeOutQuint(Math.min(1.0f, elapsed / (float) blurDuration));

        float offsetY = slideDist != 0 ? -dp(slideDist) * (1.0f - eased) : 0;
        float scale = scaleStart != 1.0f ? (scaleStart + (1.0f - scaleStart) * eased) : 1.0f;
        float alpha = Math.max(0.05f, Math.min(1.0f, eased));
        float rot = rotateAngle != 0 ? (rotateAngle * (1.0f - eased)) : 0;
        float charWidth = paint.measureText(text, start, end);
        float textSize = paint.getTextSize();
        float pivotYOffset = textSize * 0.33f;

        canvas.save();
        canvas.translate(x + charWidth / 2f, y - pivotYOffset + offsetY);
        if (rot != 0) {
            canvas.rotate(rot);
        }
        if (scale != 1.0f && scale > 0.01f) {
            canvas.scale(scale, scale);
        }

        int origAlpha = paint.getAlpha();
        int textColor = editText.getCurrentTextColor();
        paint.setColor(textColor);

        float currentBlur = blurRadius * (1.0f - blurProgress);
        if (blur && currentBlur > 0.4f) {
            float blurOffset = dp(currentBlur * 0.45f);
            int baseAlpha = (int) (255 * alpha);
            int ghostAlpha = Math.max(2, (int) (baseAlpha * 0.18f));

            // Multi-pass optical Gaussian blur spread (100% visible on all Android GPUs):
            paint.setAlpha(ghostAlpha);
            canvas.drawText(text, start, end, -charWidth / 2f - blurOffset, pivotYOffset - blurOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f + blurOffset, pivotYOffset - blurOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f - blurOffset, pivotYOffset + blurOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f + blurOffset, pivotYOffset + blurOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f - blurOffset * 1.5f, pivotYOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f + blurOffset * 1.5f, pivotYOffset, paint);
            canvas.drawText(text, start, end, -charWidth / 2f, pivotYOffset - blurOffset * 1.5f, paint);
            canvas.drawText(text, start, end, -charWidth / 2f, pivotYOffset + blurOffset * 1.5f, paint);

            paint.setAlpha((int) (baseAlpha * 0.5f));
            canvas.drawText(text, start, end, -charWidth / 2f, pivotYOffset, paint);
        } else {
            paint.setAlpha((int) (255 * alpha));
            canvas.drawText(text, start, end, -charWidth / 2f, pivotYOffset, paint);
        }

        paint.setAlpha(origAlpha);
        canvas.restore();

        editText.postInvalidateOnAnimation();
    }
}
