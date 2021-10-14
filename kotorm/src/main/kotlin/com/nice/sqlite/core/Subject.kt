@file:Suppress("unused")

package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*

interface Subject<T : Table> {

    val table: T

    val executor: StatementExecutor

}

interface ViewSubject {

    val view: View

    val executor: StatementExecutor

}

inline fun ViewSubject.createStatement(statement: () -> QueryStatement): CreateViewStatement {
    return CreateViewStatement(this, statement())
}

inline fun ViewSubject.create(
    statement: () -> QueryStatement
) {
    executor.execute(createStatement(statement))
}

inline fun ViewSubject.selectStatement(): SelectViewStatement {
    return SelectViewStatement(this)
}

inline fun ViewSubject.select(): Cursor {
    return executor.executeQuery(selectStatement())
}

inline fun <T : Table> Subject<T>.createStatement(definitions: (T) -> Sequence<Definition>): CreateStatement<T> {
    return CreateStatement(this, definitions(table))
}

inline fun <T : Table> Subject<T>.create(
    definitions: (T) -> Sequence<Definition>
) {
    executor.execute(createStatement(definitions))
}

inline fun <T : Table> Subject<T>.alterStatement(definitions: (T) -> Sequence<Definition>): AlterStatement<T> {
    return AlterStatement(this, definitions(table))
}

inline fun <T : Table> Subject<T>.alter(
    definitions: (T) -> Sequence<Definition>
) {
    executor.execute(alterStatement(definitions))
}

inline fun <T : Table> Subject<T>.dropStatement(
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
): DropStatement<T> {
    return DropStatement(this, definitions(table))
}

inline fun <T : Table> Subject<T>.drop(
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
) {
    executor.execute(dropStatement(definitions))
}

inline fun <T : Table, T2 : Table> Subject<T>.join(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Inner)
}

inline fun <T : Table, T2 : Table> Subject<T>.outerJoin(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Outer)
}

inline fun <T : Table, T2 : Table> Subject<T>.crossJoin(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Cross)
}

inline fun <T : Table> Subject<T>.where(predicate: (T) -> Predicate): WhereClause<T> {
    return WhereClause(predicate(table), this)
}

inline fun <T : Table> Subject<T>.groupBy(group: (T) -> Sequence<Column<*>>): GroupClause<T> {
    return GroupClause(group(table), this)
}

inline fun <T : Table> Subject<T>.orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> {
    return OrderClause(order(table), this)
}

inline fun <T : Table> Subject<T>.limit(limit: () -> Int): LimitClause<T> {
    return LimitClause(limit(), this)
}

inline fun <T : Table> Subject<T>.offset(offset: () -> Int): OffsetClause<T> {
    return OffsetClause(offset(), limit { -1 }, this)
}

inline fun <T : Table> Subject<T>.selectStatement(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(this, selection(table))
}

inline fun <T : Table> Subject<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(selectStatement(selection))
}

inline fun <T : Table> Subject<T>.selectDistinctStatement(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(this, selection(table), distinct = true)
}

inline fun <T : Table> Subject<T>.selectDistinct(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinctStatement(selection))
}

inline fun <T : Table> Subject<T>.updateStatement(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): UpdateStatement<T> {
    return UpdateStatement(this, conflictAlgorithm, values(table))
}

inline fun <T : Table> Subject<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): Int {
    return executor.executeUpdate(updateStatement(conflictAlgorithm, values))
}

inline fun <T : Table> Subject<T>.updateBatchStatement(
    buildAction: UpdateBatchBuilder<T>.() -> Unit
): UpdateBatchStatement<T> {
    return UpdateBatchStatement(this, UpdateBatchBuilder(this).apply(buildAction))
}

inline fun <T : Table> Subject<T>.updateBatch(
    buildAction: UpdateBatchBuilder<T>.() -> Unit
): Int {
    return executor.executeUpdateBatch(updateBatchStatement(buildAction))
}

inline fun <T : Table> Subject<T>.deleteStatement(): DeleteStatement<T> {
    return DeleteStatement(this)
}

inline fun <T : Table> Subject<T>.delete(): Int {
    return executor.executeDelete(deleteStatement())
}

inline fun <T : Table> Subject<T>.insertStatement(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): InsertStatement<T> {
    return InsertStatement(this, conflictAlgorithm, values(table))
}


inline fun <T : Table> Subject<T>.insert(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): Long {
    return executor.executeInsert(insertStatement(conflictAlgorithm, values))
}

inline fun <T : Table> Subject<T>.insertBatchStatement(
    buildAction: InsertBatchBuilder<T>.() -> Unit
): InsertBatchStatement<T> {
    return InsertBatchStatement(this, InsertBatchBuilder(this).apply(buildAction))
}

inline fun <T : Table> Subject<T>.insertBatch(
    buildAction: InsertBatchBuilder<T>.() -> Unit
): Long {
    return executor.executeInsertBatch(insertBatchStatement(buildAction))
}