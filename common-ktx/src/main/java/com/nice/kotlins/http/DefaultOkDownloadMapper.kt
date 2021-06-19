@file:Suppress("unused")

package com.nice.kotlins.http

import android.os.Handler
import android.os.Looper
import android.os.Message
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

open class DefaultOkDownloadMapper internal constructor(
        path: String,
        private val continuing: Boolean
) : OkDownloadMapper<File>() {

    private val file: File = File(path + DOWNLOAD_FILE_SUFFIX_TMP)

    override fun shouldInterceptRequest(request: Request): Request {
        val range = if (continuing) file.length() else 0L
        return request.newBuilder().header(
                DOWNLOAD_HEADER_RANGE_NAME,
                DOWNLOAD_HEADER_RANGE_VALUE.format(range)
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
            srcFile: File
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
                HANDLER.notifyProgressChanged(this, readBytes, totalBytes)
            }
            if (readBytes == totalBytes) {
                rename(srcFile)
            } else throw IOException("Response closed or failed to write to file")
        }
    }

    private class ProgressHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            (msg.obj as ProgressPerformer).perform()
        }

        fun notifyProgressChanged(
                mapper: DefaultOkDownloadMapper,
                readBytes: Long,
                totalBytes: Long
        ) {
            val message = Message()
            message.obj = ProgressPerformer(mapper, readBytes, totalBytes)
            sendMessage(message)
        }

        private class ProgressPerformer(
                private val mapper: DefaultOkDownloadMapper,
                private val readBytes: Long,
                private val totalBytes: Long
        ) {
            fun perform() {
                mapper.onProgress(readBytes, totalBytes)
            }
        }

    }

    companion object {
        private const val DOWNLOAD_FILE_SUFFIX_TMP = ".tmp"
        private const val DOWNLOAD_HEADER_RANGE_NAME = "Range"
        private const val DOWNLOAD_HEADER_RANGE_VALUE = "bytes=%d-"

        private val HANDLER = ProgressHandler()

        private fun rename(srcFile: File): File {
            val tmpFilePath = srcFile.absolutePath
            val destFile =
                    File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_FILE_SUFFIX_TMP)))
            return if (srcFile.renameTo(destFile)) {
                destFile
            } else throw IOException("Rename file failed")
        }
    }

}

fun DefaultOkDownloadMapper(
        path: String,
        continuing: Boolean = false,
        onProgress: (readBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
): DefaultOkDownloadMapper = object : DefaultOkDownloadMapper(path, continuing) {
    override fun onProgress(readBytes: Long, totalBytes: Long) {
        onProgress.invoke(readBytes, totalBytes)
    }
}