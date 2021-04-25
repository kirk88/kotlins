@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.loadFragment(
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    args: Bundle.() -> Unit = { }
): Fragment {
    val fragmentTag = tag ?: className
    val fragment = findFragmentByTag(fragmentTag)
    if (fragment != null) {
        return fragment
    }
    return fragmentFactory.instantiate(classLoader, className).apply {
        arguments = Bundle().apply(args)
    }
}

fun FragmentManager.loadFragment(
    context: Context,
    className: String,
    tag: String? = null,
    args: Bundle.() -> Unit = { }
): Fragment = loadFragment(context.classLoader, className, tag, args)

fun <T : Fragment> FragmentManager.loadFragment(
    fragmentClass: Class<T>,
    tag: String? = null,
    args: Bundle.() -> Unit = { }
): Fragment {
    val classLoader = fragmentClass.classLoader
    check(classLoader != null) {
        "Can not instantiate fragment for class: $fragmentClass"
    }
    return loadFragment(classLoader, fragmentClass.name, tag, args)
}

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Int {
    return beginTransaction().let {
        for (existedFragment in fragments) {
            if (existedFragment == fragment) continue

            it.hide(existedFragment)
        }

        if (fragment.isAdded) {
            it.show(fragment)
        } else {
            it.add(containerViewId, fragment, tag ?: fragment.javaClass.name)
        }

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    args: Bundle.() -> Unit = { }
): Fragment = loadFragment(classLoader, className, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, allowingStateLoss)
}

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    context: Context,
    className: String,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    args: Bundle.() -> Unit = { }
): Fragment = loadFragment(context.classLoader, className, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, allowingStateLoss)
}

fun <T : Fragment> FragmentManager.show(
    @IdRes containerViewId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    args: Bundle.() -> Unit = { }
): Fragment = loadFragment(fragmentClass, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, allowingStateLoss)
}

inline fun <reified T : Fragment> FragmentManager.show(
    @IdRes containerViewId: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    noinline args: Bundle.() -> Unit = { }
): Fragment = show(containerViewId, T::class.java, tag, allowingStateLoss, args)

fun FragmentManager.hide(
    fragment: Fragment,
    allowingStateLoss: Boolean = false
): Int {
    if (!fragments.contains(fragment)) {
        return -1
    }
    return beginTransaction().let {
        it.hide(fragment)

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

fun <T : Fragment> FragmentManager.hide(
    tag: String,
    allowingStateLoss: Boolean = false
): Int {
    val fragment = findFragmentByTag(tag) ?: return -1
    return hide(fragment, allowingStateLoss)
}
