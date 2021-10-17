@file:Suppress("unused")

package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*

interface TableSubject<T : Table> {

    val table: T

}

interface ViewSubject {

    val view: View

}

fun <T : Table> TableSubject(table: T) = object : TableSubject<T> {
    override val table: T = table
}

fun ViewSubject(view: View) = object : ViewSubject {
    override val view: View = view
}

fun offer(view: View): ViewSubject = ViewSubject(view)

fun <T : Table> offer(table: T): TableSubject<T> = TableSubject(table)

inline fun ViewSubject.create(
    statement: () -> QueryStatement
): ViewCreateStatement = ViewCreateStatement(this, statement())

inline fun ViewSubject.create(
    executor: StatementExecutor,
    statement: () -> QueryStatement
) = executor.execute(create(statement))

fun ViewSubject.select(): ViewSelectStatement = ViewSelectStatement(this)

fun ViewSubject.select(executor: StatementExecutor): Cursor = executor.executeQuery(select())

inline fun <T : Table> TableSubject<T>.create(
    definitions: (T) -> Sequence<Definition>
): TableCreateStatement<T> = TableCreateStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.create(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition>
) = executor.execute(create(definitions))

inline fun <T : Table> TableSubject<T>.alter(
    definitions: (T) -> Sequence<Definition>
): TableAlterStatement<T> = TableAlterStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.alter(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition>
) = executor.execute(alter(definitions))

inline fun <T : Table> TableSubject<T>.drop(
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
): TableDropStatement<T> = TableDropStatement(this, definitions(table))

inline fun <T : Table> TableSubject<T>.drop(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
) = executor.execute(drop(definitions))

fun <T : Table, T2 : Table> TableSubject<T>.innerJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Inner)

fun <T : Table, T2 : Table> TableSubject<T>.outerJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Outer)

fun <T : Table, T2 : Table> TableSubject<T>.crossJoin(table2: T2): Join2Clause<T, T2> =
    Join2Clause(this, table2, JoinType.Cross)

inline fun <T : Table> TableSubject<T>.where(predicate: (T) -> Predicate): WhereClause<T> =
    WhereClause(predicate(table), this)

inline fun <T : Table> TableSubject<T>.groupBy(group: (T) -> Sequence<Column<*>>): GroupClause<T> =
    GroupClause(group(table), this)

inline fun <T : Table> TableSubject<T>.orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> =
    OrderClause(order(table), this)

inline fun <T : Table> TableSubject<T>.limit(limit: () -> Int): LimitClause<T> =
    LimitClause(limit(), this)

inline fun <T : Table> TableSubject<T>.offset(offset: () -> Int): OffsetClause<T> =
    OffsetClause(offset(), limit { -1 }, this)

fun <T : Table> TableSubject<T>.delete(): DeleteStatement<T> = DeleteStatement(this)

fun <T : Table> TableSubject<T>.delete(executor: StatementExecutor): Int =
    executor.executeDelete(delete())

inline fun <T : Table> TableSubject<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> = SelectStatement(this, selection(table))

inline fun <T : Table> TableSubject<T>.select(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table> TableSubject<T>.selectDistinct(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> = SelectStatement(this, selection(table), distinct = true)

inline fun <T : Table> TableSubject<T>.selectDistinct(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor = executor.executeQuery(selectDistinct(selection))

inline fun <T : Table> TableSubject<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): UpdateStatement<T> = UpdateStatement(this, conflictAlgorithm, values(table))


inline fun <T : Table> TableSubject<T>.update(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): Int = executor.executeUpdate(update(conflictAlgorithm, values))

inline fun <T : Table> TableSubject<T>.updateBatch(
    buildAction: UpdateBatchBuilder<T>.() -> Unit
): UpdateBatchStatement<T> = UpdateBatchStatement(
    this,
    UpdateBatchBuilder(this).apply(buildAction)
)

inline fun <T : Table> TableSubject<T>.updateBatch(
    executor: StatementExecutor,
    buildAction: UpdateBatchBuilder<T>.() -> Unit
): Int = executor.executeUpdateBatch(updateBatch(buildAction))

inline fun <T : Table> TableSubject<T>.insert(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): InsertStatement<T> = InsertStatement(this, conflictAlgorithm, values(table))

inline fun <T : Table> TableSubject<T>.insert(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): Long = executor.executeInsert(insert(conflictAlgorithm, values))

inline fun <T : Table> TableSubject<T>.insertBatch(
    buildAction: InsertBatchBuilder<T>.() -> Unit
): InsertBatchStatement<T> = InsertBatchStatement(
    this,
    InsertBatchBuilder(this).apply(buildAction)
)

inline fun <T : Table> TableSubject<T>.insertBatch(
    executor: StatementExecutor,
    buildAction: InsertBatchBuilder<T>.() -> Unit
): Long = executor.executeInsertBatch(insertBatch(buildAction))