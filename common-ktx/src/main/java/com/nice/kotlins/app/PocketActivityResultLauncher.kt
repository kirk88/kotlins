@file:Suppress("unused")

package com.nice.kotlins.app

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.nice.kotlins.helper.putExtras

class PocketActivityResultLauncher<I, O>(
    private val activityResultContract: ActivityResultContract<I, O>
) : ActivityResultLauncher<I>() {

    private val callbacks = mutableListOf<ActivityResultCallback<O>>()
    private val oneShotCallbacks = mutableListOf<ActivityResultCallback<O>>()

    private lateinit var delegate: ActivityResultLauncher<I>
    private val activityResultCallback = ActivityResultCallback<O> {
        for (callback in callbacks) {
            callback.onActivityResult(it)
        }

        for (callback in oneShotCallbacks) {
            callback.onActivityResult(it)
        }
        oneShotCallbacks.clear()
    }
    private var isRegistered = false

    fun register(activity: ComponentActivity) {
        check(!isRegistered) { "Already registered" }
        delegate = activity.registerForActivityResult(
            activityResultContract,
            activityResultCallback
        )
        isRegistered = true
    }

    fun register(fragment: Fragment) {
        check(!isRegistered) { "Already registered" }
        delegate = fragment.registerForActivityResult(
            activityResultContract,
            activityResultCallback
        )
        isRegistered = true
    }

    fun addCallback(callback: ActivityResultCallback<O>) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: ActivityResultCallback<O>) {
        if (!callbacks.remove(callback)) {
            oneShotCallbacks.remove(callback)
        }
    }

    fun launch(input: I, callback: ActivityResultCallback<O>) {
        oneShotCallbacks.add(callback)
        delegate.launch(input)
    }

    fun launch(
        input: I,
        options: ActivityOptionsCompat?,
        callback: ActivityResultCallback<O>
    ) {
        oneShotCallbacks.add(callback)
        delegate.launch(input, options)
    }

    override fun launch(input: I) {
        delegate.launch(input)
    }

    override fun launch(input: I, options: ActivityOptionsCompat?) {
        delegate.launch(input, options)
    }

    override fun unregister() {
        check(isRegistered) { "Not registered yet" }
        callbacks.clear()
        oneShotCallbacks.clear()
        delegate.unregister()
    }


    override fun getContract(): ActivityResultContract<I, O> {
        return activityResultContract
    }

}

inline fun <reified T> PocketActivityResultLauncher<Intent, *>.launch(
    context: Context,
    vararg values: Pair<String, Any?>
) = launch(
    Intent(context, T::class.java).putExtras(*values)
)

inline fun <reified T> PocketActivityResultLauncher<Intent, *>.launch(
    context: Context,
    options: ActivityOptionsCompat?,
    vararg values: Pair<String, Any?>
) = launch(
    Intent(context, T::class.java).putExtras(*values), options
)

inline fun <reified T, R> PocketActivityResultLauncher<Intent, R>.launch(
    context: Context,
    vararg values: Pair<String, Any?>,
    callback: ActivityResultCallback<R>
) = launch(
    Intent(context, T::class.java).putExtras(*values), callback
)

inline fun <reified T, R> PocketActivityResultLauncher<Intent, R>.launch(
    context: Context,
    options: ActivityOptionsCompat?,
    vararg values: Pair<String, Any?>,
    callback: ActivityResultCallback<R>
) = launch(
    Intent(context, T::class.java).putExtras(*values), options, callback
)