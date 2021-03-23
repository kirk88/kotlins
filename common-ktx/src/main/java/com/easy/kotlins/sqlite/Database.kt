@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger

enum class SqlOrderDirection { ASC, DESC }

fun SQLiteDatabase.insert(table: String, vararg values: SqlColumnElement): Long {
    return insert(table, null, values.toContentValues())
}

fun SQLiteDatabase.insert(table: String, values: List<SqlColumnElement>): Long {
    return insert(table, null, values.toContentValues())
}

fun SQLiteDatabase.insertOrThrow(table: String, vararg values: SqlColumnElement): Long {
    return insertOrThrow(table, null, values.toContentValues())
}

fun SQLiteDatabase.insertOrThrow(table: String, values: List<SqlColumnElement>): Long {
    return insertOrThrow(table, null, values.toContentValues())
}

fun SQLiteDatabase.insertWithOnConflict(
    table: String,
    conflictAlgorithm: Int,
    vararg values: SqlColumnElement
): Long {
    return insertWithOnConflict(table, null, values.toContentValues(), conflictAlgorithm)
}

fun SQLiteDatabase.insertWithOnConflict(
    table: String,
    conflictAlgorithm: Int,
    values: List<SqlColumnElement>
): Long {
    return insertWithOnConflict(table, null, values.toContentValues(), conflictAlgorithm)
}

fun SQLiteDatabase.replace(table: String, vararg values: SqlColumnElement): Long {
    return replace(table, null, values.toContentValues())
}

fun SQLiteDatabase.replace(table: String, values: List<SqlColumnElement>): Long {
    return replace(table, null, values.toContentValues())
}

fun SQLiteDatabase.replaceOrThrow(table: String, vararg values: SqlColumnElement): Long {
    return replaceOrThrow(table, null, values.toContentValues())
}

fun SQLiteDatabase.replaceOrThrow(table: String, values: List<SqlColumnElement>): Long {
    return replaceOrThrow(table, null, values.toContentValues())
}

fun SQLiteDatabase.delete(
    table: String,
    whereClause: String = "",
    vararg whereArgs: Pair<String, Any>
): Int {
    return delete(table, applyArguments(whereClause, *whereArgs), null)
}

fun SQLiteDatabase.delete(
    table: String,
    whereCondition: SqlWhereCondition
): Int {
    return delete(table, whereCondition.whereClause, whereCondition.whereArgs)
}

fun SQLiteDatabase.update(
    table: String,
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table)
}

fun SQLiteDatabase.update(
    table: String,
    vararg values: SqlColumnElement
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table).also {
        it.values(*values)
    }
}

fun SQLiteDatabase.update(
    table: String,
    values: List<SqlColumnElement>
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table).also {
        it.values(values)
    }
}

fun SQLiteDatabase.query(table: String): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table)
}

fun SQLiteDatabase.query(
    table: String,
    vararg columns: SqlColumnProperty
): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table).also {
        it.columns(*columns)
    }
}

fun SQLiteDatabase.query(
    table: String,
    columns: List<SqlColumnProperty>
): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table).also {
        it.columns(columns)
    }
}

fun <T> SQLiteDatabase.transaction(action: SQLiteDatabase.() -> T): T {
    val result: T
    try {
        beginTransaction()
        result = action()
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
    return result
}

fun SQLiteDatabase.createTable(
    table: String,
    ifNotExists: Boolean = false,
    vararg columns: SqlColumnProperty
) {
    val escapedTableName = table.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    execSQL(columns.joinToString(
        ", ",
        prefix = "CREATE TABLE $ifNotExistsText `$escapedTableName`(",
        postfix = ");"
    ) { col -> col.render() })
}

fun SQLiteDatabase.createTable(
    table: String,
    ifNotExists: Boolean = false,
    columns: List<SqlColumnProperty>
) = createTable(table, ifNotExists, *columns.toTypedArray())

fun SQLiteDatabase.dropTable(
    table: String,
    ifExists: Boolean = false
) {
    val escapedTableName = table.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP TABLE $ifExistsText `$escapedTableName`;")
}

fun SQLiteDatabase.createIndex(
    index: String,
    table: String,
    unique: Boolean = false,
    ifNotExists: Boolean = false,
    vararg columns: SqlColumnProperty
) {
    val escapedIndexName = index.replace("`", "``")
    val escapedTableName = table.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    val uniqueText = if (unique) "UNIQUE" else ""
    execSQL(columns.joinToString(
        separator = ",",
        prefix = "CREATE $uniqueText INDEX $ifNotExistsText `$escapedIndexName` ON `$escapedTableName`(",
        postfix = ");"
    ) { it.name })
}

fun SQLiteDatabase.createIndex(
    index: String,
    table: String,
    unique: Boolean = false,
    ifNotExists: Boolean = false,
    columns: List<SqlColumnProperty>
) = createIndex(index, table, unique, ifNotExists, *columns.toTypedArray())

fun SQLiteDatabase.dropIndex(
    index: String,
    table: String,
    ifExists: Boolean = false
) {
    val escapedIndexName = index.replace("`", "``")
    val escapedTableName = table.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP INDEX $ifExistsText `$escapedIndexName` ON `$escapedTableName`;")
}

fun SQLiteDatabase.createColumn(
    table: String,
    ifNotExists: Boolean = false,
    column: SqlColumnProperty
) {
    val escapedTableName = table.replace("`", "``")
    val exists = if (ifNotExists) {
        rawQuery("SELECT * FROM `$escapedTableName` LIMIT 0", null).use {
            it.getColumnIndex(column.name) > -1
        }
    } else {
        false
    }
    if (!exists) {
        execSQL("ALTER TABLE `$escapedTableName` ADD ${column.render()};")
    }
}

fun SQLiteDatabase.createColumns(
    table: String,
    ifNotExists: Boolean = false,
    vararg columns: SqlColumnProperty
) {
    transaction {
        for (column in columns) {
            createColumn(table, ifNotExists, column)
        }
    }
}

fun SQLiteDatabase.createColumns(
    table: String,
    ifNotExists: Boolean = false,
    columns: List<SqlColumnProperty>
) = createColumns(table, ifNotExists, *columns.toTypedArray())

fun Array<out SqlColumnElement>.toContentValues(): ContentValues {
    val values = ContentValues()
    for (element in this) {
        values.put(element)
    }
    return values
}

fun List<SqlColumnElement>.toContentValues(): ContentValues {
    val values = ContentValues()
    for (element in this) {
        values.put(element)
    }
    return values
}

private fun ContentValues.put(element: SqlColumnElement) {
    val key = element.name
    when (val value = element.value) {
        is String -> put(key, value)
        is Int -> put(key, value)
        is Long -> put(key, value)
        is Double -> put(key, value)
        is Float -> put(key, value)
        is Short -> put(key, value)
        is Boolean -> put(key, value)
        is Byte -> put(key, value)
        is ByteArray -> put(key, value)
        null -> putNull(key)
        else -> throw IllegalArgumentException("Non-supported value type ${value.javaClass.name}")
    }
}

fun Any.toColumnElements(): List<SqlColumnElement> {
    return ClassReflections.getAdapter(javaClass) {
        Modifier.isTransient(it.modifiers)
                || Modifier.isStatic(it.modifiers)
                || it.isAnnotationPresent(IgnoreOnTable::class.java)
    }.read(this)
}

abstract class ManagedSQLiteOpenHelper(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = 1
) : SQLiteOpenHelper(context, name, factory, version) {

    private val counter = AtomicInteger()
    private var db: SQLiteDatabase? = null

    fun <T> use(inTransaction: Boolean = false, action: SQLiteDatabase.() -> T): T {
        try {
            return openDatabase().let {
                if (inTransaction) it.transaction(action)
                else it.action()
            }
        } finally {
            closeDatabase()
        }
    }

    @Synchronized
    private fun openDatabase(): SQLiteDatabase {
        if (counter.incrementAndGet() == 1) {
            db = writableDatabase
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