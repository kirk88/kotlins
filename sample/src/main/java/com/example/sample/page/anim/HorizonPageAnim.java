package com.example.sample.page.anim;

import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.sample.page.PdfPage;
import com.example.sample.page.PdfPageView;

public abstract class HorizonPageAnim extends PageAnimation implements GestureDetector.OnGestureListener {

    private static final int TOUCH_THRESHOLD = 10;

    private final GestureDetector mDetector;

    private boolean isMoved = false;

    public HorizonPageAnim(PdfPageView view, int width, int height) {
        super(view, width, height);
        mDetector = new GestureDetector(view.getContext(), this);
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isMoved && event.getAction() == MotionEvent.ACTION_UP) {
            startAnim();
            return true;
        }
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!mScroller.isFinished()) {
            return true;
        }
        isMoved = false;
        isStarted = false;
        isRunning = false;
        isCancel = false;
        mDirection = Direction.NONE;
        setStartPoint(e.getX(), e.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!mScroller.isFinished()) {
            return true;
        }
        float x = e.getX();
        float y = e.getY();
        if (x > mViewWidth / 2f) {
            setDirection(Direction.NEXT);
            if (!mView.hasNext()) {
                return true;
            }
        } else {
            setDirection(Direction.PREV);
            if (!mView.hasPrevious()) {
                return true;
            }
        }
        setTouchPoint(x, y);
        startAnim();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!mScroller.isFinished()) {
            return true;
        }
        float movedX = Math.abs(distanceX);
        float movedY = Math.abs(distanceY);
        if (!isMoved && movedX > TOUCH_THRESHOLD && movedX > movedY) {
            if (distanceX > 0) {
                setDirection(Direction.NEXT);
                if (!mView.hasNext()) {
                    return true;
                }
            } else {
                setDirection(Direction.PREV);
                if (!mView.hasPrevious()) {
                    return true;
                }
            }
            isMoved = true;
        }
        if (isMoved) {
            isCancel = mDirection == Direction.NEXT ? distanceX < -TOUCH_THRESHOLD : distanceX > TOUCH_THRESHOLD;
            isRunning = true;
            setTouchPoint(e2.getX(), e2.getY());
            invalidate();
        }
        return isMoved;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean draw(Canvas canvas) {
        if (isRunning) {
            drawMove(canvas);
            return true;
        } else {
            PdfPage page = null;
            if (!isCancel) {
                if (mDirection == Direction.NEXT) {
                    page = getNextPage();
                } else if (mDirection == Direction.PREV) {
                    page = getPreviousPage();
                }
            } else {
                page = getCurrentPage();
            }
            if (page != null) {
                page.draw(canvas);
            }
            return false;
        }
    }

    @Override
    public void scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isRunning = false;
            }

            setTouchPoint(x, y);
            invalidate();
        } else if (isStarted) {
            if (!isCancel) {
                if (mDirection == Direction.NEXT) {
                    mView.next();
                } else if (mDirection == Direction.PREV) {
                    mView.previous();
                }
            } else {
                mView.setDrawingIndex(mView.getCurrentPageIndex());
            }

            resetAnim();
        }
    }

    @Override
    public void animTo(Direction direction) {
        if (!mScroller.isFinished()) return;
        if (direction == Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            if (getAnimMode() == AnimMode.SIMULATION) {
                y = y * 2 / 3;
            }
            setStartPoint(x, y);
            setTouchPoint(x, y);
            setDirection(direction);
        } else {
            int x = 0;
            int y = mViewHeight;
            setStartPoint(x, y);
            setTouchPoint(x, y);
            setDirection(direction);
        }
        startAnim();
    }

    @Override
    public PdfPage getPreviousPage() {
        int curPage = mView.getCurrentPageIndex();
        return mView.getPage(curPage - 1);
    }

    @Override
    public PdfPage getNextPage() {
        final int curPage = mView.getCurrentPageIndex();
        return mView.getPage(curPage + 1);
    }

    @Override
    public PdfPage getCurrentPage() {
        final int curPage = mView.getCurrentPageIndex();
        return mView.getPage(curPage);
    }
}
