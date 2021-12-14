@file:Suppress("UNUSED", "UNCHECKED_CAST")

package com.nice.common.viewmodel

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.StringRes
import com.nice.common.applicationContext

sealed class Message {

    open val what: Int = -1

    internal val extras: MutableMap<String, Any?> by lazy { mutableMapOf() }

    operator fun <T : Any> get(key: String): T {
        return extras.getValue(key) as T
    }

    operator fun set(key: String, value: Any?) {
        extras[key] = value
    }

    class Tip(val text: CharSequence) : Message()

    class StartActivity(val intent: Intent) : Message()

    class StartActivityForResult(
        val intent: Intent,
        val callback: ActivityResultCallback<ActivityResult>
    ) : Message()

    class SetActivityResult(val resultCode: Int, val data: Intent?) : Message()

    class FinishActivity : Message()

    class ShowProgress(val text: CharSequence?) : Message()

    class DismissProgress : Message()

    class ShowLoading(val text: CharSequence?) : Message()

    class ShowEmpty(val text: CharSequence?) : Message()

    class ShowError(val text: CharSequence?) : Message()

    class ShowContent : Message()

    internal class Event(override val what: Int) : Message()

    internal class Batch(val messages: Set<Message>) : Message()

}

fun <T : Any> Message.getOrNull(key: String): T? = extras[key] as? T

fun <T : Any> Message.getOrDefault(key: String, defaultValue: T): T = getOrNull(key) ?: defaultValue

fun <T : Any> Message.getOrElse(key: String, defaultValue: () -> T): T = getOrNull(key) ?: defaultValue()

fun Message(what: Int): Message = Message.Event(what)
fun Message(vararg messages: Message): Message = Message.Batch(messages.toSet())

fun Tip(text: CharSequence) = Message.Tip(text)
fun Tip(@StringRes textId: Int) = Message.Tip(applicationContext.getText(textId))
fun StartActivity(intent: Intent) = Message.StartActivity(intent)
fun StartActivityForResult(intent: Intent, callback: ActivityResultCallback<ActivityResult>) =
    Message.StartActivityForResult(intent, callback)

fun SetActivityResult(resultCode: Int, data: Intent? = null) = Message.SetActivityResult(resultCode, data)
fun ShowProgress(text: CharSequence? = null) = Message.ShowProgress(text)
fun ShowProgress(@StringRes textId: Int) = Message.ShowProgress(applicationContext.getText(textId))
fun DismissProgress() = Message.DismissProgress()
fun ShowLoading(text: CharSequence? = null) = Message.ShowLoading(text)
fun ShowLoading(@StringRes textId: Int) = Message.ShowLoading(applicationContext.getText(textId))
fun ShowEmpty(text: CharSequence? = null) = Message.ShowEmpty(text)
fun ShowEmpty(@StringRes textId: Int) = Message.ShowEmpty(applicationContext.getText(textId))
fun ShowError(text: CharSequence? = null) = Message.ShowError(text)
fun ShowError(@StringRes textId: Int) = Message.ShowError(applicationContext.getText(textId))
fun ShowContent() = Message.ShowContent()