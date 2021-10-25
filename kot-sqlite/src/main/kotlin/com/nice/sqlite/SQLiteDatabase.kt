@file:Suppress("UNUSED")

package com.nice.sqlite

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteStatement
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.ddl.*
import java.util.concurrent.atomic.AtomicInteger

private val ANDROID_SQLITE_OPEN_HELPER_FACTORY = FrameworkSQLiteOpenHelperFactory()

class ManagedSupportSQLiteOpenHelper(
    configuration: SupportSQLiteOpenHelper.Configuration,
    factory: SupportSQLiteOpenHelper.Factory = ANDROID_SQLITE_OPEN_HELPER_FACTORY
) : SupportSQLiteOpenHelper {

    private val delegate: SupportSQLiteOpenHelper = factory.create(configuration)

    private val counter = AtomicInteger()
    private var database: SupportSQLiteDatabase? = null

    fun <T> use(action: SupportSQLiteDatabase.() -> T): T {
        try {
            return openDatabase().action()
        } finally {
            closeDatabase()
        }
    }

    fun <T> transaction(
        exclusive: Boolean = true,
        action: SupportSQLiteDatabase.() -> T
    ): T {
        try {
            return openDatabase().transaction(exclusive, action)
        } finally {
            closeDatabase()
        }
    }

    @Synchronized
    private fun openDatabase(): SupportSQLiteDatabase {
        if (counter.incrementAndGet() == 1) {
            database = delegate.writableDatabase
        }
        return database!!
    }

    @Synchronized
    private fun closeDatabase() {
        if (counter.decrementAndGet() == 0) {
            database?.close()
        }
    }

    override fun getDatabaseName(): String? = delegate.databaseName

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) = delegate.setWriteAheadLoggingEnabled(enabled)

    override fun getWritableDatabase(): SupportSQLiteDatabase = delegate.writableDatabase

    override fun getReadableDatabase(): SupportSQLiteDatabase = delegate.readableDatabase

    override fun close() {
        delegate.close()
    }

}

fun ManagedSupportSQLiteOpenHelper(
    context: Context,
    name: String?,
    callback: SupportSQLiteOpenHelper.Callback,
    noBackupDirectory: Boolean,
    factory: SupportSQLiteOpenHelper.Factory = ANDROID_SQLITE_OPEN_HELPER_FACTORY
) = ManagedSupportSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(name)
        .callback(callback)
        .noBackupDirectory(noBackupDirectory)
        .build(),
    factory
)

private class SQLiteStatementExecutor private constructor(
    private val database: SupportSQLiteDatabase
) : StatementExecutor {

    private val caches = mutableMapOf<String, SupportSQLiteStatement>()

    override fun execute(statement: Statement) {
        if (statement is TriggerCreateStatement<*>) {
            database.compileStatement(statement.toString(SQLiteDialect)).execute()
        } else {
            for (sql in statement.toString(SQLiteDialect).split(";")) {
                val executeSql = { database.compileStatement(sql).execute() }
                if (sql.startsWith("alter", true)) {
                    runCatching(executeSql).onFailure {
                        Log.w(TAG, "Alter table failed: ${it.message}")
                    }
                } else {
                    executeSql()
                }
            }
        }
    }

    override fun executeUpdate(statement: UpdateStatement<*>): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).apply {
            if (statement.nativeBindValues) {
                bindValues(statement.assignments)
            }
        }.executeUpdateDelete()
    }

    override fun executeUpdateBatch(statement: UpdateBatchStatement<*>): Int {
        var numberOfRows = 0
        while (statement.hasNext()) {
            val executable = statement.next(SQLiteDialect)
            numberOfRows += caches.getOrPut(executable.sql) {
                database.compileStatement(executable.sql)
            }.apply {
                bindValues(executable.values)
            }.executeUpdateDelete()
        }
        return numberOfRows
    }

    override fun executeDelete(statement: DeleteStatement<*>): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeUpdateDelete()
    }

    override fun executeInsert(statement: InsertStatement<*>): Long {
        return database.compileStatement(statement.toString(SQLiteDialect)).apply {
            if (statement.nativeBindValues) {
                bindValues(statement.assignments)
            }
        }.executeInsert()
    }

    override fun executeInsertBatch(statement: InsertBatchStatement<*>): Long {
        var lastRowId = -1L
        while (statement.hasNext()) {
            val executable = statement.next(SQLiteDialect)
            lastRowId = caches.getOrPut(executable.sql) {
                database.compileStatement(executable.sql)
            }.apply {
                bindValues(executable.values)
            }.executeInsert()
        }
        return lastRowId
    }

    override fun executeQuery(statement: QueryStatement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

    private fun SupportSQLiteStatement.bindValues(assignments: Bag<Value>) {
        for ((index, assignment) in assignments.withIndex()) {
            when (val value = assignment.value) {
                null -> bindNull(index + 1)
                is String -> bindString(index + 1, value)
                is Long -> bindLong(index + 1, value)
                is Int -> bindLong(index + 1, value.toLong())
                is Short -> bindLong(index + 1, value.toLong())
                is Double -> bindDouble(index + 1, value)
                is Float -> bindDouble(index + 1, value.toDouble())
                is Boolean -> bindLong(index + 1, if (value) 1 else 0)
                is ByteArray -> bindBlob(index + 1, value)
            }
        }
    }

    companion object {

        private var instance: SQLiteStatementExecutor? = null

        @Synchronized
        operator fun get(database: SupportSQLiteDatabase): SQLiteStatementExecutor {
            if (instance == null || instance!!.database !== database) {
                instance = SQLiteStatementExecutor(database)
            }
            return instance!!
        }

    }

}

operator fun StatementExecutor.Companion.get(database: SupportSQLiteDatabase): StatementExecutor {
    return SQLiteStatementExecutor[database]
}

val SupportSQLiteDatabase.statementExecutor: StatementExecutor
    get() = SQLiteStatementExecutor[this]