@file:Suppress("unused")

package com.nice.kotlins.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

fun intentOf(vararg values: Pair<String, Any?>) = Intent().putExtras(*values)

fun intentOf(action: String, vararg values: Pair<String, Any?>) = Intent(action).putExtras(*values)

inline fun <reified T> intentOf(context: Context, vararg values: Pair<String, Any?>) = Intent(context, T::class.java).putExtras(*values)

inline fun <reified T> intentOf(fragment: Fragment, vararg values: Pair<String, Any?>) = Intent(fragment.requireContext(), T::class.java).putExtras(*values)

inline fun <reified T> Context.intent(vararg values: Pair<String, Any?>) = Intent(this, T::class.java).putExtras(*values)

inline fun <reified T> Fragment.intent(vararg values: Pair<String, Any?>) = Intent(requireContext(), T::class.java).putExtras(*values)

inline fun <reified T : Activity> Context.startActivity(vararg values: Pair<String, Any?>) = startActivity(intent<T>(*values))

inline fun <reified T : Activity> Fragment.startActivity(vararg values: Pair<String, Any?>) = startActivity(intent<T>(*values))

inline fun <reified T : Activity> Context.startActivity(
        options: ActivityOptionsCompat?,
        vararg values: Pair<String, Any?>
) = startActivity(intent<T>(*values), options?.toBundle())

inline fun <reified T : Activity> Fragment.startActivity(
        options: ActivityOptionsCompat?,
        vararg values: Pair<String, Any?>
) = startActivity(intent<T>(*values), options?.toBundle())

fun Intent.putExtras(vararg values: Pair<String, Any?>) = apply {
    putExtras(bundleOf(*values))
}