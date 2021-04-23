@file:Suppress("unused")

package com.nice.kotlins.widget.divider

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

open class XDividerItemDecoration(private val divider: Divider? = null) : ItemDecoration() {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            val position = (child.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
            val divider = getDivider(parent, child, position)
            val leftSideLine = divider.leftSideLine
            if (leftSideLine != null && leftSideLine.visible) {
                val drawable = leftSideLine.drawable
                val dividerSize = leftSideLine.size
                val startPadding = leftSideLine.startPadding
                val endPadding = leftSideLine.endPadding
                drawChildLeftVertical(
                    child,
                    canvas,
                    drawable,
                    dividerSize,
                    startPadding,
                    endPadding
                )
            }

            val topSideLine = divider.topSideLine
            if (topSideLine != null && topSideLine.visible) {
                val drawable = topSideLine.drawable
                val dividerSize = topSideLine.size
                val startPadding = topSideLine.startPadding
                val endPadding = topSideLine.endPadding
                drawChildTopHorizontal(
                    child,
                    canvas,
                    drawable,
                    dividerSize,
                    startPadding,
                    endPadding
                )
            }

            val rightSideLine = divider.rightSideLine
            if (rightSideLine != null && rightSideLine.visible) {
                val drawable = rightSideLine.drawable
                val dividerSize = rightSideLine.size
                val startPadding = rightSideLine.startPadding
                val endPadding = rightSideLine.endPadding
                drawChildRightVertical(
                    child,
                    canvas,
                    drawable,
                    dividerSize,
                    startPadding,
                    endPadding
                )
            }

            val bottomSideLine = divider.bottomSideLine
            if (bottomSideLine != null && bottomSideLine.visible) {
                val drawable = bottomSideLine.drawable
                val dividerSize = bottomSideLine.size
                val startPadding = bottomSideLine.startPadding
                val endPadding = bottomSideLine.endPadding
                drawChildBottomHorizontal(
                    child,
                    canvas,
                    drawable,
                    dividerSize,
                    startPadding,
                    endPadding
                )
            }
        }
    }

    private fun drawChildBottomHorizontal(
        child: View,
        canvas: Canvas,
        drawable: Drawable,
        dividerSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val leftPadding: Int = if (startPadding <= 0) {
            -dividerSize
        } else {
            startPadding
        }
        val rightPadding: Int = if (endPadding <= 0) {
            dividerSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val top = child.bottom + params.bottomMargin
        val bottom = top + dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun drawChildTopHorizontal(
        child: View,
        canvas: Canvas,
        drawable: Drawable,
        dividerSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val leftPadding: Int = if (startPadding <= 0) {
            -dividerSize
        } else {
            startPadding
        }
        val rightPadding: Int = if (endPadding <= 0) {
            dividerSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val bottom = child.top - params.topMargin
        val top = bottom - dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun drawChildLeftVertical(
        child: View,
        canvas: Canvas,
        drawable: Drawable,
        dividerSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val topPadding: Int = if (startPadding <= 0) {
            -dividerSize
        } else {
            startPadding
        }
        val bottomPadding: Int = if (endPadding <= 0) {
            dividerSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val right = child.left - params.leftMargin
        val left = right - dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
    }

    private fun drawChildRightVertical(
        child: View,
        canvas: Canvas,
        drawable: Drawable,
        dividerSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val topPadding: Int = if (startPadding <= 0) {
            -dividerSize
        } else {
            startPadding
        }
        val bottomPadding: Int = if (endPadding <= 0) {
            dividerSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val left = child.right + params.rightMargin
        val right = left + dividerSize
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)
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
            if (leftSideLine != null && leftSideLine.visible) leftSideLine.size else 0,
            if (topSideLine != null && topSideLine.visible) topSideLine.size else 0,
            if (rightSideLine != null && rightSideLine.visible) rightSideLine.size else 0,
            if (bottomSideLine != null && bottomSideLine.visible) bottomSideLine.size else 0
        )
    }

    open fun getDivider(parent: RecyclerView, child: View, position: Int): Divider {
        val layout = parent.layoutManager as? LinearLayoutManager
        val orientation = layout?.orientation ?: DividerBuilder.VERTICAL
        return divider ?: DividerBuilder(parent.context, orientation).build()
    }

}