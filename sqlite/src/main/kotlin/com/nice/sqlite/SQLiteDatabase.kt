@file:Suppress("unused")

package com.nice.sqlite

import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteStatement
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.StatementExecutor
import com.nice.sqlite.core.dml.BatchInsertStatement
import java.util.concurrent.atomic.AtomicInteger

internal class SQLiteStatementExecutor(private val database: SupportSQLiteDatabase) :
    StatementExecutor {

    override fun execute(statement: Statement) {
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

    override fun executeUpdateDelete(statement: Statement): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeUpdateDelete()
    }

    override fun executeInsert(statement: Statement): Long {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeInsert()
    }

    override fun executeBatchInsert(statement: Statement): Long {
        return if (statement is BatchInsertStatement<*>) {
            var lastRowId: Long = -1
            while (statement.moveToNext()) {
                val executable = statement.next(SQLiteDialect)
                lastRowId = database.compileStatement(executable.first).also {
                    it.bindAssignments(executable.second)
                }.executeInsert()
            }
            lastRowId
        } else {
            executeInsert(statement)
        }
    }

    override fun executeQuery(statement: Statement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

    private fun SupportSQLiteStatement.bindAssignments(assignments: Sequence<Assignment>) {
        for ((index, assignment) in assignments.withIndex()) {
            when (val value = assignment.value) {
                null -> bindNull(index + 1)
                is String -> bindString(index + 1, value)
                is Long -> bindLong(index + 1, value)
                is Int -> bindLong(index + 1, value.toLong())
                is Short -> bindLong(index + 1, value.toLong())
                is Boolean -> bindLong(index + 1, if (value) 1 else 0)
                is Double -> bindDouble(index + 1, value)
                is Float -> bindDouble(index + 1, value.toDouble())
            }
        }
    }

}

val SupportSQLiteDatabase.statementExecutor: StatementExecutor
    get() = SQLiteStatementExecutor(this)

private val ANDROID_SQLITE_OPEN_HELPER_FACTORY = FrameworkSQLiteOpenHelperFactory()

enum class Transaction {
    None,
    Immediate,
    Exclusive
}

open class ManagedSQLiteOpenHelper(
    configuration: SupportSQLiteOpenHelper.Configuration,
    factory: SupportSQLiteOpenHelper.Factory = ANDROID_SQLITE_OPEN_HELPER_FACTORY
) {

    private val delegate: SupportSQLiteOpenHelper = factory.create(configuration)

    private val counter = AtomicInteger()
    private var db: SupportSQLiteDatabase? = null

    fun <T> use(transaction: Transaction = Transaction.None, action: SupportSQLiteDatabase.() -> T): T {
        try {
            return openDatabase().run {
                if (transaction == Transaction.None) {
                    action()
                } else {
                    transaction(transaction == Transaction.Exclusive, action)
                }
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