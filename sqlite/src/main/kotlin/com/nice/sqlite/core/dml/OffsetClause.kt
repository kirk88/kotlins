package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.ddl.StatementExecutor
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Definition

class OffsetClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val offset: Int,
    @PublishedApi
    internal val limitClause: LimitClause<T>,
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val whereClause: WhereClause<T>? = null,
    @PublishedApi
    internal val orderClause: OrderClause<T>? = null,
    @PublishedApi
    internal val groupClause: GroupClause<T>? = null,
    @PublishedApi
    internal val havingClause: HavingClause<T>? = null
)

inline fun <T : Table> OffsetClause<T>.select(
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): SelectStatement<T> {
    return SelectStatement(
        subject,
        selection(
            subject.table
        ),
        whereClause = whereClause,
        orderClause = orderClause,
        limitClause = limitClause,
        groupClause = groupClause,
        havingClause = havingClause,
        offsetClause = this
    )
}

inline fun <T : Table> OffsetClause<T>.select(
    executor: StatementExecutor,
    selection: (T) -> Sequence<Definition> = { emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

class Offset2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val offset: Int,
    @PublishedApi
    internal val limit2Clause: Limit2Clause<T, T2>,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val where2Clause: Where2Clause<T, T2>? = null,
    @PublishedApi
    internal val order2Clause: Order2Clause<T, T2>? = null,
    @PublishedApi
    internal val group2Clause: Group2Clause<T, T2>? = null,
    @PublishedApi
    internal val having2Clause: Having2Clause<T, T2>? = null
)

inline fun <T : Table, T2 : Table> Offset2Clause<T, T2>.select(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> {
    return Select2Statement(
        selection(
            joinOn2Clause.subject.table,
            joinOn2Clause.table2
        ),
        joinOn2Clause = joinOn2Clause,
        where2Clause = where2Clause,
        order2Clause = order2Clause,
        limit2Clause = limit2Clause,
        group2Clause = group2Clause,
        having2Clause = having2Clause,
        offset2Clause = this
    )
}

inline fun <T : Table, T2 : Table> Offset2Clause<T, T2>.select(
    executor: StatementExecutor,
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

class Offset3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val offset: Int,
    @PublishedApi
    internal val limit3Clause: Limit3Clause<T, T2, T3>,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val where3Clause: Where3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val order3Clause: Order3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val group3Clause: Group3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val having3Clause: Having3Clause<T, T2, T3>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table> Offset3Clause<T, T2, T3>.select(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> {
    return Select3Statement(
        selection(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3
        ),
        joinOn3Clause = joinOn3Clause,
        where3Clause = where3Clause,
        order3Clause = order3Clause,
        limit3Clause = limit3Clause,
        group3Clause = group3Clause,
        having3Clause = having3Clause,
        offset3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Offset3Clause<T, T2, T3>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

class Offset4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val offset: Int,
    @PublishedApi
    internal val limit4Clause: Limit4Clause<T, T2, T3, T4>,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    @PublishedApi
    internal val where4Clause: Where4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val order4Clause: Order4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val group4Clause: Group4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val having4Clause: Having4Clause<T, T2, T3, T4>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Offset4Clause<T, T2, T3, T4>.select(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
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
        order4Clause = order4Clause,
        limit4Clause = limit4Clause,
        group4Clause = group4Clause,
        having4Clause = having4Clause,
        offset4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Offset4Clause<T, T2, T3, T4>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}