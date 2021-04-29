@file:Suppress("unused")

package com.nice.kotlins.widget.divider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.nice.kotlins.R

abstract class XDividerItemDecoration : ItemDecoration() {

    private val dividerDrawable: ColorDrawable by lazy { ColorDrawable() }

    abstract fun getDivider(parent: RecyclerView, child: View, position: Int): Divider

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            val position = (child.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
            val divider = getDivider(parent, child, position)
            val leftSideLine = divider.leftSideLine
            if (leftSideLine != null && leftSideLine.visible) {
                drawChildLeftVertical(
                    child,
                    canvas,
                    leftSideLine
                )
            }

            val topSideLine = divider.topSideLine
            if (topSideLine != null && topSideLine.visible) {
                drawChildTopHorizontal(
                    child,
                    canvas,
                    topSideLine
                )
            }

            val rightSideLine = divider.rightSideLine
            if (rightSideLine != null && rightSideLine.visible) {
                drawChildRightVertical(
                    child,
                    canvas,
                    rightSideLine
                )
            }

            val bottomSideLine = divider.bottomSideLine
            if (bottomSideLine != null && bottomSideLine.visible) {
                drawChildBottomHorizontal(
                    child,
                    canvas,
                    bottomSideLine
                )
            }
        }
    }

    private fun drawChildLeftVertical(
        child: View,
        canvas: Canvas,
        sideLine: DividerSideLine
    ) {
        val drawable = getDividerDrawable(sideLine) ?: return
        val dividerSize = sideLine.size
        val startPadding = sideLine.startPadding
        val endPadding = sideLine.endPadding
        val topPadding: Int =
            if (startPadding == DividerSideLine.NO_PADDING) -dividerSize else startPadding
        val bottomPadding: Int =
            if (endPadding == DividerSideLine.NO_PADDING) dividerSize else -endPadding
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val right = child.left - params.leftMargin
        val left = right - dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun drawChildTopHorizontal(
        child: View,
        canvas: Canvas,
        sideLine: DividerSideLine
    ) {
        val drawable = getDividerDrawable(sideLine) ?: return
        val dividerSize = sideLine.size
        val startPadding = sideLine.startPadding
        val endPadding = sideLine.endPadding
        val leftPadding: Int =
            if (startPadding == DividerSideLine.NO_PADDING) -dividerSize else startPadding
        val rightPadding: Int =
            if (endPadding == DividerSideLine.NO_PADDING) dividerSize else -endPadding
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val bottom = child.top - params.topMargin
        val top = bottom - dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }


    private fun drawChildRightVertical(
        child: View,
        canvas: Canvas,
        sideLine: DividerSideLine
    ) {
        val drawable = getDividerDrawable(sideLine) ?: return
        val dividerSize = sideLine.size
        val startPadding = sideLine.startPadding
        val endPadding = sideLine.endPadding
        val topPadding: Int =
            if (startPadding == DividerSideLine.NO_PADDING) -dividerSize else startPadding
        val bottomPadding: Int =
            if (endPadding == DividerSideLine.NO_PADDING) dividerSize else -endPadding
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val left = child.right + params.rightMargin
        val right = left + dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun drawChildBottomHorizontal(
        child: View,
        canvas: Canvas,
        sideLine: DividerSideLine
    ) {
        val drawable = getDividerDrawable(sideLine) ?: return
        val dividerSize = sideLine.size
        val startPadding = sideLine.startPadding
        val endPadding = sideLine.endPadding
        val leftPadding: Int =
            if (startPadding == DividerSideLine.NO_PADDING) -dividerSize else startPadding
        val rightPadding: Int =
            if (endPadding == DividerSideLine.NO_PADDING) dividerSize else -endPadding
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val top = child.bottom + params.bottomMargin
        val bottom = top + dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun getDividerDrawable(sideLine: DividerSideLine): Drawable? = when (sideLine) {
        is ColorDividerSideLine -> dividerDrawable.also {
            it.color = sideLine.color
        }
        is DrawableDividerSideLine -> sideLine.drawable
        else -> null
    }

    override fun getItemOffsets(
        outRect: Rect,
        child: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = (child.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val divider = getDivider(
            parent,
            child,
            position
        )
        val leftSideLine = divider.leftSideLine
        val topSideLine = divider.topSideLine
        val rightSideLine = divider.rightSideLine
        val bottomSideLine = divider.bottomSideLine
        outRect.set(
            if (leftSideLine?.visible == true) leftSideLine.offset else 0,
            if (topSideLine?.visible == true) topSideLine.offset else 0,
            if (rightSideLine?.visible == true) rightSideLine.offset else 0,
            if (bottomSideLine?.visible == true) bottomSideLine.offset else 0
        )
    }

}

class LinearDividerItemDecoration : XDividerItemDecoration {

    private var orientation: Int = -1
    private var divider: Divider? = null

    private var dividerDrawable: Drawable?

    constructor() {
        dividerDrawable = null
    }

    constructor(drawable: Drawable) {
        dividerDrawable = drawable
    }

    constructor(@ColorInt dividerColor: Int, dividerSize: Int) {
        dividerDrawable = GradientDrawable().apply {
            setColor(dividerColor)
            setSize(dividerSize, dividerSize)
        }
    }

    override fun getDivider(parent: RecyclerView, child: View, position: Int): Divider {
        val layoutManager = parent.layoutManager
        check(layoutManager is LinearLayoutManager) {
            "LinearDividerItemDecoration only support the LinearLayoutManager"
        }
        val orientation = layoutManager.orientation
        if (divider != null && this.orientation == layoutManager.orientation) {
            return divider!!
        }
        this.orientation = orientation

        val builder = DividerBuilder()
        val dividerDrawable = getDividerDrawable(parent.context, orientation)
        if (dividerDrawable != null) {
            if (orientation == RecyclerView.VERTICAL) {
                builder.bottom(dividerDrawable)
            } else {
                builder.right(dividerDrawable)
            }
        }
        return builder.build().also {
            divider = it
        }
    }

    private fun getDividerDrawable(context: Context, attrs: IntArray): Drawable? {
        val ta = context.obtainStyledAttributes(attrs)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable
    }

    private fun getDividerDrawable(context: Context, orientation: Int): Drawable? {
        val dividerDrawable = if (orientation == RecyclerView.VERTICAL) {
            getDividerDrawable(context, HORIZONTAL_ATTRS)
        } else {
            getDividerDrawable(context, VERTICAL_ATTRS)
        }
        return dividerDrawable ?: getDividerDrawable(context, ATTRS)
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        private val VERTICAL_ATTRS = intArrayOf(R.attr.dividerVertical)
        private val HORIZONTAL_ATTRS = intArrayOf(R.attr.dividerHorizontal)
    }

}

class GridDividerItemDecoration : XDividerItemDecoration {

    private val dividerDrawable: Drawable

    constructor(drawable: Drawable) {
        dividerDrawable = drawable
    }

    constructor(@ColorInt dividerColor: Int, dividerSize: Int) {
        dividerDrawable = GradientDrawable().apply {
            setColor(dividerColor)
            setSize(dividerSize, dividerSize)
        }
    }

    override fun getDivider(parent: RecyclerView, child: View, position: Int): Divider {
        val layout = parent.layoutManager
        check(layout is GridLayoutManager) {
            "GridDividerItemDecoration only support the GridLayoutManager"
        }
        val itemCount = layout.itemCount
        val orientation = layout.orientation
        val spanCount = layout.spanCount

        val isFirstRow = position < spanCount
        val isFirstColumn = position % spanCount == 0
        val isLastRow = position % spanCount == spanCount - 1
        val isLastColumn = position >= itemCount - (itemCount % spanCount).let {
            if (it == 0) spanCount else it
        }

        val dividerSize = if (orientation == RecyclerView.VERTICAL) dividerDrawable.intrinsicHeight else dividerDrawable.intrinsicWidth
        val eachWidth: Int = (spanCount - 1) * dividerSize / spanCount

        val leftTop = position % spanCount * (dividerSize - eachWidth)
        val rightBottom = eachWidth - leftTop
        if (orientation == RecyclerView.VERTICAL) {
            return DividerBuilder()
                .left(leftTop)
                .right(
                    dividerDrawable,
                    offset = rightBottom,
                    paddingStart = if (isFirstRow) 0 else -dividerSize,
                    paddingEnd = if (!isLastColumn) -dividerSize else 0,
                    visible = !isLastRow
                )
                .bottom(
                    dividerDrawable,
                    paddingStart = if (isFirstColumn) 0 else -dividerSize,
                    paddingEnd = if (!isLastRow) -dividerSize else 0,
                    visible = !isLastColumn
                )
                .build()
        } else {
            return DividerBuilder()
                .top(leftTop)
                .right(
                    dividerDrawable,
                    paddingStart = if (isFirstColumn) 0 else -dividerSize,
                    paddingEnd = if (!isLastRow) -dividerSize else 0,
                    visible = !isLastColumn
                )
                .bottom(
                    dividerDrawable,
                    offset = rightBottom,
                    paddingStart = if (isFirstRow) 0 else -dividerSize,
                    paddingEnd = if (!isLastColumn) -dividerSize else 0,
                    visible = !isLastRow
                )
                .build()
        }
    }

}