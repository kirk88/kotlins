package com.easy.kotlins.event

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.easy.kotlins.helper.opt
import com.easy.kotlins.helper.toBundle
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Create by LiZhanPing on 2020/9/12
 */

open class Event(val what: Int = Status.NONE, val message: String? = null) : Parcelable {
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
    fun putAll(event: Event) = extras.putAll(event.extras)

    fun setIntent(intent: Intent) {
        this.intent = intent
    }

    fun getIntent(): Intent? {
        return this.intent
    }

    fun copy(what: Int = Status.NONE, message: String? = null): Event {
        return Event(what, message).also {
            it.putAll(this)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    ) {
        extras.putAll(parcel.readBundle(Bundle::class.java.classLoader))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(what)
        parcel.writeString(message)
        parcel.writeBundle(extras)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Event(what=$what, message=$message, extras=$extras)"
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

}

class MultipleEvent(what: Int = Status.NONE, val events: List<Event>) : Event(what) {

    fun copy(what: Int = Status.NONE, events: List<Event>): Event {
        return MultipleEvent(what, events).also {
            it.putAll(this)
        }
    }

    override fun toString(): String {
        return "MultipleEvent(what=$what, events=$events)"
    }
}

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
}

private val NO_EVENT = Event()

class LiveEventProxy {

    private val liveEvents: MutableMap<LifecycleOwner, SingleLiveEvent<Event>> by lazy { mutableMapOf() }

    private var latestEvent: Event = NO_EVENT

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Event {
        return latestEvent
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Event) {
        latestEvent = value

        liveEvents.toMap().forEach {
            LiveDataPoster.post(it.value, value)
        }
    }

    fun observe(owner: LifecycleOwner, observer: Observer<Event>) {
        liveEvents.getOrPut(owner) { SingleLiveEvent() }.observe(owner, observer)
    }

}

fun event(what: Int = Status.NONE, message: String? = null) = Event(what, message)

inline fun buildEvent(
    what: Int = Status.NONE,
    message: String? = null,
    crossinline init: Event.() -> Unit
) = Event(what, message).apply(init)

inline fun <reified T : Activity> intentEvent(context: Context, vararg pairs: Pair<String, Any?>) =
    buildEvent {
        setIntent(Intent(context, T::class.java).apply {
            putExtras(pairs.toBundle())
        })
    }

inline fun <reified T : Activity> intentEventForResult(
    context: Context,
    requestCode: Int,
    vararg pairs: Pair<String, Any?>
) = buildEvent(requestCode) {
        setIntent(Intent(context, T::class.java).apply {
            putExtras(pairs.toBundle())
        })
    }

fun progressShow(message: String? = null): Event = Event(Status.SHOW_PROGRESS, message)

fun progressDismiss(): Event = Event(Status.DISMISS_PROGRESS)

fun refreshCompleted(): Event = Event(Status.REFRESH_COMPLETE)

fun loadMoreCompleted(hasMore: Boolean = true): Event =
    Event(hasMore.opt(Status.LOADMORE_COMPLETE, Status.LOADMORE_COMPLETE_NO_MORE))

fun refreshFailed(): Event = Event(Status.REFRESH_FAILURE)

fun loadMoreFailed(): Event = Event(Status.LOADMORE_FAILURE)

fun loadingShow(message: String? = null) = Event(Status.SHOW_LOADING, message)

fun emptyShow(message: String? = null) = Event(Status.SHOW_EMPTY, message)

fun errorShow(message: String? = null) = Event(Status.SHOW_ERROR, message)

fun contentShow() = Event(Status.SHOW_CONTENT)

fun multipleEvent(what: Int, events: List<Event>) = MultipleEvent(what, events)

fun multipleEvent(events: List<Event>) = MultipleEvent(events = events)

fun multipleEvent(what: Int = Status.NONE, vararg events: Event) =
    MultipleEvent(what, events.toList())

fun multipleEvent(vararg events: Event) = MultipleEvent(events = events.toList())

interface EventObservableView : LifecycleOwner {

    fun onEventChanged(event: Event)

}