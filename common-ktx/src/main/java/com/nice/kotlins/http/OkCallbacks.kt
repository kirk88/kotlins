package com.nice.kotlins.http

import android.os.Handler
import android.os.Looper
import android.os.Message

internal object OkCallbacks {
    private const val MSG_WHAT_BASE = 1000000000
    private const val MSG_WHAT_ON_START = MSG_WHAT_BASE + 1
    private const val MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 2
    private const val MSG_WHAT_ON_ERROR = MSG_WHAT_BASE + 3
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
                MSG_WHAT_ON_ERROR -> {
                    dispatchOnError(body.callback, body.args[0] as Throwable)
                }
                MSG_WHAT_ON_CANCEL -> {
                    dispatchOnCancel(body.callback)
                }
            }
        }
    }

    fun <T> onSuccess(callback: OkCallback<T>, value: () -> T) {
        try {
            HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, MessageBody(callback, value() as Any))
                .sendToTarget()
        } catch (error: Throwable) {
            HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, MessageBody(callback, error))
                .sendToTarget()
        }
    }

    fun onError(callback: OkCallback<*>, error: () -> Throwable) {
        HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, MessageBody(callback, error())).sendToTarget()
    }

    fun onStart(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_START, MessageBody(callback)).sendToTarget()
    }

    fun onCompletion(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_COMPLETION, MessageBody(callback)).sendToTarget()
    }

    fun onCancel(callback: OkCallback<*>) {
        HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, MessageBody(callback)).sendToTarget()
    }

    private fun dispatchOnStart(callback: OkCallback<*>) {
        try {
            callback.onStart()
        } catch (error: Throwable) {
            dispatchOnError(callback, error)
        }
    }

    private fun dispatchOnCompletion(callback: OkCallback<*>) {
        try {
            callback.onCompletion()
        } catch (error: Throwable) {
            dispatchOnError(callback, error)
        }
    }

    private fun dispatchOnCancel(callback: OkCallback<*>) {
        try {
            callback.onCancel()
        } catch (error: Throwable) {
            dispatchOnError(callback, error)
        }
    }

    private fun <T> dispatchOnSuccess(callback: OkCallback<T>, result: T) {
        try {
            callback.onSuccess(result)
        } catch (error: Throwable) {
            dispatchOnError(callback, error)
        }
    }

    private fun dispatchOnError(callback: OkCallback<*>, error: Throwable) {
        try {
            callback.onError(error)
        } catch (_: Throwable) {
        }
    }

    private class MessageBody(val callback: OkCallback<*>, vararg args: Any) {
        val args: Array<Any> = arrayOf(*args)
    }
}