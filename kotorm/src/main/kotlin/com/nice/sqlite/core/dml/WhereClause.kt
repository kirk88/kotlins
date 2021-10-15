@file:Suppress("unused")

package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*

data class WhereClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val subject: TableSubject<T>
)

inline fun <T : Table> WhereClause<T>.groupBy(group: (T) -> Sequence<Column<*>>): GroupClause<T> {
    return GroupClause(group(subject.table), subject, whereClause = this)
}

inline fun <T : Table> WhereClause<T>.orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> {
    return OrderClause(order(subject.table), subject, whereClause = this)
}

inline fun <T : Table> WhereClause<T>.limit(limit: () -> Int): LimitClause<T> {
    return LimitClause(
        limit(),
        subject,
        whereClause = this
    )
}

inline fun <T : Table> WhereClause<T>.offset(offset: () -> Int): OffsetClause<T> {
    return OffsetClause(
        offset(),
        limit { -1 },
        subject,
        whereClause = this
    )
}

@PublishedApi
internal inline fun <T : Table> WhereClause<T>.selectStatement(
    selection: (T) -> Sequence<Definition>,
    distinct: Boolean
): SelectStatement<T> {
    return SelectStatement(
        subject,
        selection(subject.table),
        whereClause = this,
        distinct = distinct
    )
}

inline fun <T : Table> WhereClause<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return subject.executor.executeQuery(selectStatement(selection, false))
}

inline fun <T : Table> WhereClause<T>.selectDistinct(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return subject.executor.executeQuery(selectStatement(selection, true))
}

inline fun <T : Table> WhereClause<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    values: (T) -> Sequence<Value>
): Int {
    return subject.executor.executeUpdate(UpdateStatement(subject, conflictAlgorithm, values(subject.table), whereClause = this))
}

inline fun <T : Table> WhereClause<T>.updateBatch(
    buildAction: UpdateBatchBuilder<T>.() -> Unit
): Int {
    return subject.executor.executeUpdateBatch(
        UpdateBatchStatement(
            subject,
            UpdateBatchBuilder(subject).apply(buildAction)
        )
    )
}

inline fun <T : Table> WhereClause<T>.delete(): Int {
    return subject.executor.executeDelete(DeleteStatement(subject, whereClause = this))
}

data class Where2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.groupBy(group: (T, T2) -> Sequence<Column<*>>): Group2Clause<T, T2> {
    return Group2Clause(
        group(joinOn2Clause.subject.table, joinOn2Clause.table2),
        joinOn2Clause,
        where2Clause = this
    )
}

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.orderBy(order: (T, T2) -> Sequence<Ordering>): Order2Clause<T, T2> {
    return Order2Clause(
        order(joinOn2Clause.subject.table, joinOn2Clause.table2),
        joinOn2Clause,
        where2Clause = this
    )
}

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.limit(limit: () -> Int): Limit2Clause<T, T2> {
    return Limit2Clause(
        limit(),
        joinOn2Clause,
        where2Clause = this
    )
}

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.offset(offset: () -> Int): Offset2Clause<T, T2> {
    return Offset2Clause(
        offset(),
        limit { -1 },
        joinOn2Clause,
        where2Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.selectStatement(
    selection: (T, T2) -> Sequence<Definition>,
    distinct: Boolean
): Select2Statement<T, T2> {
    return Select2Statement(
        selection(
            joinOn2Clause.subject.table,
            joinOn2Clause.table2
        ),
        joinOn2Clause,
        where2Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.select(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return joinOn2Clause.subject.executor.executeQuery(selectStatement(selection, false))
}

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.selectDistinct(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return joinOn2Clause.subject.executor.executeQuery(selectStatement(selection, true))
}

data class Where3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.groupBy(group: (T, T2, T3) -> Sequence<Column<*>>): Group3Clause<T, T2, T3> {
    return Group3Clause(
        group(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3
        ), joinOn3Clause,
        where3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.orderBy(order: (T, T2, T3) -> Sequence<Ordering>): Order3Clause<T, T2, T3> {
    return Order3Clause(
        order(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3
        ),
        joinOn3Clause,
        where3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
    return Limit3Clause(
        limit(),
        joinOn3Clause,
        where3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
    return Offset3Clause(
        offset(),
        limit { -1 },
        joinOn3Clause,
        where3Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.selectStatement(
    selection: (T, T2, T3) -> Sequence<Definition>,
    distinct: Boolean
): Select3Statement<T, T2, T3> {
    return Select3Statement(
        selection(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3
        ),
        joinOn3Clause,
        where3Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.select(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return joinOn3Clause.joinOn2Clause.subject.executor.executeQuery(
        selectStatement(
            selection,
            false
        )
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.selectDistinct(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return joinOn3Clause.joinOn2Clause.subject.executor.executeQuery(
        selectStatement(
            selection,
            true
        )
    )
}

data class Where4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.groupBy(group: (T, T2, T3, T4) -> Sequence<Column<*>>): Group4Clause<T, T2, T3, T4> {
    return Group4Clause(
        group(
            joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
            joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
            joinOn4Clause.joinOn3Clause.table3,
            joinOn4Clause.table4
        ),
        joinOn4Clause,
        this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.orderBy(order: (T, T2, T3, T4) -> Sequence<Ordering>): Order4Clause<T, T2, T3, T4> {
    return Order4Clause(
        order(
            joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
            joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
            joinOn4Clause.joinOn3Clause.table3,
            joinOn4Clause.table4
        ),
        joinOn4Clause,
        where4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
    return Limit4Clause(
        limit(),
        joinOn4Clause,
        where4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
    return Offset4Clause(
        offset(),
        limit { -1 },
        joinOn4Clause,
        where4Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.selectStatement(
    selection: (T, T2, T3, T4) -> Sequence<Definition>,
    distinct: Boolean
): Select4Statement<T, T2, T3, T4> {
    return Select4Statement(
        selection(
            joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
            joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
            joinOn4Clause.joinOn3Clause.table3,
            joinOn4Clause.table4
        ),
        joinOn4Clause,
        where4Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.select(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.executor.executeQuery(
        selectStatement(
            selection,
            false
        )
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.selectDistinct(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.executor.executeQuery(
        selectStatement(
            selection,
            true
        )
    )
}