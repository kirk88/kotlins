@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun FragmentManager.commit(
    allowingStateLoss: Boolean = false,
    crossinline body: FragmentTransaction.() -> FragmentTransaction
): Int {
    return beginTransaction().body().let {
        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

inline fun FragmentManager.commitNow(
    allowingStateLoss: Boolean = false,
    crossinline body: FragmentTransaction.() -> FragmentTransaction
) {
    beginTransaction().body().let {
        if (allowingStateLoss) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
    }
}

inline fun <reified T : Fragment> FragmentManager.add(
    @IdRes id: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Fragment {
    return beginTransaction().let {
        val fragment = T::class.java.newInstance().withBundle(*args)
        it.add(id, fragment, tag)
        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
        fragment
    }
}

inline fun <reified T : Fragment> FragmentManager.addNow(
    @IdRes id: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Fragment {
    return beginTransaction().let {
        val fragment = T::class.java.newInstance().withBundle(*args)
        it.add(id, fragment, tag)
        if (allowingStateLoss) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
        fragment
    }
}

inline fun <reified T : Fragment> FragmentManager.replace(
    @IdRes id: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Fragment {
    return beginTransaction().let {
        val fragment = T::class.java.newInstance().withBundle(*args)
        it.replace(id, fragment, tag)
        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
        fragment
    }
}


inline fun <reified T : Fragment> FragmentManager.replaceNow(
    @IdRes id: Int,
    vararg args: Pair<String, Any?>,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Fragment {
    return beginTransaction().let {
        val fragment = T::class.java.newInstance().withBundle(*args)
        it.replace(id, fragment, tag)
        if (allowingStateLoss) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
        fragment
    }
}

inline fun <reified T : Fragment> FragmentManager.show(
    @IdRes id: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false,
    args: Bundle.() -> Unit = { }
): Int {
    return beginTransaction().let {
        val fragmentClass = T::class.java
        val fragmentTag = tag ?: fragmentClass.canonicalName

        val fragment = findFragmentByTag(fragmentTag) ?: fragmentClass.newInstance().apply {
            arguments = Bundle().apply(args)
        }

        for (existedFragment in fragments) {
            if (existedFragment == fragment) continue

            it.hide(existedFragment)
        }

        if (fragment.isAdded) {
            it.show(fragment)
        } else {
            it.add(id, fragment, fragmentTag)
        }

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

fun FragmentManager.show(
    fragment: Fragment,
    @IdRes id: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Int {
    if (fragment.isAdded && fragment.id != id) {
        return -1
    }

    return beginTransaction().let {
        val fragmentTag = tag ?: fragment.javaClass.canonicalName

        for (existedFragment in fragments) {
            if (existedFragment == fragment) continue

            it.hide(existedFragment)
        }

        if (fragment.isAdded) {
            it.show(fragment)
        } else {
            it.add(id, fragment, fragmentTag)
        }

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

inline fun <reified T : Fragment> FragmentManager.hide(
    @IdRes id: Int,
    tag: String? = null,
    allowingStateLoss: Boolean = false
): Int {
    val fragmentClass = T::class.java
    val fragmentTag = tag ?: fragmentClass.canonicalName

    val fragment = findFragmentByTag(fragmentTag) ?: fragmentClass.newInstance()

    if (!fragment.isAdded || fragment.id != id) {
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

fun FragmentManager.hide(
    fragment: Fragment,
    @IdRes id: Int,
    allowingStateLoss: Boolean = false
): Int {
    if (!fragment.isAdded || fragment.id != id) {
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