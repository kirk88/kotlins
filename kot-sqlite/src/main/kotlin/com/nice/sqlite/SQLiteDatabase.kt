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

    fun <T> execute(action: SupportSQLiteDatabase.() -> T): T {
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
            database = writableDatabase
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

    override fun close() = delegate.close()

}

fun ManagedSupportSQLiteOpenHelper(
    context: Context,
    name: String?,
    callback: SupportSQLiteOpenHelper.Callback,
    noBackupDirectory: Boolean = false,
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
        val stm = database.compileStatement(statement.toString(SQLiteDialect))
        if (statement.nativeBindValues) {
            bindValues(stm, statement.values)
        }
        return stm.executeUpdateDelete()
    }

    override fun executeUpdateBatch(statement: UpdateBatchStatement<*>): Int {
        var numberOfRows = 0
        while (statement.hasNext()) {
            val executable = statement.next(SQLiteDialect)
            val stm = caches.getOrPut(executable.sql) { database.compileStatement(executable.sql) }
            bindValues(stm, executable.values)
            numberOfRows += stm.executeUpdateDelete()
        }
        return numberOfRows
    }

    override fun executeDelete(statement: DeleteStatement<*>): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeUpdateDelete()
    }

    override fun executeInsert(statement: InsertStatement<*>): Long {
        val stm = database.compileStatement(statement.toString(SQLiteDialect))
        if (statement.nativeBindValues) {
            bindValues(stm, statement.values)
        }
        return stm.executeInsert()
    }

    override fun executeInsertBatch(statement: InsertBatchStatement<*>): Long {
        var lastRowId = -1L
        while (statement.hasNext()) {
            val executable = statement.next(SQLiteDialect)
            val stm = caches.getOrPut(executable.sql) { database.compileStatement(executable.sql) }
            bindValues(stm, executable.values)
            val rowId = stm.executeInsert()
            if (rowId >= 0L) lastRowId = rowId
        }
        return lastRowId
    }

    override fun executeQuery(statement: QueryStatement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

    private fun bindValues(statement: SupportSQLiteStatement, values: Bag<Assignment>) {
        for ((index, assignment) in values.withIndex()) {
            when (val value = assignment.value) {
                null -> statement.bindNull(index + 1)
                is String -> statement.bindString(index + 1, value)
                is Long -> statement.bindLong(index + 1, value)
                is Int -> statement.bindLong(index + 1, value.toLong())
                is Short -> statement.bindLong(index + 1, value.toLong())
                is Double -> statement.bindDouble(index + 1, value)
                is Float -> statement.bindDouble(index + 1, value.toDouble())
                is Boolean -> statement.bindLong(index + 1, if (value) 1 else 0)
                is ByteArray -> statement.bindBlob(index + 1, value)
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