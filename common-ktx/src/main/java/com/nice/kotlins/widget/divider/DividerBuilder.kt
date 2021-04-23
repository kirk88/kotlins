@file:Suppress("unused")

package com.nice.kotlins.widget.divider

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px

class DividerBuilder {

    private var leftSideLine: DividerSideLine? = null
    private var topSideLine: DividerSideLine? = null
    private var rightSideLine: DividerSideLine? = null
    private var bottomSideLine: DividerSideLine? = null

    private val orientation: Int
    private val drawable: Drawable

    constructor() : this(VERTICAL)

    constructor(context: Context): this(context, VERTICAL)

    constructor(orientation: Int) {
        this.orientation = orientation
        drawable = ColorDrawable(Color.TRANSPARENT)
    }

    constructor(context: Context, orientation: Int) {
        this.orientation = orientation
        val a = context.obtainStyledAttributes(ATTRS)
        drawable = a.getDrawable(0) ?: ColorDrawable(Color.TRANSPARENT)
        a.recycle()
    }

    fun left(sideLine: DividerSideLine): DividerBuilder = apply {
        leftSideLine = sideLine
    }

    fun left(
        @ColorInt color: Int,
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        leftSideLine =
            DividerSideLine(ColorDrawable(color), size, offset, startPadding, endPadding, visible)
    }

    fun left(
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        leftSideLine =
            DividerSideLine(
                drawable,
                size,
                offset,
                startPadding,
                endPadding,
                visible
            )
    }

    fun left(
        drawable: Drawable,
        @Px size: Int = drawable.intrinsicWidth,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        leftSideLine =
            DividerSideLine(drawable, size, offset, startPadding, endPadding, visible)
    }

    fun top(sideLine: DividerSideLine) = apply {
        topSideLine = sideLine
    }

    fun top(
        @ColorInt color: Int,
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        topSideLine =
            DividerSideLine(ColorDrawable(color), size, offset, startPadding, endPadding, visible)
    }

    fun top(
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        topSideLine =
            DividerSideLine(
                drawable,
                size,
                offset,
                startPadding,
                endPadding,
                visible
            )
    }

    fun top(
        drawable: Drawable,
        @Px size: Int = drawable.intrinsicHeight,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        topSideLine =
            DividerSideLine(drawable, size, offset, startPadding, endPadding, visible)
    }

    fun right(sideLine: DividerSideLine) = apply {
        rightSideLine = sideLine
    }

    fun right(
        @ColorInt color: Int,
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        rightSideLine =
            DividerSideLine(ColorDrawable(color), size, offset, startPadding, endPadding, visible)
    }

    fun right(
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        rightSideLine =
            DividerSideLine(
                drawable,
                size,
                offset,
                startPadding,
                endPadding,
                visible
            )
    }

    fun right(
        drawable: Drawable,
        @Px size: Int = drawable.intrinsicWidth,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        rightSideLine =
            DividerSideLine(drawable, size, offset, startPadding, endPadding, visible)
    }

    fun bottom(sideLine: DividerSideLine) = apply {
        bottomSideLine = sideLine
    }

    fun bottom(
        @ColorInt color: Int,
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        bottomSideLine =
            DividerSideLine(ColorDrawable(color), size, offset, startPadding, endPadding, visible)
    }

    fun bottom(
        @Px size: Int,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        bottomSideLine =
            DividerSideLine(
                drawable,
                size,
                offset,
                startPadding,
                endPadding,
                visible
            )
    }

    fun bottom(
        drawable: Drawable,
        @Px size: Int = drawable.intrinsicHeight,
        @Px offset: Int = size,
        @Px startPadding: Int = 0,
        @Px endPadding: Int = 0,
        visible: Boolean = true
    ): DividerBuilder = apply {
        bottomSideLine =
            DividerSideLine(drawable, size, offset, startPadding, endPadding, visible)
    }

    fun build(): Divider {
        val vSideLine = if (orientation == HORIZONTAL) DividerSideLine(
            drawable,
            drawable.intrinsicWidth,
            drawable.intrinsicWidth
        ) else null
        val hSideLine = if (orientation == VERTICAL) DividerSideLine(
            drawable,
            drawable.intrinsicHeight,
            drawable.intrinsicHeight
        ) else null
        return Divider(
            leftSideLine,
            topSideLine,
            rightSideLine ?: vSideLine,
            bottomSideLine ?: hSideLine
        )
    }

    companion object {

        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

        private val ATTRS = intArrayOf(android.R.attr.listDivider)

    }

}