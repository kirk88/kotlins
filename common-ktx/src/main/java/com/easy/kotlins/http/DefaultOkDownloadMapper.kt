package com.easy.kotlins.http

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.MainThread
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.text.MessageFormat

open class DefaultOkDownloadMapper(path: String, private val continuing: Boolean = false) :
    OkDownloadMapper<Response, File> {

    private val file: File by lazy { File(path + DOWNLOAD_SUFFIX_TMP) }

    override fun shouldInterceptRequest(request: Request): Request {
        val range = if (continuing) file.length() else 0L
        return request.newBuilder().header(
            DOWNLOAD_HEADER_RANGE_NAME,
            MessageFormat.format(DOWNLOAD_HEADER_RANGE_VALUE, range)
        ).build()
    }

    override fun map(value: Response): File {
        if (file.exists() && !continuing) {
            file.delete()
        }

        return writeStreamToFile(requireNotNull(value.body) { "ResponseBody is null" }, file)
    }

    open fun onProgress(readBytes: Long, totalBytes: Long) {
    }

    private fun writeStreamToFile(
        body: ResponseBody,
        srcFile: File,
    ): File = body.use {
        val inputStream = it.byteStream()
        return@use RandomAccessFile(srcFile, "rw").use { accessFile ->
            var readBytes = srcFile.length()
            val totalBytes = it.contentLength() + readBytes
            val buffer = ByteArray(1024)
            var length: Int
            accessFile.seek(readBytes)
            while (inputStream.read(buffer).also { len -> length = len } != -1) {
                readBytes += length.toLong()
                accessFile.write(buffer, 0, length)
                HANDLER.updateProgress(this, readBytes, totalBytes)
            }
            if (readBytes == totalBytes) {
                rename(srcFile)
            } else throw IOException("Response is closed or write file failed")
        }
    }

    private class ProgressHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            val performer = msg.obj as ProgressPerformer
            performer.perform()
        }

        fun updateProgress(mapper: DefaultOkDownloadMapper, readBytes: Long, totalBytes: Long) {
            val message = Message()
            message.obj = ProgressPerformer(mapper, readBytes, totalBytes)
            sendMessage(message)
        }

        private class ProgressPerformer(
            private val mapper: DefaultOkDownloadMapper,
            private val readBytes: Long,
            private val totalBytes: Long
        ) {
            @MainThread
            fun perform() {
                mapper.onProgress(readBytes, totalBytes)
            }
        }

    }

    companion object {
        private const val DOWNLOAD_SUFFIX_TMP = ".tmp" // 下载临时文件后缀
        private const val DOWNLOAD_HEADER_RANGE_NAME = "Range"
        private const val DOWNLOAD_HEADER_RANGE_VALUE = "bytes={0,number,#}-"

        private val HANDLER = ProgressHandler()

        private fun rename(srcFile: File): File {
            val tmpFilePath = srcFile.absolutePath
            val destFile = File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_SUFFIX_TMP)))

            return if (srcFile.renameTo(destFile)) {
                destFile
            } else throw IOException("Rename file failed")
        }

    }
}