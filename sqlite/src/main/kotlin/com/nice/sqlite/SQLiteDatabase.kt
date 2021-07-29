@file:Suppress("unused")

package com.nice.sqlite

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.Database
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*
import java.util.concurrent.atomic.AtomicInteger

private class AndroidDatabase(private val database: SupportSQLiteDatabase) : Database {

    override fun execute(statement: Statement) {
        for (sql in statement.toString(SQLiteDialect).split(";")) {
            database.compileStatement(sql).use {
                it.execute()
            }
        }
    }

    override fun executeUpdateDelete(statement: Statement): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).use {
            it.executeUpdateDelete()
        }
    }

    override fun executeInsert(statement: Statement): Long {
        return database.compileStatement(statement.toString(SQLiteDialect)).use {
            it.executeInsert()
        }
    }

    override fun query(statement: Statement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

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