@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

fun FragmentManager.createFragment(
    className: String,
    args: Bundle? = null
): Fragment {
    val classLoader = Thread.currentThread().contextClassLoader
    check(classLoader != null) { "Can not create fragment in current thread: ${Thread.currentThread().name}" }
    return fragmentFactory.instantiate(classLoader, className).apply {
        arguments = args
    }
}

inline fun <reified T : Fragment> FragmentManager.createFragment(
    args: Bundle? = null
): Fragment = createFragment(T::class.java.name, args)

fun FragmentManager.loadFragment(
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

inline fun <reified T : Fragment> FragmentManager.loadFragment(
    tag: String? = null,
    noinline args: () -> Bundle? = { null }
): T = loadFragment(T::class.java.name, tag, args) as T

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false
) = commit(allowingStateLoss) {
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

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null }
): Fragment = loadFragment(className, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, enter, exit, allowingStateLoss)
}

inline fun <reified T : Fragment> FragmentManager.show(
    @IdRes containerViewId: Int,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    noinline args: () -> Bundle? = { null }
): T = show(containerViewId, T::class.java.name, tag, enter, exit, allowingStateLoss, args) as T

fun FragmentManager.add(
    @IdRes containerViewId: Int,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null }
): Fragment = loadFragment(className, tag, args).also { fragment ->
    if (fragment.isAdded) return@also
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)
        add(containerViewId, fragment, tag ?: className)
    }
}

inline fun <reified T : Fragment> FragmentManager.add(
    @IdRes containerViewId: Int,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    noinline args: () -> Bundle? = { null }
): T = add(containerViewId, T::class.java.name, tag, enter, exit, allowingStateLoss, args) as T

fun FragmentManager.replace(
    @IdRes containerViewId: Int,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null
): Fragment = createFragment(className, args).also { fragment ->
    if (fragment.isAdded) return@also
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)
        replace(containerViewId, fragment, tag ?: className)
    }
}

inline fun <reified T : Fragment> FragmentManager.replace(
    @IdRes containerViewId: Int,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null
): T = replace(containerViewId, T::class.java.name, tag, enter, exit, allowingStateLoss, args) as T