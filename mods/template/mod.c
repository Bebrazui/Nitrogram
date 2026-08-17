#include <jni.h>
#include <string.h>
#include "dex_payload.h"

/*
 * Шаблон мода Nitrogram.
 *
 * Этот .so — самодостаточный мод: он несёт вшитый DEX (DEX_BYTES из dex_payload.h)
 * и при загрузке сам регистрирует свой класс входа в клиенте (ModManager).
 *
 * ЧТО МЕНЯТЬ:
 *   1. MOD_META       — заполни метаданные (name/version/icon/settings ...).
 *   2. ModEntry.java  — свою логику эффекта и экран настроек.
 * Остальное трогать не нужно.
 *
 * ВАЖНО: ModManager резолвится через загрузчик приложения (loader->loadClass),
 * НЕ через env->FindClass — иначе регистрация молча не сработает.
 */

static const char MOD_META[] =
"NITROGRAM_MOD_META_START"
"{"
"\"name\":\"My Mod\","
"\"description\":\"Опиши свой мод.\","
"\"version\":\"1.0\","
"\"extra\":\"Self-contained mod.\","
"\"settings\":true,\"icon\":\"\""
"}"
"NITROGRAM_MOD_META_END";

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    /* Reference the embedded metadata so the linker keeps it in the binary
     * for the host app's ModManager to parse. */
    (void) MOD_META[0];
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    /* Obtain the application class loader so the in-memory DEX can resolve
     * framework and app classes (including org.nitrogram.modsdk.*). */
    jclass atClass = (*env)->FindClass(env, "android/app/ActivityThread");
    if (atClass == NULL) {
        return JNI_VERSION_1_6;
    }
    jmethodID currentApp = (*env)->GetStaticMethodID(env, atClass, "currentApplication",
            "()Landroid/app/Application;");
    if (currentApp == NULL) {
        return JNI_VERSION_1_6;
    }
    jobject app = (*env)->CallStaticObjectMethod(env, atClass, currentApp);
    if (app == NULL) {
        return JNI_VERSION_1_6;
    }
    jclass appClass = (*env)->GetObjectClass(env, app);
    jmethodID getCL = (*env)->GetMethodID(env, appClass, "getClassLoader",
            "()Ljava/lang/ClassLoader;");
    if (getCL == NULL) {
        return JNI_VERSION_1_6;
    }
    jobject loader = (*env)->CallObjectMethod(env, app, getCL);
    if (loader == NULL) {
        return JNI_VERSION_1_6;
    }

    /* Wrap the embedded DEX bytes into a ByteBuffer. */
    jclass bbClass = (*env)->FindClass(env, "java/nio/ByteBuffer");
    if (bbClass == NULL) {
        return JNI_VERSION_1_6;
    }
    jmethodID wrap = (*env)->GetStaticMethodID(env, bbClass, "wrap", "([B)Ljava/nio/ByteBuffer;");
    if (wrap == NULL) {
        return JNI_VERSION_1_6;
    }
    jbyteArray arr = (*env)->NewByteArray(env, DEX_LEN);
    if (arr == NULL) {
        return JNI_VERSION_1_6;
    }
    (*env)->SetByteArrayRegion(env, arr, 0, DEX_LEN, (const jbyte*) DEX_BYTES);
    jobject buf = (*env)->CallStaticObjectMethod(env, bbClass, wrap, arr);
    if (buf == NULL) {
        return JNI_VERSION_1_6;
    }

    /* Create an InMemoryDexClassLoader that can see the embedded DEX. */
    jclass imdcClass = (*env)->FindClass(env, "dalvik/system/InMemoryDexClassLoader");
    if (imdcClass == NULL) {
        return JNI_VERSION_1_6;
    }
    jmethodID imdcCtor = (*env)->GetMethodID(env, imdcClass, "<init>",
            "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    if (imdcCtor == NULL) {
        return JNI_VERSION_1_6;
    }
    jobject imdc = (*env)->NewObject(env, imdcClass, imdcCtor, buf, loader);
    if (imdc == NULL) {
        return JNI_VERSION_1_6;
    }

    /* Load our entry class from the in-memory DEX and invoke apply(). */
    jmethodID loadClass = (*env)->GetMethodID(env, imdcClass, "loadClass",
            "(Ljava/lang/String;)Ljava/lang/Class;");
    if (loadClass == NULL) {
        return JNI_VERSION_1_6;
    }
    jstring entryName = (*env)->NewStringUTF(env, "org.nitrogram.mod.ModEntry");
    jclass entryClass = (jclass) (*env)->CallObjectMethod(env, imdc, loadClass, entryName);
    if (entryClass == NULL) {
        return JNI_VERSION_1_6;
    }

    /* Register the mod's entry class with the client's ModManager. NOTE: from
     * JNI_OnLoad, env->FindClass can only see bootclasspath classes, so app
     * classes (ModManager) must be resolved through the application class loader. */
    jclass clClass = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID loadClassM = (*env)->GetMethodID(env, clClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    jstring mmName = (*env)->NewStringUTF(env, "org.telegram.messenger.ModManager");
    jclass mmClass = (jclass) (*env)->CallObjectMethod(env, loader, loadClassM, mmName);
    if (mmClass != NULL) {
        jmethodID reg = (*env)->GetStaticMethodID(env, mmClass, "onModEntryClass", "(Ljava/lang/Class;)V");
        if (reg != NULL) {
            (*env)->CallStaticVoidMethod(env, mmClass, reg, entryClass);
        }
    }
    if (mmName != NULL) {
        (*env)->DeleteLocalRef(env, mmName);
    }

    /* Apply the mod. Wrap in exception clearing so a failure here can never
     * break registration or the host app. */
    jmethodID apply = (*env)->GetStaticMethodID(env, entryClass, "apply", "()V");
    if (apply != NULL) {
        (*env)->CallStaticVoidMethod(env, entryClass, apply);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
    }

    return JNI_VERSION_1_6;
}
