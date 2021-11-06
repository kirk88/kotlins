@file:Suppress("UNUSED")

package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.ddl.*

data class WhereClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val subject: TableSubject<T>
)

inline fun <T : Table> WhereClause<T>.groupBy(
    crossinline group: (T) -> Bag<Column<*>>
): GroupClause<T> = GroupClause(group(subject.table), subject, whereClause = this)

inline fun <T : Table> WhereClause<T>.orderBy(
    crossinline order: (T) -> Bag<Ordering>
): OrderClause<T> = OrderClause(order(subject.table), subject, whereClause = this)

inline fun <T : Table> WhereClause<T>.limit(
    crossinline limit: () -> Int
): LimitClause<T> = LimitClause(limit(), subject, whereClause = this)

inline fun <T : Table> WhereClause<T>.offset(
    crossinline offset: () -> Int
): OffsetClause<T> = OffsetClause(offset(), limit { -1 }, subject, whereClause = this)

@PublishedApi
internal inline fun <T : Table> WhereClause<T>.select(
    crossinline selection: (T) -> Bag<Definition>,
    distinct: Boolean
): SelectStatement<T> = SelectStatement(
    subject,
    selection(subject.table),
    whereClause = this,
    distinct = distinct
)

inline fun <T : Table> WhereClause<T>.select(
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): SelectStatement<T> = select(selection, false)

inline fun <T : Table> WhereClause<T>.selectDistinct(
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): SelectStatement<T> = select(selection, true)

inline fun <T : Table> WhereClause<T>.select(
    executor: StatementExecutor,
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table> WhereClause<T>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): Cursor = executor.executeQuery(selectDistinct(selection))

inline fun <T : Table> WhereClause<T>.update(
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Bag<Assignment>
): UpdateStatement<T> = UpdateStatement(
    subject,
    conflictAlgorithm,
    values(subject.table),
    whereClause = this,
    nativeBindValues = nativeBindValues
)

inline fun <T : Table> WhereClause<T>.update(
    executor: StatementExecutor,
    conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None,
    nativeBindValues: Boolean = false,
    crossinline values: (T) -> Bag<Assignment>
): Int = executor.executeUpdate(update(conflictAlgorithm, nativeBindValues, values))

inline fun <T : Table> WhereClause<T>.updateBatch(
    crossinline buildAction: UpdateBatchBuilder<T>.() -> Unit
): UpdateBatchStatement<T> = UpdateBatchStatement(
    subject,
    UpdateBatchBuilder(subject).apply(buildAction)
)

inline fun <T : Table> WhereClause<T>.updateBatch(
    executor: StatementExecutor,
    crossinline buildAction: UpdateBatchBuilder<T>.() -> Unit
): Int = executor.executeUpdateBatch(updateBatch(buildAction))

fun <T : Table> WhereClause<T>.delete(): DeleteStatement<T> =
    DeleteStatement(subject, whereClause = this)

fun <T : Table> WhereClause<T>.delete(executor: StatementExecutor): Int =
    executor.executeDelete(delete())

data class Where2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.groupBy(
    crossinline group: (T, T2) -> Bag<Column<*>>
): Group2Clause<T, T2> = Group2Clause(
    group(joinOn2Clause.subject.table, joinOn2Clause.table2),
    joinOn2Clause,
    where2Clause = this
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.orderBy(
    crossinline order: (T, T2) -> Bag<Ordering>
): Order2Clause<T, T2> = Order2Clause(
    order(joinOn2Clause.subject.table, joinOn2Clause.table2),
    joinOn2Clause,
    where2Clause = this
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.limit(
    crossinline limit: () -> Int
): Limit2Clause<T, T2> = Limit2Clause(
    limit(),
    joinOn2Clause,
    where2Clause = this
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.offset(
    crossinline offset: () -> Int
): Offset2Clause<T, T2> = Offset2Clause(
    offset(),
    limit { -1 },
    joinOn2Clause,
    where2Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition>,
    distinct: Boolean
): Select2Statement<T, T2> = Select2Statement(
    selection(
        joinOn2Clause.subject.table,
        joinOn2Clause.table2
    ),
    joinOn2Clause,
    where2Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, false)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.selectDistinct(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, true)

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table> Where2Clause<T, T2>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = executor.executeQuery(selectDistinct(selection))

data class Where3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.groupBy(
    crossinline group: (T, T2, T3) -> Bag<Column<*>>
): Group3Clause<T, T2, T3> = Group3Clause(
    group(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3
    ), joinOn3Clause,
    where3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.orderBy(
    crossinline order: (T, T2, T3) -> Bag<Ordering>
): Order3Clause<T, T2, T3> = Order3Clause(
    order(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3
    ),
    joinOn3Clause,
    where3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.limit(
    crossinline limit: () -> Int
): Limit3Clause<T, T2, T3> = Limit3Clause(
    limit(),
    joinOn3Clause,
    where3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.offset(
    crossinline offset: () -> Int
): Offset3Clause<T, T2, T3> = Offset3Clause(
    offset(),
    limit { -1 },
    joinOn3Clause,
    where3Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition>,
    distinct: Boolean
): Select3Statement<T, T2, T3> = Select3Statement(
    selection(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3
    ),
    joinOn3Clause,
    where3Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.selectDistinct(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table> Where3Clause<T, T2, T3>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = executor.executeQuery(selectDistinct(selection))

data class Where4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.groupBy(
    crossinline group: (T, T2, T3, T4) -> Bag<Column<*>>
): Group4Clause<T, T2, T3, T4> = Group4Clause(
    group(
        joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
        joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
        joinOn4Clause.joinOn3Clause.table3,
        joinOn4Clause.table4
    ),
    joinOn4Clause,
    this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.orderBy(
    crossinline order: (T, T2, T3, T4) -> Bag<Ordering>
): Order4Clause<T, T2, T3, T4> = Order4Clause(
    order(
        joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
        joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
        joinOn4Clause.joinOn3Clause.table3,
        joinOn4Clause.table4
    ),
    joinOn4Clause,
    where4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.limit(
    crossinline limit: () -> Int
): Limit4Clause<T, T2, T3, T4> = Limit4Clause(
    limit(),
    joinOn4Clause,
    where4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.offset(
    crossinline offset: () -> Int
): Offset4Clause<T, T2, T3, T4> = Offset4Clause(
    offset(),
    limit { -1 },
    joinOn4Clause,
    where4Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition>,
    distinct: Boolean
): Select4Statement<T, T2, T3, T4> = Select4Statement(
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

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.selectDistinct(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.select(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = executor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Where4Clause<T, T2, T3, T4>.selectDistinct(
    executor: StatementExecutor,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = executor.executeQuery(selectDistinct(selection))