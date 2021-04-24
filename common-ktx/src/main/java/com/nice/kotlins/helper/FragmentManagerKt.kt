@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun <T : Fragment> FragmentManager.loadFragment(
    fragmentClass: Class<T>,
    tag: String? = null,
    args: Bundle.() -> Unit = { }
): Fragment {
    val fragmentTag = tag ?: fragmentClass.canonicalName
    val fragment = findFragmentByTag(fragmentTag)
    if (fragment != null) {
        return fragment
    }
    val classLoader = fragmentClass.classLoader
    val className = fragmentClass.canonicalName
    check(classLoader != null && className != null) {
        "Can not instantiate fragment for class: $fragmentClass"
    }
    return fragmentFactory.instantiate(classLoader, className).apply {
        arguments = Bundle().apply(args)
    }
}

fun FragmentManager.show(
    fragment: Fragment,
    @IdRes id: Int,
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
            it.add(id, fragment, tag ?: fragment.javaClass.canonicalName)
        }

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

fun <T : Fragment> FragmentManager.show(
    fragmentClass: Class<T>,
    @IdRes id: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    args: Bundle.() -> Unit = { }
): Fragment = loadFragment(fragmentClass, tag, args).also { fragment ->
    show(fragment, id, tag, allowingStateLoss)
}

inline fun <reified T : Fragment> FragmentManager.show(
    @IdRes id: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    noinline args: Bundle.() -> Unit = { }
): Fragment = show(T::class.java, id, tag, allowingStateLoss, args)

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
