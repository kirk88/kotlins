package com.easy.kotlins.helper

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * Create by LiZhanPing on 2020/8/29
 */

inline fun FragmentManager.commit(allowingStateLoss: Boolean = false, crossinline body: FragmentTransaction.() -> FragmentTransaction): Int {
    return beginTransaction().body().let {
        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }
    }
}

inline fun <reified T : Fragment> FragmentManager.add(@IdRes id: Int, vararg args: Pair<String, Any?>, tag: String? = null, allowingStateLoss: Boolean = false): Fragment {
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

inline fun <reified T : Fragment> FragmentManager.replace(@IdRes id: Int, vararg args: Pair<String, Any?>, tag: String? = null, allowingStateLoss: Boolean = false): Fragment {
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


inline fun FragmentManager.commitNow(allowingStateLoss: Boolean = false, crossinline body: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().body().let {
        if (allowingStateLoss) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
    }
}

inline fun <reified T : Fragment> FragmentManager.addNow(@IdRes id: Int, vararg args: Pair<String, Any?>, tag: String? = null, allowingStateLoss: Boolean = false): Fragment {
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

inline fun <reified T : Fragment> FragmentManager.replaceNow(@IdRes id: Int, vararg args: Pair<String, Any?>, tag: String? = null, allowingStateLoss: Boolean = false): Fragment {
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

inline fun <reified T : Fragment> FragmentManager.show(@IdRes id: Int, tag: String? = null, allowingStateLoss: Boolean = false, args: Bundle.() -> Unit = { }): Fragment {
    return beginTransaction().let {
        val fragmentTag = tag ?: T::class.simpleName

        val fragment = findFragmentByTag(fragmentTag) ?: T::class.java.newInstance().apply {
            arguments = Bundle().apply(args)
        }

        for (existedFragment in fragments) {
            if (existedFragment.id != id) continue

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
        fragment
    }
}

/**
 * hide fragment which showed by @see [show]
 */
inline fun <reified T : Fragment> FragmentManager.hide(@IdRes id: Int, tag: String? = null, allowingStateLoss: Boolean = false): Fragment? {
    for (existedFragment in fragments) {
        if (existedFragment.id == id) break
        return null
    }
    val fragment = findFragmentByTag(tag ?: T::class.simpleName) ?: return null
    return beginTransaction().let {

        it.hide(fragment)

        if (allowingStateLoss) {
            it.commitAllowingStateLoss()
        } else {
            it.commit()
        }

        fragment
    }
}
