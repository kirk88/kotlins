@file:Suppress("unused")

package com.nice.sqlite

import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.ddl.AlterStatement
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.StatementExecutor
import java.util.concurrent.atomic.AtomicInteger

internal class SQLiteStatementExecutor(private val database: SupportSQLiteDatabase) :
    StatementExecutor {

    override fun execute(statement: Statement) {
        for (sql in statement.toString(SQLiteDialect).split(";")) {
            val execution = { database.compileStatement(sql).execute() }
            //SQLite doesn't support an IF NOT EXISTS clause on ALTER TABLE.
            if (statement is AlterStatement<*>
                && sql.startsWith("alter", true)
            ) {
                runCatching(execution).onFailure {
                    Log.w(TAG, "Altering ${statement.subject.table}: ${it.message}")
                }
            } else {
                execution.invoke()
            }
        }
    }

    override fun executeUpdateDelete(statement: Statement): Int {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeUpdateDelete()
    }

    override fun executeInsert(statement: Statement): Long {
        return database.compileStatement(statement.toString(SQLiteDialect)).executeInsert()
    }

    override fun executeQuery(statement: Statement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

}

val SupportSQLiteDatabase.statementExecutor: StatementExecutor
    get() = SQLiteStatementExecutor(this)

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