package com.nice.kotlins.app

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri


class ContextProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val application = context!!.applicationContext as Application
        ApplicationContextHolder.initContext(application)
        ScreenAdaptation.init(application)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = -1

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = -1

}

internal object ApplicationContextHolder {

    lateinit var context: Context
        private set

    fun initContext(context: Context) {
        this.context = context.applicationContext
    }

}


val applicationContext: Context
    get() = ApplicationContextHolder.context