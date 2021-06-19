package com.example.sample.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

class DefaultPdfPageFactory(context: Context) : PdfPageFactory {

    private var isOpened = false
    private val core = PdfiumCore(context)
    private lateinit var document: PdfDocument

    override val pageCount: Int
        get() {
            check(isOpened) { "not open yet" }
            return core.getPageCount(document)
        }

    override fun open(file: File) {
        check(!isOpened) { "Already opened" }
        document =
                core.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        isOpened = true
    }


    override fun getPage(index: Int, bounds: Rect, preload: Boolean): PdfPage? {
        check(isOpened) { "not open yet" }
        if (preload) {
            GlobalScope.launch(Dispatchers.IO) {
                val fromIndex = max(0, index - 2)
                val toIndex = min(index + 2, pageCount - 1)
                for (i in fromIndex..toIndex) {
                    loadPage(i, bounds)
                }
            }
        }
        return loadPage(index, bounds)
    }

    override fun hasPage(index: Int): Boolean {
        return document.hasPage(index)
    }

    override fun close() {
        if (!isOpened) {
            return
        }
        PdfPageCache.clear()
        core.closeDocument(document)
        isOpened = false
    }

    private fun loadPage(index: Int, bounds: Rect): PdfPage? {
        val cached = PdfPageCache[index, bounds]
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        if (index < 0 || index >= pageCount) {
            return null
        }

        if (!document.hasPage(index) && core.openPage(document, index) == 0.toLong()) {
            return null
        }

        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.RGB_565)
        core.renderPageBitmap(document,
                bitmap,
                index,
                bounds.left,
                bounds.top,
                bounds.width(),
                bounds.height())
        return PdfPage(index, bounds, bitmap).also {
            PdfPageCache[index, bounds] = it
        }
    }

}