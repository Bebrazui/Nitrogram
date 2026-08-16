package org.telegram.messenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static final class ModMeta {
        public String id;
        public String name;
        public String description;
        public String version;
        public String extra;
        public Bitmap icon;
        public boolean loaded;

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

    private static String optString(JSONObject o, String k, String def) {
        try {
            if (o.has(k) && !o.isNull(k)) {
                return o.getString(k);
            }
        } catch (JSONException ignored) {
        }
        return def;
    }

    private static String computeId(File f, ModMeta meta) {
        String base = meta.name + "_" + meta.version + "_" + f.length();
        return "m" + Integer.toHexString(base.hashCode());
    }

    public static boolean installMod(File soFile, ModMeta meta) {
        if (soFile == null || !soFile.exists() || meta == null) {
            return false;
        }
        File dir = getModsDir();
        dir.mkdirs();
        File dest = new File(dir, meta.id + ".so");
        try {
            copyFile(soFile, dest);
        } catch (IOException e) {
            Log.e(TAG, "copy failed", e);
            return false;
        }
        meta.loaded = loadNative(dest);
        return true;
    }

    public static boolean loadNative(File soFile) {
        if (soFile == null || !soFile.exists()) {
            return false;
        }
        try {
            System.load(soFile.getAbsolutePath());
            loadedPaths.add(soFile.getAbsolutePath());
            Log.i(TAG, "loaded " + soFile.getName());
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "load failed " + soFile.getName(), e);
            return false;
        }
    }

    public static void loadInstalledMods() {
        File dir = getModsDir();
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".so"));
        if (files == null) {
            return;
        }
        for (File f : files) {
            loadNative(f);
        }
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
        for (File f : files) {
            ModMeta m = parseMeta(f);
            if (m != null) {
                m.loaded = loadedPaths.contains(f.getAbsolutePath());
                list.add(m);
            }
        }
        return list;
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
