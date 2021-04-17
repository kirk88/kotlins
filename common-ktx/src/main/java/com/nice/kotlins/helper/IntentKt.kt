@file:Suppress("unused")

package com.nice.kotlins.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

fun intentOf(vararg values: Pair<String, Any?>) = Intent().putExtras(*values)

fun intentOf(action: String, vararg values: Pair<String, Any?>) =
    Intent(action).putExtras(*values)

inline fun <reified A> intentOf(context: Context, vararg values: Pair<String, Any?>) =
    Intent(context, A::class.java).putExtras(*values)

inline fun <reified A> intentOf(fragment: Fragment, vararg values: Pair<String, Any?>) =
    Intent(fragment.requireContext(), A::class.java).putExtras(*values)

inline fun <reified A> Context.intent(vararg values: Pair<String, Any?>) =
    Intent(this, A::class.java).putExtras(*values)

inline fun <reified A> Fragment.intent(vararg values: Pair<String, Any?>) =
    Intent(requireContext(), A::class.java).putExtras(*values)

inline fun <reified A : Activity> Context.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(intent<A>(*values))

inline fun <reified A : Activity> Fragment.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(intent<A>(*values))

inline fun <reified A : Activity> Context.startActivity(
    options: ActivityOptionsCompat?,
    vararg values: Pair<String, Any?>
) = startActivity(intent<A>(*values), options?.toBundle())

inline fun <reified A : Activity> Fragment.startActivity(
    options: ActivityOptionsCompat?,
    vararg values: Pair<String, Any?>
) = startActivity(intent<A>(*values), options?.toBundle())

fun Intent.putExtras(vararg values: Pair<String, Any?>) = apply {
    putExtras(bundleOf(*values))
}