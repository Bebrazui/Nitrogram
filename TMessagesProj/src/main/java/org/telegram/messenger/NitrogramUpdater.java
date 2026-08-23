package org.telegram.messenger;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import org.json.JSONObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NitrogramUpdater {

    private static final String TAG = "NitrogramUpdater";
    private static final String VERSION_JSON_URL = "https://raw.githubusercontent.com/Bebrazui/Nitrogram/master/version.json";
    private static final String GITHUB_RELEASES_LATEST = "https://github.com/Bebrazui/Nitrogram/releases/latest";
    private static final String PREF_LAST_CHECK = "nitro_last_update_check_time";
    private static final long CHECK_INTERVAL_MS = 60 * 60 * 1000L; // 1 hour

    public interface UpdateCallback {
        void onResult(boolean hasUpdate, String version, String changelog, String downloadUrl);
    }

    public static void checkUpdate(Activity activity, boolean isManual, UpdateCallback callback) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (!isManual) {
            long lastCheck = MessagesController.getGlobalMainSettings().getLong(PREF_LAST_CHECK, 0);
            if (Math.abs(System.currentTimeMillis() - lastCheck) < CHECK_INTERVAL_MS) {
                return;
            }
        }

        MessagesController.getGlobalMainSettings().edit().putLong(PREF_LAST_CHECK, System.currentTimeMillis()).apply();

        Utilities.globalQueue.postRunnable(() -> {
            String tagName = null;
            String body = null;
            String apkDownloadUrl = null;

            // 1. Попытка через version.json на GitHub Raw (нет лимитов API, никогда не возвращает 403)
            try {
                URL url = new URL(VERSION_JSON_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                if (conn.getResponseCode() == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    tagName = json.optString("tag", json.optString("version", ""));
                    body = json.optString("changelog", "");
                    apkDownloadUrl = json.optString("apk_url", "");
                }
                conn.disconnect();
            } catch (Throwable t) {
                FileLog.e("NitrogramUpdater version.json failed: " + t);
            }

            // 2. Фолбэк через редирект github.com/releases/latest (также без лимитов API)
            if (TextUtils.isEmpty(tagName)) {
                try {
                    URL url = new URL(GITHUB_RELEASES_LATEST);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)");
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);

                    int code = conn.getResponseCode();
                    if (code == 302 || code == 301) {
                        String location = conn.getHeaderField("Location");
                        if (!TextUtils.isEmpty(location) && location.contains("/tag/")) {
                            tagName = location.substring(location.lastIndexOf("/tag/") + 5);
                            apkDownloadUrl = "https://github.com/Bebrazui/Nitrogram/releases/download/" + tagName + "/Nitrogram-" + tagName + ".apk";
                        }
                    }
                    conn.disconnect();
                } catch (Throwable t) {
                    FileLog.e("NitrogramUpdater redirect check failed: " + t);
                }
            }

            final String finalTag = tagName;
            final String finalBody = body;
            final String finalApkUrl = !TextUtils.isEmpty(apkDownloadUrl) ? apkDownloadUrl : GITHUB_RELEASES_LATEST;

            AndroidUtilities.runOnUIThread(() -> {
                if (activity.isFinishing()) return;

                if (TextUtils.isEmpty(finalTag)) {
                    if (isManual) {
                        try {
                            BulletinFactory.global().createSimpleBulletin(
                                    R.raw.error,
                                    "Не удалось проверить обновления",
                                    "Проверьте подключение к сети"
                            ).show();
                        } catch (Throwable ignore) {}
                    }
                    return;
                }

                String currentVersion = BuildVars.BUILD_VERSION_STRING;
                boolean isNewer = isVersionNewer(finalTag, currentVersion);

                if (callback != null) {
                    callback.onResult(isNewer, finalTag, finalBody, finalApkUrl);
                }

                if (isNewer) {
                    showUpdateDialog(activity, finalTag, finalBody, finalApkUrl);
                } else if (isManual) {
                    try {
                        BulletinFactory.global().createSimpleBulletin(
                                R.raw.chats_infotip,
                                LocaleController.getString(R.string.YourVersionIsLatest)
                        ).show();
                    } catch (Throwable ignore) {}
                }
            });
        });
    }

    private static void showUpdateDialog(Context context, String version, String changelog, String downloadUrl) {
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Доступно обновление Nitrogram (" + version + ")");

        StringBuilder msg = new StringBuilder();
        if (!TextUtils.isEmpty(changelog)) {
            msg.append(changelog).append("\n\n");
        }
        msg.append("Хотите скачать и установить новую версию?");
        builder.setMessage(msg.toString());

        builder.setPositiveButton("Обновить", (dialog, which) -> {
            try {
                if (downloadUrl.endsWith(".apk")) {
                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                    request.setTitle("Nitrogram " + version);
                    request.setDescription("Загрузка обновления Nitrogram...");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Nitrogram-" + version + ".apk");
                    dm.enqueue(request);

                    try {
                        BulletinFactory.global().createSimpleBulletin(
                                R.raw.ic_download,
                                "Загрузка начата",
                                "Файл будет сохранён в папку Загрузки"
                        ).show();
                    } catch (Throwable ignore) {}
                } else {
                    Browser.openUrl(context, downloadUrl);
                }
            } catch (Throwable t) {
                Browser.openUrl(context, downloadUrl);
            }
        });

        builder.setNegativeButton("Позже", null);
        builder.show();
    }

    public static boolean isVersionNewer(String remoteTag, String currentVersion) {
        if (TextUtils.isEmpty(remoteTag) || TextUtils.isEmpty(currentVersion)) {
            return false;
        }

        String remote = cleanVersion(remoteTag);
        String current = cleanVersion(currentVersion);

        if (remote.equals(current)) {
            return false;
        }

        String[] remoteParts = remote.split("\\.");
        String[] currentParts = current.split("\\.");
        int length = Math.max(remoteParts.length, currentParts.length);

        for (int i = 0; i < length; i++) {
            int remoteNum = i < remoteParts.length ? parseNumber(remoteParts[i]) : 0;
            int currentNum = i < currentParts.length ? parseNumber(currentParts[i]) : 0;
            if (remoteNum > currentNum) {
                return true;
            } else if (remoteNum < currentNum) {
                return false;
            }
        }
        return false;
    }

    private static String cleanVersion(String version) {
        if (version == null) return "";
        version = version.trim();
        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1).trim();
        }
        int dashIdx = version.indexOf('-');
        if (dashIdx != -1) {
            version = version.substring(0, dashIdx);
        }
        return version;
    }

    private static int parseNumber(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
