package com.easy.kotlins.widget.divider;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class XDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;

    public XDividerItemDecoration() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        //left, top, right, bottom
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            int itemPosition = ((RecyclerView.LayoutParams) child.getLayoutParams()).getViewLayoutPosition();

            Divider divider = getDivider(parent, itemPosition);

            if (divider.getLeftSideLine().isVisible()) {
                int lineSize = divider.getLeftSideLine().getSize();
                int startPadding = divider.getLeftSideLine().getStartPadding();
                int endPadding = divider.getLeftSideLine().getEndPadding();
                drawChildLeftVertical(child, c, parent, divider.getLeftSideLine().getColor(), lineSize, startPadding, endPadding);
            }
            if (divider.getTopSideLine().isVisible()) {
                int lineSize = divider.getTopSideLine().getSize();
                int startPadding = divider.getTopSideLine().getStartPadding();
                int endPadding = divider.getTopSideLine().getEndPadding();
                drawChildTopHorizontal(child, c, parent, divider.topSideLine.getColor(), lineSize, startPadding, endPadding);
            }
            if (divider.getRightSideLine().isVisible()) {
                int lineSize = divider.getRightSideLine().getSize();
                int startPadding = divider.getRightSideLine().getStartPadding();
                int endPadding = divider.getRightSideLine().getEndPadding();
                drawChildRightVertical(child, c, parent, divider.getRightSideLine().getColor(), lineSize, startPadding, endPadding);
            }
            if (divider.getBottomSideLine().isVisible()) {
                int lineSize = divider.getBottomSideLine().getSize();
                int startPadding = divider.getBottomSideLine().getStartPadding();
                int endPadding = divider.getBottomSideLine().getEndPadding();
                drawChildBottomHorizontal(child, c, parent, divider.getBottomSideLine().getColor(), lineSize, startPadding, endPadding);
            }
        }
    }

    private void drawChildBottomHorizontal(View child, Canvas c, RecyclerView parent, @ColorInt int color, int lineSize, int startPadding, int endPadding) {
        final int leftPadding;
        final int rightPadding;

        if (startPadding <= 0) {
            //padding<0当作==0处理
            //上下左右默认分割线的两头都出头一个分割线的宽度，避免十字交叉的时候，交叉点是空白
            leftPadding = -lineSize;
        } else {
            leftPadding = startPadding;
        }

        if (endPadding <= 0) {
            rightPadding = lineSize;
        } else {
            rightPadding = -endPadding;
        }

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int left = child.getLeft() - params.leftMargin + leftPadding;
        int right = child.getRight() + params.rightMargin + rightPadding;
        int top = child.getBottom() + params.bottomMargin;
        int bottom = top + lineSize;
        mPaint.setColor(color);

        c.drawRect(left, top, right, bottom, mPaint);

    }

    private void drawChildTopHorizontal(View child, Canvas c, RecyclerView parent, @ColorInt int color, int lineSize, int startPadding, int endPadding) {
        final int leftPadding;
        final int rightPadding;

        if (startPadding <= 0) {
            //padding<0当作==0处理
            //上下左右默认分割线的两头都出头一个分割线的宽度，避免十字交叉的时候，交叉点是空白
            leftPadding = -lineSize;
        } else {
            leftPadding = startPadding;
        }
        if (endPadding <= 0) {
            rightPadding = lineSize;
        } else {
            rightPadding = -endPadding;
        }

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int left = child.getLeft() - params.leftMargin + leftPadding;
        int right = child.getRight() + params.rightMargin + rightPadding;
        int bottom = child.getTop() - params.topMargin;
        int top = bottom - lineSize;
        mPaint.setColor(color);

        c.drawRect(left, top, right, bottom, mPaint);

    }

    private void drawChildLeftVertical(View child, Canvas c, RecyclerView parent, @ColorInt int color, int lineSize, int startPadding, int endPadding) {
        final int topPadding;
        final int bottomPadding;

        if (startPadding <= 0) {
            //padding<0当作==0处理
            //上下左右默认分割线的两头都出头一个分割线的宽度，避免十字交叉的时候，交叉点是空白
            topPadding = -lineSize;
        } else {
            topPadding = startPadding;
        }
        if (endPadding <= 0) {
            bottomPadding = lineSize;
        } else {
            bottomPadding = -endPadding;
        }

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int top = child.getTop() - params.topMargin + topPadding;
        int bottom = child.getBottom() + params.bottomMargin + bottomPadding;
        int right = child.getLeft() - params.leftMargin;
        int left = right - lineSize;
        mPaint.setColor(color);

        c.drawRect(left, top, right, bottom, mPaint);

    }

    private void drawChildRightVertical(View child, Canvas c, RecyclerView parent, @ColorInt int color, int lineSize, int startPadding, int endPadding) {
        final int topPadding;
        final int bottomPadding;

        if (startPadding <= 0) {
            //padding<0当作==0处理
            //上下左右默认分割线的两头都出头一个分割线的宽度，避免十字交叉的时候，交叉点是空白
            topPadding = -lineSize;
        } else {
            topPadding = startPadding;
        }
        if (endPadding <= 0) {
            bottomPadding = lineSize;
        } else {
            bottomPadding = -endPadding;
        }

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
        int top = child.getTop() - params.topMargin + topPadding;
        int bottom = child.getBottom() + params.bottomMargin + bottomPadding;
        int left = child.getRight() + params.rightMargin;
        int right = left + lineSize;
        mPaint.setColor(color);

        c.drawRect(left, top, right, bottom, mPaint);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        //outRect 看源码可知这里只是把Rect类型的outRect作为一个封装了left,right,top,bottom的数据结构,
        //作为传递left,right,top,bottom的偏移值来用的

        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        Divider divider = getDivider(parent, itemPosition);
        int left = divider.getLeftSideLine().isVisible() ? divider.getLeftSideLine().getSize() : 0;
        int top = divider.getTopSideLine().isVisible() ? divider.getTopSideLine().getSize() : 0;
        int right = divider.getRightSideLine().isVisible() ? divider.getRightSideLine().getSize() : 0;
        int bottom = divider.getBottomSideLine().isVisible() ? divider.getBottomSideLine().getSize() : 0;

        outRect.set(left, top, right, bottom);
    }


    public abstract Divider getDivider(@NonNull RecyclerView parent, int itemPosition);


}

















