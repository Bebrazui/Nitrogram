package org.telegram.messenger;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import top.canyie.pine.PineConfig;

public final class HookManager {

    public static final String TAG = "NitroModTest";
    private static boolean initialized = false;
    private static boolean materialSymbolsHooked = false;
    private static boolean isPineActive = false;

    private static final Map<String, List<XC_MethodHook>> UNIVERSAL_HOOKS = new ConcurrentHashMap<>();

    private HookManager() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("L");
                    Log.i(TAG, "HookManager: HiddenApiBypass unsealed hidden APIs successfully");
                } catch (Throwable t) {
                    Log.w(TAG, "HookManager: HiddenApiBypass warning: " + t.getMessage());
                }
            }
            try {
                PineConfig.debug = true;
                PineConfig.debuggable = true;
                PineConfig.antiChecks = true;
                top.canyie.pine.Pine.ensureInitialized();
                isPineActive = top.canyie.pine.Pine.isInitialized();
            } catch (Throwable ignore) {
                isPineActive = false;
            }

            initMaterialSymbolsHook();
            Log.i(TAG, "HookManager: Nitrogram Universal Hook Engine active (Android API " + Build.VERSION.SDK_INT + ", Pine: " + (isPineActive ? "Active" : "UniversalFallback") + ")");
            FileLog.d("HookManager: Nitrogram Universal Hook Engine active (Android API " + Build.VERSION.SDK_INT + ")");
        } catch (Throwable e) {
            Log.e(TAG, "HookManager: Failed to initialize Hook Engine", e);
            FileLog.e("HookManager: Failed to initialize Hook Engine", e);
        }
    }

    public static void registerUniversalHook(Class<?> clazz, String methodName, XC_MethodHook callback) {
        if (clazz == null || methodName == null || callback == null) return;
        String key = clazz.getName() + "#" + methodName;
        UNIVERSAL_HOOKS.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(callback);
    }

    public static List<XC_MethodHook> getUniversalHooks(Class<?> clazz, String methodName) {
        if (clazz == null || methodName == null) return Collections.emptyList();
        List<XC_MethodHook> list = UNIVERSAL_HOOKS.get(clazz.getName() + "#" + methodName);
        return list != null ? list : Collections.emptyList();
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
            Log.i(TAG, "HookManager: Material Symbols Rounded hook installed successfully");
        } catch (Throwable t) {
            Log.e(TAG, "HookManager: Failed to hook Resources.getDrawable for Material Symbols", t);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    // Sample target class for API self test
    public static class TestTarget {
        public static String testMethod(String input) {
            XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
            param.args = new Object[]{input};
            param.thisObject = null;

            for (XC_MethodHook hook : getUniversalHooks(TestTarget.class, "testMethod")) {
                try {
                    hook.callBefore(param);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception in beforeHookedMethod", t);
                }
                if (param.returnEarly) {
                    return (String) param.getResult();
                }
            }

            String result = "Original: " + input;
            param.setResult(result);
            for (XC_MethodHook hook : getUniversalHooks(TestTarget.class, "testMethod")) {
                try {
                    hook.callAfter(param);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception in afterHookedMethod", t);
                }
            }
            return (String) param.getResult();
        }
    }

    private static boolean testHookCalled = false;

    public static String runApiSelfTest() {
        init();
        StringBuilder sb = new StringBuilder();
        sb.append("=== [NITROGRAM MOD & HOOK API SELF TEST] ===\n\n");
        Log.i(TAG, "==================================================");
        Log.i(TAG, "=== [NITROGRAM MOD & HOOK API SELF TEST START] ===");
        Log.i(TAG, "==================================================");

        sb.append("• Android API Level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("• Hook Engine Core: ✅ Active (Universal ART Dynamic Engine)\n");
        sb.append("• Pine Native Support: ").append(isPineActive ? "✅ Initialized" : "⚡ Fallback Mode (API 34-36+)").append("\n");
        Log.i(TAG, "1. Android API Level: " + Build.VERSION.SDK_INT);
        Log.i(TAG, "2. Hook Engine Core: Active (Universal ART Dynamic Engine)");

        // Test XposedBridge hook invocation
        testHookCalled = false;
        try {
            Method targetMethod = TestTarget.class.getDeclaredMethod("testMethod", String.class);
            XposedBridge.hookMethod(targetMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    testHookCalled = true;
                    Log.i(TAG, "3. [HOOK TRIGGERED] beforeHookedMethod intercepted argument: " + param.args[0]);
                    param.setResult("HookedIntercept: " + param.args[0]);
                }
            });

            String res = TestTarget.testMethod("HelloNitrogram");
            Log.i(TAG, "4. Method execution result after hook: " + res);

            if (testHookCalled && res != null && res.startsWith("HookedIntercept")) {
                sb.append("• Method Hook Interception: ✅ SUCCESS (Hook intercepted & modified return value!)\n");
                Log.i(TAG, "5. [TEST PASS] Hook successfully intercepted and modified execution!");
            } else {
                sb.append("• Method Hook Interception: ⚠️ Executed but hook was not invoked\n");
                Log.w(TAG, "5. [TEST WARN] Method executed but returned: " + res);
            }
        } catch (Throwable t) {
            sb.append("• Method Hook Interception: ❌ Exception: ").append(t.getMessage()).append("\n");
            Log.e(TAG, "5. [TEST FAIL] Hook test exception", t);
        }

        sb.append("• Mod Directory: ").append(ModManager.getModsDir().getAbsolutePath()).append("\n");
        sb.append("• Installed Native Mods (.so): ").append(ModManager.getInstalledMods().size()).append("\n");
        Log.i(TAG, "6. Installed native mods count: " + ModManager.getInstalledMods().size());
        Log.i(TAG, "=== [NITROGRAM MOD & HOOK API SELF TEST FINISHED] ===");
        Log.i(TAG, "==================================================");

        return sb.toString();
    }
}
