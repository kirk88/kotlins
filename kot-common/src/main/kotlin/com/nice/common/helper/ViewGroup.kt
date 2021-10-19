package com.nice.common.helper

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = true): View {
    return layoutInflater.inflate(layoutId, this, attachToRoot)
}