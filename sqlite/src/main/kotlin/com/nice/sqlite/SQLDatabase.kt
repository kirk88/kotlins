@file:Suppress("unused")

package com.nice.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.dml.Projection
import java.util.concurrent.atomic.AtomicInteger

fun SupportSQLiteDatabase.createIndex(
    table: String,
    unique: Boolean,
    name: String,
    vararg columns: Projection.Column
) {
    val escapedTableName = table.replace("`", "``")
    val escapedIndexName = name.replace("`", "``")
    val uniqueText = if (unique) "UNIQUE" else ""
    execSQL(columns.joinToString(
        separator = ",",
        prefix = "CREATE $uniqueText INDEX IF NOT EXISTS `$escapedIndexName` ON `$escapedTableName`(",
        postfix = ")"
    ))
}

fun SupportSQLiteDatabase.dropIndex(
    table: String,
    name: String
) {
    val escapedTableName = table.replace("`", "``")
    val escapedIndexName = name.replace("`", "``")
    execSQL("DROP INDEX IF EXISTS `$escapedIndexName` ON `$escapedTableName`")
}

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