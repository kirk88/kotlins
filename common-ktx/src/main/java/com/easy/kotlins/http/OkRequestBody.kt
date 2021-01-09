package com.easy.kotlins.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

internal class OkRequestBody(
    private val body: RequestBody, private val action: ProgressAction
) : RequestBody() {

    private var bufferedSink: BufferedSink? = null

    override fun contentType(): MediaType? {
        return body.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return body.contentLength()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val wrappedSink = bufferedSink ?: Okio.buffer(SinkWrapper(sink)).also { bufferedSink = it }
        body.writeTo(wrappedSink)
        wrappedSink.flush()
    }

    private inner class SinkWrapper(sink: Sink) : ForwardingSink(sink) {

        var wroteBytes = 0L

        var totalBytes = 0L

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            if (totalBytes == 0L) {
                totalBytes = contentLength()
            }
            wroteBytes += byteCount
            action(wroteBytes, totalBytes)
        }

    }
}