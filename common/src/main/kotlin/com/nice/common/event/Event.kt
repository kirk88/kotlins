@file:Suppress("unused")

package com.nice.common.event

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.nice.common.widget.InfiniteState

interface Event

sealed class Message : Event {

    private val extras: MutableMap<String, Any?> by lazy { mutableMapOf() }

    operator fun <T : Any> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return this.extras[key] as T?
    }

    operator fun Message.set(key: String, value: Any?) {
        extras[key] = value
    }

    class ShowToast(val text: CharSequence, val duration: Int = Toast.LENGTH_SHORT) : Message()

    class StartActivity(val intent: Intent) : Message()

    class StartActivityForResult(val intent: Intent, val callback: ActivityResultCallback<ActivityResult>) : Message()

    class SetActivityResult(val resultCode: Int, val data: Intent? = null) : Message()

    object FinishActivity : Message()

    class ShowProgress(val text: CharSequence? = null) : Message()

    object DismissProgress : Message()

    class RefreshState(val state: InfiniteState) : Message()

    class LoadMoreState(val state: InfiniteState) : Message()

    class ShowLoading(val text: CharSequence? = null) : Message()

    class ShowEmpty(val text: CharSequence? = null) : Message()

    class ShowError(val text: CharSequence? = null) : Message()

    object ShowContent : Message()

    class Batch(val messages: Set<Message>) : Message()

}

fun <T : Any> Message.getValue(key: String): T = requireNotNull(get(key))

fun <T : Any> Message.getOrDefault(key: String, defaultValue: T): T = get(key) ?: defaultValue

fun <T : Any> Message.getOrElse(key: String, defaultValue: () -> T): T = get(key) ?: defaultValue()