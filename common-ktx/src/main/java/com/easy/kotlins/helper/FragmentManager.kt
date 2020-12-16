package com.easy.kotlins.helper

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
