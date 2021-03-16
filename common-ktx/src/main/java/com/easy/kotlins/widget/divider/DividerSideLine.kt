package com.easy.kotlins.widget.divider;


import androidx.annotation.ColorInt;

public class DividerSideLine {

    public boolean isVisible;
    public int color;
    public int size;
    public int startPadding;
    public int endPadding;

    public DividerSideLine(boolean isVisible, @ColorInt int color, int size, int startPadding, int endPadding) {
        this.isVisible = isVisible;
        this.color = color;
        this.size = size;
        this.startPadding = startPadding;
        this.endPadding = endPadding;
    }

    public int getStartPadding() {
        return startPadding;
    }

    public void setStartPadding(int startPadding) {
        this.startPadding = startPadding;
    }

    public int getEndPadding() {
        return endPadding;
    }

    public void setEndPadding(int endPadding) {
        this.endPadding = endPadding;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
