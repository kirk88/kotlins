package com.example.sample.page

import android.graphics.Rect
import java.io.Closeable
import java.io.File

interface PdfPageFactory : Closeable {

    val pageCount: Int

    fun open(file: File)

    fun getPage(index: Int, bounds: Rect, preload: Boolean): PdfPage?

    fun hasPage(index: Int): Boolean

}