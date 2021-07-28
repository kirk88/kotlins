@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class HavingClause<T : Table>(
    val predicate: Predicate,
    val subject: Subject<T>,
    val groupClause: GroupClause<T>,
    val whereClause: WhereClause<T>?
) {

    inline fun orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> {
        return OrderClause(order(subject.table), subject, whereClause, groupClause, this)
    }

    inline fun limit(limit: () -> Int): LimitClause<T> {
        return LimitClause(
            limit(),
            subject,
            whereClause,
            null,
            groupClause,
            this
        )
    }

    inline fun offset(offset: () -> Int): OffsetClause<T> {
        return OffsetClause(
            offset(),
            limit { -1 },
            subject,
            whereClause,
            null,
            groupClause,
            this
        )
    }

    inline fun select(selection: (T) -> Sequence<Projection>): SelectStatement<T> {
        return SelectStatement(
            selection(
                subject.table
            ),
            subject,
            whereClause,
            null,
            null,
            null,
            groupClause,
            this
        )
    }

}

class Having2Clause<T : Table, T2 : Table>(
    val predicate: Predicate,
    val joinOn2Clause: JoinOn2Clause<T, T2>,
    val group2Clause: Group2Clause<T, T2>,
    val where2Clause: Where2Clause<T, T2>?
) {

    inline fun orderBy(order: (T, T2) -> Sequence<Ordering>): Order2Clause<T, T2> {
        return Order2Clause(
            order(
                joinOn2Clause.subject.table,
                joinOn2Clause.table2
            ),
            joinOn2Clause,
            where2Clause,
            group2Clause,
            this
        )
    }

    inline fun limit(limit: () -> Int): Limit2Clause<T, T2> {
        return Limit2Clause(
            limit(),
            joinOn2Clause,
            where2Clause,
            null,
            group2Clause,
            this
        )
    }

    inline fun offset(offset: () -> Int): Offset2Clause<T, T2> {
        return Offset2Clause(
            offset(),
            limit { -1 },
            joinOn2Clause,
            where2Clause,
            null,
            group2Clause,
            this
        )
    }

    inline fun select(selection: (T, T2) -> Sequence<Projection>): Select2Statement<T, T2> {
        return Select2Statement(
            selection(
                joinOn2Clause.subject.table,
                joinOn2Clause.table2
            ),
            joinOn2Clause,
            where2Clause,
            null,
            null,
            null,
            group2Clause,
            this
        )
    }

}

class Having3Clause<T : Table, T2 : Table, T3 : Table>(
    val predicate: Predicate,
    val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    val group3Clause: Group3Clause<T, T2, T3>,
    val where3Clause: Where3Clause<T, T2, T3>?
) {

    inline fun orderBy(order: (T, T2, T3) -> Sequence<Ordering>): Order3Clause<T, T2, T3> {
        return Order3Clause(
            order(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3
            ),
            joinOn3Clause,
            where3Clause,
            group3Clause,
            this
        )
    }

    inline fun limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
        return Limit3Clause(
            limit(),
            joinOn3Clause,
            where3Clause,
            null,
            group3Clause,
            this
        )
    }

    inline fun offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
        return Offset3Clause(
            offset(),
            limit { -1 },
            joinOn3Clause,
            where3Clause,
            null,
            group3Clause,
            this
        )
    }

    inline fun select(selection: (T, T2, T3) -> Sequence<Projection>): Select3Statement<T, T2, T3> {
        return Select3Statement(
            selection(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3
            ),
            joinOn3Clause,
            where3Clause,
            null,
            null,
            null,
            group3Clause,
            this
        )
    }

}

class Having4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table>(
    val predicate: Predicate,
    val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    val group4Clause: Group4Clause<T, T2, T3, T4>,
    val where4Clause: Where4Clause<T, T2, T3, T4>?
) {

    inline fun orderBy(order: (T, T2, T3, T4) -> Sequence<Ordering>): Order4Clause<T, T2, T3, T4> {
        return Order4Clause(
            order(
                joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
                joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
                joinOn4Clause.joinOn3Clause.table3,
                joinOn4Clause.table4
            ),
            joinOn4Clause,
            where4Clause,
            group4Clause,
            this
        )
    }

    inline fun limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
        return Limit4Clause(
            limit(),
            joinOn4Clause,
            where4Clause,
            null,
            group4Clause,
            this
        )
    }

    inline fun offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
        return Offset4Clause(
            offset(),
            limit { -1 },
            joinOn4Clause,
            where4Clause,
            null,
            group4Clause,
            this
        )
    }

    inline fun select(selection: (T, T2, T3, T4) -> Sequence<Projection>): Select4Statement<T, T2, T3, T4> {
        return Select4Statement(
            selection(
                joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
                joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
                joinOn4Clause.joinOn3Clause.table3,
                joinOn4Clause.table4
            ),
            joinOn4Clause,
            where4Clause,
            null,
            null,
            null,
            group4Clause,
            this
        )
    }

}