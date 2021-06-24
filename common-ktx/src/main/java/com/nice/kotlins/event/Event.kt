@file:Suppress("unused")

package com.nice.kotlins.event

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.nice.kotlins.helper.intent
import com.nice.kotlins.helper.intentOf
import com.nice.kotlins.widget.LoadState


object EventCode {

    private const val CODE_BASE = 10000

    const val NONE = -1

    const val SHOW_PROGRESS = CODE_BASE + 1
    const val DISMISS_PROGRESS = CODE_BASE + 2
    const val REFRESH_STATE = CODE_BASE + 3
    const val LOADMORE_STATE = CODE_BASE + 4

    const val SHOW_LOADING = CODE_BASE + 5
    const val SHOW_EMPTY = CODE_BASE + 6
    const val SHOW_ERROR = CODE_BASE + 7
    const val SHOW_CONTENT = CODE_BASE + 8

    const val ACTIVITY_FINISH = CODE_BASE + 9
    const val ACTIVITY_START = CODE_BASE + 10
    const val ACTIVITY_RESULT = CODE_BASE + 11

    operator fun contains(code: Int): Boolean = code > CODE_BASE && code <= CODE_BASE + 11

}

open class Event(
    val code: Int = EventCode.NONE,
    val message: CharSequence? = null
) {

    private val extras: MutableMap<String, Any?> by lazy { mutableMapOf() }

    internal var intent: Intent? = null
        private set
    internal var resultCode: Int = Activity.RESULT_OK
        private set
    internal var resultCallback: ActivityResultCallback<ActivityResult>? = null
        private set

    operator fun <T : Any?> get(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return this.extras[key] as T
    }

    operator fun Event.set(key: String, value: Any?) {
        put(key, value)
    }

    fun put(key: String, value: Any?): Any? {
        return this.extras.put(key, value)
    }

    fun putAll(extras: Map<String, Any?>) {
        this.extras.putAll(extras)
    }

    fun setIntent(intent: Intent?, callback: ActivityResultCallback<ActivityResult>? = null) {
        this.intent = intent
        resultCallback = callback
    }

    fun setResult(resultCode: Int, data: Intent? = null) {
        this.resultCode = resultCode
        intent = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        return code
    }

    override fun toString(): String {
        return "Event(code=$code, message=$message)"
    }

}

class EventCollection(val events: List<Event>) : Event()

fun <T : Any?> Event.getOrDefault(key: String, defaultValue: T): T = get(key) ?: defaultValue

fun <T : Any?> Event.getOrElse(key: String, defaultValue: () -> T): T = get(key) ?: defaultValue()

fun Event.putAll(vararg extras: Pair<String, Any?>) = putAll(extras.toMap())

fun event(message: CharSequence): Event = Event(message = message)

fun event(code: Int, message: CharSequence? = null): Event = Event(code, message)

fun eventOf(vararg events: Event): Event = EventCollection(events.toList())

inline fun buildEvent(
    code: Int = EventCode.NONE,
    message: CharSequence? = null,
    crossinline init: Event.() -> Unit
) = Event(code, message).apply(init)

inline fun <reified A : Activity> activityStart(
    context: Context,
    vararg pairs: Pair<String, Any?>
) = buildEvent(EventCode.ACTIVITY_START) {
    setIntent(context.intent<A>(*pairs))
}

inline fun <reified A : Activity> activityStartForResult(
    context: Context,
    vararg pairs: Pair<String, Any?>,
    callback: ActivityResultCallback<ActivityResult>
) = buildEvent((EventCode.ACTIVITY_START)) {
    setIntent(context.intent<A>(*pairs), callback)
}

fun activityReturnResult(resultCode: Int) = buildEvent(EventCode.ACTIVITY_RESULT) {
    setResult(resultCode)
}

fun activityReturnResult(
    resultCode: Int,
    vararg pairs: Pair<String, Any?>
) = buildEvent(EventCode.ACTIVITY_RESULT) {
    setResult(resultCode, intentOf(*pairs))
}

fun activityFinish(): Event = event(EventCode.ACTIVITY_FINISH)

fun progressShow(message: CharSequence? = null): Event = Event(EventCode.SHOW_PROGRESS, message)

fun progressDismiss(): Event = Event(EventCode.DISMISS_PROGRESS)

fun refreshState(state: LoadState): Event = buildEvent(EventCode.REFRESH_STATE) {
    put("state", state)
}

fun loadMoreState(state: LoadState): Event = buildEvent(EventCode.LOADMORE_STATE) {
    put("state", state)
}

fun loadingShow(message: CharSequence? = null): Event = Event(EventCode.SHOW_LOADING, message)

fun emptyShow(message: CharSequence? = null): Event = Event(EventCode.SHOW_EMPTY, message)

fun errorShow(message: CharSequence? = null): Event = Event(EventCode.SHOW_ERROR, message)

fun contentShow(): Event = Event(EventCode.SHOW_CONTENT)