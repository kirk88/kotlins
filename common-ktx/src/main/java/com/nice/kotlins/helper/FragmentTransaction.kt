package com.nice.kotlins.helper

import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

inline fun <reified F : Fragment> FragmentTransaction.add(
    @IdRes containerViewId: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
): FragmentTransaction = add(containerViewId, F::class.java, bundleOf(*args), tag)

inline fun <reified F : Fragment> FragmentTransaction.replace(
    @IdRes containerViewId: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
): FragmentTransaction = replace(containerViewId, F::class.java, bundleOf(*args), tag)