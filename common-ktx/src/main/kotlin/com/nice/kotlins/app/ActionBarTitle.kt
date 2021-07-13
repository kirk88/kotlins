package com.nice.kotlins.app

import androidx.annotation.StringRes

interface HasActionBarTitle {

    fun setTitle(title: CharSequence?)

    fun setTitle(@StringRes resId: Int)

    fun getTitle(): CharSequence?

}

interface HasActionBarSubtitle {

    fun setSubtitle(subtitle: CharSequence?)

    fun setSubtitle(@StringRes resId: Int)

    fun getSubtitle(): CharSequence?

}

var HasActionBarTitle.title: CharSequence?
    get() = getTitle()
    set(title) {
        setTitle(title)
    }

var HasActionBarSubtitle.subtitle: CharSequence?
    get() = getSubtitle()
    set(subtitle) {
        setSubtitle(subtitle)
    }