@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

enum class SqlOrderDirection { ASC, DESC }


/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param values this array contains the initial column values for the row,
 * The [Pair.first]  should be the column names and the  [Pair.second] the
 * column values
 */
fun SQLiteDatabase.insert(table: String, vararg values: Pair<String, Any?>): Long {
    return insert(table, null, values.toContentValues())
}

/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param valuesFrom this object contains column names and values, support [Map] or entity annotated with [TableClass]
 */
fun SQLiteDatabase.insert(table: String, valuesFrom: Any): Long {
    return insert(table, values = valuesFrom.toPairs())
}

/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param values this array contains the initial column values for the row,
 * The [Pair.first]  should be the column names and the  [Pair.second] the
 * column values
 *
 * @throws [android.database.SQLException]
 */
fun SQLiteDatabase.insertOrThrow(table: String, vararg values: Pair<String, Any?>): Long {
    return insertOrThrow(table, null, values.toContentValues())
}

/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param valuesFrom this object contains column names and values, support [Map] or entity annotated with [TableClass]
 *
 * @throws [android.database.SQLException]
 */
fun SQLiteDatabase.insertOrThrow(table: String, valuesFrom: Any): Long {
    return insertOrThrow(table, values = valuesFrom.toPairs())
}

/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param conflictAlgorithm for insert conflict resolver
 *
 * @param values this array contains the initial column values for the row,
 * The [Pair.first]  should be the column names and the  [Pair.second] the
 * column values
 */
fun SQLiteDatabase.insertWithOnConflict(
    table: String,
    conflictAlgorithm: Int,
    vararg values: Pair<String, Any?>
): Long {
    return insertWithOnConflict(table, null, values.toContentValues(), conflictAlgorithm)
}

/**
 * Convenience method for inserting a row into the database.
 *
 * @param table the table to insert the row into
 *
 * @param conflictAlgorithm for insert conflict resolver
 *
 * @param valuesFrom this object contains column names and values, support [Map] or entity annotated with [TableClass]
 */
fun SQLiteDatabase.insertWithOnConflict(
    table: String,
    conflictAlgorithm: Int,
    valuesFrom: Any
): Long {
    return insertWithOnConflict(table, conflictAlgorithm, values = valuesFrom.toPairs())
}

/**
 * Convenience method for replacing a row in the database.
 * Inserts a new row if a row does not already exist.
 *
 * @param table the table to insert the row into
 *
 * @param values this array contains the initial column values for the row,
 * The [Pair.first]  should be the column names and the  [Pair.second] the
 * column values
 */
fun SQLiteDatabase.replace(table: String, vararg values: Pair<String, Any?>): Long {
    return replace(table, null, values.toContentValues())
}

/**
 * Convenience method for replacing a row in the database.
 * Inserts a new row if a row does not already exist.
 *
 * @param table the table to insert the row into
 *
 * @param valuesFrom this object contains column names and values, support [Map] or entity annotated with [TableClass]
 */
fun SQLiteDatabase.replace(table: String, valuesFrom: Any): Long {
    return replace(table, values = valuesFrom.toPairs())
}


/**
 * Convenience method for replacing a row in the database.
 * Inserts a new row if a row does not already exist.
 *
 * @param table the table to insert the row into
 *
 * @param values this array contains the initial column values for the row,
 * The [Pair.first]  should be the column names and the  [Pair.second] the
 * column values
 *
 * @throws [android.database.SQLException]
 */
fun SQLiteDatabase.replaceOrThrow(table: String, vararg values: Pair<String, Any?>): Long {
    return replaceOrThrow(table, null, values.toContentValues())
}

/**
 * Convenience method for replacing a row in the database.
 * Inserts a new row if a row does not already exist.
 *
 * @param table the table to insert the row into
 *
 * @param valuesFrom this object contains column names and values, support [Map] or entity annotated with [TableClass]
 *
 * @throws [android.database.SQLException]
 */
fun SQLiteDatabase.replaceOrThrow(table: String, valuesFrom: Any): Long {
    return replaceOrThrow(table, values = valuesFrom.toPairs())
}

fun SQLiteDatabase.select(table: String): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, table)
}

fun SQLiteDatabase.select(table: String, vararg columns: String): SelectQueryBuilder {
    val builder = AndroidDatabaseSelectQueryBuilder(this, table)
    builder.columns(*columns)
    return builder
}

fun SQLiteDatabase.update(
    table: String,
    vararg values: Pair<String, Any?>
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table, values)
}

fun SQLiteDatabase.update(
    table: String,
    valuesFrom: Any
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, table, valuesFrom.toPairs())
}

fun SQLiteDatabase.delete(
    table: String,
    whereClause: String = "",
    vararg whereArgs: Pair<String, Any>
): Int {
    return delete(table, applyArguments(whereClause, *whereArgs), null)
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
    vararg columns: Pair<String, SqlType>
) {
    val escapedTableName = table.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    execSQL(
        columns.joinToString(
            ", ",
            prefix = "CREATE TABLE $ifNotExistsText `$escapedTableName`(",
            postfix = ");"
        ) { col ->
            "${col.first} ${col.second.render()}"
        }
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

fun SQLiteDatabase.dropIndex(indexName: String, ifExists: Boolean = false) {
    val escapedIndexName = indexName.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP INDEX $ifExistsText `$escapedIndexName`;")
}

fun SQLiteDatabase.createColumn(
    table: String,
    ifNotExists: Boolean = false,
    column: Pair<String, SqlType>
) {
    val escapedTableName = table.replace("`", "``")
    val exists = if (ifNotExists) {
        rawQuery("SELECT * FROM $table LIMIT 0", null).use {
            it.getColumnIndex(column.first) != -1
        }
    } else {
        false
    }
    if (!exists) {
        execSQL("ALTER TABLE $escapedTableName ADD ${column.first} ${column.second.render()}")
    }
}

fun SQLiteDatabase.createColumns(
    table: String,
    ifNotExists: Boolean = false,
    vararg columns: Pair<String, SqlType>
) {
    transaction {
        for (column in columns) {
            createColumn(table, ifNotExists, column)
        }
    }
}

private val ARG_PATTERN: Pattern = Pattern.compile("([^\\\\])\\{([^{}]+)\\}")

internal fun applyArguments(whereClause: String, vararg args: Pair<String, Any>): String {
    val argsMap = args.fold(hashMapOf<String, Any>()) { map, arg ->
        map[arg.first] = arg.second
        map
    }
    return applyArguments(whereClause, argsMap)
}

internal fun applyArguments(whereClause: String, args: Map<String, Any>): String {
    val matcher = ARG_PATTERN.matcher(whereClause)
    val buffer = StringBuffer(whereClause.length)
    while (matcher.find()) {
        val key = matcher.group(2)
        val value = args[key] ?: throw IllegalStateException("Can't find a value for key $key")

        val valueString = if (value is Int || value is Long || value is Byte || value is Short) {
            value.toString()
        } else if (value is Boolean) {
            if (value) "1" else "0"
        } else if (value is Float || value is Double) {
            value.toString()
        } else {
            '\'' + value.toString().replace("'", "''") + '\''
        }
        matcher.appendReplacement(buffer, matcher.group(1) + valueString)
    }
    matcher.appendTail(buffer)
    return buffer.toString()
}

internal fun Array<out Pair<String, Any?>>.toContentValues(): ContentValues {
    val values = ContentValues()
    for ((key, value) in this) {
        when (value) {
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

internal fun Any.toPairs(): Array<Pair<String, Any?>> {
    if (this is Map<*, *>) {
        return this.map { it.key.toString() to it.value }.toTypedArray()
    }
    if (!javaClass.isAnnotationPresent(TableClass::class.java)) {
        throw IllegalStateException("The ${javaClass.name} Class is not annotated with TableClass")
    }
    return ColumnReflections.get(this) {
        Modifier.isTransient(it.modifiers)
                || Modifier.isStatic(it.modifiers)
                || it.isAnnotationPresent(IgnoredOnTable::class.java)
    }
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