package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class OrderClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val whereClause: WhereClause<T>?,
    @PublishedApi
    internal val groupClause: GroupClause<T>?,
    @PublishedApi
    internal val havingClause: HavingClause<T>?
) {

    inline fun limit(limit: () -> Int): LimitClause<T> {
        return LimitClause(
            limit(),
            subject,
            whereClause,
            this,
            groupClause,
            havingClause
        )
    }

    inline fun offset(offset: () -> Int): OffsetClause<T> {
        return OffsetClause(
            offset(),
            limit { -1 },
            subject,
            whereClause,
            this,
            groupClause,
            havingClause
        )
    }

    inline fun select(selection: (T) -> Sequence<Projection> = { emptySequence() }): SelectStatement<T> {
        return SelectStatement(
            selection(
                subject.table
            ),
            subject,
            whereClause,
            this,
            null,
            null,
            groupClause,
            havingClause
        )
    }

}

class Order2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val where2Clause: Where2Clause<T, T2>?,
    @PublishedApi
    internal val group2Clause: Group2Clause<T, T2>?,
    @PublishedApi
    internal val having2Clause: Having2Clause<T, T2>?
) {

    inline fun limit(limit: () -> Int): Limit2Clause<T, T2> {
        return Limit2Clause(
            limit(),
            joinOn2Clause,
            where2Clause,
            this,
            group2Clause,
            having2Clause
        )
    }

    inline fun offset(offset: () -> Int): Offset2Clause<T, T2> {
        return Offset2Clause(
            offset(),
            limit { -1 },
            joinOn2Clause,
            where2Clause,
            this,
            group2Clause,
            having2Clause
        )
    }

    inline fun select(selection: (T, T2) -> Sequence<Projection> = { _, _ -> emptySequence() }): Select2Statement<T, T2> {
        return Select2Statement(
            selection(
                joinOn2Clause.subject.table,
                joinOn2Clause.table2
            ),
            joinOn2Clause,
            where2Clause,
            this,
            null,
            null,
            group2Clause,
            having2Clause
        )
    }

}

class Order3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val where3Clause: Where3Clause<T, T2, T3>?,
    @PublishedApi
    internal val group3Clause: Group3Clause<T, T2, T3>?,
    @PublishedApi
    internal val having3Clause: Having3Clause<T, T2, T3>?
) {

    inline fun limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
        return Limit3Clause(
            limit(),
            joinOn3Clause,
            where3Clause,
            this,
            group3Clause,
            having3Clause
        )
    }

    inline fun offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
        return Offset3Clause(
            offset(),
            limit { -1 },
            joinOn3Clause,
            where3Clause,
            this,
            group3Clause,
            having3Clause
        )
    }

    inline fun select(selection: (T, T2, T3) -> Sequence<Projection> = { _, _, _ -> emptySequence() }): Select3Statement<T, T2, T3> {
        return Select3Statement(
            selection(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3
            ),
            joinOn3Clause,
            where3Clause,
            this,
            null,
            null,
            group3Clause,
            having3Clause
        )
    }

}

class Order4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val orderings: Sequence<Ordering>,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    @PublishedApi
    internal val where4Clause: Where4Clause<T, T2, T3, T4>?,
    @PublishedApi
    internal val group4Clause: Group4Clause<T, T2, T3, T4>?,
    @PublishedApi
    internal val having4Clause: Having4Clause<T, T2, T3, T4>?
) {

    inline fun limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
        return Limit4Clause(
            limit(),
            joinOn4Clause,
            where4Clause,
            this,
            group4Clause,
            having4Clause
        )
    }

    inline fun offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
        return Offset4Clause(
            offset(),
            limit { -1 },
            joinOn4Clause,
            where4Clause,
            this,
            group4Clause,
            having4Clause
        )
    }

    inline fun select(selection: (T, T2, T3, T4) -> Sequence<Projection> = { _, _, _, _ -> emptySequence() }): Select4Statement<T, T2, T3, T4> {
        return Select4Statement(
            selection(
                joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
                joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
                joinOn4Clause.joinOn3Clause.table3,
                joinOn4Clause.table4
            ),
            joinOn4Clause,
            where4Clause,
            this,
            null,
            null,
            group4Clause,
            having4Clause
        )
    }

}

