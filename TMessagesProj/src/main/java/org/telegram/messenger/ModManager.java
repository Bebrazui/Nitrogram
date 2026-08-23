package org.telegram.messenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Nitrogram custom .so mod system.
 *
 * A mod is a shared library (.so) that embeds a JSON metadata block between the
 * markers NITROGRAM_MOD_META_START ... NITROGRAM_MOD_META_END. The block may contain:
 *   name        - mod display name
 *   description - short description
 *   version     - mod version
 *   extra       - any extra info
 *   icon        - base64 encoded PNG used as the mod icon
 *
 * Installed mods are copied to the app-private "mods" directory and loaded on startup.
 */
public final class ModManager {

    public static final String TAG = "NitroMod";
    private static final String MARKER_START = "NITROGRAM_MOD_META_START";
    private static final String MARKER_END = "NITROGRAM_MOD_META_END";
    private static final Set<String> loadedPaths = new HashSet<>();
    // Класс точки входа (ModEntry) каждого загруженного мода, нужен для проброса настроек.
    private static Class<?> pendingEntry = null;
    private static final Map<String, Class<?>> modEntryClasses = new HashMap<>();

    public static final class ModMeta {
        public String id;
        public String name;
        public String description;
        public String version;
        public String extra;
        public Bitmap icon;
        public boolean loaded;
        public boolean enabled;
        public boolean hasSettings;

        public ModMeta() {
        }
    }

    private ModManager() {
    }

    public static File getModsDir() {
        return ApplicationLoader.applicationContext.getDir("mods", Context.MODE_PRIVATE);
    }

    public static ModMeta parseMeta(File soFile) {
        if (soFile == null || !soFile.exists()) {
            return null;
        }
        try {
            byte[] data = readAllBytes(soFile);
            String s = new String(data, "UTF-8");
            int start = s.indexOf(MARKER_START);
            if (start < 0) {
                return null;
            }
            int end = s.indexOf(MARKER_END, start);
            if (end < 0) {
                return null;
            }
            String json = s.substring(start + MARKER_START.length(), end);
            JSONObject obj = new JSONObject(json);
            ModMeta meta = new ModMeta();
            meta.name = optString(obj, "name", soFile.getName());
            meta.description = optString(obj, "description", "");
            meta.version = optString(obj, "version", "");
            meta.extra = optString(obj, "extra", "");
            meta.hasSettings = obj.optBoolean("settings", false);
            String iconB64 = optString(obj, "icon", null);
            if (iconB64 != null && !iconB64.isEmpty()) {
                try {
                    byte[] iconBytes = Base64.decode(iconB64, Base64.DEFAULT);
                    meta.icon = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
                } catch (Exception e) {
                    Log.e(TAG, "icon decode failed", e);
                }
            }
            meta.id = computeId(soFile, meta);
            return meta;
        } catch (Exception e) {
            Log.e(TAG, "parseMeta failed", e);
            return null;
        }
    }

    private static final WeakHashMap<MessageObject, ModMeta> modMetaCache = new WeakHashMap<>();

    /** Мета мод-файла (.so) для сообщения, либо null (не .so / ещё не скачан / нет мета). */
    public static ModMeta getModMeta(MessageObject mo) {
        if (mo == null || mo.messageOwner == null) {
            return null;
        }
        ModMeta cached = modMetaCache.get(mo);
        if (cached != null) {
            return cached;
        }
        TLRPC.Document doc = mo.getDocument();
        if (doc == null) {
            return null;
        }
        String name = FileLoader.getDocumentFileName(doc);
        if (name == null || !name.toLowerCase().endsWith(".so")) {
            return null;
        }
        try {
            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(mo.messageOwner);
            if (f == null || !f.exists()) {
                return null;
            }
            ModMeta meta = parseMeta(f);
            if (meta != null) {
                modMetaCache.put(mo, meta);
            }
            return meta;
        } catch (Exception e) {
            Log.e(TAG, "getModMeta failed", e);
            return null;
        }
    }

    private static String optString(JSONObject o, String k, String def) {
        try {
            if (o.has(k) && !o.isNull(k)) {
                return o.getString(k);
            }
        } catch (JSONException ignored) {
        }
        return def;
    }

    private static String lastError = null;

    public static String getLastError() {
        return lastError;
    }

    public static boolean isModInstalled(String id) {
        if (id == null) return false;
        File f = new File(getModsDir(), id + ".so");
        return f.exists();
    }

    private static String computeId(File f, ModMeta meta) {
        String cleanName = meta.name != null ? meta.name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase() : "mod";
        cleanName = cleanName.replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        return "m_" + cleanName;
    }

    public static File installMod(File soFile, ModMeta meta) {
        if (soFile == null || !soFile.exists() || meta == null) {
            return null;
        }
        File dir = getModsDir();
        dir.mkdirs();

        File[] existing = dir.listFiles((d, name) -> name.endsWith(".so"));
        if (existing != null) {
            for (File ef : existing) {
                ModMeta em = parseMeta(ef);
                if (em != null && (em.id.equals(meta.id) || (em.name != null && em.name.equalsIgnoreCase(meta.name)))) {
                    Log.i(TAG, "Deleting previous version of mod before install: " + ef.getName());
                    ef.delete();
                }
            }
        }

        File dest = new File(dir, meta.id + ".so");
        try {
            copyFile(soFile, dest);
        } catch (IOException e) {
            Log.e(TAG, "copy failed", e);
            return null;
        }
        return dest;
    }

    public static boolean loadNative(File soFile) {
        if (soFile == null || !soFile.exists()) {
            lastError = "Файл мода не существует: " + (soFile != null ? soFile.getAbsolutePath() : "null");
            return false;
        }
        try {
            System.load(soFile.getAbsolutePath());
            loadedPaths.add(soFile.getAbsolutePath());
            lastError = null;
            Log.i(TAG, "loaded " + soFile.getName());
            return true;
        } catch (Throwable e) {
            lastError = Log.getStackTraceString(e);
            Log.e(TAG, "load failed " + soFile.getName(), e);
            return false;
        }
    }

    private static boolean crashShieldInitialized = false;
    private static final String CRASH_FLAG_FILE = "startup_active.flag";

    public static void initCrashShield() {
        if (crashShieldInitialized) return;
        crashShieldInitialized = true;

        try {
            File flag = new File(getModsDir(), CRASH_FLAG_FILE);
            if (flag.exists()) {
                Log.e(TAG, "CRASH SHIELD: Startup crash detected from previous run! Disabling all mods to restore app stability.");
                disableAllMods();
                flag.delete();
            } else {
                flag.createNewFile();
                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        if (flag.exists()) flag.delete();
                    } catch (Throwable ignore) {}
                }, 5000);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed startup crash check", t);
        }

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String stackTrace = Log.getStackTraceString(throwable);
            boolean isModRelated = false;
            if (stackTrace != null) {
                String s = stackTrace.toLowerCase();
                if (s.contains("nitrogram.mod") || s.contains("pine") || s.contains("xposed")
                        || s.contains("streaks") || s.contains("exteragram") || s.contains("canyie")
                        || s.contains("modmanager") || s.contains("voicechanger") || s.contains("motionblur")
                        || s.contains("inmemorydexclassloader")) {
                    isModRelated = true;
                }
            }
            if (isModRelated) {
                Log.e(TAG, "CRASH SHIELD: Intercepted mod crash! Automatically disabling all mods to protect Telegram.", throwable);
                lastError = stackTrace;
                disableAllMods();
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    public static void disableAllMods() {
        List<ModMeta> installed = getInstalledMods();
        for (ModMeta m : installed) {
            setEnabled(m.id, false);
        }
    }

    public static void loadInstalledMods() {
        initCrashShield();

        try {
            File dl = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (dl != null && dl.exists()) {
                File devMod = new File(dl, "libtg_streaks.so");
                if (devMod.exists()) {
                    ModMeta meta = parseMeta(devMod);
                    if (meta != null) {
                        File target = getModFile(meta.id);
                        if (target != null && (!target.exists() || devMod.lastModified() > target.lastModified() || devMod.length() != target.length())) {
                            Log.i(TAG, "Auto-updating mod from Download: " + meta.id);
                            installMod(devMod, meta);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Auto-update mod check failed", t);
        }

        File dir = getModsDir();
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".so"));
        if (files == null) {
            return;
        }
        for (File f : files) {
            ModMeta m = parseMeta(f);
            if (m != null) {
                if (f.getName().startsWith("m9d") || f.getName().startsWith("ma4")) {
                    Log.i(TAG, "Deleting old mod file: " + f.getName());
                    f.delete();
                    continue;
                }
                if (isEnabled(m.id)) {
                    try {
                        boolean ok = loadNative(f);
                        if (ok) {
                            registerLoadedMod(m.id);
                        } else {
                            Log.e(TAG, "Mod load failed for " + m.id + "! Disabling mod for safety.");
                            setEnabled(m.id, false);
                        }
                    } catch (Throwable t) {
                        lastError = Log.getStackTraceString(t);
                        Log.e(TAG, "CRASH SHIELD: Mod " + m.id + " threw exception during load! Disabling mod.", t);
                        setEnabled(m.id, false);
                    }
                }
            }
        }
    }

    /**
     * Перенести ожидающий класс входа мода (установленный JNI_OnLoad через
     * onModEntryClass) в зарегистрированные, и применить сохранённые настройки.
     * Вызывается после каждой загрузки .so, чтобы hasSettings() работало сразу,
     * без перезапуска приложения.
     */
    public static void registerLoadedMod(String id) {
        Log.i(TAG, "registerLoadedMod " + id + " pending=" + (pendingEntry != null));
        Class<?> entryClass = pendingEntry;
        if (entryClass != null) {
            modEntryClasses.put(id, entryClass);
            pendingEntry = null;
        } else {
            entryClass = modEntryClasses.get(id);
        }

        if (entryClass != null) {
            try {
                java.lang.reflect.Method startM = entryClass.getMethod("start");
                startM.invoke(null);
                Log.i(TAG, "Successfully invoked start() on " + entryClass.getName());
            } catch (Throwable t) {
                Log.i(TAG, "Invoke start note: " + t.getMessage());
            }

            String stored = getModSettingsJson(id);
            if (stored != null) {
                applySettings(id, stored);
            }
        }
    }

    /**
     * Вызывается из .so (JNI_OnLoad) сразу после загрузки DEX мода. Передаёт
     * класс точки входа (ModEntry), чтобы клиент мог вызывать его методы настроек.
     */
    public static void onModEntryClass(Class<?> entry) {
        Log.i(TAG, "onModEntryClass: " + (entry != null ? entry.getName() : "null"));
        pendingEntry = entry;
    }

    public static String debugState() {
        return "registered=" + modEntryClasses.size() + " pending=" + (pendingEntry != null);
    }

    /** Передать моду сохранённые значения настроек в виде JSON-объекта. */
    public static void applySettings(String id, String json) {
        Class<?> e = modEntryClasses.get(id);
        if (e == null || json == null) {
            return;
        }
        try {
            Method m = e.getMethod("applySettings", String.class);
            if (m != null) {
                m.invoke(null, json);
            }
        } catch (Throwable t) {
            Log.e(TAG, "applySettings failed " + id, t);
        }
    }

    /** Есть ли у мода экран настроек (createSettingsScreen() возвращает не-null). */
    public static boolean hasSettings(String id) {
        Class<?> e = modEntryClasses.get(id);
        if (e == null) {
            return false;
        }
        try {
            Method m = e.getMethod("createSettingsScreen");
            return m.invoke(null) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Зарегистрирован ли класс входа мода (загружен ли его DEX). */
    public static boolean isModRegistered(String id) {
        return modEntryClasses.containsKey(id);
    }

    /** Получить экземпляр экрана настроек мода либо null. */
    public static Object getSettingsScreen(String id) {
        Class<?> e = modEntryClasses.get(id);
        Log.i(TAG, "getSettingsScreen " + id + " registered=" + (e != null) + " " + debugState());
        if (e == null) {
            return null;
        }
        try {
            Method m = e.getMethod("createSettingsScreen");
            Object res = m.invoke(null);
            Log.i(TAG, "getSettingsScreen result=" + (res != null ? res.getClass().getName() : "null"));
            return res;
        } catch (Throwable t) {
            Log.e(TAG, "getSettingsScreen error", t);
            return null;
        }
    }

    public static Object getModSetting(String id, String key, Object def) {
        JSONObject s = loadState();
        JSONObject settings = s.optJSONObject("settings");
        if (settings == null) {
            return def;
        }
        JSONObject m = settings.optJSONObject(id);
        if (m == null || !m.has(key)) {
            return def;
        }
        try {
            return m.get(key);
        } catch (Exception e) {
            return def;
        }
    }

    public static void setModSetting(String id, String key, Object value) {
        JSONObject s = loadState();
        JSONObject settings = s.optJSONObject("settings");
        if (settings == null) {
            settings = new JSONObject();
            try {
                s.put("settings", settings);
            } catch (JSONException ignored) {
                return;
            }
        }
        JSONObject m = settings.optJSONObject(id);
        if (m == null) {
            m = new JSONObject();
            try {
                settings.put(id, m);
            } catch (JSONException ignored) {
                return;
            }
        }
        try {
            m.put(key, value);
        } catch (JSONException ignored) {
            return;
        }
        saveState(s);
    }

    private static String getModSettingsJson(String id) {
        JSONObject s = loadState();
        JSONObject settings = s.optJSONObject("settings");
        if (settings == null) {
            return null;
        }
        JSONObject m = settings.optJSONObject(id);
        return m == null ? null : m.toString();
    }

    public static List<ModMeta> getInstalledMods() {
        List<ModMeta> list = new ArrayList<>();
        File dir = getModsDir();
        if (dir == null || !dir.exists()) {
            return list;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".so"));
        if (files == null) {
            return list;
        }
        Set<String> seenIds = new HashSet<>();
        for (File f : files) {
            ModMeta m = parseMeta(f);
            if (m != null) {
                if (seenIds.contains(m.id)) {
                    Log.i(TAG, "Deleting duplicate mod file in app_mods: " + f.getName());
                    f.delete();
                    continue;
                }
                seenIds.add(m.id);
                m.loaded = loadedPaths.contains(f.getAbsolutePath());
                m.enabled = isEnabled(m.id);
                m.hasSettings = hasSettings(m.id) || m.hasSettings;
                list.add(m);
            }
        }
        return list;
    }

    private static final String STATE_FILE = "mods_state.json";

    private static JSONObject loadState() {
        File f = new File(getModsDir(), STATE_FILE);
        if (!f.exists()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(new String(readAllBytes(f), "UTF-8"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static void saveState(JSONObject s) {
        File f = new File(getModsDir(), STATE_FILE);
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(s.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "saveState failed", e);
        }
    }

    public static boolean isEnabled(String id) {
        return loadState().optBoolean(id + "_enabled", true);
    }

    public static void setEnabled(String id, boolean enabled) {
        JSONObject s = loadState();
        try {
            s.put(id + "_enabled", enabled);
        } catch (JSONException ignored) {
        }
        saveState(s);
    }

    public static File getModFile(String id) {
        return new File(getModsDir(), id + ".so");
    }

    public static void deleteMod(String id) {
        File f = getModFile(id);
        if (f.exists()) {
            f.delete();
        }
        JSONObject s = loadState();
        s.remove(id + "_enabled");
        saveState(s);
        loadedPaths.remove(f.getAbsolutePath());
    }

    private static byte[] readAllBytes(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        try {
            long len = f.length();
            if (len > Integer.MAX_VALUE) {
                throw new IOException("file too large");
            }
            byte[] buf = new byte[(int) len];
            int off = 0;
            int n;
            while (off < buf.length && (n = in.read(buf, off, buf.length - off)) >= 0) {
                off += n;
            }
            return buf;
        } finally {
            in.close();
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
        } finally {
            in.close();
            out.close();
        }
    }
}
