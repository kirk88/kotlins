package com.nice.kotlins.widget.divider

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Px

class DividerBuilder {
    private var leftSideLine: DividerSideLine? = null
    private var topSideLine: DividerSideLine? = null
    private var rightSideLine: DividerSideLine? = null
    private var bottomSideLine: DividerSideLine? = null

    fun left(
        @ColorInt color: Int,
        @Px size: Int,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true,
    ): DividerBuilder {
        leftSideLine = DividerSideLine(color, size, startPadding, endPadding, visible)
        return this
    }

    fun top(
        @ColorInt color: Int,
        @Px size: Int,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean
    ): DividerBuilder {
        topSideLine = DividerSideLine(color, size, startPadding, endPadding, visible)
        return this
    }

    fun right(
        @ColorInt color: Int,
        @Px size: Int,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean,
    ): DividerBuilder {
        rightSideLine = DividerSideLine(color, size, startPadding, endPadding, visible)
        return this
    }

    fun bottom(
        @ColorInt color: Int,
        @Px size: Int,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean,
    ): DividerBuilder {
        bottomSideLine = DividerSideLine(color, size, startPadding, endPadding, visible)
        return this
    }

    fun create(): Divider {
        val defaultSideLine = DividerSideLine(Color.GRAY, 1, 0, 0, true)
        leftSideLine = if (leftSideLine != null) leftSideLine else defaultSideLine
        topSideLine = if (topSideLine != null) topSideLine else defaultSideLine
        rightSideLine = if (rightSideLine != null) rightSideLine else defaultSideLine
        bottomSideLine = if (bottomSideLine != null) bottomSideLine else defaultSideLine
        return Divider(leftSideLine!!, topSideLine!!, rightSideLine!!, bottomSideLine!!)
    }

}