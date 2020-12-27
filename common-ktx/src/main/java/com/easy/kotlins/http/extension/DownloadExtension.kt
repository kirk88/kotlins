package com.easy.kotlins.http.extension

import okhttp3.Request
import okhttp3.Response
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.text.MessageFormat

/**
 * Create by LiZhanPing on 2020/8/25
 */
class DownloadExtension private constructor(path: String, continuing: Boolean) : OkExtension,
    Closeable {

    private val file: File?

    @Volatile
    private var closed = false

    override fun shouldInterceptRequest(request: Request): Request {
        return request.newBuilder().apply {
            val range = if (file!!.exists()) file.length() else 0L
            header(
                DOWNLOAD_HEADER_RANGE_NAME,
                MessageFormat.format(DOWNLOAD_HEADER_RANGE_VALUE, range)
            )
        }.build()
    }

    override fun onResponse(response: Response): Boolean {
        return false
    }

    override fun onError(error: Throwable): Boolean {
        return false
    }

    override fun close() {
        closed = true
    }

    @Throws(Exception::class)
    fun onResponse(response: Response, listener: OnProgressListener): File? {
        if (file == null) {
            throw NullPointerException("download path must not be null")
        }
        if (!file.exists()) {
            val parent = file.parentFile
            if (parent == null || !parent.exists() && !parent.mkdirs()) {
                throw IOException("create download parent directory failed")
            }
            if (!file.createNewFile()) {
                throw IOException("create download file failed")
            }
        }
        return if (response.isSuccessful) writeStreamToFile(response, file, listener) else null
    }

    @Throws(IOException::class)
    private fun writeStreamToFile(
        response: Response,
        srcFile: File,
        listener: OnProgressListener
    ): File? = response.body()?.let { body ->
        body.byteStream().use { inputStream ->
            return@use RandomAccessFile(srcFile, "rw").use { accessFile ->
                var readBytes = srcFile.length()
                val totalBytes = body.contentLength() + readBytes
                val buffer = ByteArray(1024)
                var length = 0
                accessFile.seek(readBytes)
                while (!closed && inputStream.read(buffer).also { length = it } != -1) {
                    readBytes += length.toLong()
                    accessFile.write(buffer, 0, length)
                    onProgress(listener, readBytes, totalBytes)
                }
                if (!closed && readBytes == totalBytes) {
                    rename(srcFile)
                } else null
            }
        }
    }

    private fun onProgress(listener: OnProgressListener?, downloadedBytes: Long, totalBytes: Long) {
        if (closed) return
        listener?.onProgressChanged(downloadedBytes, totalBytes)
    }

    interface OnProgressListener {
        fun onProgressChanged(downloadedBytes: Long, totalBytes: Long)
    }

    companion object {
        private const val DOWNLOAD_SUFFIX_TMP = ".tmp" // 下载临时文件后缀
        private const val DOWNLOAD_HEADER_RANGE_NAME = "Range"
        private const val DOWNLOAD_HEADER_RANGE_VALUE = "bytes={0,number,#}-"

        fun create(path: String, continuing: Boolean): DownloadExtension {
            return DownloadExtension(path, continuing)
        }

        fun create(path: String): DownloadExtension {
            return DownloadExtension(path, false)
        }

        private fun rename(srcFile: File): File? {
            val tmpFilePath = srcFile.absolutePath
            val destFile = File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_SUFFIX_TMP)))

            // 下载完成后去除临时文件后缀
            return if (srcFile.renameTo(destFile)) {
                destFile
            } else null
        }

        private fun checkDownloadFile(path: String, breakpoint: Boolean): File {
            val file = File(path)
            if (file.exists() && !breakpoint) {
                file.delete()
            }
            return file
        }
    }

    init {
        file = checkDownloadFile(path + DOWNLOAD_SUFFIX_TMP, continuing)
    }
}