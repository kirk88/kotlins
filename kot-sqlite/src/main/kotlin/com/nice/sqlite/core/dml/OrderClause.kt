@file:Suppress("UNUSED")

package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.ddl.*

data class OrderClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val subject: TableSubject<T>,
    @PublishedApi
    internal val whereClause: WhereClause<T>? = null,
    @PublishedApi
    internal val groupClause: GroupClause<T>? = null,
    @PublishedApi
    internal val havingClause: HavingClause<T>? = null
)

inline fun <T : Table> OrderClause<T>.limit(
    crossinline limit: () -> Int
): LimitClause<T> = LimitClause(
    limit(),
    subject,
    whereClause = whereClause,
    groupClause = groupClause,
    havingClause = havingClause,
    orderClause = this
)

inline fun <T : Table> OrderClause<T>.offset(
    crossinline offset: () -> Int
): OffsetClause<T> = OffsetClause(
    offset(),
    limit { -1 },
    subject,
    whereClause = whereClause,
    groupClause = groupClause,
    havingClause = havingClause,
    orderClause = this
)

@PublishedApi
internal inline fun <T : Table> OrderClause<T>.select(
    crossinline selection: (T) -> Sequence<Definition> = { emptySequence() },
    distinct: Boolean
): SelectStatement<T> = SelectStatement(
    subject,
    selection(subject.table),
    whereClause = whereClause,
    groupClause = groupClause,
    havingClause = havingClause,
    orderClause = this,
    distinct = distinct
)

inline fun <T : Table> OrderClause<T>.select(
    crossinline selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> = select(selection, false)

inline fun <T : Table> OrderClause<T>.selectDistinct(
    crossinline selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> = select(selection, true)

inline fun <T : Table> OrderClause<T>.select(
    executor: StatementExecutor,
    crossinline selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table> OrderClause<T>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor = executor.executeQuery(selectDistinct(selection))

data class Order2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val where2Clause: Where2Clause<T, T2>? = null,
    @PublishedApi
    internal val group2Clause: Group2Clause<T, T2>? = null,
    @PublishedApi
    internal val having2Clause: Having2Clause<T, T2>? = null
)

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.limit(
    crossinline limit: () -> Int
): Limit2Clause<T, T2> = Limit2Clause(
    limit(),
    joinOn2Clause = joinOn2Clause,
    where2Clause = where2Clause,
    group2Clause = group2Clause,
    having2Clause = having2Clause,
    order2Clause = this
)

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.offset(
    crossinline offset: () -> Int
): Offset2Clause<T, T2> = Offset2Clause(
    offset(),
    limit { -1 },
    joinOn2Clause = joinOn2Clause,
    where2Clause = where2Clause,
    group2Clause = group2Clause,
    having2Clause = having2Clause,
    order2Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Sequence<Definition>,
    distinct: Boolean
): Select2Statement<T, T2> = Select2Statement(
    selection(
        joinOn2Clause.subject.table,
        joinOn2Clause.table2
    ),
    joinOn2Clause = joinOn2Clause,
    where2Clause = where2Clause,
    group2Clause = group2Clause,
    having2Clause = having2Clause,
    order2Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> = select(selection, false)

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.selectDistinct(
    crossinline selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> = select(selection, true)

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor = executor.executeQuery(selectDistinct(selection))

data class Order3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val where3Clause: Where3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val group3Clause: Group3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val having3Clause: Having3Clause<T, T2, T3>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.limit(
    crossinline limit: () -> Int
): Limit3Clause<T, T2, T3> = Limit3Clause(
    limit(),
    joinOn3Clause = joinOn3Clause,
    where3Clause = where3Clause,
    group3Clause = group3Clause,
    having3Clause = having3Clause,
    order3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.offset(
    crossinline offset: () -> Int
): Offset3Clause<T, T2, T3> = Offset3Clause(
    offset(),
    limit { -1 },
    joinOn3Clause = joinOn3Clause,
    where3Clause = where3Clause,
    group3Clause = group3Clause,
    having3Clause = having3Clause,
    order3Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Sequence<Definition>,
    distinct: Boolean
): Select3Statement<T, T2, T3> = Select3Statement(
    selection(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3
    ),
    joinOn3Clause = joinOn3Clause,
    where3Clause = where3Clause,
    group3Clause = group3Clause,
    having3Clause = having3Clause,
    order3Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.selectDistinct(
    crossinline selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor = executor.executeQuery(selectDistinct(selection))

data class Order4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    @PublishedApi
    internal val where4Clause: Where4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val group4Clause: Group4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val having4Clause: Having4Clause<T, T2, T3, T4>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.limit(
    crossinline limit: () -> Int
): Limit4Clause<T, T2, T3, T4> = Limit4Clause(
    limit(),
    joinOn4Clause = joinOn4Clause,
    where4Clause = where4Clause,
    group4Clause = group4Clause,
    having4Clause = having4Clause,
    order4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.offset(
    crossinline offset: () -> Int
): Offset4Clause<T, T2, T3, T4> = Offset4Clause(
    offset(),
    limit { -1 },
    joinOn4Clause = joinOn4Clause,
    where4Clause = where4Clause,
    group4Clause = group4Clause,
    having4Clause = having4Clause,
    order4Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Sequence<Definition>,
    distinct: Boolean
): Select4Statement<T, T2, T3, T4> = Select4Statement(
    selection(
        joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
        joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
        joinOn4Clause.joinOn3Clause.table3,
        joinOn4Clause.table4
    ),
    joinOn4Clause = joinOn4Clause,
    where4Clause = where4Clause,
    group4Clause = group4Clause,
    having4Clause = having4Clause,
    order4Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.selectDistinct(
    crossinline selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor = executor.executeQuery(selectDistinct(selection))