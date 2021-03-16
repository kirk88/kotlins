package com.easy.kotlins.widget.divider;

import androidx.annotation.ColorInt;
import androidx.annotation.Px;

public class DividerBuilder {

    private DividerSideLine leftSideLine;
    private DividerSideLine topSideLine;
    private DividerSideLine rightSideLine;
    private DividerSideLine bottomSideLine;


    public DividerBuilder setLeftSideLine(boolean isVisible, @ColorInt int color, @Px int size, @Px int startPadding, @Px int endPadding) {
        this.leftSideLine = new DividerSideLine(isVisible, color, size, startPadding, endPadding);
        return this;
    }

    public DividerBuilder setTopSideLine(boolean isVisible, @ColorInt int color, @Px int size, @Px int startPadding, @Px int endPadding) {
        this.topSideLine = new DividerSideLine(isVisible, color, size, startPadding, endPadding);
        return this;
    }

    public DividerBuilder setRightSideLine(boolean isVisible, @ColorInt int color, @Px int size, @Px int startPadding, @Px int endPadding) {
        this.rightSideLine = new DividerSideLine(isVisible, color, size, startPadding, endPadding);
        return this;
    }

    public DividerBuilder setBottomSideLine(boolean isVisible, @ColorInt int color, @Px int size, @Px int startPadding, @Px int endPadding) {
        this.bottomSideLine = new DividerSideLine(isVisible, color, size, startPadding, endPadding);
        return this;
    }

    public Divider create() {
        final DividerSideLine defaultSideLine = new DividerSideLine(false, 0xff666666, 0, 0, 0);

        leftSideLine = (leftSideLine != null ? leftSideLine : defaultSideLine);
        topSideLine = (topSideLine != null ? topSideLine : defaultSideLine);
        rightSideLine = (rightSideLine != null ? rightSideLine : defaultSideLine);
        bottomSideLine = (bottomSideLine != null ? bottomSideLine : defaultSideLine);

        return new Divider(leftSideLine, topSideLine, rightSideLine, bottomSideLine);
    }


}



