package com.nice.kotlins.widget.divider

import androidx.annotation.ColorInt
import androidx.annotation.Px

data class DividerSideLine(
    @ColorInt val color: Int,
    @Px val size: Int,
    @Px val startPadding: Int = 0,
    @Px val endPadding: Int = 0,
    val visible: Boolean = true
)