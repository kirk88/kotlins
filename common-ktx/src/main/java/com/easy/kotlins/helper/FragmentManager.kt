package com.easy.kotlins.helper

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


inline fun FragmentManager.commitNow(allowingStateLoss: Boolean = false, crossinline body: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().body().let {
        if (allowingStateLoss) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
    }
}
