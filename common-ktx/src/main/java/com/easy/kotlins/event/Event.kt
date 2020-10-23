package com.easy.kotlins.event

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.easy.kotlins.helper.opt
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Create by LiZhanPing on 2020/9/12
 */

open class Event(val what: Int = 0, val message: String? = null) : Parcelable {
    private val extras: Bundle by lazy { Bundle() }

    fun getString(key: String, defaultValue: String? = null): String? = extras.getString(key, defaultValue)
    fun getInt(key: String, defaultValue: Int = 0): Int = extras.getInt(key, defaultValue)
    fun getLong(key: String, defaultValue: Long = 0.toLong()): Long = extras.getLong(key, defaultValue)
    fun getFloat(key: String, defaultValue: Float = 0.toFloat()): Float = extras.getFloat(key, defaultValue)
    fun getDouble(key: String, defaultValue: Double = 0.toDouble()): Double = extras.getDouble(key, defaultValue)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = extras.getBoolean(key, defaultValue)
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

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
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

}

class LiveEventProxy(initializer: () -> SingleLiveEvent<Event>) {

    private val liveEvent: SingleLiveEvent<Event> by lazy { initializer() }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Event? {
        return liveEvent.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Event?) {
        LiveDataPoster.post(liveEvent, value)
    }

    fun observe(owner: LifecycleOwner, observer: Observer<Event>) {
        liveEvent.observe(owner, observer)
    }
}

fun event(what: Int = 0, message: String? = null) = Event(what, message)

inline fun buildEvent(what: Int = 0, message: String? = null, crossinline init: Event.() -> Unit) = Event(what, message).apply(init)

fun progressShow(message: String? = null): Event = Event(Status.SHOW_PROGRESS, message)

fun progressDismiss(): Event = Event(Status.DISMISS_PROGRESS)

fun refreshCompleted(): Event = Event(Status.REFRESH_COMPLETE)

fun loadMoreCompleted(hasMore: Boolean = true): Event = Event(hasMore.opt(Status.LOADMORE_COMPLETE, Status.LOADMORE_COMPLETE_NO_MORE))

fun refreshFailed(): Event = Event(Status.REFRESH_FAILURE)

fun loadMoreFailed(): Event = Event(Status.LOADMORE_FAILURE)


interface EventObservableView : LifecycleOwner {

    fun onEventChanged(event: Event)

}