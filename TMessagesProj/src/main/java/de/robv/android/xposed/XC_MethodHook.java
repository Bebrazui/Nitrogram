package de.robv.android.xposed;

import java.lang.reflect.Member;

public abstract class XC_MethodHook {

    public int priority = 50;

    public XC_MethodHook() {
        this(50);
    }

    public XC_MethodHook(int priority) {
        this.priority = priority;
    }

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    public static class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;

        private Object result;
        private Throwable throwable;
        public boolean returnEarly = false;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }

        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) {
                throw throwable;
            }
            return result;
        }
    }

    public class Unhook {
        private final Member hookMethod;
        private final top.canyie.pine.callback.MethodHook.Unhook pineUnhook;

        public Unhook(Member hookMethod, top.canyie.pine.callback.MethodHook.Unhook pineUnhook) {
            this.hookMethod = hookMethod;
            this.pineUnhook = pineUnhook;
        }

        public Member getHookedMethod() {
            return hookMethod;
        }

        public XC_MethodHook getCallback() {
            return XC_MethodHook.this;
        }

        public void unhook() {
            if (pineUnhook != null) {
                pineUnhook.unhook();
            }
        }
    }
}
