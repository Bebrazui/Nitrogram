package org.telegram.messenger;

import android.os.SystemClock;
import android.text.TextUtils;

import org.json.JSONObject;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatCapsuleManager {

    private static volatile ChatCapsuleManager instance;

    public static ChatCapsuleManager getInstance() {
        if (instance == null) {
            synchronized (ChatCapsuleManager.class) {
                if (instance == null) {
                    instance = new ChatCapsuleManager();
                }
            }
        }
        return instance;
    }

    public static class CapsuleInfo {
        public String id;
        public long dialogId;
        public String title;
        public long createdAt;
        public int dateFrom;
        public int dateTo;
        public int messagesCount;
        public int mediaCount;
        public long totalSizeBytes;
        public boolean includePhotos;
        public boolean includeVideos;
        public boolean includeVoice;
        public boolean includeDocs;

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(createdAt));
        }

        public String getFormattedSize() {
            return AndroidUtilities.formatFileSize(totalSizeBytes);
        }
    }

    public static class CapsuleData {
        public CapsuleInfo info;
        public List<MessageObject> messageObjects = new ArrayList<>();
        public HashMap<Long, TLRPC.User> users = new HashMap<>();
        public HashMap<Long, TLRPC.Chat> chats = new HashMap<>();
    }

    public interface CreateCallback {
        void onProgress(int current, int total, String status);
        void onSuccess(CapsuleInfo info);
        void onError(String error);
    }

    public interface LoadCallback {
        void onLoaded(CapsuleData data);
        void onError(String error);
    }

    public File getCapsulesBaseDir() {
        File dir = new File(ApplicationLoader.applicationContext.getFilesDir(), "chat_capsules");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getCapsuleDir(String capsuleId) {
        return new File(getCapsulesBaseDir(), capsuleId);
    }

    public List<CapsuleInfo> getCapsules(long dialogId) {
        List<CapsuleInfo> all = getAllCapsules();
        List<CapsuleInfo> result = new ArrayList<>();
        for (CapsuleInfo info : all) {
            if (dialogId == 0 || info.dialogId == dialogId) {
                result.add(info);
            }
        }
        return result;
    }

    public List<CapsuleInfo> getAllCapsules() {
        List<CapsuleInfo> list = new ArrayList<>();
        File baseDir = getCapsulesBaseDir();
        File[] folders = baseDir.listFiles();
        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    File metaFile = new File(folder, "info.json");
                    if (metaFile.exists()) {
                        CapsuleInfo info = readCapsuleInfo(metaFile);
                        if (info != null) {
                            list.add(info);
                        }
                    }
                }
            }
        }
        Collections.sort(list, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        return list;
    }

    private CapsuleInfo readCapsuleInfo(File metaFile) {
        try {
            FileInputStream fis = new FileInputStream(metaFile);
            byte[] bytes = new byte[(int) metaFile.length()];
            fis.read(bytes);
            fis.close();
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);

            CapsuleInfo info = new CapsuleInfo();
            info.id = json.optString("id");
            info.dialogId = json.optLong("dialogId");
            info.title = json.optString("title");
            info.createdAt = json.optLong("createdAt");
            info.dateFrom = json.optInt("dateFrom");
            info.dateTo = json.optInt("dateTo");
            info.messagesCount = json.optInt("messagesCount");
            info.mediaCount = json.optInt("mediaCount");
            info.totalSizeBytes = json.optLong("totalSizeBytes");
            info.includePhotos = json.optBoolean("includePhotos", true);
            info.includeVideos = json.optBoolean("includeVideos", true);
            info.includeVoice = json.optBoolean("includeVoice", true);
            info.includeDocs = json.optBoolean("includeDocs", true);
            return info;
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public boolean deleteCapsule(String capsuleId) {
        try {
            File folder = getCapsuleDir(capsuleId);
            if (folder.exists()) {
                deleteDirectory(folder);
                return true;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return false;
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    public void createCapsule(int currentAccount, long dialogId, int daysPeriod,
                              boolean includePhotos, boolean includeVideos,
                              boolean includeVoice, boolean includeDocs,
                              CreateCallback callback) {
        final long currentTime = System.currentTimeMillis();
        final int minDate = daysPeriod > 0 ? (int) ((currentTime / 1000) - (long) daysPeriod * 86400L) : 0;
        final String capsuleId = "capsule_" + Math.abs(dialogId) + "_" + currentTime;

        // Resolve title
        String chatTitle = "Чат";
        if (dialogId > 0) {
            TLRPC.User u = MessagesController.getInstance(currentAccount).getUser(dialogId);
            if (u != null) chatTitle = UserObject.getUserName(u);
        } else {
            TLRPC.Chat c = MessagesController.getInstance(currentAccount).getChat(-dialogId);
            if (c != null) chatTitle = c.title;
        }
        final String finalTitle = chatTitle;

        MessagesStorage.getInstance(currentAccount).getStorageQueue().postRunnable(() -> {
            try {
                AndroidUtilities.runOnUIThread(() -> callback.onProgress(0, 100, "Чтение сообщений из базы данных..."));

                SQLiteDatabase db = MessagesStorage.getInstance(currentAccount).getDatabase();
                String query = minDate > 0
                        ? String.format(Locale.US, "SELECT data, mid, date FROM messages_v2 WHERE uid = %d AND date >= %d ORDER BY date ASC", dialogId, minDate)
                        : String.format(Locale.US, "SELECT data, mid, date FROM messages_v2 WHERE uid = %d ORDER BY date ASC", dialogId);

                List<TLRPC.Message> messages = new ArrayList<>();
                Set<Long> userIds = new HashSet<>();
                Set<Long> chatIds = new HashSet<>();
                int dateFrom = Integer.MAX_VALUE;
                int dateTo = 0;

                SQLiteCursor cursor = db.queryFinalized(query);
                while (cursor.next()) {
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data != null) {
                        try {
                            TLRPC.Message msg = TLRPC.Message.TLdeserialize(data, data.readInt32(false), false);
                            if (msg != null) {
                                msg.readAttachPath(data, UserConfig.getInstance(currentAccount).clientUserId);
                                messages.add(msg);
                                if (msg.date < dateFrom) dateFrom = msg.date;
                                if (msg.date > dateTo) dateTo = msg.date;

                                if (msg.from_id != null) {
                                    if (msg.from_id.user_id != 0) userIds.add(msg.from_id.user_id);
                                    if (msg.from_id.chat_id != 0) chatIds.add(msg.from_id.chat_id);
                                    if (msg.from_id.channel_id != 0) chatIds.add(msg.from_id.channel_id);
                                }
                                if (msg.peer_id != null) {
                                    if (msg.peer_id.user_id != 0) userIds.add(msg.peer_id.user_id);
                                    if (msg.peer_id.chat_id != 0) chatIds.add(msg.peer_id.chat_id);
                                    if (msg.peer_id.channel_id != 0) chatIds.add(msg.peer_id.channel_id);
                                }
                            }
                        } catch (Throwable ignored) {}
                        data.reuse();
                    }
                }
                cursor.dispose();

                if (messages.isEmpty()) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Не найдено сообщений за выбранный период"));
                    return;
                }

                File capsuleDir = getCapsuleDir(capsuleId);
                capsuleDir.mkdirs();
                File mediaDir = new File(capsuleDir, "media");
                mediaDir.mkdirs();

                // Load users & chats
                List<TLRPC.User> users = new ArrayList<>();
                if (!userIds.isEmpty()) {
                    SQLiteCursor uCursor = db.queryFinalized("SELECT data FROM users WHERE uid IN (" + TextUtils.join(",", userIds) + ")");
                    while (uCursor.next()) {
                        NativeByteBuffer data = uCursor.byteBufferValue(0);
                        if (data != null) {
                            try {
                                TLRPC.User u = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                                if (u != null) users.add(u);
                            } catch (Throwable ignored) {}
                            data.reuse();
                        }
                    }
                    uCursor.dispose();
                }

                List<TLRPC.Chat> chats = new ArrayList<>();
                if (!chatIds.isEmpty()) {
                    SQLiteCursor cCursor = db.queryFinalized("SELECT data FROM chats WHERE uid IN (" + TextUtils.join(",", chatIds) + ")");
                    while (cCursor.next()) {
                        NativeByteBuffer data = cCursor.byteBufferValue(0);
                        if (data != null) {
                            try {
                                TLRPC.Chat c = TLRPC.Chat.TLdeserialize(data, data.readInt32(false), false);
                                if (c != null) chats.add(c);
                            } catch (Throwable ignored) {}
                            data.reuse();
                        }
                    }
                    cCursor.dispose();
                }

                // Copy media
                int totalMsgs = messages.size();
                int mediaSavedCount = 0;
                for (int i = 0; i < totalMsgs; i++) {
                    final int idx = i;
                    if (idx % 20 == 0 || idx == totalMsgs - 1) {
                        AndroidUtilities.runOnUIThread(() -> callback.onProgress(idx + 1, totalMsgs, "Сохранение медиафайлов (" + (idx + 1) + "/" + totalMsgs + ")..."));
                    }

                    TLRPC.Message msg = messages.get(i);
                    File srcFile = null;
                    if (msg.media != null) {
                        if (msg.media instanceof TLRPC.TL_messageMediaPhoto && includePhotos && msg.media.photo != null) {
                            srcFile = FileLoader.getInstance(currentAccount).getPathToMessage(msg);
                            if (srcFile == null || !srcFile.exists()) {
                                TLRPC.PhotoSize size = FileLoader.getClosestPhotoSizeWithSize(msg.media.photo.sizes, 1280);
                                if (size != null) {
                                    srcFile = FileLoader.getInstance(currentAccount).getPathToAttach(size);
                                }
                            }
                        } else if (msg.media instanceof TLRPC.TL_messageMediaDocument && msg.media.document != null) {
                            boolean isVoiceOrRound = MessageObject.isVoiceMessage(msg) || MessageObject.isRoundVideoMessage(msg);
                            boolean isVideo = MessageObject.isVideoMessage(msg);
                            if ((isVoiceOrRound && includeVoice) || (isVideo && includeVideos) || (!isVoiceOrRound && !isVideo && includeDocs)) {
                                srcFile = FileLoader.getInstance(currentAccount).getPathToAttach(msg.media.document);
                            }
                        }
                    }

                    if (srcFile != null && srcFile.exists() && srcFile.length() > 0) {
                        File destFile = new File(mediaDir, srcFile.getName());
                        try {
                            if (!destFile.exists()) {
                                AndroidUtilities.copyFile(srcFile, destFile);
                            }
                            msg.attachPath = destFile.getAbsolutePath();
                            mediaSavedCount++;
                        } catch (Throwable ignored) {}
                    }
                }

                // Serialize messages
                AndroidUtilities.runOnUIThread(() -> callback.onProgress(totalMsgs, totalMsgs, "Запись архива капсулы..."));
                SerializedData sData = new SerializedData();
                sData.writeInt32(users.size());
                for (TLRPC.User u : users) u.serializeToStream(sData);

                sData.writeInt32(chats.size());
                for (TLRPC.Chat c : chats) c.serializeToStream(sData);

                sData.writeInt32(messages.size());
                for (TLRPC.Message m : messages) m.serializeToStream(sData);

                File messagesFile = new File(capsuleDir, "messages.dat");
                FileOutputStream fos = new FileOutputStream(messagesFile);
                byte[] bytes = sData.toByteArray();
                fos.write(bytes);
                fos.flush();
                fos.close();
                sData.cleanup();

                // Compute size
                long totalSize = getFolderSize(capsuleDir);

                // Write metadata JSON
                CapsuleInfo info = new CapsuleInfo();
                info.id = capsuleId;
                info.dialogId = dialogId;
                info.title = finalTitle;
                info.createdAt = currentTime;
                info.dateFrom = dateFrom;
                info.dateTo = dateTo;
                info.messagesCount = messages.size();
                info.mediaCount = mediaSavedCount;
                info.totalSizeBytes = totalSize;
                info.includePhotos = includePhotos;
                info.includeVideos = includeVideos;
                info.includeVoice = includeVoice;
                info.includeDocs = includeDocs;

                JSONObject json = new JSONObject();
                json.put("id", info.id);
                json.put("dialogId", info.dialogId);
                json.put("title", info.title);
                json.put("createdAt", info.createdAt);
                json.put("dateFrom", info.dateFrom);
                json.put("dateTo", info.dateTo);
                json.put("messagesCount", info.messagesCount);
                json.put("mediaCount", info.mediaCount);
                json.put("totalSizeBytes", info.totalSizeBytes);
                json.put("includePhotos", info.includePhotos);
                json.put("includeVideos", info.includeVideos);
                json.put("includeVoice", info.includeVoice);
                json.put("includeDocs", info.includeDocs);

                File infoFile = new File(capsuleDir, "info.json");
                FileOutputStream infoFos = new FileOutputStream(infoFile);
                infoFos.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
                infoFos.flush();
                infoFos.close();

                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(info));
            } catch (Throwable e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> callback.onError("Ошибка при создании снимка: " + e.getMessage()));
            }
        });
    }

    private long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += getFolderSize(f);
                }
            }
        } else {
            size += file.length();
        }
        return size;
    }

    public void loadCapsuleData(int currentAccount, String capsuleId, LoadCallback callback) {
        File capsuleDir = getCapsuleDir(capsuleId);
        File infoFile = new File(capsuleDir, "info.json");
        File messagesFile = new File(capsuleDir, "messages.dat");

        if (!infoFile.exists() || !messagesFile.exists()) {
            callback.onError("Файлы капсулы повреждены или удалены");
            return;
        }

        Utilities.globalQueue.postRunnable(() -> {
            try {
                CapsuleInfo info = readCapsuleInfo(infoFile);
                if (info == null) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Не удалось прочитать метаданные"));
                    return;
                }

                FileInputStream fis = new FileInputStream(messagesFile);
                byte[] bytes = new byte[(int) messagesFile.length()];
                fis.read(bytes);
                fis.close();

                SerializedData sData = new SerializedData(bytes);
                CapsuleData capsuleData = new CapsuleData();
                capsuleData.info = info;

                int uCount = sData.readInt32(false);
                for (int i = 0; i < uCount; i++) {
                    TLRPC.User u = TLRPC.User.TLdeserialize(sData, sData.readInt32(false), false);
                    if (u != null) capsuleData.users.put(u.id, u);
                }

                int cCount = sData.readInt32(false);
                for (int i = 0; i < cCount; i++) {
                    TLRPC.Chat c = TLRPC.Chat.TLdeserialize(sData, sData.readInt32(false), false);
                    if (c != null) capsuleData.chats.put(c.id, c);
                }

                int mCount = sData.readInt32(false);
                for (int i = 0; i < mCount; i++) {
                    TLRPC.Message m = TLRPC.Message.TLdeserialize(sData, sData.readInt32(false), false);
                    if (m != null) {
                        MessageObject obj = new MessageObject(currentAccount, m, capsuleData.users, capsuleData.chats, false, true);
                        capsuleData.messageObjects.add(obj);
                    }
                }
                sData.cleanup();

                AndroidUtilities.runOnUIThread(() -> callback.onLoaded(capsuleData));
            } catch (Throwable e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> callback.onError("Ошибка при загрузке капсулы: " + e.getMessage()));
            }
        });
    }
}
