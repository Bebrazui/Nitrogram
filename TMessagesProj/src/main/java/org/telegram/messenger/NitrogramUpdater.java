package org.telegram.messenger;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NitrogramUpdater {

    private static final String TAG = "NitrogramUpdater";
    private static final String GITHUB_API_RELEASES = "https://api.github.com/repos/Bebrazui/Nitrogram/releases/latest";
    private static final String GITHUB_RELEASES_PAGE = "https://github.com/Bebrazui/Nitrogram/releases";
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
            HttpURLConnection connection = null;
            try {
                URL url = new URL(GITHUB_API_RELEASES);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Nitrogram-App/" + BuildVars.BUILD_VERSION_STRING);
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    String tagName = json.optString("tag_name", "").trim();
                    String releaseName = json.optString("name", "").trim();
                    String body = json.optString("body", "").trim();
                    String htmlUrl = json.optString("html_url", GITHUB_RELEASES_PAGE);

                    String apkDownloadUrl = null;
                    JSONArray assets = json.optJSONArray("assets");
                    if (assets != null) {
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            String assetName = asset.optString("name", "");
                            if (assetName.endsWith(".apk")) {
                                apkDownloadUrl = asset.optString("browser_download_url", null);
                                break;
                            }
                        }
                    }

                    if (apkDownloadUrl == null) {
                        apkDownloadUrl = htmlUrl;
                    }

                    String currentVersion = BuildVars.BUILD_VERSION_STRING;
                    boolean isNewer = isVersionNewer(tagName, currentVersion);

                    final boolean hasUpdate = isNewer;
                    final String displayTag = !TextUtils.isEmpty(tagName) ? tagName : releaseName;
                    final String finalDownloadUrl = apkDownloadUrl;
                    final String finalBody = body;

                    AndroidUtilities.runOnUIThread(() -> {
                        if (activity.isFinishing()) return;

                        if (callback != null) {
                            callback.onResult(hasUpdate, displayTag, finalBody, finalDownloadUrl);
                        }

                        if (hasUpdate) {
                            showUpdateDialog(activity, displayTag, finalBody, finalDownloadUrl);
                        } else if (isManual) {
                            try {
                                BulletinFactory.global().createSimpleBulletin(
                                        R.raw.chats_infotip,
                                        LocaleController.getString(R.string.YourVersionIsLatest)
                                ).show();
                            } catch (Throwable ignore) {}
                        }
                    });
                } else {
                    if (isManual) {
                        AndroidUtilities.runOnUIThread(() -> {
                            if (!activity.isFinishing()) {
                                try {
                                    BulletinFactory.global().createSimpleBulletin(
                                            R.raw.error,
                                            "Не удалось проверить обновления",
                                            "Код ответа: " + responseCode
                                    ).show();
                                } catch (Throwable ignore) {}
                            }
                        });
                    }
                }
            } catch (Throwable t) {
                FileLog.e("Failed checking Nitrogram update", t);
                if (isManual) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (!activity.isFinishing()) {
                            try {
                                BulletinFactory.global().createSimpleBulletin(
                                        R.raw.error,
                                        "Ошибка проверки обновлений",
                                        t.getMessage()
                                ).show();
                            } catch (Throwable ignore) {}
                        }
                    });
                }
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Throwable ignore) {}
                }
            }
        });
    }

    private static void showUpdateDialog(Context context, String version, String changelog, String downloadUrl) {
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Доступно обновление Nitrogram");

        StringBuilder msg = new StringBuilder();
        msg.append("Вышла новая версия: ").append(version).append("\n\n");
        if (!TextUtils.isEmpty(changelog)) {
            msg.append("Что нового:\n").append(changelog).append("\n\n");
        }
        msg.append("Хотите скачать и установить обновление сейчас?");
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
                                "Загрузка обновления началась",
                                "Следите за статусом в шторке уведомлений"
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
