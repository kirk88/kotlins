@file:Suppress("UNUSED")

package com.nice.sqlite.core

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*
import com.nice.sqlite.statementExecutor

interface TableSubject<T : Table> {
    val table: T
}

interface ViewSubject {
    val view: View
}

interface TriggerSubject<T : Table> {
    val trigger: Trigger<T>
}

internal fun <T : Table> TableSubject(table: T) = object : TableSubject<T> {
    override val table: T = table
}

internal fun ViewSubject(view: View) = object : ViewSubject {
    override val view: View = view
}

internal fun <T : Table> TriggerSubject(trigger: Trigger<T>) = object : TriggerSubject<T> {
    override val trigger: Trigger<T> = trigger
}

fun <T : Table> offer(trigger: Trigger<T>): TriggerSubject<T> = TriggerSubject(trigger)

fun offer(view: View): ViewSubject = ViewSubject(view)

fun <T : Table> offer(table: T): TableSubject<T> = TableSubject(table)

fun <T : Table> TriggerSubject<T>.create(): TriggerCreateStatement<T> = TriggerCreateStatement(this)

fun <T : Table> TriggerSubject<T>.create(database: SupportSQLiteDatabase) = database.statementExecutor.execute(create())

fun ViewSubject.create(): ViewCreateStatement = ViewCreateStatement(this)

fun ViewSubject.create(database: SupportSQLiteDatabase) = database.statementExecutor.execute(create())

fun ViewSubject.select(): ViewSelectStatement = ViewSelectStatement(this)

fun ViewSubject.select(database: SupportSQLiteDatabase): Cursor = database.statementExecutor.executeQuery(select())

inline fun <T : Table> TableSubject<T>.create(
    crossinline definitions: (T) -> Shell<Definition>
): TableCreateStatement<T> = TableCreateStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.create(
    database: SupportSQLiteDatabase,
    crossinline definitions: (T) -> Shell<Definition>
) = database.statementExecutor.execute(create(definitions))

inline fun <T : Table> TableSubject<T>.alter(
    crossinline definitions: (T) -> Shell<Definition>
): TableAlterStatement<T> = TableAlterStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.alter(
    database: SupportSQLiteDatabase,
    crossinline definitions: (T) -> Shell<Definition>
) = database.statementExecutor.execute(alter(definitions))

inline fun <T : Table> TableSubject<T>.drop(
    crossinline definitions: (T) -> Shell<Definition> = { emptyShell() }
): TableDropStatement<T> = TableDropStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.drop(
    database: SupportSQLiteDatabase,
    crossinline definitions: (T) -> Shell<Definition> = { emptyShell() }
) = database.statementExecutor.execute(drop(definitions))

fun <T : Table, T2 : Table> TableSubject<T>.innerJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Inner)

fun <T : Table, T2 : Table> TableSubject<T>.outerJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Outer)

fun <T : Table, T2 : Table> TableSubject<T>.crossJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Cross)

inline fun <T : Table> TableSubject<T>.where(predicate: (T) -> Predicate): WhereClause<T> =
    WhereClause(predicate(table), this)

inline fun <T : Table> TableSubject<T>.groupBy(crossinline group: (T) -> Shell<Column<*>>): GroupClause<T> =
    GroupClause(group(table), this)

inline fun <T : Table> TableSubject<T>.orderBy(order: (T) -> Shell<Ordering>): OrderClause<T> =
    OrderClause(order(table), this)

inline fun <T : Table> TableSubject<T>.limit(limit: () -> Int): LimitClause<T> =
    LimitClause(limit(), this)

inline fun <T : Table> TableSubject<T>.offset(offset: () -> Int): OffsetClause<T> =
    OffsetClause(offset(), limit { -1 }, this)

fun <T : Table> TableSubject<T>.delete(): DeleteStatement<T> = DeleteStatement(this)

fun <T : Table> TableSubject<T>.delete(database: SupportSQLiteDatabase): Int =
    database.statementExecutor.executeDelete(delete())

inline fun <T : Table> TableSubject<T>.select(
    crossinline selection: (T) -> Shell<Definition> = { emptyShell() }
): SelectStatement<T> = SelectStatement(this, selection(table))

inline fun <T : Table> TableSubject<T>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T) -> Shell<Definition> = { emptyShell() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table> TableSubject<T>.selectDistinct(
    crossinline selection: (T) -> Shell<Definition> = { emptyShell() }
): SelectStatement<T> = SelectStatement(this, selection(table), distinct = true)

inline fun <T : Table> TableSubject<T>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T) -> Shell<Definition> = { emptyShell() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

inline fun <T : Table> TableSubject<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Shell<ColumnValue>
): UpdateStatement<T> = UpdateStatement(this, conflictAlgorithm, values(table), nativeBindValues = nativeBindValues)


inline fun <T : Table> TableSubject<T>.update(
    database: SupportSQLiteDatabase,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Shell<ColumnValue>
): Int = database.statementExecutor.executeUpdate(update(conflictAlgorithm, nativeBindValues, values))

inline fun <T : Table> TableSubject<T>.updateBatch(
    crossinline buildAction: UpdateBatchBuilder<T>.() -> Unit
): UpdateBatchStatement<T> = UpdateBatchStatement(
    this,
    UpdateBatchBuilder(this).apply(buildAction)
)

inline fun <T : Table> TableSubject<T>.updateBatch(
    database: SupportSQLiteDatabase,
    crossinline buildAction: UpdateBatchBuilder<T>.() -> Unit
): Int = database.statementExecutor.executeUpdateBatch(updateBatch(buildAction))

inline fun <T : Table> TableSubject<T>.insert(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Shell<ColumnValue>
): InsertStatement<T> = InsertStatement(this, conflictAlgorithm, values(table), nativeBindValues = nativeBindValues)

inline fun <T : Table> TableSubject<T>.insert(
    database: SupportSQLiteDatabase,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Shell<ColumnValue>
): Long = database.statementExecutor.executeInsert(insert(conflictAlgorithm, nativeBindValues, values))

inline fun <T : Table> TableSubject<T>.insertBatch(
    crossinline buildAction: InsertBatchBuilder<T>.() -> Unit
): InsertBatchStatement<T> = InsertBatchStatement(
    this,
    InsertBatchBuilder(this).apply(buildAction)
)

inline fun <T : Table> TableSubject<T>.insertBatch(
    database: SupportSQLiteDatabase,
    crossinline buildAction: InsertBatchBuilder<T>.() -> Unit
): Long = database.statementExecutor.executeInsertBatch(insertBatch(buildAction))