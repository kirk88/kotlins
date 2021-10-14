@file:Suppress("unused")

package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Definition
import com.nice.sqlite.core.ddl.Ordering
import com.nice.sqlite.core.ddl.StatementExecutor

data class OrderClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val whereClause: WhereClause<T>? = null,
    @PublishedApi
    internal val groupClause: GroupClause<T>? = null,
    @PublishedApi
    internal val havingClause: HavingClause<T>? = null
)

inline fun <T : Table> OrderClause<T>.limit(limit: () -> Int): LimitClause<T> {
    return LimitClause(
        limit(),
        subject,
        whereClause = whereClause,
        groupClause = groupClause,
        havingClause = havingClause,
        orderClause = this
    )
}

inline fun <T : Table> OrderClause<T>.offset(offset: () -> Int): OffsetClause<T> {
    return OffsetClause(
        offset(),
        limit { -1 },
        subject,
        whereClause = whereClause,
        groupClause = groupClause,
        havingClause = havingClause,
        orderClause = this
    )
}

inline fun <T : Table> OrderClause<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(
        subject,
        selection(subject.table),
        whereClause = whereClause,
        groupClause = groupClause,
        havingClause = havingClause,
        orderClause = this
    )
}

inline fun <T : Table> OrderClause<T>.select(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table> OrderClause<T>.selectDistinct(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(
        subject,
        selection(subject.table),
        whereClause = whereClause,
        groupClause = groupClause,
        havingClause = havingClause,
        orderClause = this,
        distinct = true
    )
}

inline fun <T : Table> OrderClause<T>.selectDistinct(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

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

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.limit(limit: () -> Int): Limit2Clause<T, T2> {
    return Limit2Clause(
        limit(),
        joinOn2Clause = joinOn2Clause,
        where2Clause = where2Clause,
        group2Clause = group2Clause,
        having2Clause = having2Clause,
        order2Clause = this
    )
}

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.offset(offset: () -> Int): Offset2Clause<T, T2> {
    return Offset2Clause(
        offset(),
        limit { -1 },
        joinOn2Clause = joinOn2Clause,
        where2Clause = where2Clause,
        group2Clause = group2Clause,
        having2Clause = having2Clause,
        order2Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    distinct: Boolean,
    selection: (T, T2) -> Sequence<Definition>
): Select2Statement<T, T2> {
    return Select2Statement(
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
}

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.select(
    executor: StatementExecutor,
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.selectDistinct(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table> Order2Clause<T, T2>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

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

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
    return Limit3Clause(
        limit(),
        joinOn3Clause = joinOn3Clause,
        where3Clause = where3Clause,
        group3Clause = group3Clause,
        having3Clause = having3Clause,
        order3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
    return Offset3Clause(
        offset(),
        limit { -1 },
        joinOn3Clause = joinOn3Clause,
        where3Clause = where3Clause,
        group3Clause = group3Clause,
        having3Clause = having3Clause,
        order3Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    distinct: Boolean,
    selection: (T, T2, T3) -> Sequence<Definition>
): Select3Statement<T, T2, T3> {
    return Select3Statement(
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
}

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.selectDistinct(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table> Order3Clause<T, T2, T3>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

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

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
    return Limit4Clause(
        limit(),
        joinOn4Clause = joinOn4Clause,
        where4Clause = where4Clause,
        group4Clause = group4Clause,
        having4Clause = having4Clause,
        order4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
    return Offset4Clause(
        offset(),
        limit { -1 },
        joinOn4Clause = joinOn4Clause,
        where4Clause = where4Clause,
        group4Clause = group4Clause,
        having4Clause = having4Clause,
        order4Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    distinct: Boolean,
    selection: (T, T2, T3, T4) -> Sequence<Definition>
): Select4Statement<T, T2, T3, T4> {
    return Select4Statement(
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
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.selectDistinct(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Order4Clause<T, T2, T3, T4>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}