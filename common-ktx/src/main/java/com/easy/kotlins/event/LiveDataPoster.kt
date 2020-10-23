package com.easy.kotlins.event

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

/**
 * data are not lost
 */
object LiveDataPoster {
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    fun <T> post(liveData: MutableLiveData<T>, data: T?) {
        if (Looper.myLooper() === Looper.getMainLooper()) {
            liveData.setValue(data)
        } else {
            setValue(liveData, data)
        }
    }

    private fun <T> setValue(liveData: MutableLiveData<T>, data: T?) {
        handler.post(SetValueRunnable.create(liveData, data))
    }

    private class SetValueRunnable<T> private constructor(private val liveData: MutableLiveData<T>, private val data: T?) : Runnable {
        override fun run() {
            liveData.value = data
        }

        companion object {
            fun <T> create(liveData: MutableLiveData<T>, data: T?): SetValueRunnable<T> {
                return SetValueRunnable(liveData, data)
            }
        }
    }
}