@file:Suppress("UNUSED")

package com.nice.common.helper

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

interface FragmentProvider {

    fun get(className: String, tag: String? = null, args: () -> Bundle? = { null }): Fragment

}

private class DefaultFragmentProvider(
    private val manager: FragmentManager
) : FragmentProvider {

    override fun get(className: String, tag: String?, args: () -> Bundle?): Fragment {
        val fragmentTag = tag ?: className
        val fragment = manager.findFragmentByTag(fragmentTag)
        if (fragment != null) {
            return fragment
        }
        val classLoader = requireNotNull(Thread.currentThread().contextClassLoader) {
            "Can not create fragment on current thread: ${Thread.currentThread().name}"
        }
        return manager.fragmentFactory.instantiate(classLoader, className).apply {
            arguments = args()
        }
    }

}

val FragmentManager.fragmentProvider: FragmentProvider
    get() = DefaultFragmentProvider(this)

fun FragmentManager.loadFragment(
    className: String,
    tag: String? = null,
    args: () -> Bundle? = { null }
): Fragment = fragmentProvider.get(className, tag, args)

inline fun <reified T : Fragment> FragmentManager.loadFragment(
    tag: String? = null,
    noinline args: () -> Bundle? = { null }
): T = fragmentProvider.get(T::class.java.name, tag, args) as T

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false
) {
    beginTransaction().run {
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

        if (allowingStateLoss) {
            commitAllowingStateLoss()
        } else {
            commit()
        }
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
): Fragment = fragmentProvider.get(className, tag, args).also { fragment ->
    if (fragment.isAdded) {
        return@also
    }
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
    args: () -> Bundle? = { null }
): Fragment = fragmentProvider.get(className, tag, args).also { fragment ->
    if (fragment.isAdded) {
        return@also
    }
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
    noinline args: () -> Bundle? = { null }
): T = replace(containerViewId, T::class.java.name, tag, enter, exit, allowingStateLoss, args) as T