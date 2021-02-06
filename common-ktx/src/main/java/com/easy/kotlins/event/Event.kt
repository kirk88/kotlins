@file:Suppress("unused")

package com.easy.kotlins.event

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.easy.kotlins.helper.opt
import com.easy.kotlins.helper.toBundle
import java.io.Serializable

/**
 * Create by LiZhanPing on 2020/9/12
 */

open class Event(val what: Int = Status.NONE, val message: String? = null) {
    private val extras: Bundle by lazy { Bundle() }

    private var intent: Intent? = null

    fun getString(key: String, defaultValue: String? = null): String? =
        extras.getString(key, defaultValue)
    fun getInt(key: String, defaultValue: Int = 0): Int = extras.getInt(key, defaultValue)
    fun getLong(key: String, defaultValue: Long = 0.toLong()): Long =
        extras.getLong(key, defaultValue)
    fun getFloat(key: String, defaultValue: Float = 0.toFloat()): Float =
        extras.getFloat(key, defaultValue)
    fun getDouble(key: String, defaultValue: Double = 0.toDouble()): Double =
        extras.getDouble(key, defaultValue)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        extras.getBoolean(key, defaultValue)
    fun <T : Parcelable> getParcelable(key: String): T? = extras.getParcelable<T>(key)
    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> getSerializable(key: String): T? = extras.getSerializable(key) as? T

    fun putString(key: String, value: String?) = extras.putString(key, value)
    fun putInt(key: String, value: Int) = extras.putInt(key, value)
    fun putLong(key: String, value: Long) = extras.putLong(key, value)
    fun putFloat(key: String, value: Float) = extras.putFloat(key, value)
    fun putDouble(key: String, value: Double) = extras.putDouble(key, value)
    fun putBoolean(key: String, value: Boolean) = extras.putBoolean(key, value)
    fun putParcelable(key: String, value: Parcelable?) = extras.putParcelable(key, value)
    fun putSerializable(key: String, value: Serializable?) = extras.putSerializable(key, value)
    fun putAll(bundle: Bundle) = extras.putAll(bundle)

    fun setIntent(intent: Intent) {
        this.intent = intent
    }

    fun getIntent(): Intent? {
        return this.intent
    }

    fun copy(what: Int = Status.NONE, message: String? = null): Event {
        return Event(what, message).also {
            it.putAll(extras)
        }
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

internal class EventCollection(val events: List<Event>) : Event()

object Status {

    private const val STATUS_BASE = 10000

    const val NONE = -1

    const val SHOW_PROGRESS = STATUS_BASE + 1
    const val DISMISS_PROGRESS = STATUS_BASE + 2
    const val REFRESH_COMPLETE = STATUS_BASE + 3
    const val LOADMORE_COMPLETE = STATUS_BASE + 4
    const val LOADMORE_COMPLETE_NO_MORE = STATUS_BASE + 5
    const val REFRESH_FAILURE = STATUS_BASE + 6
    const val LOADMORE_FAILURE = STATUS_BASE + 7

    const val SHOW_LOADING = STATUS_BASE + 8
    const val SHOW_EMPTY = STATUS_BASE + 9
    const val SHOW_ERROR = STATUS_BASE + 10
    const val SHOW_CONTENT = STATUS_BASE + 11

    const val FINISH_ACTIVITY = STATUS_BASE + 12

}

fun event(what: Int = Status.NONE, message: String? = null): Event = Event(what, message)

inline fun buildEvent(
    what: Int = Status.NONE,
    message: String? = null,
    crossinline init: Event.() -> Unit
) = Event(what, message).apply(init)

inline fun <reified T : Activity> activityLauncher(
    context: Context,
    vararg pairs: Pair<String, Any?>
) = buildEvent {
        setIntent(Intent(context, T::class.java).apply {
            putExtras(pairs.toBundle())
        })
    }

inline fun <reified T : Activity> activityLauncherForResult(
    context: Context,
    requestCode: Int,
    vararg pairs: Pair<String, Any?>
) = buildEvent(requestCode) {
    setIntent(Intent(context, T::class.java).apply {
        putExtras(pairs.toBundle())
    })
}

fun activityResult(vararg pairs: Pair<String, Any?>) = buildEvent {
    setIntent(Intent().apply {
        putExtras(pairs.toBundle())
    })
}

fun activityResult(code: Int, vararg pairs: Pair<String, Any?>) = buildEvent(code) {
    setIntent(Intent().apply {
        putExtras(pairs.toBundle())
    })
}

fun progressShower(message: String? = null): Event = Event(Status.SHOW_PROGRESS, message)

fun progressDismissal(): Event = Event(Status.DISMISS_PROGRESS)

fun refreshCompletion(): Event = Event(Status.REFRESH_COMPLETE)

fun loadMoreCompletion(hasMore: Boolean = true): Event =
    Event(hasMore.opt(Status.LOADMORE_COMPLETE, Status.LOADMORE_COMPLETE_NO_MORE))

fun refreshFailure(): Event = Event(Status.REFRESH_FAILURE)

fun loadMoreFailure(): Event = Event(Status.LOADMORE_FAILURE)

fun loadingShower(message: String? = null): Event = Event(Status.SHOW_LOADING, message)

fun emptyShower(message: String? = null): Event = Event(Status.SHOW_EMPTY, message)

fun errorShower(message: String? = null): Event = Event(Status.SHOW_ERROR, message)

fun contentShower(): Event = Event(Status.SHOW_CONTENT)

fun eventOf(events: List<Event>): Event = EventCollection(events = events)

fun eventOf(vararg events: Event): Event = EventCollection(events = events.toList())