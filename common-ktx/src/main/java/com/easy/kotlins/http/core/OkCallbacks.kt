package com.easy.kotlins.http.core

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.facebook.stetho.BuildConfig

/**
 * Create by LiZhanPing on 2020/4/27
 * desc: 请求回调处理
 */
internal object OkCallbacks {
    private const val MSG_WHAT_BASE = 1000000000
    private const val MSG_WHAT_ON_START = MSG_WHAT_BASE + 1
    private const val MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 2
    private const val MSG_WHAT_ON_ERROR = MSG_WHAT_BASE + 3
    private const val MSG_WHAT_ON_UPDATE = MSG_WHAT_BASE + 4
    private const val MSG_WHAT_ON_CANCEL = MSG_WHAT_BASE + 5
    private const val MSG_WHAT_ON_COMPLETE = MSG_WHAT_BASE + 6
    private val HANDLER: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val body = msg.obj as MessageBody
            when (msg.what) {
                MSG_WHAT_ON_START -> {
                    @Suppress("UNCHECKED_CAST") val callback = body.callback as OkCallback<Any>
                    callOnStart(callback)
                }
                MSG_WHAT_ON_COMPLETE -> {
                    @Suppress("UNCHECKED_CAST") val callback = body.callback as OkCallback<Any>
                    callOnComplete(callback)
                }
                MSG_WHAT_ON_SUCCESS -> {
                    @Suppress("UNCHECKED_CAST") val callback = body.callback as OkCallback<Any>
                    callOnSuccess(callback, body.args[0])
                }
                MSG_WHAT_ON_ERROR -> {
                    callOnError(body.callback, body.args[0] as Throwable)
                }
                MSG_WHAT_ON_UPDATE -> {
                    callOnProgress(
                        body.callback as OkDownloadCallback,
                        body.args[0] as Long,
                        body.args[1] as Long
                    )
                }
                MSG_WHAT_ON_CANCEL -> {
                    callOnCancel(body.callback as OkDownloadCallback)
                }
            }
        }
    }

    fun <T: Any> success(callback: OkCallback<T>?, result: T) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, MessageBody(callback, result)).sendToTarget()
        }
    }

    fun error(callback: OkCallback<*>?, error: Throwable?) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, MessageBody(callback, error!!)).sendToTarget()
        }
    }

    fun start(callback: OkCallback<*>?) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_START, MessageBody(callback)).sendToTarget()
        }
    }

    fun complete(callback: OkCallback<*>?) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_COMPLETE, MessageBody(callback)).sendToTarget()
        }
    }

    fun cancel(callback: OkDownloadCallback?) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, MessageBody(callback)).sendToTarget()
        }
    }

    fun progress(callback: OkDownloadCallback?, downloadedBytes: Long, totalBytes: Long) {
        if (callback != null) {
            HANDLER.obtainMessage(
                MSG_WHAT_ON_UPDATE,
                MessageBody(callback, downloadedBytes, totalBytes)
            ).sendToTarget()
        }
    }

    private fun callOnStart(callback: OkCallback<*>) {
        try {
            callback.onStart()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun callOnComplete(callback: OkCallback<*>) {
        try {
            callback.onComplete()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun callOnCancel(callback: OkDownloadCallback) {
        try {
            callback.onCancel()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun callOnProgress(
        callback: OkDownloadCallback,
        downloadedBytes: Long,
        totalBytes: Long
    ) {
        try {
            callback.onProgress(downloadedBytes, totalBytes)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun <T> callOnSuccess(callback: OkCallback<T>, result: T) {
        try {
            callback.onSuccess(result)
        } catch (e: Exception) {
            callOnError(callback, e)
        }
    }

    private fun callOnError(callback: OkCallback<*>, e: Throwable) {
        try {
            if (BuildConfig.DEBUG) {
                Log.e(OkRequest::class.simpleName, e.localizedMessage, e)
            }
            callback.onError(e)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    internal class MessageBody(val callback: OkCallback<*>, vararg args: Any) {
        val args: Array<Any> = arrayOf(args)
    }
}