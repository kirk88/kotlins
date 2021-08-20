@file:Suppress("unused")

package com.nice.kothttp

import android.os.Handler
import android.os.Looper
import android.os.Message
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

interface OkExtension<T> {

    fun shouldInterceptRequest(request: Request): Request {
        return request
    }

    fun shouldInterceptResponse(response: Response): Response {
        return response
    }

    fun map(response: Response): T

}

abstract class OkDownloadExtension<T> : OkExtension<T> {

    override fun shouldInterceptRequest(request: Request): Request {
        return request
    }

    override fun shouldInterceptResponse(response: Response): Response {
        return response
    }

}

open class DefaultOkDownloadExtension internal constructor(
    path: String,
    private val continuing: Boolean
) : OkDownloadExtension<File>() {

    private val file: File = File(path + DOWNLOAD_FILE_SUFFIX_TMP)

    override fun shouldInterceptRequest(request: Request): Request {
        return if (continuing) {
            request.newBuilder().header(
                DOWNLOAD_HEADER_RANGE_NAME,
                DOWNLOAD_HEADER_RANGE_VALUE.format(file.length())
            ).build()
        } else request
    }


    override fun map(response: Response): File {
        if (file.exists() && !continuing) {
            file.delete()
        }

        if (!file.exists()) {
            val parent = file.parentFile

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw IOException("Create download directory failed")
            }

            if (!file.createNewFile()) {
                throw IOException("Create download file failed")
            }
        }

        return writeStreamToFile(requireNotNull(response.body) { "ResponseBody is null" }, file)
    }

    open fun onProgress(readBytes: Long, totalBytes: Long) {
    }

    private fun writeStreamToFile(
        body: ResponseBody,
        file: File
    ): File {
        val inputStream = body.byteStream()
        return RandomAccessFile(file, "rw").use { accessFile ->
            var readBytes = file.length()
            val totalBytes = body.contentLength() + readBytes
            val buffer = ByteArray(1024)
            var length: Int
            accessFile.seek(readBytes)
            while (inputStream.read(buffer).also { len -> length = len } != -1) {
                readBytes += length.toLong()
                accessFile.write(buffer, 0, length)
                HANDLER.notifyProgressChanged(this, readBytes, totalBytes)
            }
            if (readBytes == totalBytes) {
                rename(file)
            } else throw IOException("Response closed or failed to write to file")
        }
    }

    private class ProgressHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            (msg.obj as ProgressPerformer).perform()
        }

        fun notifyProgressChanged(
            mapper: DefaultOkDownloadExtension,
            readBytes: Long,
            totalBytes: Long
        ) {
            val message = Message()
            message.obj = ProgressPerformer(mapper, readBytes, totalBytes)
            sendMessage(message)
        }

        private class ProgressPerformer(
            private val mapper: DefaultOkDownloadExtension,
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
            val destFile = File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_FILE_SUFFIX_TMP)))
            return if (srcFile.renameTo(destFile)) {
                destFile
            } else throw IOException("Rename file failed")
        }
    }

}

fun DefaultOkDownloadExtension(
    path: String,
    continuing: Boolean = false,
    onProgress: (readBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
): DefaultOkDownloadExtension = object : DefaultOkDownloadExtension(path, continuing) {
    override fun onProgress(readBytes: Long, totalBytes: Long) {
        onProgress.invoke(readBytes, totalBytes)
    }
}