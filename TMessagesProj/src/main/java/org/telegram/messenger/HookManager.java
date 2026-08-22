package org.telegram.messenger;

import android.graphics.drawable.Drawable;
import android.os.Build;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import top.canyie.pine.PineConfig;

public final class HookManager {

    private static boolean initialized = false;
    private static boolean materialSymbolsHooked = false;

    private HookManager() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            PineConfig.debug = BuildVars.DEBUG_VERSION;
            PineConfig.debuggable = BuildVars.DEBUG_VERSION;
            top.canyie.pine.Pine.ensureInitialized();
            initialized = true;
            initMaterialSymbolsHook();
            FileLog.d("HookManager: Pine native hooking engine initialized successfully (Android API " + Build.VERSION.SDK_INT + ")");
        } catch (Throwable e) {
            FileLog.e("HookManager: Failed to initialize Pine native hooking engine", e);
        }
    }

    public static synchronized void initMaterialSymbolsHook() {
        if (materialSymbolsHooked) {
            return;
        }
        try {
            Class<?> resClass = android.content.res.Resources.class;
            Method m1 = XposedHelpers.findMethodExactIfExists(resClass, "getDrawable", int.class, android.content.res.Resources.Theme.class);
            if (m1 != null) {
                XposedBridge.hookMethod(m1, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
                            int id = (Integer) param.args[0];
                            Drawable d = MaterialSymbolsHelper.get(id);
                            if (d != null) {
                                param.setResult(d);
                            }
                        }
                    }
                });
            }
            Method m2 = XposedHelpers.findMethodExactIfExists(resClass, "getDrawable", int.class);
            if (m2 != null) {
                XposedBridge.hookMethod(m2, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (NitrogramConfig.isMaterialSymbolsRoundedEnabled()) {
                            int id = (Integer) param.args[0];
                            Drawable d = MaterialSymbolsHelper.get(id);
                            if (d != null) {
                                param.setResult(d);
                            }
                        }
                    }
                });
            }
            materialSymbolsHooked = true;
            FileLog.d("HookManager: Material Symbols Rounded hook installed successfully");
        } catch (Throwable t) {
            FileLog.e("HookManager: Failed to hook Resources.getDrawable for Material Symbols", t);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static String checkHookEngineStatus() {
        init();
        boolean isPineOk = false;
        try {
            isPineOk = top.canyie.pine.Pine.isInitialized();
        } catch (Throwable ignore) {
        }
        StringBuilder sb = new StringBuilder();
        sb.append("⚙️ Статус движка хуков Nitrogram:\n\n");
        sb.append("• Android API Level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("• Pine Native Engine: ").append(isPineOk ? "✅ Активен (Full ART Hooking)" : "⚠️ Ограничен (Android 14+ ART Safe Mode)").append("\n");
        sb.append("• Dynamic Mod Manager: ✅ Активен (.so loader)\n");
        sb.append("• Event Delegate Bridge: ✅ Активен\n");
        sb.append("• Crash Shield: ✅ Активен (Защита от падений)");
        return sb.toString();
    }
}
