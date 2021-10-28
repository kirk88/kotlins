@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.viewmodel

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.StringRes
import com.nice.common.applicationContext
import com.nice.common.widget.InfiniteState

sealed class Message(val what: Int = -1) {

    internal val extras: MutableMap<String, Any?> by lazy { mutableMapOf() }

    operator fun <T : Any> get(key: String): T {
        return this.extras.getValue(key) as T
    }

    operator fun Message.set(key: String, value: Any?) {
        extras[key] = value
    }

    class Event : Message {
        constructor(what: Int) : super(what)

        constructor(what: Int, vararg args: Pair<String, Any?>) : super(what) {
            for (arg in args) {
                set(arg.first, arg.second)
            }
        }
    }

    class Tip(val text: CharSequence) : Message() {
        constructor(@StringRes textId: Int) : this(applicationContext.getText(textId))
    }

    class StartActivity(val intent: Intent) : Message()

    class StartActivityForResult(
        val intent: Intent,
        val callback: ActivityResultCallback<ActivityResult>
    ) : Message()

    class SetActivityResult(val resultCode: Int, val data: Intent? = null) : Message()

    class FinishActivity : Message() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class ShowProgress(val text: CharSequence? = null) : Message()

    class DismissProgress : Message() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class RefreshState(val state: InfiniteState) : Message()

    class LoadMoreState(val state: InfiniteState) : Message()

    class ShowLoading(val text: CharSequence? = null) : Message()

    class ShowEmpty(val text: CharSequence? = null) : Message()

    class ShowError(val text: CharSequence? = null) : Message()

    class ShowContent : Message() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    class Batch(val messages: Set<Message>) : Message()

}

fun <T : Any> Message.getOrNull(key: String): T? = extras[key] as? T

fun <T : Any> Message.getOrDefault(key: String, defaultValue: T): T = getOrNull(key) ?: defaultValue

fun <T : Any> Message.getOrElse(key: String, defaultValue: () -> T): T = getOrNull(key) ?: defaultValue()