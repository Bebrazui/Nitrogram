package org.telegram.messenger;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import org.telegram.tgnet.TLRPC;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public final class NitrogramChunkedFileHelper {

    public static final long MAX_CHUNK_SIZE_BYTES = 1_990_000_000L;
    private static final String FILE_NAME_PREFIX = "ngc__";
    private static final String FILE_NAME_DELIMITER = "__";

    private NitrogramChunkedFileHelper() {
    }

    public static final class ChunkInfo {
        public final String transferId;
        public final int partIndex;
        public final int totalParts;
        public final long totalSize;
        public final String originalName;

        public ChunkInfo(String transferId, int partIndex, int totalParts, long totalSize, String originalName) {
            this.transferId = transferId;
            this.partIndex = partIndex;
            this.totalParts = totalParts;
            this.totalSize = totalSize;
            this.originalName = originalName;
        }

        public boolean isFirstPart() {
            return partIndex == 1;
        }
    }

    public static final class ExpandedDocuments {
        public final ArrayList<String> paths = new ArrayList<>();
        public final ArrayList<String> originalPaths = new ArrayList<>();
        public final ArrayList<Uri> uris = new ArrayList<>();
    }

    public static boolean shouldSplit(long length) {
        return length > MAX_CHUNK_SIZE_BYTES;
    }

    public static String buildChunkFileName(String transferId, int partIndex, int totalParts, long totalSize, String originalName) {
        String safeOriginalName = Uri.encode(TextUtils.isEmpty(originalName) ? "file.bin" : originalName);
        return FILE_NAME_PREFIX + transferId + FILE_NAME_DELIMITER + partIndex + FILE_NAME_DELIMITER + totalParts + FILE_NAME_DELIMITER + totalSize + FILE_NAME_DELIMITER + safeOriginalName;
    }

    public static ChunkInfo parse(String fileName) {
        if (TextUtils.isEmpty(fileName) || !fileName.startsWith(FILE_NAME_PREFIX)) {
            return null;
        }
        String[] parts = fileName.split(FILE_NAME_DELIMITER, 6);
        if (parts.length != 6) {
            return null;
        }
        try {
            String transferId = parts[1];
            int partIndex = Integer.parseInt(parts[2]);
            int totalParts = Integer.parseInt(parts[3]);
            long totalSize = Long.parseLong(parts[4]);
            String originalName = Uri.decode(parts[5]);
            if (TextUtils.isEmpty(transferId) || TextUtils.isEmpty(originalName) || partIndex <= 0 || totalParts <= 0 || partIndex > totalParts || totalSize <= 0) {
                return null;
            }
            return new ChunkInfo(transferId, partIndex, totalParts, totalSize, originalName);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static ChunkInfo getChunkInfo(TLRPC.Document document) {
        if (document == null || document.attributes == null) {
            return null;
        }
        for (int i = 0; i < document.attributes.size(); i++) {
            TLRPC.DocumentAttribute attribute = document.attributes.get(i);
            if (attribute instanceof TLRPC.TL_documentAttributeFilename) {
                return parse(((TLRPC.TL_documentAttributeFilename) attribute).file_name);
            }
        }
        return null;
    }

    public static ChunkInfo getChunkInfo(MessageObject messageObject) {
        return messageObject == null ? null : getChunkInfo(messageObject.getDocument());
    }

    public static String getDisplayName(MessageObject messageObject) {
        ChunkInfo info = getChunkInfo(messageObject);
        return info != null ? info.originalName : null;
    }

    public static long getDisplaySize(MessageObject messageObject) {
        ChunkInfo info = getChunkInfo(messageObject);
        return info != null ? info.totalSize : -1;
    }

    public static ExpandedDocuments expandDocuments(ArrayList<String> paths, ArrayList<String> originalPaths, ArrayList<Uri> uris) throws IOException {
        ExpandedDocuments expanded = new ExpandedDocuments();
        int pathCount = paths != null ? paths.size() : 0;
        for (int i = 0; i < pathCount; i++) {
            String path = paths.get(i);
            String originalPath = originalPaths != null && i < originalPaths.size() ? originalPaths.get(i) : path;
            expandPath(expanded, path, originalPath);
        }
        int uriCount = uris != null ? uris.size() : 0;
        for (int i = 0; i < uriCount; i++) {
            expandUri(expanded, uris.get(i));
        }
        return expanded;
    }

    private static void expandPath(ExpandedDocuments expanded, String path, String originalPath) throws IOException {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            expanded.paths.add(path);
            expanded.originalPaths.add(originalPath != null ? originalPath : path);
            return;
        }
        if (!shouldSplit(file.length())) {
            expanded.paths.add(path);
            expanded.originalPaths.add(originalPath != null ? originalPath : path);
            return;
        }
        String transferId = Utilities.random.nextLong() + "_" + Long.toHexString(System.currentTimeMillis());
        ArrayList<File> chunks = splitStream(new BufferedInputStream(new FileInputStream(file)), file.getName(), file.length(), transferId);
        for (int i = 0; i < chunks.size(); i++) {
            expanded.paths.add(chunks.get(i).getAbsolutePath());
            expanded.originalPaths.add(chunks.get(i).getAbsolutePath());
        }
    }

    private static void expandUri(ExpandedDocuments expanded, Uri uri) throws IOException {
        if (uri == null) {
            return;
        }
        ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        String displayName = "file.bin";
        long size = -1;
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex);
                }
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (!shouldSplit(size)) {
            expanded.uris.add(uri);
            return;
        }
        String transferId = Utilities.random.nextLong() + "_" + Long.toHexString(System.currentTimeMillis());
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            expanded.uris.add(uri);
            return;
        }
        try {
            ArrayList<File> chunks = splitStream(new BufferedInputStream(inputStream), displayName, size, transferId);
            for (int i = 0; i < chunks.size(); i++) {
                expanded.paths.add(chunks.get(i).getAbsolutePath());
                expanded.originalPaths.add(chunks.get(i).getAbsolutePath());
            }
        } finally {
            inputStream.close();
        }
    }

    private static ArrayList<File> splitStream(InputStream inputStream, String originalName, long totalSize, String transferId) throws IOException {
        ArrayList<File> result = new ArrayList<>();
        int totalParts = (int) ((totalSize + MAX_CHUNK_SIZE_BYTES - 1) / MAX_CHUNK_SIZE_BYTES);
        File chunksDir = new File(ApplicationLoader.applicationContext.getCacheDir(), "nitrogram/chunks/" + transferId);
        if (!chunksDir.exists() && !chunksDir.mkdirs()) {
            throw new IOException(String.format(Locale.US, "Unable to create chunk directory: %s", chunksDir));
        }
        byte[] buffer = new byte[1024 * 1024];
        for (int partIndex = 1; partIndex <= totalParts; partIndex++) {
            long remaining = MAX_CHUNK_SIZE_BYTES;
            File chunkFile = new File(chunksDir, buildChunkFileName(transferId, partIndex, totalParts, totalSize, originalName));
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(chunkFile))) {
                while (remaining > 0) {
                    int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (read < 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, read);
                    remaining -= read;
                }
                outputStream.flush();
            }
            result.add(chunkFile);
        }
        return result;
    }
}
