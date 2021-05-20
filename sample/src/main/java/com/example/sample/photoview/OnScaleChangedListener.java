package com.example.sample.photoview;


/**
 * Interface definition for callback to be invoked when attached ImageView scale changes
 */
public interface OnScaleChangedListener {

    void onScaleBegin();

    /**
     * Callback for when the scale changes
     *
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX      focal point X position
     * @param focusY      focal point Y position
     */
    void onScaleChange(float scaleFactor, float focusX, float focusY);

    void onScaleEnd();

}
