package com.easy.kotlins.http

import android.os.Handler
import android.os.Looper
import android.os.Message

internal object OkCallbacks {
    private const val MSG_WHAT_BASE = 1000000000
    private const val MSG_WHAT_ON_START = MSG_WHAT_BASE + 1
    private const val MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 2
    private const val MSG_WHAT_ON_FAILURE = MSG_WHAT_BASE + 3
    private const val MSG_WHAT_ON_CANCEL = MSG_WHAT_BASE + 4
    private const val MSG_WHAT_ON_COMPLETION = MSG_WHAT_BASE + 5

    private val HANDLER: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val body = msg.obj as MessageBody
            when (msg.what) {
                MSG_WHAT_ON_START -> {
                    dispatchOnStart(body.callback)
                }
                MSG_WHAT_ON_COMPLETION -> {
                    dispatchOnCompletion(body.callback)
                }
                MSG_WHAT_ON_SUCCESS -> {
                    @Suppress("UNCHECKED_CAST") val callback = body.callback as OkCallback<Any>
                    dispatchOnSuccess(callback, body.args[0])
                }
                MSG_WHAT_ON_FAILURE -> {
                    dispatchOnFailure(body.callback, body.args[0] as Exception)
                }
                MSG_WHAT_ON_CANCEL -> {
                    dispatchOnCancel(body.callback)
                }
            }
        }
    }

    fun <T> onSuccess(callback: OkCallback<T>?, result: T) {
        if (callback == null) return
        HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, MessageBody(callback, result as Any))
            .sendToTarget()
    }

    fun onFailure(callback: OkCallback<*>?, exception: Throwable) {
        if (callback == null) return
        HANDLER.obtainMessage(MSG_WHAT_ON_FAILURE, MessageBody(callback, exception)).sendToTarget()
    }

    fun onStart(callback: OkCallback<*>?) {
        if (callback == null) return
        HANDLER.obtainMessage(MSG_WHAT_ON_START, MessageBody(callback)).sendToTarget()
    }

    fun onCompletion(callback: OkCallback<*>?) {
        if (callback == null) return
        HANDLER.obtainMessage(MSG_WHAT_ON_COMPLETION, MessageBody(callback)).sendToTarget()
    }

    fun onCancel(callback: OkCallback<*>?) {
        if (callback == null) return
        HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, MessageBody(callback)).sendToTarget()
    }

    private fun dispatchOnStart(callback: OkCallback<*>) {
        try {
            callback.onStart()
        } catch (exception: Exception) {
            dispatchOnFailure(callback, exception)
        }
    }

    private fun dispatchOnCompletion(callback: OkCallback<*>) {
        try {
            callback.onCompletion()
        } catch (exception: Exception) {
            dispatchOnFailure(callback, exception)
        }
    }

    private fun dispatchOnCancel(callback: OkCallback<*>) {
        try {
            callback.onCancel()
        } catch (exception: Exception) {
            dispatchOnFailure(callback, exception)
        }
    }

    private fun <T> dispatchOnSuccess(callback: OkCallback<T>, result: T) {
        try {
            callback.onSuccess(result)
        } catch (exception: Exception) {
            dispatchOnFailure(callback, exception)
        }
    }

    private fun dispatchOnFailure(callback: OkCallback<*>, exception: Throwable) {
        try {
            callback.onFailure(exception)
        } catch (_: Exception) {
        }
    }

    private class MessageBody(val callback: OkCallback<*>, vararg args: Any) {
        val args: Array<Any> = arrayOf(*args)
    }
}