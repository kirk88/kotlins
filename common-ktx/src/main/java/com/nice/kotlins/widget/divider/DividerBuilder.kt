@file:Suppress("unused")

package com.nice.kotlins.widget.divider

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.nice.kotlins.widget.divider.DividerSideLine.Companion.NO_PADDING

class DividerBuilder {

    private var leftSideLine: DividerSideLine? = null
    private var topSideLine: DividerSideLine? = null
    private var rightSideLine: DividerSideLine? = null
    private var bottomSideLine: DividerSideLine? = null

    fun left(sideLine: DividerSideLine): DividerBuilder = apply {
        leftSideLine = sideLine
    }

    fun left(
            @ColorInt color: Int,
            @Px size: Int,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        leftSideLine = ColorDividerSideLine(color, size, offset, paddingStart, paddingEnd, visible)
    }

    fun left(
            drawable: Drawable,
            @Px size: Int = drawable.intrinsicWidth,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        leftSideLine = DrawableDividerSideLine(drawable, size, offset, paddingStart, paddingEnd, visible)
    }


    fun left(
            @Px offset: Int
    ): DividerBuilder = apply {
        leftSideLine = DividerSideLine(offset = offset)
    }

    fun top(sideLine: DividerSideLine) = apply {
        topSideLine = sideLine
    }

    fun top(
            @ColorInt color: Int,
            @Px size: Int,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        topSideLine = ColorDividerSideLine(color, size, offset, paddingStart, paddingEnd, visible)
    }

    fun top(
            drawable: Drawable,
            @Px size: Int = drawable.intrinsicHeight,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        topSideLine = DrawableDividerSideLine(drawable, size, offset, paddingStart, paddingEnd, visible)
    }

    fun top(@Px offset: Int) = apply {
        topSideLine = DividerSideLine(offset = offset)
    }

    fun right(sideLine: DividerSideLine) = apply {
        rightSideLine = sideLine
    }

    fun right(
            @ColorInt color: Int,
            @Px size: Int,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        rightSideLine = ColorDividerSideLine(color, size, offset, paddingStart, paddingEnd, visible)
    }

    fun right(
            drawable: Drawable,
            @Px size: Int = drawable.intrinsicWidth,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        rightSideLine = DrawableDividerSideLine(drawable, size, offset, paddingStart, paddingEnd, visible)
    }

    fun right(@Px offset: Int) = apply {
        rightSideLine = DividerSideLine(offset = offset)
    }

    fun bottom(sideLine: DividerSideLine) = apply {
        bottomSideLine = sideLine
    }

    fun bottom(
            @ColorInt color: Int,
            @Px size: Int,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        bottomSideLine = ColorDividerSideLine(color, size, offset, paddingStart, paddingEnd, visible)
    }

    fun bottom(
            drawable: Drawable,
            @Px size: Int = drawable.intrinsicHeight,
            @Px offset: Int = size,
            @Px paddingStart: Int = NO_PADDING,
            @Px paddingEnd: Int = NO_PADDING,
            visible: Boolean = true
    ): DividerBuilder = apply {
        bottomSideLine = DrawableDividerSideLine(drawable, size, offset, paddingStart, paddingEnd, visible)
    }

    fun bottom(@Px offset: Int) = apply {
        bottomSideLine = DividerSideLine(offset = offset)
    }

    fun build(): Divider {
        return Divider(
                leftSideLine,
                topSideLine,
                rightSideLine,
                bottomSideLine
        )
    }

}