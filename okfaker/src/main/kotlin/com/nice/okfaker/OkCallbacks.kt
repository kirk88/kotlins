package com.nice.okfaker

import android.os.Handler
import android.os.Looper
import android.os.Message

internal object OkCallbacks {
    private const val MSG_WHAT_BASE = 1000000000
    private const val MSG_WHAT_ON_START = MSG_WHAT_BASE + 1
    private const val MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 2
    private const val MSG_WHAT_ON_ERROR = MSG_WHAT_BASE + 3
    private const val MSG_WHAT_ON_CANCEL = MSG_WHAT_BASE + 4
    private const val MSG_WHAT_ON_COMPLETE = MSG_WHAT_BASE + 5

    private val HANDLER: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val body = msg.obj as MessageBody
            when (msg.what) {
                MSG_WHAT_ON_START -> {
                    body.callback.onStart()
                }
                MSG_WHAT_ON_COMPLETE -> {
                    body.callback.onComplete()
                }
                MSG_WHAT_ON_SUCCESS -> {
                    @Suppress("UNCHECKED_CAST") val callback = body.callback as OkCallback<Any>
                    callback.onSuccess(body.args[0])
                }
                MSG_WHAT_ON_ERROR -> {
                    body.callback.onError(body.args[0] as Throwable)
                }
                MSG_WHAT_ON_CANCEL -> {
                    body.callback.onCancel()
                }
            }
        }
    }

    fun <T> onSuccess(callback: OkCallback<T>, value: T) {
        HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, MessageBody(callback, value as Any))
            .sendToTarget()
    }

    fun onError(callback: OkCallback<*>, error: Throwable) {
        HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, MessageBody(callback, error)).sendToTarget()
    }

    fun onStart(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_START, MessageBody(callback)).sendToTarget()
    }

    fun onComplete(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_COMPLETE, MessageBody(callback)).sendToTarget()
    }

    fun onCancel(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, MessageBody(callback)).sendToTarget()
    }

    private class MessageBody(val callback: OkCallback<*>, vararg args: Any) {
        val args: Array<Any> = arrayOf(*args)
    }
}