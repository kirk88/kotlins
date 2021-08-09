@file:Suppress("unused")

package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*

interface Subject<T : Table> {

    val table: T

}

interface ViewSubject {

    val view: View

}

class StatementSubject<T : Table> @PublishedApi internal constructor(override val table: T) :
    Subject<T>

class StatementViewSubject @PublishedApi internal constructor(override val view: View) : ViewSubject

inline fun offer(view: View): StatementViewSubject {
    return StatementViewSubject(view)
}

inline fun StatementViewSubject.create(statement: () -> QueryStatement): CreateViewStatement {
    return CreateViewStatement(this, statement())
}

inline fun StatementViewSubject.select(): SelectViewStatement {
    return SelectViewStatement(this)
}

inline fun StatementViewSubject.select(executor: StatementExecutor): Cursor {
    return executor.executeQuery(select())
}

inline fun <T : Table> offer(table: T): StatementSubject<T> {
    return StatementSubject(table)
}

inline fun <T : Table> StatementSubject<T>.create(definitions: (T) -> Sequence<Definition>): CreateStatement<T> {
    return CreateStatement(this, definitions(table))
}

inline fun <T : Table> StatementSubject<T>.create(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition>
) {
    executor.execute(create(definitions))
}

inline fun <T : Table> StatementSubject<T>.alter(definitions: (T) -> Sequence<Definition>): AlterStatement<T> {
    return AlterStatement(this, definitions(table))
}

inline fun <T : Table> StatementSubject<T>.alter(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition>
) {
    executor.execute(alter(definitions))
}

inline fun <T : Table> StatementSubject<T>.drop(
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
): DropStatement<T> {
    return DropStatement(this, definitions(table))
}

inline fun <T : Table> StatementSubject<T>.drop(
    executor: StatementExecutor,
    definitions: (T) -> Sequence<Definition> = { emptySequence() }
) {
    executor.execute(drop(definitions))
}

inline fun <T : Table, T2 : Table> StatementSubject<T>.join(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Inner)
}

inline fun <T : Table, T2 : Table> StatementSubject<T>.outerJoin(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Outer)
}

inline fun <T : Table, T2 : Table> StatementSubject<T>.crossJoin(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.Cross)
}

inline fun <T : Table> StatementSubject<T>.where(predicate: (T) -> Predicate): WhereClause<T> {
    return WhereClause(predicate(table), this)
}

inline fun <T : Table> StatementSubject<T>.groupBy(group: (T) -> Sequence<Column>): GroupClause<T> {
    return GroupClause(group(table), this)
}

inline fun <T : Table> StatementSubject<T>.orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> {
    return OrderClause(order(table), this)
}

inline fun <T : Table> StatementSubject<T>.limit(limit: () -> Int): LimitClause<T> {
    return LimitClause(limit(), this)
}

inline fun <T : Table> StatementSubject<T>.offset(offset: () -> Int): OffsetClause<T> {
    return OffsetClause(offset(), limit { -1 }, this)
}

inline fun <T : Table> StatementSubject<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(this, selection(table))
}

inline fun <T : Table> StatementSubject<T>.select(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table> StatementSubject<T>.selectDistinct(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(this, selection(table), distinct = true)
}

inline fun <T : Table> StatementSubject<T>.selectDistinct(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

inline fun <T : Table> StatementSubject<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Assignment>
): UpdateStatement<T> {
    return UpdateStatement(this, values(table), conflictAlgorithm)
}

inline fun <T : Table> StatementSubject<T>.update(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Assignment>
): Int {
    return executor.executeUpdateDelete(update(conflictAlgorithm, values))
}

inline fun <T : Table> StatementSubject<T>.delete(): DeleteStatement<T> {
    return DeleteStatement(this)
}

inline fun <T : Table> StatementSubject<T>.delete(executor: StatementExecutor): Int {
    return executor.executeUpdateDelete(delete())
}

inline fun <T : Table> StatementSubject<T>.insert(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Assignment>
): InsertStatement<T> {
    return InsertStatement(this, values(table), conflictAlgorithm)
}


inline fun <T : Table> StatementSubject<T>.insert(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Assignment>
): Long {
    return executor.executeInsert(insert(conflictAlgorithm, values))
}

inline fun <T : Table> StatementSubject<T>.insertBatch(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    buildAction: BatchAssignmentsBuilder<T>.() -> Unit
): BatchInsertStatement<T> {
    return BatchInsertStatement(this, BatchAssignmentsBuilder(table).apply(buildAction), conflictAlgorithm)
}

inline fun <T : Table> StatementSubject<T>.insertBatch(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    buildAction: BatchAssignmentsBuilder<T>.() -> Unit
): Long {
    return executor.executeBatchInsert(insertBatch(conflictAlgorithm, buildAction))
}