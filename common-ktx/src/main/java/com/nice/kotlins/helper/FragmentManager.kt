@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

fun FragmentManager.createFragment(
    classLoader: ClassLoader,
    className: String,
    args: Bundle? = null,
): Fragment = fragmentFactory.instantiate(classLoader, className).apply {
    arguments = args
}

fun FragmentManager.loadFragment(
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    args: () -> Bundle? = { null },
): Fragment {
    val fragmentTag = tag ?: className
    val fragment = findFragmentByTag(fragmentTag)
    if (fragment != null) {
        return fragment
    }
    return createFragment(classLoader, className, args())
}

fun FragmentManager.loadFragment(
    context: Context,
    className: String,
    tag: String? = null,
    args: () -> Bundle? = { null },
): Fragment = loadFragment(context.classLoader, className, tag, args)

fun <T : Fragment> FragmentManager.loadFragment(
    fragmentClass: Class<T>,
    tag: String? = null,
    args: () -> Bundle? = { null },
): T {
    val classLoader = fragmentClass.classLoader
    check(classLoader != null) {
        "Can not instantiate fragment for class: $fragmentClass"
    }
    @Suppress("UNCHECKED_CAST")
    return loadFragment(classLoader, fragmentClass.name, tag, args) as T
}

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
) {
    beginTransaction().run {
        setCustomAnimations(enter, exit)

        for (existingFragment in fragments) {
            if (existingFragment == fragment ||
                (existingFragment.isAdded && existingFragment.id != containerViewId)
            ) continue

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
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null },
): Fragment = loadFragment(classLoader, className, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, enter, exit, allowingStateLoss)
}

fun FragmentManager.show(
    @IdRes containerViewId: Int,
    context: Context,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null },
): Fragment = show(
    containerViewId,
    context.classLoader,
    className,
    tag,
    enter,
    exit,
    allowingStateLoss,
    args
)

fun <T : Fragment> FragmentManager.show(
    @IdRes containerViewId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null },
): T = loadFragment(fragmentClass, tag, args).also { fragment ->
    show(containerViewId, fragment, tag, enter, exit, allowingStateLoss)
}

inline fun <reified T : Fragment> FragmentManager.show(
    @IdRes containerViewId: Int,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    noinline args: () -> Bundle? = { null },
): T = show(containerViewId, T::class.java, tag, enter, exit, allowingStateLoss, args)

fun FragmentManager.hide(
    fragment: Fragment,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
) {
    if (!fragments.contains(fragment)) {
        return
    }
    beginTransaction().run {
        setCustomAnimations(enter, exit)

        hide(fragment)

        if (allowingStateLoss) {
            commitAllowingStateLoss()
        } else {
            commit()
        }
    }
}

fun <T : Fragment> FragmentManager.hide(
    tag: String,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
) {
    val fragment = findFragmentByTag(tag) ?: return
    hide(fragment, enter, exit, allowingStateLoss)
}


fun FragmentManager.add(
    @IdRes containerViewId: Int,
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): Fragment = createFragment(classLoader, className, args).also {
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)
        add(containerViewId, it, tag)
    }
}

fun FragmentManager.replace(
    @IdRes containerViewId: Int,
    classLoader: ClassLoader,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): Fragment = createFragment(classLoader, className, args).also {
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)
        replace(containerViewId, it, tag)
    }
}

fun FragmentManager.add(
    @IdRes containerViewId: Int,
    context: Context,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): Fragment = add(
    containerViewId,
    context.classLoader,
    className,
    tag,
    enter,
    exit,
    allowingStateLoss,
    args
)

fun FragmentManager.replace(
    @IdRes containerViewId: Int,
    context: Context,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): Fragment = replace(
    containerViewId,
    context.classLoader,
    className,
    tag,
    enter,
    exit,
    allowingStateLoss,
    args
)

fun <T : Fragment> FragmentManager.add(
    @IdRes containerViewId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): T {
    val classLoader = fragmentClass.classLoader
    check(classLoader != null) {
        "Can not instantiate fragment for class: $fragmentClass"
    }
    @Suppress("UNCHECKED_CAST")
    return add(
        containerViewId,
        classLoader,
        fragmentClass.name,
        tag,
        enter,
        exit,
        allowingStateLoss,
        args
    ) as T
}

fun <T : Fragment> FragmentManager.replace(
    @IdRes containerViewId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: Bundle? = null,
): T {
    val classLoader = fragmentClass.classLoader
    check(classLoader != null) {
        "Can not instantiate fragment for class: $fragmentClass"
    }
    @Suppress("UNCHECKED_CAST")
    return replace(
        containerViewId,
        classLoader,
        fragmentClass.name,
        tag,
        enter,
        exit,
        allowingStateLoss,
        args
    ) as T
}