@file:Suppress("unused")

package com.nice.kotlins.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

fun intentOf(vararg values: Pair<String, Any?>) = Intent().putExtras(values.toBundle())

fun intentOf(action: String, vararg values: Pair<String, Any?>) =
    Intent(action).putExtras(values.toBundle())

inline fun <reified A> intentOf(context: Context, vararg values: Pair<String, Any?>) =
    Intent(context, A::class.java).putExtras(values.toBundle())

inline fun <reified A> intentOf(fragment: Fragment, vararg values: Pair<String, Any?>) =
    Intent(fragment.requireContext(), A::class.java).putExtras(values.toBundle())

inline fun <reified A> Context.intent(vararg values: Pair<String, Any?>) =
    Intent(this, A::class.java).putExtras(values.toBundle())

inline fun <reified A> Fragment.intent(vararg values: Pair<String, Any?>) =
    Intent(requireContext(), A::class.java).putExtras(values.toBundle())

inline fun <reified A : Activity> Context.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(Intent(this, A::class.java).putExtras(values.toBundle()))

inline fun <reified A : Activity> Fragment.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(Intent(requireContext(), A::class.java).putExtras(values.toBundle()))

fun Intent.putAll(vararg values: Pair<String, Any?>) {
    putExtras(values.toBundle())
}