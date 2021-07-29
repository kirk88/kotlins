@file:Suppress("unused")

package com.nice.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import java.util.concurrent.atomic.AtomicInteger



private val ANDROID_SQLITE_OPEN_HELPER_FACTORY = FrameworkSQLiteOpenHelperFactory()

open class ManagedSQLiteOpenHelper(
    configuration: SupportSQLiteOpenHelper.Configuration,
    factory: SupportSQLiteOpenHelper.Factory = ANDROID_SQLITE_OPEN_HELPER_FACTORY
) {

    private val delegate: SupportSQLiteOpenHelper = factory.create(configuration)

    private val counter = AtomicInteger()
    private var db: SupportSQLiteDatabase? = null

    fun <T> use(inTransaction: Boolean = false, action: SupportSQLiteDatabase.() -> T): T {
        try {
            return openDatabase().let {
                if (inTransaction) it.transaction { action() }
                else it.action()
            }
        } finally {
            closeDatabase()
        }
    }

    @Synchronized
    private fun openDatabase(): SupportSQLiteDatabase {
        if (counter.incrementAndGet() == 1) {
            db = delegate.writableDatabase
        }
        return db!!
    }

    @Synchronized
    private fun closeDatabase() {
        if (counter.decrementAndGet() == 0) {
            db?.close()
        }
    }

}