package de.robv.android.xposed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class XposedHelpers {

    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static final Map<String, Method> methodCache = new HashMap<>();

    private XposedHelpers() {
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundError(e);
        }
    }

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (ClassNotFoundError e) {
            return null;
        }
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        Class<?>[] paramClasses = getParameterClasses(clazz.getClassLoader(), parameterTypes);
        String fullMethodName = clazz.getName() + '#' + methodName + getParametersString(paramClasses);

        if (methodCache.containsKey(fullMethodName)) {
            Method method = methodCache.get(fullMethodName);
            if (method == null) {
                throw new NoSuchMethodError(fullMethodName);
            }
            return method;
        }

        try {
            Method method = clazz.getDeclaredMethod(methodName, paramClasses);
            method.setAccessible(true);
            methodCache.put(fullMethodName, method);
            return method;
        } catch (NoSuchMethodException e) {
            methodCache.put(fullMethodName, null);
            throw new NoSuchMethodError(fullMethodName);
        }
    }

    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Object... parameterTypes) {
        try {
            return findMethodExact(clazz, methodName, parameterTypes);
        } catch (NoSuchMethodError e) {
            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length - 1] instanceof XC_MethodHook)) {
            throw new IllegalArgumentException("no callback specified");
        }

        XC_MethodHook callback = (XC_MethodHook) parameterTypesAndCallback[parameterTypesAndCallback.length - 1];
        Object[] parameterTypes = Arrays.copyOf(parameterTypesAndCallback, parameterTypesAndCallback.length - 1);
        Method method = findMethodExact(clazz, methodName, (Object[]) parameterTypes);
        return XposedBridge.hookMethod(method, callback);
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        return findAndHookMethod(findClass(className, classLoader), methodName, parameterTypesAndCallback);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length - 1] instanceof XC_MethodHook)) {
            throw new IllegalArgumentException("no callback specified");
        }

        XC_MethodHook callback = (XC_MethodHook) parameterTypesAndCallback[parameterTypesAndCallback.length - 1];
        Object[] parameterTypes = Arrays.copyOf(parameterTypesAndCallback, parameterTypesAndCallback.length - 1);

        Class<?>[] paramClasses = getParameterClasses(clazz.getClassLoader(), (Object[]) parameterTypes);
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(paramClasses);
            constructor.setAccessible(true);
            return XposedBridge.hookMethod(constructor, callback);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(clazz.getName() + getParametersString(paramClasses));
        }
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        String fullFieldName = clazz.getName() + '#' + fieldName;

        if (fieldCache.containsKey(fullFieldName)) {
            Field field = fieldCache.get(fullFieldName);
            if (field == null) {
                throw new NoSuchFieldError(fullFieldName);
            }
            return field;
        }

        Class<?> current = clazz;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                fieldCache.put(fullFieldName, field);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        fieldCache.put(fullFieldName, null);
        throw new NoSuchFieldError(fullFieldName);
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static void setObjectField(Object obj, String fieldName, Object value) {
        try {
            findField(obj.getClass(), fieldName).set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static Object callMethod(Object obj, String methodName, Object... args) {
        try {
            Method method = findMethodExact(obj.getClass(), methodName, (Object[]) getClassesFromObjects(args));
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Method method = findMethodExact(clazz, methodName, (Object[]) getClassesFromObjects(args));
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?>[] getParameterClasses(ClassLoader classLoader, Object[] parameterTypes) {
        Class<?>[] paramClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] instanceof Class<?>) {
                paramClasses[i] = (Class<?>) parameterTypes[i];
            } else if (parameterTypes[i] instanceof String) {
                paramClasses[i] = findClass((String) parameterTypes[i], classLoader);
            } else {
                throw new IllegalArgumentException("parameter type must be Class or String");
            }
        }
        return paramClasses;
    }

    private static Class<?>[] getClassesFromObjects(Object[] args) {
        if (args == null) return new Class<?>[0];
        Class<?>[] classes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return classes;
    }

    private static String getParametersString(Class<?>... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Class<?> clazz : clazzes) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(clazz != null ? clazz.getCanonicalName() : "null");
        }
        sb.append(")");
        return sb.toString();
    }

    public static class ClassNotFoundError extends Error {
        private static final long serialVersionUID = 1L;

        public ClassNotFoundError(Throwable cause) {
            super(cause);
        }
    }
}
