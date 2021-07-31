@file:Suppress("unused")

package com.nice.sqlite.core

import android.database.Cursor
import com.nice.sqlite.SQLiteDefinitionBuilder
import com.nice.sqlite.core.ddl.AlertTableStatement
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.CreateTableStatement
import com.nice.sqlite.core.ddl.DropTableStatement
import com.nice.sqlite.core.dml.*

interface Subject<T : Table> {

    val table: T

}

class StatementSubject<T : Table>(override val table: T) : Subject<T>

inline fun <T : Table> offer(table: T): StatementSubject<T> {
    return StatementSubject(table)
}

inline fun <T : Table> StatementSubject<T>.create(action: SQLiteDefinitionBuilder.(T) -> Unit): CreateTableStatement<T> {
    return CreateTableStatement(SQLiteDefinitionBuilder().apply {
        action(table)
    }.build(), this)
}

inline fun <T : Table> StatementSubject<T>.create(
    executor: StatementExecutor,
    action: SQLiteDefinitionBuilder.(T) -> Unit
) {
    executor.execute(create(action))
}

inline fun <T : Table> StatementSubject<T>.alert(action: SQLiteDefinitionBuilder.(T) -> Unit): AlertTableStatement<T> {
    return AlertTableStatement(SQLiteDefinitionBuilder().apply {
        action(table)
    }.build(), this)
}

inline fun <T : Table> StatementSubject<T>.alert(
    executor: StatementExecutor,
    action: SQLiteDefinitionBuilder.(T) -> Unit
) {
    executor.execute(alert(action))
}

inline fun <T : Table> StatementSubject<T>.drop(action: SQLiteDefinitionBuilder.(T) -> Unit = {}): DropTableStatement<T> {
    return DropTableStatement(SQLiteDefinitionBuilder().apply {
        action(table)
    }.build(), this)
}

inline fun <T : Table> StatementSubject<T>.drop(
    executor: StatementExecutor,
    action: SQLiteDefinitionBuilder.(T) -> Unit = {}
) {
    executor.execute(drop(action))
}

inline fun <T : Table, T2 : Table> StatementSubject<T>.join(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.INNER)
}

inline fun <T : Table, T2 : Table> StatementSubject<T>.outerJoin(table2: T2): Join2Clause<T, T2> {
    return Join2Clause(this, table2, JoinType.OUTER)
}

inline fun <T : Table> StatementSubject<T>.where(predicate: (T) -> Predicate): WhereClause<T> {
    return WhereClause(predicate(table), this)
}

inline fun <T : Table> StatementSubject<T>.groupBy(group: (T) -> Sequence<Projection>): GroupClause<T> {
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
    selection: (T) -> Sequence<Projection> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(selection(table), this)
}

inline fun <T : Table> StatementSubject<T>.select(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Projection> = { emptySequence() }
): Cursor {
    return executor.queryForCursor(select(selection))
}

inline fun <T : Table> StatementSubject<T>.update(
    conflict: Conflict = Conflict.None,
    values: (T) -> Sequence<Assignment>
): UpdateStatement<T> {
    return UpdateStatement(values(table), this, conflict)
}

inline fun <T : Table> StatementSubject<T>.update(
    executor: StatementExecutor,
    conflict: Conflict = Conflict.None,
    values: (T) -> Sequence<Assignment>
): Int {
    return executor.executeUpdateDelete(update(conflict, values))
}

inline fun <T : Table> StatementSubject<T>.delete(): DeleteStatement<T> {
    return DeleteStatement(this)
}

inline fun <T : Table> StatementSubject<T>.delete(executor: StatementExecutor): Int {
    return executor.executeUpdateDelete(delete())
}

inline fun <T : Table> StatementSubject<T>.insert(
    conflict: Conflict = Conflict.None,
    values: (T) -> Sequence<Assignment>
): InsertStatement<T> {
    return InsertStatement(values(table), this, conflict)
}


inline fun <T : Table> StatementSubject<T>.insert(
    executor: StatementExecutor,
    conflict: Conflict = Conflict.None,
    values: (T) -> Sequence<Assignment>
): Long {
    return executor.executeInsert(insert(conflict, values))
}