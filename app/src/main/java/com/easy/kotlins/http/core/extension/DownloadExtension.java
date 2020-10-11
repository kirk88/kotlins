package com.easy.kotlins.http.core.extension;

import androidx.core.util.ObjectsCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Create by LiZhanPing on 2020/8/25
 */
public class DownloadExtension extends OkExtension {

    private static final String DOWNLOAD_SUFFIX_TMP = ".tmp"; // 下载临时文件后缀
    private static final String DOWNLOAD_HEADER_RANGE_NAME = "Range";
    private static final String DOWNLOAD_HEADER_RANGE_VALUE = "bytes={0,number,#}-";


    private final File mFile;

    private DownloadExtension(String path, boolean continuing) {
        this.mFile = checkDownloadFile(path + DOWNLOAD_SUFFIX_TMP, continuing);
    }

    public static DownloadExtension create(String path, boolean continuing) {
        return new DownloadExtension(path, continuing);
    }

    public static DownloadExtension create(String path) {
        return new DownloadExtension(path, false);
    }

    public void addHeader(Request.Builder builder) {
        final long range = mFile.exists() ? mFile.length() : 0L;
        builder.header(DOWNLOAD_HEADER_RANGE_NAME, MessageFormat.format(DOWNLOAD_HEADER_RANGE_VALUE, range));
    }

    public File download(Response response, OnProgressListener listener) throws Exception {
        if (mFile == null) {
            throw new NullPointerException("download path must not be null");
        }

        if (!mFile.exists()) {
            File parent = mFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("create download parent directory failed");
            }

            if (!mFile.createNewFile()) {
                throw new IOException("create download file failed");
            }
        }

        return response.isSuccessful() ? writeStreamToFile(response, mFile, listener) : null;
    }

    private File writeStreamToFile(Response response, File srcFile, OnProgressListener listener) {
        final ResponseBody body = response.body();

        try (InputStream inputStream = Objects.requireNonNull(body).byteStream(); RandomAccessFile accessFile = new RandomAccessFile(srcFile, "rw")) {
            long readBytes = srcFile.length();
            long totalBytes = body.contentLength() + readBytes;

            byte[] buf = new byte[4096];
            int len;

            accessFile.seek(readBytes);

            while (!isCanceled() && (len = inputStream.read(buf)) != -1) {
                readBytes += len;
                accessFile.write(buf, 0, len);
                onProgress(listener, readBytes, totalBytes);
            }

            if (!isCanceled() && readBytes == totalBytes) {
                return DownloadExtension.rename(srcFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onProgress(OnProgressListener listener, long downloadedBytes, long totalBytes) {
        if (isCanceled()) return;

        if (listener != null) {
            listener.onProgress(downloadedBytes, totalBytes);
        }
    }

    private boolean isCanceled() {
        return getOkFaker() != null && getOkFaker().isCanceled();
    }

    private static File rename(File srcFile) {
        String tmpFilePath = srcFile.getAbsolutePath();
        File destFile = new File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_SUFFIX_TMP)));

        // 下载完成后去除临时文件后缀
        if (srcFile.renameTo(destFile)) {
            return destFile;
        }
        return null;
    }

    private static File checkDownloadFile(String path, boolean breakpoint) {
        File file = new File(path);
        if (file.exists() && !breakpoint) {
            file.delete();
        }
        return file;
    }

    public interface OnProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);
    }
}
