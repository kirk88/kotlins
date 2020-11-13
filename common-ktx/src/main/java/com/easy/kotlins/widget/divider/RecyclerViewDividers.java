package com.easy.kotlins.widget.divider;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class RecyclerViewDividers {

    public static DividerItemDecoration attachTo(RecyclerView recyclerView, @ColorInt int color, @Px int size, @Px int paddingStart, @Px int paddingEnd, boolean showLastDivider) {
        if (recyclerView == null) {
            return null;
        }

        final DividerItemDecoration decor = new DividerItemDecoration(recyclerView, color, size, paddingStart, paddingEnd, showLastDivider);
        recyclerView.addItemDecoration(decor);
        return decor;
    }

    public static DividerItemDecoration attachTo(RecyclerView recyclerView, @ColorInt int color, @Px int size, @Px int paddingStart, @Px int paddingEnd) {
        if (recyclerView == null) {
            return null;
        }

        final DividerItemDecoration decor = new DividerItemDecoration(recyclerView, color, size, paddingStart, paddingEnd, false);
        recyclerView.addItemDecoration(decor);
        return decor;
    }

    public static DividerItemDecoration attachTo(RecyclerView recyclerView, @ColorInt int color, @Px int size, boolean showLastDivider) {
        return attachTo(recyclerView, color, size, 0, 0, showLastDivider);
    }

    public static DividerItemDecoration attachTo(RecyclerView recyclerView, @ColorInt int color, @Px int size) {
        return attachTo(recyclerView, color, size, 0, 0);
    }

    public static DividerItemDecoration attachTo(RecyclerView recyclerView, @Px int size) {
        return attachTo(recyclerView, Color.TRANSPARENT, size);
    }

    public static DividerItemDecoration attachTo(RecyclerView recyclerView) {
        return attachTo(recyclerView, recyclerView.getResources().getColor(android.R.color.darker_gray), 1);
    }


    public static class DividerItemDecoration extends XDividerItemDecoration {

        private final RecyclerView mRecyclerView;

        private int mColor;
        private int mSize;
        private int mPaddingStart;
        private int mPaddingEnd;

        private boolean mShowLastDivider;

        DividerItemDecoration(RecyclerView recyclerView, @ColorInt int color, @Px int size, @Px int paddingStart, @Px int paddingEnd, boolean showLastDivider) {
            this.mRecyclerView = recyclerView;
            this.mColor = color;
            this.mSize = size;
            this.mPaddingStart = paddingStart;
            this.mPaddingEnd = paddingEnd;
            this.mShowLastDivider = showLastDivider;
        }

        public void setDivider(@ColorInt int color, @Px int size, boolean showLastDivider) {
            this.mColor = color;
            this.mSize = size;
            this.mShowLastDivider = showLastDivider;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setColor(@ColorInt int mColor) {
            this.mColor = mColor;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setSize(@Px int mSize) {
            this.mSize = mSize;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setPaddingStart(int mPaddingStart) {
            this.mPaddingStart = mPaddingStart;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setPaddingEnd(int mPaddingEnd) {
            this.mPaddingEnd = mPaddingEnd;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setShowLastDivider(boolean mShowLastDivider) {
            this.mShowLastDivider = mShowLastDivider;
            mRecyclerView.invalidateItemDecorations();
        }

        @NonNull
        @Override
        public Divider getDivider(@NonNull RecyclerView parent, int itemPosition) {
            RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
            if (manager == null) return new DividerBuilder().create();
            final int itemCount = manager.getItemCount();
            final int spanCount;
            if (manager instanceof GridLayoutManager) {
                spanCount = ((GridLayoutManager) manager).getSpanCount();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) manager).getSpanCount();
            } else {
                spanCount = -1;
            }

            if (spanCount > -1) {
                final boolean haveRightDivider = itemPosition % spanCount != spanCount - 1;
                final boolean haveBottomDivider;
                if (mShowLastDivider) {
                    haveBottomDivider = true;
                } else if (itemCount < spanCount) {
                    haveBottomDivider = false;
                } else {
                    int extraCount = itemCount % spanCount;
                    int lastCount = extraCount == 0 ? spanCount : extraCount;
                    haveBottomDivider = itemPosition < itemCount - lastCount;
                }
                return new DividerBuilder()
                        .setRightSideLine(haveRightDivider, mColor, mSize, mPaddingStart, mPaddingEnd)
                        .setBottomSideLine(haveBottomDivider, mColor, mSize, mPaddingStart, mPaddingEnd)
                        .create();
            }

            final boolean showLastDivider = mShowLastDivider || itemPosition < itemCount - 1;
            if (manager.canScrollVertically()) {
                return new DividerBuilder().setBottomSideLine(showLastDivider, mColor, mSize, mPaddingStart, mPaddingEnd).create();
            } else {
                return new DividerBuilder().setRightSideLine(showLastDivider, mColor, mSize, mPaddingStart, mPaddingEnd).create();
            }
        }
    }
}
