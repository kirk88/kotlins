@file:Suppress("unused")

package com.nice.kotlins.widget.divider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

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

    private val divider: Divider

    constructor(context: Context) : this(context, VERTICAL)

    constructor(context: Context, orientation: Int) {
        val builder = DividerBuilder()
        if (orientation == VERTICAL) {
            val a = context.obtainStyledAttributes(HORIZONTAL_ATTRS)
            val drawable = a.getDrawable(0) ?: ColorDrawable(Color.TRANSPARENT)
            a.recycle()
            builder.bottom(drawable)
        } else {
            val a = context.obtainStyledAttributes(VERTICAL_ATTRS)
            val drawable = a.getDrawable(0) ?: ColorDrawable(Color.TRANSPARENT)
            a.recycle()
            builder.right(drawable)
        }
        divider = builder.build()
    }

    override fun getDivider(parent: RecyclerView, child: View, position: Int): Divider {
        return divider
    }

    companion object {

        private val VERTICAL_ATTRS = intArrayOf(android.R.attr.dividerVertical)
        private val HORIZONTAL_ATTRS = intArrayOf(android.R.attr.dividerHorizontal)

        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

    }

}

class GridDividerItemDecoration(
    @ColorInt private val dividerColor: Int,
    private val dividerSize: Int
) : XDividerItemDecoration() {

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
        val eachWidth: Int = (spanCount - 1) * dividerSize / spanCount

        val leftTop = position % spanCount * (dividerSize - eachWidth)
        val rightBottom = eachWidth - leftTop
        if (orientation == RecyclerView.VERTICAL) {
            return DividerBuilder()
                .left(leftTop)
                .right(
                    dividerColor,
                    dividerSize,
                    rightBottom,
                    paddingStart = if (isFirstRow) 0 else -dividerSize,
                    paddingEnd = if (!isLastColumn) -dividerSize else 0,
                    visible = !isLastRow
                )
                .bottom(
                    dividerColor,
                    dividerSize,
                    paddingStart = if (isFirstColumn) 0 else -dividerSize,
                    paddingEnd = if (!isLastRow) -dividerSize else 0,
                    visible = !isLastColumn
                )
                .build()
        } else {
            return DividerBuilder()
                .top(leftTop)
                .right(
                    dividerColor,
                    dividerSize,
                    paddingStart = if (isFirstColumn) 0 else -dividerSize,
                    paddingEnd = if (!isLastRow) -dividerSize else 0,
                    visible = !isLastColumn
                )
                .bottom(
                    dividerColor,
                    dividerSize,
                    rightBottom,
                    paddingStart = if (isFirstRow) 0 else -dividerSize,
                    paddingEnd = if (!isLastColumn) -dividerSize else 0,
                    visible = !isLastRow
                )
                .build()
        }
    }

}