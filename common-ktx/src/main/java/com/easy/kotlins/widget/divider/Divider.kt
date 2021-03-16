package com.easy.kotlins.widget.divider;

/**
 * Created by mac on 2017/5/17.
 */

public class Divider {

    public DividerSideLine leftSideLine;
    public DividerSideLine topSideLine;
    public DividerSideLine rightSideLine;
    public DividerSideLine bottomSideLine;


    public Divider(DividerSideLine leftSideLine, DividerSideLine topSideLine, DividerSideLine rightSideLine, DividerSideLine bottomSideLine) {
        this.leftSideLine = leftSideLine;
        this.topSideLine = topSideLine;
        this.rightSideLine = rightSideLine;
        this.bottomSideLine = bottomSideLine;
    }

    public DividerSideLine getLeftSideLine() {
        return leftSideLine;
    }

    public void setLeftSideLine(DividerSideLine leftSideLine) {
        this.leftSideLine = leftSideLine;
    }

    public DividerSideLine getTopSideLine() {
        return topSideLine;
    }

    public void setTopSideLine(DividerSideLine topSideLine) {
        this.topSideLine = topSideLine;
    }

    public DividerSideLine getRightSideLine() {
        return rightSideLine;
    }

    public void setRightSideLine(DividerSideLine rightSideLine) {
        this.rightSideLine = rightSideLine;
    }

    public DividerSideLine getBottomSideLine() {
        return bottomSideLine;
    }

    public void setBottomSideLine(DividerSideLine bottomSideLine) {
        this.bottomSideLine = bottomSideLine;
    }
}



