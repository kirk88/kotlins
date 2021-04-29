package com.nice.kotlins.widget.divider

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px

open class DividerSideLine(
    @Px val size: Int = 0,
    @Px val offset: Int = 0,
    @Px val startPadding: Int = NO_PADDING,
    @Px val endPadding: Int = NO_PADDING,
    val visible: Boolean = true
) {

    companion object {
        const val NO_PADDING = Int.MIN_VALUE
    }

}

class DrawableDividerSideLine(
    val drawable: Drawable? = null,
    @Px size: Int = 0,
    @Px offset: Int = 0,
    @Px startPadding: Int = 0,
    @Px endPadding: Int = 0,
    visible: Boolean = true
) : DividerSideLine(size, offset, startPadding, endPadding, visible)

class ColorDividerSideLine(
    @ColorInt val color: Int = 0,
    @Px size: Int = 0,
    @Px offset: Int = 0,
    @Px startPadding: Int = 0,
    @Px endPadding: Int = 0,
    visible: Boolean = true
) : DividerSideLine(size, offset, startPadding, endPadding, visible)
