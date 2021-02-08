package com.easy.kotlins.http

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
        return request.newBuilder().apply {
            val range = if (continuing) file.length() else 0L
            header(
                DOWNLOAD_HEADER_RANGE_NAME,
                MessageFormat.format(DOWNLOAD_HEADER_RANGE_VALUE, range)
            )
        }.build()
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
                onProgress(readBytes, totalBytes)
            }
            if (readBytes == totalBytes) {
                rename(srcFile)
            } else throw IOException("Response is closed or write file failed")
        }
    }

    companion object {
        private const val DOWNLOAD_SUFFIX_TMP = ".tmp" // 下载临时文件后缀
        private const val DOWNLOAD_HEADER_RANGE_NAME = "Range"
        private const val DOWNLOAD_HEADER_RANGE_VALUE = "bytes={0,number,#}-"

        private fun rename(srcFile: File): File {
            val tmpFilePath = srcFile.absolutePath
            val destFile = File(tmpFilePath.substring(0, tmpFilePath.indexOf(DOWNLOAD_SUFFIX_TMP)))

            return if (srcFile.renameTo(destFile)) {
                destFile
            } else throw IOException("Rename file failed")
        }

    }
}