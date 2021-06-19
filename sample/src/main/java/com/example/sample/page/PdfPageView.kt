package com.example.sample.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.sample.page.anim.PageAnimation
import com.example.sample.page.anim.SimulationPageAnim
import com.example.sample.photoview.PhotoView
import kotlinx.coroutines.*
import java.io.File

class PdfPageView(context: Context, attrs: AttributeSet?) : PhotoView(context, attrs),
        CoroutineScope by MainScope() {

    val factory = DefaultPdfPageFactory(context)

    private val bounds = Rect()
    private var currentPage: PdfPage? = null
    private var thumbnailPage: PdfPage? = null

    private var pageAnim: PageAnimation? = null

    private var drawingIndex = 0

    val currentPageIndex: Int
        get() = currentPage?.index ?: 0

    init {
        isFlingable = false
    }

    override fun onDraw(canvas: Canvas) {
        if (pageAnim?.draw(canvas) == true) {
            return
        }

        val page = currentPage ?: return
        if (drawingIndex == page.index && !page.isRecycled) {
            super.onDraw(canvas)
        }
        thumbnailPage?.draw(canvas)
    }

    fun setDrawingIndex(index: Int) {
        drawingIndex = index;
    }

    fun open(file: File) {
        factory.open(file)

        post {
            pageAnim = SimulationPageAnim(this, width, height)

            bounds.set(0, 0, width, height)
            showPage(0, bounds)
        }
    }

    fun hasPrevious(): Boolean = factory.hasPage((currentPage?.index ?: 0) - 1)

    fun hasNext(): Boolean = factory.hasPage((currentPage?.index ?: 0) + 1)

    fun next() {
        show((currentPage?.index ?: 0) + 1)
    }

    fun previous() {
        show((currentPage?.index ?: 0) - 1)
    }

    fun show(index: Int, inThread: Boolean = false) {
        val shown = Runnable {
            thumbnailPage = null
            setScale(1f, false)
            bounds.set(0, 0, width, height)
            showPage(index, bounds, inThread)
        }
        if (width == 0 && height == 0) {
            post(shown)
        } else {
            shown.run()
        }
    }

    fun getPage(index: Int): PdfPage? {
        bounds.set(0, 0, width, height)
        return factory.getPage(index, bounds, true)
    }

    override fun computeScroll() {
        pageAnim?.scrollAnim()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
        factory.close()
    }

    private fun showPage(index: Int, bounds: Rect, inThread: Boolean = false) {
        val pagePromise = {
            factory.getPage(index, bounds, true)
        }
        if (inThread) {
            launch(Dispatchers.IO) {
                val page = pagePromise() ?: return@launch
                currentPage = page
                drawingIndex = page.index
                withContext(Dispatchers.Main) {
                    page.into(this@PdfPageView)
                }
            }
        } else {
            val page = pagePromise() ?: return
            currentPage = page
            drawingIndex = page.index
            page.into(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && thumbnailPage != null) {
            thumbnailPage = null
            postInvalidateOnAnimation()
        } else if (event.action == MotionEvent.ACTION_UP && scale > 1f) {
            updateThumbnail(displayRect)
        }

        val handled = if (pageAnim == null || scale != 1f) false
        else pageAnim!!.onTouchEvent(event)

        return handled || super.onTouchEvent(event)
    }

    private fun updateThumbnail(it: RectF) {
        val page = currentPage ?: return
        bounds.set(it.left.toInt(), it.top.toInt(), it.right.toInt(), it.bottom.toInt())
        launch(Dispatchers.IO) {
            thumbnailPage = factory.getPage(page.index, bounds, false)
            postInvalidateOnAnimation()
        }
    }
}