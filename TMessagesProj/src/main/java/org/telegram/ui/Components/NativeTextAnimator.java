package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;

import org.telegram.messenger.TextAnimationManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class NativeTextAnimator implements TextWatcher {

    private final EditText editText;
    private final Random random = new Random();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particleGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cursorRect = new RectF();
    private final Path particlePath = new Path();
    private final ArrayList<Integer> pendingAnims = new ArrayList<>();

    private float smoothCursorX = -1;
    private float smoothCursorY = -1;
    private long lastCursorTime = 0;

    public static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float alpha = 1.0f;
        int color;
        int style;
        float rotation;
        float rotSpeed;
        float wobblePhase;
        long spawnTime;
        long lifeTime;
        String letter;
    }

    public NativeTextAnimator(EditText editText) {
        this.editText = editText;
        this.particlePaint.setStyle(Paint.Style.FILL);
        this.particleGlowPaint.setStyle(Paint.Style.FILL);
        this.particleStrokePaint.setStyle(Paint.Style.STROKE);
        this.particleStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        this.particleTextPaint.setTextAlign(Paint.Align.CENTER);
        this.cursorPaint.setStyle(Paint.Style.FILL);
    }

    public void attach() {
        editText.removeTextChangedListener(this);
        editText.addTextChangedListener(this);
    }

    public boolean isSmoothCursorEnabled() {
        return isEnabled() && TextAnimationManager.getPrefs().getBoolean("cursor_enabled", true);
    }

    private int[] getPaletteColors(int paletteIndex, int textColor) {
        switch (paletteIndex) {
            case 1: // Rainbow mix
                return new int[]{0xFF3390EC, 0xFFFF8453, 0xFFFFD700, 0xFFE040FB, 0xFF00E676, 0xFF00B0FF};
            case 2: // Neon Cyberpunk
                return new int[]{0xFF00E5FF, 0xFFFF007F, 0xFF76FF03, 0xFFD500F9, 0xFF00F5D4};
            case 3: // Golden sparks
                return new int[]{0xFFFFD700, 0xFFFFAB00, 0xFFFF6D00, 0xFFFFE57F, 0xFFFFC400};
            case 4: // Pastel vibes (Sakura)
                return new int[]{0xFFFFB7B2, 0xFFFFDAC1, 0xFFE2F0CB, 0xFFB5EAD7, 0xFFC7CEEA, 0xFFFF9AA2};
            case 5: // Monochrome
                return new int[]{0xFFFFFFFF, 0xFFE0E0E0, 0xFFBDBDBD, 0xFF9E9E9E};
            case 0: // Text / Theme color
            default:
                return new int[]{textColor};
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!isEnabled()) return;
        if (count > 0 && after < count) {
            SharedPreferences p = TextAnimationManager.getPrefs();
            if (p.getBoolean("delete_anim_enabled", true)) {
                try {
                    Layout layout = editText.getLayout();
                    if (layout != null && start < s.length()) {
                        int line = layout.getLineForOffset(start);
                        float lineBaseline = layout.getLineBaseline(line);
                        int voffset = 0;
                        if ((editText.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
                            voffset = Math.max(0, (editText.getMeasuredHeight() - layout.getHeight()) / 2);
                        }
                        float left = editText.getPaddingLeft() + layout.getPrimaryHorizontal(start) - editText.getScrollX();
                        float top = editText.getExtendedPaddingTop() + voffset + lineBaseline;

                        int countToSpawn = Math.min(24, Math.max(3, Integer.parseInt(p.getString("particle_count", "8"))));
                        int style = p.getInt("particle_style", 0);
                        int colorPalette = p.getInt("particle_palette", 0);
                        float baseSize = Float.parseFloat(p.getString("particle_size", "3"));
                        baseSize = Math.max(1f, Math.min(15f, baseSize));

                        int textColor = editText.getCurrentTextColor();
                        int[] palette = getPaletteColors(colorPalette, textColor);

                        for (int i = 0; i < countToSpawn; i++) {
                            Particle pt = new Particle();
                            pt.x = left + (random.nextFloat() - 0.5f) * dp(10);
                            pt.y = top + (random.nextFloat() - 0.5f) * dp(10);

                            float angle = (float) (random.nextFloat() * Math.PI * 2);
                            float speed = (random.nextFloat() * 2.2f + 0.8f) * dp(1.8f);
                            pt.vx = (float) Math.cos(angle) * speed;
                            pt.vy = (float) Math.sin(angle) * speed - dp(1.5f);
                            pt.size = dp(baseSize * (random.nextFloat() * 0.4f + 0.8f));
                            pt.color = palette[random.nextInt(palette.length)];
                            pt.style = style;
                            pt.spawnTime = SystemClock.uptimeMillis();
                            pt.lifeTime = 380 + random.nextInt(220);
                            pt.rotation = random.nextFloat() * 360;
                            pt.rotSpeed = (random.nextFloat() - 0.5f) * 16f;
                            pt.wobblePhase = random.nextFloat() * (float) (Math.PI * 2);
                            if (style == 4 && start < s.length()) {
                                pt.letter = String.valueOf(s.charAt(Math.min(s.length() - 1, start + (i % count))));
                            }
                            particles.add(pt);
                        }
                        editText.postInvalidateOnAnimation();
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!isEnabled()) return;
        if (count > 0 && count > before) {
            boolean ignoreSpaces = TextAnimationManager.getPrefs().getBoolean("ignore_spaces", true);
            for (int i = 0; i < count; i++) {
                int idx = start + i;
                if (idx < s.length()) {
                    char c = s.charAt(idx);
                    if (ignoreSpaces && Character.isWhitespace(c)) continue;
                    pendingAnims.add(idx);
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!isEnabled() || s == null || pendingAnims.isEmpty()) return;
        try {
            SharedPreferences p = TextAnimationManager.getPrefs();
            long dur = Long.parseLong(p.getString("duration", "300"));
            long blurDur = Long.parseLong(p.getString("blur_duration", "300"));
            float slide = p.getBoolean("slide_enabled", true) ? Float.parseFloat(p.getString("slide_dist", "20")) : 0;
            float scaleSt = p.getBoolean("scale_enabled", false) ? Float.parseFloat(p.getString("scale_start", "0.2")) : 1.0f;
            float rot = p.getBoolean("rotate_enabled", false) ? Float.parseFloat(p.getString("rotate_angle", "-15")) : 0;
            float blurRadius = Float.parseFloat(p.getString("blur_radius", "10"));
            boolean blur = p.getBoolean("blur_enabled", true);
            int easing = p.getInt("easing_type", 0);

            for (int idx : pendingAnims) {
                if (idx >= 0 && idx < s.length()) {
                    char c = s.charAt(idx);
                    AnimatedCharSpan span = new AnimatedCharSpan(editText, c, dur, blurDur, slide, scaleSt, rot, blurRadius, blur, easing);
                    s.setSpan(span, idx, idx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            pendingAnims.clear();
            editText.postInvalidateOnAnimation();
        } catch (Throwable ignored) {
        }
    }

    public boolean isEnabled() {
        return TextAnimationManager.getPrefs().getBoolean("enabled", true);
    }

    private void drawSakura(Canvas canvas, Particle pt, float alpha) {
        float r = pt.size;
        particlePath.rewind();
        particlePath.moveTo(0, -r);
        particlePath.cubicTo(r * 0.9f, -r * 0.4f, r * 0.8f, r * 0.6f, 0, r);
        particlePath.cubicTo(-r * 0.8f, r * 0.6f, -r * 0.9f, -r * 0.4f, 0, -r);
        particlePath.close();

        particlePaint.setColor(pt.color);
        particlePaint.setAlpha((int) (255 * alpha));
        canvas.drawPath(particlePath, particlePaint);
    }

    private void drawSnowflake(Canvas canvas, Particle pt, float alpha) {
        float r = pt.size;
        particleStrokePaint.setColor(pt.color);
        particleStrokePaint.setStrokeWidth(dp(1.2f));
        particleStrokePaint.setAlpha((int) (255 * alpha));

        for (int i = 0; i < 3; i++) {
            canvas.drawLine(0, -r, 0, r, particleStrokePaint);
            canvas.rotate(60);
        }
    }

    private void drawSpark(Canvas canvas, Particle pt, float alpha) {
        float r = pt.size;
        particleGlowPaint.setColor(pt.color);
        particleGlowPaint.setAlpha((int) (90 * alpha));
        canvas.drawCircle(0, 0, r * 1.8f, particleGlowPaint);

        particlePaint.setColor(0xFFFFFFFF);
        particlePaint.setAlpha((int) (255 * alpha));
        canvas.drawRoundRect(-r * 1.6f, -r * 0.35f, r * 1.6f, r * 0.35f, r * 0.35f, r * 0.35f, particlePaint);
    }

    public void onDraw(Canvas canvas) {
        if (!isEnabled()) return;
        long now = SystemClock.uptimeMillis();
        boolean needInvalidate = false;

        // Draw smooth cursor
        if (isSmoothCursorEnabled()) {
            drawCursor(canvas);
        }

        // Draw and update particles
        if (!particles.isEmpty()) {
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle pt = it.next();
                long elapsed = now - pt.spawnTime;
                if (elapsed >= pt.lifeTime) {
                    it.remove();
                    continue;
                }
                float progress = elapsed / (float) pt.lifeTime;
                float alpha = 1.0f - (float) Math.pow(progress, 1.5);
                pt.x += pt.vx;
                pt.y += pt.vy;
                pt.vy += dp(0.12f);
                pt.rotation += pt.rotSpeed;
                pt.wobblePhase += 0.15f;
                float wobbleX = (float) Math.sin(pt.wobblePhase) * dp(0.8f);

                canvas.save();
                canvas.translate(pt.x + wobbleX, pt.y);
                canvas.rotate(pt.rotation);

                switch (pt.style) {
                    case 1: // Sparks
                        drawSpark(canvas, pt, alpha);
                        break;
                    case 2: // Snowflakes
                        drawSnowflake(canvas, pt, alpha);
                        break;
                    case 3: // Sakura / Petals
                        drawSakura(canvas, pt, alpha);
                        break;
                    case 4: // Letters
                        if (pt.letter != null) {
                            particleTextPaint.setColor(pt.color);
                            particleTextPaint.setTextSize(pt.size * 2.6f);
                            particleTextPaint.setAlpha((int) (255 * alpha));
                            canvas.drawText(pt.letter, 0, pt.size * 0.9f, particleTextPaint);
                        }
                        break;
                    case 0: // Dust
                    default:
                        particlePaint.setColor(pt.color);
                        particlePaint.setAlpha((int) (255 * alpha));
                        canvas.drawCircle(0, 0, pt.size * 0.75f, particlePaint);
                        break;
                }
                canvas.restore();
                needInvalidate = true;
            }
        }

        if (needInvalidate) {
            editText.postInvalidateOnAnimation();
        }
    }

    private void drawCursor(Canvas canvas) {
        if (!editText.isFocused() || editText.getLayout() == null) {
            return;
        }
        int sel = editText.getSelectionStart();
        if (sel < 0) return;
        Layout layout = editText.getLayout();
        int line = layout.getLineForOffset(sel);

        int voffset = 0;
        if ((editText.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
            voffset = Math.max(0, (editText.getMeasuredHeight() - layout.getHeight()) / 2);
        }

        float targetX = editText.getPaddingLeft() + layout.getPrimaryHorizontal(sel) - editText.getScrollX();
        float lineTop = layout.getLineTop(line);
        float lineBottom = layout.getLineBottom(line);
        float targetY = editText.getExtendedPaddingTop() + voffset + lineTop;
        float height = lineBottom - lineTop;

        long now = SystemClock.uptimeMillis();
        if (smoothCursorX < 0 || smoothCursorY < 0 || Math.abs(smoothCursorX - targetX) > (editText.getWidth() > 0 ? editText.getWidth() : dp(300)) || Math.abs(smoothCursorY - targetY) > (editText.getHeight() > 0 ? editText.getHeight() : dp(100))) {
            smoothCursorX = targetX;
            smoothCursorY = targetY;
            lastCursorTime = now;
        } else {
            float dt = Math.min(60f, (now - lastCursorTime)) / 1000f;
            lastCursorTime = now;
            float speedSetting = Float.parseFloat(TextAnimationManager.getPrefs().getString("cursor_speed", "25"));
            float speed = Math.max(12f, speedSetting * 0.9f);

            smoothCursorX += (targetX - smoothCursorX) * Math.min(1.0f, dt * speed);
            smoothCursorY += (targetY - smoothCursorY) * Math.min(1.0f, dt * speed);

            if (Math.abs(smoothCursorX - targetX) > 0.5f || Math.abs(smoothCursorY - targetY) > 0.5f) {
                editText.postInvalidateOnAnimation();
            }
        }

        if ((now % 1000) > 500 && Math.abs(smoothCursorX - targetX) < 1.0f) {
            return;
        }

        boolean liquid = TextAnimationManager.getPrefs().getBoolean("liquid_cursor_enabled", false);
        float stretch = 0;
        if (liquid) {
            float vx = Math.abs(targetX - smoothCursorX);
            float factor = Float.parseFloat(TextAnimationManager.getPrefs().getString("liquid_scale_factor", "15")) / 10f;
            stretch = vx * factor * 0.15f;
        }

        float cursorW = dp(2.0f);
        cursorPaint.setColor(editText.getCurrentTextColor());
        cursorRect.set(smoothCursorX - stretch, smoothCursorY + dp(2), smoothCursorX + cursorW + stretch, smoothCursorY + height - dp(2));
        canvas.drawRoundRect(cursorRect, cursorW / 2f, cursorW / 2f, cursorPaint);
    }
}
