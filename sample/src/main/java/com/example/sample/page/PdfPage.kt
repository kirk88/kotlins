package com.example.sample.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

data class PdfPage(
    val index: Int,
    val bounds: Rect,
    val bitmap: Bitmap,
) {

    val isRecycled: Boolean
        get() = bitmap.isRecycled

    fun recycle() = bitmap.recycle()


    fun into(view: PdfPageView) {
        if (bitmap.isRecycled) {
            return
        }
        view.setImageBitmap(bitmap)
    }

    fun draw(canvas: Canvas) {
        if (bitmap.isRecycled) {
            return
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }

}