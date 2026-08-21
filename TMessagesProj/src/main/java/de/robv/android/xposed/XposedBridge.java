package de.robv.android.xposed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.HookManager;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public final class XposedBridge {

    public static final int XPOSED_BRIDGE_VERSION = 93;

    private XposedBridge() {
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        if (!(hookMethod instanceof Method) && !(hookMethod instanceof Constructor<?>)) {
            throw new IllegalArgumentException("Only methods and constructors can be hooked: " + hookMethod);
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        HookManager.init();

        try {
            Class<?> pineClass = Class.forName("top.canyie.pine.Pine");
            Class<?> methodHookClass = Class.forName("top.canyie.pine.callback.MethodHook");

            Object pineCallback = java.lang.reflect.Proxy.newProxyInstance(
                methodHookClass.getClassLoader(),
                new Class<?>[]{methodHookClass},
                (proxy, method, args) -> {
                    String mName = method.getName();
                    if ("beforeCall".equals(mName)) {
                        Object callFrame = args[0];
                        Member m = (Member) XposedHelpers.getObjectField(callFrame, "method");
                        Object thisObj = XposedHelpers.getObjectField(callFrame, "thisObject");
                        Object[] mArgs = (Object[]) XposedHelpers.getObjectField(callFrame, "args");

                        XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
                        param.method = m;
                        param.thisObject = thisObj;
                        param.args = mArgs;

                        try {
                            callback.beforeHookedMethod(param);
                        } catch (Throwable t) {
                            FileLog.e("XposedBridge: exception in beforeHookedMethod for " + hookMethod, t);
                        }

                        if (param.returnEarly) {
                            if (param.hasThrowable()) {
                                XposedHelpers.callMethod(callFrame, "setThrowable", param.getThrowable());
                            } else {
                                XposedHelpers.callMethod(callFrame, "setResult", param.getResult());
                            }
                        }
                        return null;
                    } else if ("afterCall".equals(mName)) {
                        Object callFrame = args[0];
                        Member m = (Member) XposedHelpers.getObjectField(callFrame, "method");
                        Object thisObj = XposedHelpers.getObjectField(callFrame, "thisObject");
                        Object[] mArgs = (Object[]) XposedHelpers.getObjectField(callFrame, "args");

                        XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
                        param.method = m;
                        param.thisObject = thisObj;
                        param.args = mArgs;

                        try {
                            Object res = XposedHelpers.callMethod(callFrame, "getResult");
                            param.setResult(res);
                        } catch (Throwable ignored) {
                        }
                        try {
                            Object t = XposedHelpers.callMethod(callFrame, "getThrowable");
                            param.setThrowable((Throwable) t);
                        } catch (Throwable ignored) {
                        }

                        try {
                            callback.afterHookedMethod(param);
                        } catch (Throwable t) {
                            FileLog.e("XposedBridge: exception in afterHookedMethod for " + hookMethod, t);
                        }

                        if (param.hasThrowable()) {
                            XposedHelpers.callMethod(callFrame, "setThrowable", param.getThrowable());
                        } else {
                            XposedHelpers.callMethod(callFrame, "setResult", param.getResult());
                        }
                        return null;
                    }
                    return null;
                }
            );

            Method pineHookM = pineClass.getMethod("hook", Member.class, methodHookClass);
            pineHookM.invoke(null, hookMethod, pineCallback);

        } catch (Throwable t) {
            FileLog.e("XposedBridge: Pine reflection hook failed for " + hookMethod, t);
        }

        return callback.new Unhook(hookMethod, null);
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Method method : hookClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(hookMethod(method, callback));
            }
        }
        return unhooks;
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
            unhooks.add(hookMethod(constructor, callback));
        }
        return unhooks;
    }

    public static void log(String text) {
        FileLog.d("[XposedBridge] " + text);
    }

    public static void log(Throwable t) {
        FileLog.e("[XposedBridge] Exception", t);
    }
}
