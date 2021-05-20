package com.example.sample.page.anim;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.example.sample.page.PdfPage;
import com.example.sample.page.PdfPageView;

public abstract class PageAnimation {
    //正在使用的View
    protected PdfPageView mView;
    //滑动装置
    protected final Scroller mScroller;
    //移动方向
    protected Direction mDirection = Direction.NONE;

    //是否取消翻页
    protected boolean isCancel = false;
    protected boolean isRunning = false;
    protected boolean isStarted = false;

    //视图的尺寸
    protected int mViewWidth;
    protected int mViewHeight;
    //起始点
    protected float mStartX;
    protected float mStartY;
    //触碰点
    protected float mTouchX;
    protected float mTouchY;
    //上一个触碰点
    protected float mLastX;
    protected float mLastY;

    PageAnimation(PdfPageView view, int width, int height) {
        mView = view;
        mViewWidth = width;
        mViewHeight = height;
        mScroller = new Scroller(mView.getContext(), new LinearInterpolator());
    }


    public Scroller getScroller() {
        return mScroller;
    }

    public void setStartPoint(float x, float y) {
        mStartX = x;
        mStartY = y;

        mLastX = mStartX;
        mLastY = mStartY;
    }

    public void setTouchPoint(float x, float y) {
        mLastX = mTouchX;
        mLastY = mTouchY;

        mTouchX = x;
        mTouchY = y;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void invalidate() {
        mView.postInvalidateOnAnimation();
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public void clear() {
        mView = null;
    }

    public abstract AnimMode getAnimMode();

    /**
     * 点击事件的处理
     */
    public abstract boolean onTouchEvent(MotionEvent event);

    /**
     * 绘制图形
     */
    public abstract boolean draw(Canvas canvas);

    public void resetAnim() {
        isCancel = false;
        isStarted = false;
        isRunning = false;
        mDirection = Direction.NONE;
    }

    /**
     * 开启翻页动画
     */
    public void startAnim() {
        if (mDirection == Direction.NEXT) {
            mView.setDrawingIndex(mView.getCurrentPageIndex() + 1);
        } else if (mDirection == Direction.PREV) {
            mView.setDrawingIndex(mView.getCurrentPageIndex() - 1);
        }
        isStarted = true;
        isRunning = true;
        invalidate();
    }

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    public abstract void scrollAnim();

    /**
     * 自动加载动画
     */
    public abstract void animTo(Direction direction);

    public abstract PdfPage getPreviousPage();

    public abstract PdfPage getNextPage();

    public abstract PdfPage getCurrentPage();

}