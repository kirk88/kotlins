@file:Suppress("UNUSED")

package com.nice.common.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

fun intentOf(vararg values: Pair<String, Any?>) = Intent().putExtras(*values)

fun intentOf(action: String, vararg values: Pair<String, Any?>) = Intent(action).putExtras(*values)

inline fun <reified T> intentOf(context: Context, vararg values: Pair<String, Any?>) =
    Intent(context, T::class.java).putExtras(*values)

inline fun <reified T : Activity> Context.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(intentOf<T>(this, *values))

inline fun <reified T : Activity> Fragment.startActivity(vararg values: Pair<String, Any?>) =
    startActivity(intentOf<T>(requireContext(), *values))

inline fun <reified T : Activity> Context.startActivity(
    options: ActivityOptionsCompat,
    vararg values: Pair<String, Any?>,
) = startActivity(intentOf<T>(this, *values), options.toBundle())

inline fun <reified T : Activity> Fragment.startActivity(
    options: ActivityOptionsCompat,
    vararg values: Pair<String, Any?>
) = startActivity(intentOf<T>(requireContext(), *values), options.toBundle())

fun Intent.putExtras(vararg values: Pair<String, Any?>) = apply {
    putExtras(bundleOf(*values))
}