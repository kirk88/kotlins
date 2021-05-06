@file:Suppress("unused")

package com.nice.kotlins.event

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.nice.kotlins.helper.intent
import com.nice.kotlins.helper.intentOf
import com.nice.kotlins.helper.opt

open class Event(
    val what: Int = Status.NONE,
    val message: CharSequence? = null
) {

    private val extras: MutableMap<String, Any?> by lazy { mutableMapOf() }

    internal var intent: Intent? = null
        private set
    internal var resultCode: Int = Activity.RESULT_OK
        private set
    internal var resultCallback: ActivityResultCallback<ActivityResult>? = null
        private set

    operator fun <T : Any> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return this.extras[key] as T?
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

        if (what != other.what) return false

        return true
    }

    override fun hashCode(): Int {
        return what
    }

}

fun <T : Any> Event.getValue(key: String): T = requireNotNull(get(key)) {
    "Key $key is missing in the event"
}

fun <T : Any?> Event.getOrDefault(key: String, defaultValue: T): T = get(key) ?: defaultValue

fun <T : Any?> Event.getOrElse(key: String, defaultValue: () -> T): T = get(key) ?: defaultValue()

operator fun Event.set(key: String, value: Any?) = put(key, value)

fun Event.putAll(vararg extras: Pair<String, Any?>) = putAll(extras.toMap())

class EventCollection(val events: List<Event>) : Event()

object Status {

    private const val STATUS_BASE = 10000

    const val NONE = -1

    const val SHOW_PROGRESS = STATUS_BASE + 1
    const val DISMISS_PROGRESS = STATUS_BASE + 2
    const val REFRESH_SUCCESS = STATUS_BASE + 3
    const val LOADMORE_SUCCESS = STATUS_BASE + 4
    const val LOADMORE_SUCCESS_NO_MORE = STATUS_BASE + 5
    const val REFRESH_FAILURE = STATUS_BASE + 6
    const val LOADMORE_FAILURE = STATUS_BASE + 7

    const val SHOW_LOADING = STATUS_BASE + 8
    const val SHOW_EMPTY = STATUS_BASE + 9
    const val SHOW_ERROR = STATUS_BASE + 10
    const val SHOW_CONTENT = STATUS_BASE + 11

    const val ACTIVITY_FINISH = STATUS_BASE + 12
    const val ACTIVITY_START = STATUS_BASE + 13
    const val ACTIVITY_RESULT = STATUS_BASE + 14

    fun isStatus(value: Int): Boolean = value > STATUS_BASE && value < STATUS_BASE + 15

}

fun event(what: Int): Event = Event(what)

fun event(message: CharSequence): Event = Event(message = message)

fun event(what: Int, message: CharSequence): Event = Event(what, message)

fun event(events: List<Event>): Event = EventCollection(events = events)

fun event(vararg events: Event): Event = EventCollection(events = events.toList())

inline fun buildEvent(
    what: Int = Status.NONE,
    message: CharSequence? = null,
    crossinline init: Event.() -> Unit
) = Event(what, message).apply(init)

inline fun <reified A : Activity> activityStart(
    context: Context,
    vararg pairs: Pair<String, Any?>
) = buildEvent(Status.ACTIVITY_START) {
    setIntent(context.intent<A>(*pairs))
}

inline fun <reified A : Activity> activityStartForResult(
    context: Context,
    vararg pairs: Pair<String, Any?>,
    callback: ActivityResultCallback<ActivityResult>
) = buildEvent((Status.ACTIVITY_START)) {
    setIntent(context.intent<A>(*pairs), callback)
}

fun activityReturnResult(resultCode: Int) = buildEvent(Status.ACTIVITY_RESULT) {
    setResult(resultCode)
}

fun activityReturnResult(resultCode: Int, vararg pairs: Pair<String, Any?>) =
    buildEvent(Status.ACTIVITY_RESULT) {
        setResult(resultCode, intentOf(*pairs))
    }

fun activityFinish(): Event = event(Status.ACTIVITY_FINISH)

fun progressShow(message: CharSequence? = null): Event = Event(Status.SHOW_PROGRESS, message)

fun progressDismiss(): Event = Event(Status.DISMISS_PROGRESS)

fun refreshSuccess(): Event = Event(Status.REFRESH_SUCCESS)

fun refreshFailure(): Event = Event(Status.REFRESH_FAILURE)

fun loadMoreSuccess(hasMore: Boolean = true): Event =
    Event(hasMore.opt(Status.LOADMORE_SUCCESS, Status.LOADMORE_SUCCESS_NO_MORE))

fun loadMoreFailure(): Event = Event(Status.LOADMORE_FAILURE)

fun loadingShow(message: CharSequence? = null): Event = Event(Status.SHOW_LOADING, message)

fun emptyShow(message: CharSequence? = null): Event = Event(Status.SHOW_EMPTY, message)

fun errorShow(message: CharSequence? = null): Event = Event(Status.SHOW_ERROR, message)

fun contentShow(): Event = Event(Status.SHOW_CONTENT)