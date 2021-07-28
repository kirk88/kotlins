package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class LimitClause<T : Table>(
    val limit: Int,
    val subject: Subject<T>,
    val whereClause: WhereClause<T>?,
    val orderClause: OrderClause<T>?,
    val groupClause: GroupClause<T>?,
    val havingClause: HavingClause<T>?
) {

    inline fun offset(offset: () -> Int): OffsetClause<T> {
        return OffsetClause(
            offset(),
            this,
            subject,
            whereClause,
            orderClause,
            groupClause,
            havingClause
        )
    }

    inline fun select(selection: (T) -> Sequence<Projection>): SelectStatement<T> {
        return SelectStatement(
            selection(
                subject.table
            ),
            subject,
            whereClause,
            orderClause,
            this,
            null,
            groupClause,
            havingClause
        )
    }
}

class Limit2Clause<T : Table, T2 : Table>(
    val limit: Int,
    val joinOn2Clause: JoinOn2Clause<T, T2>,
    val where2Clause: Where2Clause<T, T2>?,
    val order2Clause: Order2Clause<T, T2>?,
    val group2Clause: Group2Clause<T, T2>?,
    val having2Clause: Having2Clause<T, T2>?
) {

    inline fun offset(offset: () -> Int): Offset2Clause<T, T2> {
        return Offset2Clause(
            offset(),
            this,
            joinOn2Clause,
            where2Clause,
            order2Clause,
            group2Clause,
            having2Clause
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
            order2Clause,
            this,
            null,
            group2Clause,
            having2Clause
        )
    }

}

class Limit3Clause<T : Table, T2 : Table, T3 : Table>(
    val limit: Int,
    val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    val where3Clause: Where3Clause<T, T2, T3>?,
    val order3Clause: Order3Clause<T, T2, T3>?,
    val group3Clause: Group3Clause<T, T2, T3>?,
    val having3Clause: Having3Clause<T, T2, T3>?
) {

    inline fun offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
        return Offset3Clause(
            offset(),
            this,
            joinOn3Clause,
            where3Clause,
            order3Clause,
            group3Clause,
            having3Clause
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
            order3Clause,
            this,
            null,
            group3Clause,
            having3Clause
        )
    }
}

class Limit4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table>(
    val limit: Int,
    val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    val where4Clause: Where4Clause<T, T2, T3, T4>?,
    val order4Clause: Order4Clause<T, T2, T3, T4>?,
    val group4Clause: Group4Clause<T, T2, T3, T4>?,
    val having4Clause: Having4Clause<T, T2, T3, T4>?
) {

    inline fun offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
        return Offset4Clause(
            offset(),
            this,
            joinOn4Clause,
            where4Clause,
            order4Clause,
            group4Clause,
            having4Clause
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
            order4Clause,
            this,
            null,
            group4Clause,
            having4Clause
        )
    }
}