package com.easy.kotlins.widget.divider

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

abstract class XDividerItemDecoration : ItemDecoration() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            val itemPosition = (child.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
            val divider = getDivider(parent, child, itemPosition)
            if (divider.leftSideLine.visible) {
                val lineSize = divider.leftSideLine.size
                val startPadding = divider.leftSideLine.startPadding
                val endPadding = divider.leftSideLine.endPadding
                drawChildLeftVertical(
                    child,
                    c,
                    divider.leftSideLine.color,
                    lineSize,
                    startPadding,
                    endPadding
                )
            }
            if (divider.topSideLine.visible) {
                val lineSize = divider.topSideLine.size
                val startPadding = divider.topSideLine.startPadding
                val endPadding = divider.topSideLine.endPadding
                drawChildTopHorizontal(
                    child,
                    c,
                    divider.topSideLine.color,
                    lineSize,
                    startPadding,
                    endPadding
                )
            }
            if (divider.rightSideLine.visible) {
                val lineSize = divider.rightSideLine.size
                val startPadding = divider.rightSideLine.startPadding
                val endPadding = divider.rightSideLine.endPadding
                drawChildRightVertical(
                    child,
                    c,
                    divider.rightSideLine.color,
                    lineSize,
                    startPadding,
                    endPadding
                )
            }
            if (divider.bottomSideLine.visible) {
                val lineSize = divider.bottomSideLine.size
                val startPadding = divider.bottomSideLine.startPadding
                val endPadding = divider.bottomSideLine.endPadding
                drawChildBottomHorizontal(
                    child,
                    c,
                    divider.bottomSideLine.color,
                    lineSize,
                    startPadding,
                    endPadding
                )
            }
        }
    }

    private fun drawChildBottomHorizontal(
        child: View,
        c: Canvas,
        @ColorInt color: Int,
        lineSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val leftPadding: Int = if (startPadding <= 0) {
            -lineSize
        } else {
            startPadding
        }
        val rightPadding: Int = if (endPadding <= 0) {
            lineSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val top = child.bottom + params.bottomMargin
        val bottom = top + lineSize
        paint.color = color
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    private fun drawChildTopHorizontal(
        child: View,
        c: Canvas,
        @ColorInt color: Int,
        lineSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val leftPadding: Int = if (startPadding <= 0) {
            -lineSize
        } else {
            startPadding
        }
        val rightPadding: Int = if (endPadding <= 0) {
            lineSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val left = child.left - params.leftMargin + leftPadding
        val right = child.right + params.rightMargin + rightPadding
        val bottom = child.top - params.topMargin
        val top = bottom - lineSize
        paint.color = color
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    private fun drawChildLeftVertical(
        child: View,
        c: Canvas,
        @ColorInt color: Int,
        lineSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val topPadding: Int = if (startPadding <= 0) {
            -lineSize
        } else {
            startPadding
        }
        val bottomPadding: Int = if (endPadding <= 0) {
            lineSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val right = child.left - params.leftMargin
        val left = right - lineSize
        paint.color = color
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    private fun drawChildRightVertical(
        child: View,
        c: Canvas,
        @ColorInt color: Int,
        lineSize: Int,
        startPadding: Int,
        endPadding: Int
    ) {
        val topPadding: Int = if (startPadding <= 0) {
            -lineSize
        } else {
            startPadding
        }
        val bottomPadding: Int = if (endPadding <= 0) {
            lineSize
        } else {
            -endPadding
        }
        val params = child
            .layoutParams as RecyclerView.LayoutParams
        val top = child.top - params.topMargin + topPadding
        val bottom = child.bottom + params.bottomMargin + bottomPadding
        val left = child.right + params.rightMargin
        val right = left + lineSize
        paint.color = color
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    override fun getItemOffsets(
        outRect: Rect,
        child: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val divider = getDivider(
            parent,
            child,
            (child.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        )
        val left = if (divider.leftSideLine.visible) divider.leftSideLine.size else 0
        val top = if (divider.topSideLine.visible) divider.topSideLine.size else 0
        val right = if (divider.rightSideLine.visible) divider.rightSideLine.size else 0
        val bottom = if (divider.bottomSideLine.visible) divider.bottomSideLine.size else 0
        outRect.set(left, top, right, bottom)
    }

    abstract fun getDivider(parent: RecyclerView, child: View, position: Int): Divider

}