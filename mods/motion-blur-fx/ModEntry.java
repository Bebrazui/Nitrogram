package org.nitrogram.mod;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.nitrogram.modsdk.ModSettingsHost;
import org.nitrogram.modsdk.ModSettingsScreen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Self-contained Nitrogram mod logic.
 *
 * Compiled into a DEX, embedded inside the mod's .so library and loaded by the
 * host app purely as an injector (System.load). The client contains no effect
 * logic of its own.
 *
 * Effect: directional "motion blur". The whole UI is blurred along the axis of
 * movement while the user is scrolling, swiping or dragging (lists, page
 * swipes, folder swipes, the side menu). The blur follows the velocity of the
 * gesture and is cleared a few milliseconds after motion stops.
 *
 * Settings are described in code via the ModSettingsScreen contract: createSettingsScreen()
 * returns an instance that builds the UI; values are persisted by the client and
 * pushed back through applySettings(String json).
 */
public final class ModEntry {

    private static volatile boolean applied = false;

    // Настраиваемые параметры (изменяются из экрана настроек).
    private static float intensity = 0.7f;
    private static float maxRadius = 30f;
    private static long idleTimeout = 75;
    private static boolean enableScroll = true;
    private static boolean enableSwipe = true;
    private static boolean enableMenu = true;

    // Below this many pixels per move event the motion is ignored (jitter).
    private static final float MIN_DELTA = 1.5f;

    private static final String RECYCLER = "androidx.recyclerview.widget.RecyclerView";
    private static final String VIEWPAGER = "androidx.viewpager.widget.ViewPager";

    private static final WeakHashMap<Activity, Boolean> WATCHING = new WeakHashMap<>();
    private static final Map<View, Boolean> HOOKED = new WeakHashMap<>();
    private static final Map<View, Runnable> FADE_TASKS = new WeakHashMap<>();
    private static final Map<View, ValueAnimator> FADE_ANIMS = new WeakHashMap<>();
    private static final Map<View, String> LAST_AXIS = new WeakHashMap<>();
    private static final Map<View, Float> SMOOTHED = new WeakHashMap<>();

    private static float lastX = -1f;
    private static float lastY = -1f;

    public static void apply() {
        if (applied || Build.VERSION.SDK_INT < 31) {
            return;
        }
        applied = true;
        try {
            Application app = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null);
            if (app == null) {
                return;
            }
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity a, android.os.Bundle b) {
                }

                @Override
                public void onActivityStarted(Activity a) {
                }

                @Override
                public void onActivityResumed(Activity a) {
                    watch(a);
                }

                @Override
                public void onActivityPaused(Activity a) {
                }

                @Override
                public void onActivityStopped(Activity a) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity a, android.os.Bundle b) {
                }

                @Override
                public void onActivityDestroyed(Activity a) {
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** Вызывается клиентом при изменении/загрузке сохранённых настроек. */
    public static void applySettings(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            JSONObject o = new JSONObject(json);
            java.util.Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                setSetting(k, o.get(k));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Установить одну настройку по ключу (используется и applySettings, и UI). */
    public static void setSetting(String key, Object value) {
        if (value == null) {
            return;
        }
        switch (key) {
            case "intensity":
                intensity = toFloat(value, 0.7f);
                break;
            case "maxRadius":
                maxRadius = toFloat(value, 30f);
                break;
            case "idleTimeout":
                idleTimeout = (long) toFloat(value, 75f);
                break;
            case "enableScroll":
                enableScroll = toBool(value, true);
                break;
            case "enableSwipe":
                enableSwipe = toBool(value, true);
                break;
            case "enableMenu":
                enableMenu = toBool(value, true);
                break;
        }
    }

    private static float toFloat(Object v, float def) {
        if (v instanceof Number) {
            return ((Number) v).floatValue();
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1f : 0f;
        }
        try {
            return Float.parseFloat(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean toBool(Object v, boolean def) {
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0;
        }
        return def;
    }

    /** Возвращает экран настроек мода либо null, если настроек нет. */
    public static Object createSettingsScreen() {
        return new MotionBlurSettingsScreen();
    }

    // ----- эффект -----

    private static void watch(final Activity a) {
        if (a == null || a.isDestroyed() || WATCHING.containsKey(a)) {
            return;
        }
        WATCHING.put(a, true);
        final View target = a.findViewById(android.R.id.content);
        if (target == null) {
            return;
        }

        try {
            final Window w = a.getWindow();
            final Window.Callback orig = w.getCallback();
            if (orig != null && !Proxy.isProxyClass(orig.getClass())) {
                final Window.Callback proxy = (Window.Callback) Proxy.newProxyInstance(
                        ModEntry.class.getClassLoader(),
                        new Class[]{Class.forName("android.view.Window$Callback")},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object p, Method m, Object[] args) throws Throwable {
                                if ("dispatchTouchEvent".equals(m.getName()) && args != null && args.length == 1) {
                                    handleTouch((MotionEvent) args[0], target);
                                }
                                return m.invoke(orig, args);
                            }
                        });
                w.setCallback(proxy);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        hookTree(target, target);
        try {
            target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                private long lastHook = 0;

                @Override
                public void onGlobalLayout() {
                    long now = System.currentTimeMillis();
                    if (now - lastHook < 600) {
                        return;
                    }
                    lastHook = now;
                    hookTree(target, target);
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean isInstance(Class<?> vc, String name) {
        try {
            return Class.forName(name).isAssignableFrom(vc);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void hookTree(View v, View target) {
        if (v == null) {
            return;
        }
        if (!HOOKED.containsKey(v)) {
            Class<?> vc = v.getClass();
            if (isInstance(vc, RECYCLER)) {
                HOOKED.put(v, true);
                try {
                    Class<?> rvc = Class.forName(RECYCLER);
                    Class<?> iface = Class.forName(RECYCLER + "$OnScrollListener");
                    final View tgt = target;
                    Object listener = Proxy.newProxyInstance(ModEntry.class.getClassLoader(),
                            new Class[]{iface},
                            new InvocationHandler() {
                                @Override
                                public Object invoke(Object p, Method m, Object[] a) {
                                    if ("onScrolled".equals(m.getName()) && a != null && a.length >= 3) {
                                        if (!enableScroll) {
                                            return null;
                                        }
                                        int dx = ((Number) a[1]).intValue();
                                        int dy = ((Number) a[2]).intValue();
                                        float adx = Math.abs(dx);
                                        float ady = Math.abs(dy);
                                        if (adx > MIN_DELTA || ady > MIN_DELTA) {
                                            if (ady >= adx) {
                                                applyBlur(tgt, ady, "vertical");
                                            } else {
                                                applyBlur(tgt, adx, "horizontal");
                                            }
                                        }
                                    }
                                    return null;
                                }
                            });
                    rvc.getMethod("addOnScrollListener", iface).invoke(v, listener);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (isInstance(vc, VIEWPAGER)) {
                HOOKED.put(v, true);
                try {
                    Class<?> vpc = Class.forName(VIEWPAGER);
                    Class<?> iface = Class.forName(VIEWPAGER + "$OnPageChangeListener");
                    final View tgt = target;
                    final float[] lastOffset = {-1f};
                    Object listener = Proxy.newProxyInstance(ModEntry.class.getClassLoader(),
                            new Class[]{iface},
                            new InvocationHandler() {
                                @Override
                                public Object invoke(Object p, Method m, Object[] a) {
                                    if ("onPageScrolled".equals(m.getName()) && a != null && a.length >= 3) {
                                        if (!enableSwipe) {
                                            return null;
                                        }
                                        float offset = ((Number) a[1]).floatValue();
                                        if (lastOffset[0] >= 0f) {
                                            float d = Math.abs(offset - lastOffset[0]);
                                            if (d > 0.001f) {
                                                applyBlur(tgt, d * (tgt.getWidth() * 0.6f), "horizontal");
                                            }
                                        }
                                        lastOffset[0] = offset;
                                    } else if ("onPageScrollStateChanged".equals(m.getName()) && a != null && a.length >= 1) {
                                        if (((Number) a[0]).intValue() == 0) {
                                            lastOffset[0] = -1f;
                                        }
                                    }
                                    return null;
                                }
                            });
                    vpc.getMethod("addOnPageChangeListener", iface).invoke(v, listener);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                hookTree(g.getChildAt(i), target);
            }
        }
    }

    private static void handleTouch(MotionEvent e, View target) {
        if (e == null) {
            return;
        }
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            float x = e.getX();
            float y = e.getY();
            if (lastX >= 0f) {
                float dx = x - lastX;
                float dy = y - lastY;
                float adx = Math.abs(dx);
                float ady = Math.abs(dy);
                if (adx > MIN_DELTA || ady > MIN_DELTA) {
                    boolean horizontal = adx >= ady;
                    if (horizontal && !(enableSwipe || enableMenu)) {
                        return;
                    }
                    if (!horizontal && !enableScroll) {
                        return;
                    }
                    if (horizontal) {
                        applyBlur(target, adx, "horizontal");
                    } else {
                        applyBlur(target, ady, "vertical");
                    }
                }
            }
            lastX = x;
            lastY = y;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            lastX = -1f;
            lastY = -1f;
        }
    }

    private static void applyBlur(View target, float delta, String axis) {
        if (target == null || delta <= 0f) {
            return;
        }
        float amount = Math.min(delta * intensity, maxRadius);

        cancelFade(target);

        Float prev = SMOOTHED.get(target);
        float r = (prev == null) ? amount : (prev * 0.5f + amount * 0.5f);
        SMOOTHED.put(target, r);
        LAST_AXIS.put(target, axis);

        setBlur(target, r, axis);
        scheduleFade(target);
    }

    private static void setBlur(View target, float r, String axis) {
        float rx = "horizontal".equals(axis) ? r : 0f;
        float ry = "vertical".equals(axis) ? r : 0f;
        try {
            target.setRenderEffect(RenderEffect.createBlurEffect(rx, ry, Shader.TileMode.CLAMP));
        } catch (Throwable t) {
            // ignore
        }
    }

    /** По окончании движения плавно (со спадом) убираем размытие за idleTimeout мс. */
    private static void scheduleFade(final View target) {
        Runnable old = FADE_TASKS.get(target);
        if (old != null) {
            target.removeCallbacks(old);
        }
        Runnable task = new Runnable() {
            @Override
            public void run() {
                startFade(target);
            }
        };
        FADE_TASKS.put(target, task);
        if (Build.VERSION.SDK_INT >= 31) {
            target.postDelayed(task, (long) idleTimeout);
        }
    }

    private static void startFade(final View target) {
        cancelFadeAnimator(target);
        Float cur = SMOOTHED.get(target);
        String axis = LAST_AXIS.get(target);
        if (axis == null) {
            axis = "horizontal";
        }
        if (cur == null || cur <= 0.5f) {
            clear(target);
            return;
        }
        final String faxis = axis;
        ValueAnimator a = ValueAnimator.ofFloat(cur, 0f);
        a.setDuration(Math.max(80, (long) idleTimeout));
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> setBlur(target, (Float) anim.getAnimatedValue(), faxis));
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                clear(target);
            }
        });
        FADE_ANIMS.put(target, a);
        a.start();
    }

    private static void cancelFade(View target) {
        Runnable task = FADE_TASKS.get(target);
        if (task != null) {
            target.removeCallbacks(task);
        }
        cancelFadeAnimator(target);
    }

    private static void cancelFadeAnimator(View target) {
        ValueAnimator a = FADE_ANIMS.get(target);
        if (a != null) {
            a.cancel();
            FADE_ANIMS.remove(target);
        }
    }

    private static void clear(View target) {
        try {
            target.setRenderEffect(null);
        } catch (Throwable t) {
            // ignore
        }
        SMOOTHED.remove(target);
        LAST_AXIS.remove(target);
        FADE_ANIMS.remove(target);
    }

    // ----- экран настроек в коде -----

    private static final int CARD_BG = 0xFF1C1C1E;
    private static final int TITLE_COLOR = 0xFFECECEC;
    private static final int VALUE_COLOR = 0xFF8A8A8A;

    private static int dp(int v) {
        return (int) (v * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }

    private static class MotionBlurSettingsScreen implements ModSettingsScreen {
        @Override
        public View createView(android.content.Context context, ModSettingsHost host, ViewGroup parent) {
            ScrollView scroll = new ScrollView(context);
            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            scroll.addView(root, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            root.addView(header(context, "Эффект"));

            root.addView(slider(context, host, "intensity", "Интенсивность", 0.1f, 2.0f, 0.1f, 0.7f,
                    v -> String.format(java.util.Locale.US, "%.1f", v)));
            root.addView(slider(context, host, "maxRadius", "Максимальный радиус", 5f, 60f, 1f, 30f,
                    v -> String.format(java.util.Locale.US, "%.0f px", v)));
            root.addView(slider(context, host, "idleTimeout", "Время затухания", 20f, 300f, 5f, 75f,
                    v -> String.format(java.util.Locale.US, "%.0f мс", v)));

            root.addView(header(context, "Где применять"));

            root.addView(toggle(context, host, "enableScroll", "Размывать скролл", true));
            root.addView(toggle(context, host, "enableSwipe", "Размывать свайпы", true));
            root.addView(toggle(context, host, "enableMenu", "Размывать меню (свайп)", true));

            return scroll;
        }

        private View header(android.content.Context context, String text) {
            TextView t = new TextView(context);
            t.setText(text);
            t.setTextSize(13);
            t.setAllCaps(true);
            t.setTextColor(VALUE_COLOR);
            t.setPadding(dp(16), dp(16), dp(16), dp(6));
            return t;
        }

        private View slider(android.content.Context context, ModSettingsHost host, final String key,
                            String title, final float min, final float max, final float step,
                            float def, final java.util.function.Function<Float, String> fmt) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(16), dp(12), dp(16), dp(12));
            row.setBackgroundColor(CARD_BG);

            LinearLayout top = new LinearLayout(context);
            top.setOrientation(LinearLayout.HORIZONTAL);
            TextView titleTv = new TextView(context);
            titleTv.setText(title);
            titleTv.setTextSize(16);
            titleTv.setTextColor(TITLE_COLOR);
            top.addView(titleTv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            final TextView valTv = new TextView(context);
            valTv.setTextSize(14);
            valTv.setTextColor(VALUE_COLOR);
            top.addView(valTv);
            row.addView(top);

            float current = host.getFloat(key, def);
            final int steps = Math.round((max - min) / step);
            SeekBar sb = new SeekBar(context);
            sb.setMax(steps);
            sb.setProgress(Math.round((current - min) / step));
            valTv.setText(fmt.apply(current));
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float v = min + progress * step;
                    valTv.setText(fmt.apply(v));
                    setAndPersist(host, key, v);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            row.addView(sb, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            return row;
        }

        private View toggle(android.content.Context context, ModSettingsHost host, final String key,
                            String title, boolean def) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16), dp(14), dp(16), dp(14));
            row.setBackgroundColor(CARD_BG);

            TextView titleTv = new TextView(context);
            titleTv.setText(title);
            titleTv.setTextSize(16);
            titleTv.setTextColor(TITLE_COLOR);
            row.addView(titleTv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            final Switch sw = new Switch(context);
            boolean on = host.getBool(key, def);
            sw.setChecked(on);
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> setAndPersist(host, key, isChecked));
            row.addView(sw);
            return row;
        }

        private void setAndPersist(ModSettingsHost host, String key, Object value) {
            ModEntry.setSetting(key, value);
            host.setValue(key, value);
        }
    }
}
