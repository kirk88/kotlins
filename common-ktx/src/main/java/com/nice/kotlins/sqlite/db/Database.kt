@file:Suppress("unused")

package com.nice.kotlins.sqlite.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.transaction
import java.util.concurrent.atomic.AtomicInteger

enum class SqlOrderDirection { ASC, DESC }

fun SupportSQLiteDatabase.insert(
        table: String,
        conflictAlgorithm: Int,
        vararg values: SqlColumnElement
): Long = insert(table, conflictAlgorithm, values.toContentValues())

fun SupportSQLiteDatabase.insert(
        table: String,
        vararg values: SqlColumnElement
): Long = insert(table, SQLiteDatabase.CONFLICT_NONE, *values)

fun SupportSQLiteDatabase.insert(
        table: String,
        conflictAlgorithm: Int,
        values: Collection<SqlColumnElement>
): Long = insert(table, conflictAlgorithm, values.toContentValues())

fun SupportSQLiteDatabase.insert(
        table: String,
        values: Collection<SqlColumnElement>
): Long = insert(table, SQLiteDatabase.CONFLICT_NONE, values)

fun SupportSQLiteDatabase.delete(
        table: String,
        whereClause: String,
        vararg whereArgs: Pair<String, Any>
): Int = delete(
        table,
        applyArguments(whereClause, *whereArgs),
        null
)

fun SupportSQLiteDatabase.delete(
        table: String,
        condition: SqlWhereCondition
): Int = delete(
        table,
        condition.whereClause,
        condition.whereArgs
)

fun SupportSQLiteDatabase.updateBuilder(table: String): SupportDatabaseUpdateBuilder {
    return SupportDatabaseUpdateBuilder(this, table)
}

fun SupportSQLiteDatabase.queryBuilder(table: String): SupportDatabaseQueryBuilder {
    return SupportDatabaseQueryBuilder(this, table)
}

fun SupportSQLiteDatabase.createTable(
        table: String,
        ifNotExists: Boolean,
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

fun SupportSQLiteDatabase.createTable(
        table: String,
        vararg columns: SqlColumnProperty
) = createTable(table, false, *columns)

fun SupportSQLiteDatabase.createTable(
        table: String,
        ifNotExists: Boolean,
        columns: Collection<SqlColumnProperty>
) = createTable(table, ifNotExists, *columns.toTypedArray())

fun SupportSQLiteDatabase.createTable(
        table: String,
        columns: Collection<SqlColumnProperty>
) = createTable(table, false, columns)


fun SupportSQLiteDatabase.dropTable(
        table: String,
        ifExists: Boolean
) {
    val escapedTableName = table.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP TABLE $ifExistsText `$escapedTableName`;")
}

fun SupportSQLiteDatabase.dropTable(
        table: String
) = dropTable(table, false)

fun SupportSQLiteDatabase.createIndex(
        table: String,
        unique: Boolean,
        ifNotExists: Boolean,
        index: String,
        vararg columns: SqlColumnProperty
) {
    val escapedTableName = table.replace("`", "``")
    val escapedIndexName = index.replace("`", "``")
    val ifNotExistsText = if (ifNotExists) "IF NOT EXISTS" else ""
    val uniqueText = if (unique) "UNIQUE" else ""
    execSQL(columns.joinToString(
            separator = ",",
            prefix = "CREATE $uniqueText INDEX $ifNotExistsText `$escapedIndexName` ON `$escapedTableName`(",
            postfix = ");"
    ) { it.name })
}

fun SupportSQLiteDatabase.createIndex(
        table: String,
        index: String,
        vararg columns: SqlColumnProperty
) = createIndex(
        table = table,
        unique = false,
        ifNotExists = false,
        index = index,
        columns = columns
)

fun SupportSQLiteDatabase.createIndex(
        table: String,
        unique: Boolean,
        ifNotExists: Boolean,
        index: String,
        columns: Collection<SqlColumnProperty>
) = createIndex(table, unique, ifNotExists, index, *columns.toTypedArray())

fun SupportSQLiteDatabase.createIndex(
        table: String,
        index: String,
        columns: Collection<SqlColumnProperty>
) = createIndex(
        table = table,
        unique = false,
        ifNotExists = false,
        index = index,
        columns = columns
)

fun SupportSQLiteDatabase.dropIndex(
        table: String,
        ifExists: Boolean,
        index: String
) {
    val escapedTableName = table.replace("`", "``")
    val escapedIndexName = index.replace("`", "``")
    val ifExistsText = if (ifExists) "IF EXISTS" else ""
    execSQL("DROP INDEX $ifExistsText `$escapedIndexName` ON `$escapedTableName`;")
}

fun SupportSQLiteDatabase.dropIndex(
        table: String,
        index: String
) = dropIndex(table, false, index)

fun SupportSQLiteDatabase.addColumn(
        table: String,
        ifNotExists: Boolean,
        column: SqlColumnProperty
) {
    val escapedTableName = table.replace("`", "``")
    val exists = if (ifNotExists) {
        query("SELECT * FROM `$escapedTableName` LIMIT 0", null).use {
            it.getColumnIndex(column.name) > -1
        }
    } else {
        false
    }
    if (!exists) {
        execSQL("ALTER TABLE `$escapedTableName` ADD ${column.render()};")
    }
}

fun SupportSQLiteDatabase.addColumn(
        table: String,
        column: SqlColumnProperty
) = addColumn(table, false, column)

fun SupportSQLiteDatabase.addColumns(
        table: String,
        ifNotExists: Boolean,
        vararg columns: SqlColumnProperty
) {
    for (column in columns) {
        addColumn(table, ifNotExists, column)
    }
}

fun SupportSQLiteDatabase.addColumns(
        table: String,
        vararg columns: SqlColumnProperty
) = addColumns(table, false, *columns)

fun SupportSQLiteDatabase.addColumns(
        table: String,
        ifNotExists: Boolean,
        columns: Collection<SqlColumnProperty>
) {
    for (column in columns) {
        addColumn(table, ifNotExists, column)
    }
}

fun SupportSQLiteDatabase.addColumns(
        table: String,
        columns: Collection<SqlColumnProperty>
) = addColumns(table, false, columns)

fun Array<out SqlColumnElement>.toContentValues(): ContentValues {
    val values = ContentValues()
    for (element in this) {
        values.put(element)
    }
    return values
}

fun Collection<SqlColumnElement>.toContentValues(): ContentValues {
    val values = ContentValues()
    for (element in this) {
        values.put(element)
    }
    return values
}

private fun ContentValues.put(element: SqlColumnElement) {
    val key = element.name
    when (val value = element.value) {
        null -> putNull(key)
        is String -> put(key, value)
        is Int -> put(key, value)
        is Long -> put(key, value)
        is Double -> put(key, value)
        is Float -> put(key, value)
        is Short -> put(key, value)
        is Boolean -> put(key, value)
        is Byte -> put(key, value)
        is ByteArray -> put(key, value)
        else -> throw IllegalArgumentException("Non-supported value type ${value.javaClass.name}")
    }
}

fun Map<String, Any?>.toColumnElements(): List<SqlColumnElement> {
    return map { SqlColumnElement.create(it.key, it.value) }
}

fun Any.toColumnElements(): List<SqlColumnElement> {
    return ClassReflections.getAdapter(javaClass).read(this)
}

fun Any.toColumnElements(destination: MutableList<SqlColumnElement>): List<SqlColumnElement> {
    destination.addAll(this.toColumnElements())
    return destination
}

val AndroidSQLiteOpenHelperFactory = FrameworkSQLiteOpenHelperFactory()

open class ManagedSQLiteOpenHelper(
        configuration: SupportSQLiteOpenHelper.Configuration,
        factory: SupportSQLiteOpenHelper.Factory = AndroidSQLiteOpenHelperFactory
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