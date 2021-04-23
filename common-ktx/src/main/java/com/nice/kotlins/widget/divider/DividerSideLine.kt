package com.nice.kotlins.widget.divider

import android.graphics.drawable.Drawable
import androidx.annotation.Px

data class DividerSideLine(
    val drawable: Drawable,
    @Px val size: Int,
    @Px val offset: Int,
    @Px val startPadding: Int = 0,
    @Px val endPadding: Int = 0,
    val visible: Boolean = true,
)