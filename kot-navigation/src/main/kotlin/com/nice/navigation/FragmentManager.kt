@file:Suppress("UNUSED")

package com.nice.navigation

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

internal fun FragmentManager.createFragment(
    className: String,
    args: Bundle? = null
): Fragment {
    val classLoader = Thread.currentThread().contextClassLoader
    check(classLoader != null) { "Can not create fragment in current thread: ${Thread.currentThread().name}" }
    return fragmentFactory.instantiate(classLoader, className).apply {
        arguments = args
    }
}

internal fun FragmentManager.loadFragment(
    className: String,
    tag: String? = null,
    args: () -> Bundle? = { null }
): Fragment {
    val fragment = findFragmentByTag(tag ?: className)
    if (fragment != null) {
        return fragment
    }
    return createFragment(className, args())
}

internal fun FragmentManager.show(
    @IdRes containerViewId: Int,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null }
): Fragment = loadFragment(className, tag, args).also { fragment ->
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)

        for (existingFragment in fragments) {
            if (existingFragment == fragment || (existingFragment.isAdded && existingFragment.id != containerViewId)) {
                continue
            }

            hide(existingFragment)
        }

        if (fragment.isAdded) {
            show(fragment)
        } else {
            add(containerViewId, fragment, tag ?: fragment.javaClass.name)
        }
    }
}