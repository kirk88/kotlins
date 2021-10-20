@file:Suppress("unused")

package com.nice.sqlite

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteStatement
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import com.nice.sqlite.core.ddl.*
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

val SupportSQLiteDatabase.statementExecutor: StatementExecutor
    get() = SQLiteStatementExecutor(this)

private class SQLiteStatementExecutor(private val database: SupportSQLiteDatabase) :
    StatementExecutor {

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
            numberOfRows += database.compileStatement(executable.sql).apply {
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
            lastRowId = database.compileStatement(executable.sql).apply {
                bindValues(executable.values)
            }.executeInsert()
        }
        return lastRowId
    }

    override fun executeQuery(statement: QueryStatement): Cursor {
        return database.query(statement.toString(SQLiteDialect))
    }

    private fun SupportSQLiteStatement.bindValues(assignments: Sequence<Value>) {
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

}

private val ANDROID_SQLITE_OPEN_HELPER_FACTORY = FrameworkSQLiteOpenHelperFactory()

enum class Transaction {
    None,
    Immediate,
    Exclusive
}

abstract class ManagedSQLiteOpenHelper(
    context: Context,
    name: String,
    version: Int,
    useNoBackupDirectory: Boolean = false,
    factory: SupportSQLiteOpenHelper.Factory = ANDROID_SQLITE_OPEN_HELPER_FACTORY
) {

    private val delegate: SupportSQLiteOpenHelper = factory.create(
        SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .noBackupDirectory(useNoBackupDirectory)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        this@ManagedSQLiteOpenHelper.onConfigure(db)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        this@ManagedSQLiteOpenHelper.onCreate(db)
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                        this@ManagedSQLiteOpenHelper.onUpgrade(db, oldVersion, newVersion)
                    }

                    override fun onDowngrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                        this@ManagedSQLiteOpenHelper.onDowngrade(db, oldVersion, newVersion)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        this@ManagedSQLiteOpenHelper.onOpen(db)
                    }

                    override fun onCorruption(db: SupportSQLiteDatabase) {
                        this@ManagedSQLiteOpenHelper.onCorruption(db)
                    }
                }
            ).build()
    )

    private val counter = AtomicInteger()
    private var db: SupportSQLiteDatabase? = null

    fun <T> use(
        transaction: Transaction = Transaction.None,
        action: SupportSQLiteDatabase.() -> T
    ): T {
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

    open fun onConfigure(db: SupportSQLiteDatabase) {}

    abstract fun onCreate(db: SupportSQLiteDatabase)

    abstract fun onUpgrade(
        db: SupportSQLiteDatabase, oldVersion: Int,
        newVersion: Int
    )

    open fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw SQLiteException(
            "Can't downgrade database from version "
                    + oldVersion + " to " + newVersion
        )
    }

    open fun onOpen(db: SupportSQLiteDatabase) {}

    open fun onCorruption(db: SupportSQLiteDatabase) {
        Log.e(TAG, "Corruption reported by sqlite on database: " + db.path)
        if (!db.isOpen) {
            deleteDatabaseFile(db.path)
            return
        }
        var attachedDbs: List<Pair<String?, String>>? = null
        try {
            try {
                attachedDbs = db.attachedDbs
            } catch (_: SQLiteException) {
            }
            try {
                db.close()
            } catch (_: IOException) {
            }
        } finally {
            if (attachedDbs != null) {
                for (p in attachedDbs) {
                    deleteDatabaseFile(p.second)
                }
            } else {
                deleteDatabaseFile(db.path)
            }
        }
    }

    private fun deleteDatabaseFile(fileName: String) {
        if (fileName.equals(":memory:", ignoreCase = true)
            || fileName.trim { it <= ' ' }.isEmpty()
        ) {
            return
        }
        Log.w(TAG, "deleting the database file: $fileName")
        try {
            val deleted = File(fileName).delete()
            if (!deleted) {
                Log.e(TAG, "Could not delete the database file $fileName")
            }
        } catch (error: Exception) {
            Log.e(TAG, "error while deleting corrupted database file", error)
        }
    }

}