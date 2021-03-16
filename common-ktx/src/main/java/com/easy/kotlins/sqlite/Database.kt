@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.easy.kotlins.sqlite.SqlColumnElement
import com.easy.kotlins.sqlite.SqlColumnProperty
import com.easy.kotlins.sqlite.SqlWhereCondition
import com.easy.kotlins.sqlite.applyArguments
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger

enum class SqlOrderDirection { ASC, DESC }

fun SQLiteDatabase.insert(table: String, vararg values: SqlColumnElement): Long {
    return insert(table, null, values.toContentValues())
}

fun SQLiteDatabase.insertOrThrow(table: String, vararg values: SqlColumnElement): Long {
    return insertOrThrow(table, null, values.toContentValues())
}

fun SQLiteDatabase.insertWithOnConflict(
    table: String,
    conflictAlgorithm: Int,
    vararg values: SqlColumnElement
): Long {
    return insertWithOnConflict(table, null, values.toContentValues(), conflictAlgorithm)
}

fun SQLiteDatabase.replace(table: String, vararg values: SqlColumnElement): Long {
    return replace(table, null, values.toContentValues())
}

fun SQLiteDatabase.replaceOrThrow(table: String, vararg values: SqlColumnElement): Long {
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
    return delete(table, whereCondition.whereCause, whereCondition.whereArgs)
}

fun SQLiteDatabase.queryBuilder(table: String): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table)
}

fun SQLiteDatabase.queryBuilder(
    table: String,
    vararg columns: SqlColumnProperty
): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table).also {
        it.columns(*columns)
    }
}

fun SQLiteDatabase.updateBuilder(
    table: String,
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table)
}

fun SQLiteDatabase.updateBuilder(
    table: String,
    vararg values: SqlColumnElement
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table).also {
        it.values(*values)
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
    execSQL(
        columns.joinToString(
            ", ",
            prefix = "CREATE TABLE $ifNotExistsText `$escapedTableName`(",
            postfix = ");"
        ) { col -> col.render() }
    )
}

fun SQLiteDatabase.dropTable(table: String, ifExists: Boolean = false) {
    val escapedTableName = table.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP TABLE $ifExistsText `$escapedTableName`;")
}

fun SQLiteDatabase.createIndex(
    indexName: String,
    table: String,
    unique: Boolean = false,
    ifNotExists: Boolean = false,
    vararg columns: String
) {
    val escapedIndexName = indexName.replace("`", "``")
    val escapedTableName = table.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    val uniqueText = if (unique) "UNIQUE" else ""
    execSQL(
        columns.joinToString(
            separator = ",",
            prefix = "CREATE $uniqueText INDEX $ifNotExistsText `$escapedIndexName` ON `$escapedTableName`(",
            postfix = ");"
        )
    )
}

fun SQLiteDatabase.createIndex(
    indexName: String,
    tableName: String,
    unique: Boolean = false,
    ifNotExists: Boolean = false,
    vararg columns: SqlColumnProperty
) {
    val escapedIndexName = indexName.replace("`", "``")
    val escapedTableName = tableName.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    val uniqueText = if (unique) "UNIQUE" else ""
    execSQL(
        columns.joinToString(
            separator = ",",
            prefix = "CREATE $uniqueText INDEX $ifNotExistsText `$escapedIndexName` ON `$escapedTableName`(",
            postfix = ");"
        ) { it.name }
    )
}

fun SQLiteDatabase.dropIndex(indexName: String, ifExists: Boolean = false) {
    val escapedIndexName = indexName.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP INDEX $ifExistsText `$escapedIndexName`;")
}

fun SQLiteDatabase.createColumn(
    table: String,
    ifNotExists: Boolean = false,
    column: SqlColumnProperty
) {
    val escapedTableName = table.replace("`", "``")
    val exists = if (ifNotExists) {
        rawQuery("SELECT * FROM $table LIMIT 0", null).use {
            it.getColumnIndex(column.name) != -1
        }
    } else {
        false
    }
    if (!exists) {
        execSQL("ALTER TABLE $escapedTableName ADD ${column.render()}")
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

fun Array<out SqlColumnElement>.toContentValues(): ContentValues {
    val values = ContentValues()
    for (element in this) {
        val key = element.name
        when (val value = element.value) {
            null -> values.putNull(key)
            is Boolean -> values.put(key, value)
            is Byte -> values.put(key, value)
            is ByteArray -> values.put(key, value)
            is Double -> values.put(key, value)
            is Float -> values.put(key, value)
            is Int -> values.put(key, value)
            is Long -> values.put(key, value)
            is Short -> values.put(key, value)
            is String -> values.put(key, value)
            else -> throw IllegalArgumentException("Non-supported value type ${value.javaClass.name}")
        }
    }
    return values
}

fun Iterable<SqlColumnElement>.toContentValues(): ContentValues {
    return this.toList().toTypedArray().toContentValues()
}

fun Any.toColumnElements(): Array<SqlColumnElement> {
    check(javaClass.isAnnotationPresent(DatabaseTable::class.java)) { "Class $javaClass must be annotated with DatabaseTable" }
    return ClassReflections.getAdapter(javaClass) {
        Modifier.isTransient(it.modifiers) || Modifier.isStatic(it.modifiers)
    }.read(this)
}

fun Map<out String, Any?>.toColumnElements(): Array<SqlColumnElement> {
    return map { SqlColumnElement.create(it.key, it.value) }.toTypedArray()
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