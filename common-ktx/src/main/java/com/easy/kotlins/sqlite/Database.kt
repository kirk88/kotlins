@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.easy.kotlins.sqlite.ColumnReflections
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

enum class SqlOrderDirection { ASC, DESC }

fun SQLiteDatabase.insert(tableName: String, vararg values: Pair<String, Any?>): Long {
    return insert(tableName, null, values.toContentValues())
}

fun SQLiteDatabase.insert(tableName: String, valuesFrom: Any): Long {
    return insert(tableName, values = valuesFrom.toPairs())
}

fun SQLiteDatabase.insertOrThrow(tableName: String, vararg values: Pair<String, Any?>): Long {
    return insertOrThrow(tableName, null, values.toContentValues())
}

fun SQLiteDatabase.insertOrThrow(tableName: String, valuesFrom: Any): Long {
    return insertOrThrow(tableName, values = valuesFrom.toPairs())
}

fun SQLiteDatabase.insertWithOnConflict(
    tableName: String,
    conflictAlgorithm: Int,
    vararg values: Pair<String, Any?>
): Long {
    return insertWithOnConflict(tableName, null, values.toContentValues(), conflictAlgorithm)
}

fun SQLiteDatabase.insertWithOnConflict(
    tableName: String,
    conflictAlgorithm: Int,
    valuesFrom: Any
): Long {
    return insertWithOnConflict(tableName, conflictAlgorithm, values = valuesFrom.toPairs())
}

fun SQLiteDatabase.replace(tableName: String, vararg values: Pair<String, Any?>): Long {
    return replace(tableName, null, values.toContentValues())
}

fun SQLiteDatabase.replace(tableName: String, valuesFrom: Any): Long {
    return replace(tableName, values = valuesFrom.toPairs())
}

fun SQLiteDatabase.replaceOrThrow(tableName: String, vararg values: Pair<String, Any?>): Long {
    return replaceOrThrow(tableName, null, values.toContentValues())
}

fun SQLiteDatabase.replaceOrThrow(tableName: String, valuesFrom: Any): Long {
    return replaceOrThrow(tableName, values = valuesFrom.toPairs())
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

fun SQLiteDatabase.select(tableName: String): SelectQueryBuilder {
    return AndroidDatabaseSelectQueryBuilder(this, tableName)
}

fun SQLiteDatabase.select(tableName: String, vararg columns: String): SelectQueryBuilder {
    val builder = AndroidDatabaseSelectQueryBuilder(this, tableName)
    builder.columns(*columns)
    return builder
}

fun SQLiteDatabase.update(
    tableName: String,
    vararg values: Pair<String, Any?>
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, tableName, values)
}

fun SQLiteDatabase.update(
    tableName: String,
    valuesFrom: Any
): UpdateQueryBuilder {
    return AndroidDatabaseUpdateQueryBuilder(this, tableName, valuesFrom.toPairs())
}

fun SQLiteDatabase.delete(
    tableName: String,
    whereClause: String = "",
    vararg whereArgs: Pair<String, Any>
): Int {
    return delete(tableName, applyArguments(whereClause, *whereArgs), null)
}

fun SQLiteDatabase.createTable(
    tableName: String,
    ifNotExists: Boolean = false,
    vararg columns: Pair<String, SqlType>
) {
    val escapedTableName = tableName.replace("`", "``")
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

fun SQLiteDatabase.dropTable(tableName: String, ifExists: Boolean = false) {
    val escapedTableName = tableName.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP TABLE $ifExistsText `$escapedTableName`;")
}

fun SQLiteDatabase.createIndex(
    indexName: String,
    tableName: String,
    unique: Boolean = false,
    ifNotExists: Boolean = false,
    vararg columns: String
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
        )
    )
}

fun SQLiteDatabase.dropIndex(indexName: String, ifExists: Boolean = false) {
    val escapedIndexName = indexName.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP INDEX $ifExistsText `$escapedIndexName`;")
}

fun SQLiteDatabase.createColumn(
    tableName: String,
    ifNotExists: Boolean = false,
    column: Pair<String, SqlType>
) {
    val escapedTableName = tableName.replace("`", "``")
    val exists = if (ifNotExists) {
        rawQuery("SELECT * FROM $tableName LIMIT 0", null).use {
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
    tableName: String,
    ifNotExists: Boolean = false,
    vararg columns: Pair<String, SqlType>
) {
    transaction {
        for (column in columns) {
            createColumn(tableName, ifNotExists, column)
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

@Suppress("UNCHECKED_CAST")
internal fun Any.toPairs(): Array<Pair<String, Any?>> {
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