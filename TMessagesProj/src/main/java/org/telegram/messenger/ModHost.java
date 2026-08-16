package org.telegram.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import java.lang.reflect.Method;

/**
 * Small host API used by native (.so) mods loaded by ModManager.
 * A mod's JNI_OnLoad can call these static methods to apply effects that
 * require cooperation from the host app (window/rendering hooks, etc.).
 */
public final class ModHost {

    private static volatile boolean blurApplied = false;
    private static int blurRadius = 7;

    private ModHost() {
    }

    public static void applyMotionBlur(int radius) {
        if (radius > 0) {
            blurRadius = radius;
        }
        if (blurApplied) {
            return;
        }
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return;
        }
        if (!(context instanceof Application)) {
            return;
        }
        if (Build.VERSION.SDK_INT < 31) {
            return;
        }
        ((Application) context).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                applyTo(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                applyTo(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                applyTo(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
        blurApplied = true;
    }

    private static void applyTo(Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < 31) {
            return;
        }
        try {
            Window w = activity.getWindow();
            Method enable = Window.class.getMethod("setBlurBehindEnabled", boolean.class);
            enable.invoke(w, true);
            try {
                Method radius = Window.class.getMethod("setBlurBehindRadius", int.class);
                radius.invoke(w, blurRadius);
            } catch (Throwable ignored) {
                // setBlurBehindRadius added in API 31; ignore if unavailable
            }
        } catch (Throwable t) {
            // ignore: some vendors disable backdrop blur
        }
    }
}
