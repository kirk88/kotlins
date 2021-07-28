package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class OffsetClause<T : Table>(
    val offset: Int,
    val limit: LimitClause<T>,
    val subject: Subject<T>,
    val whereClause: WhereClause<T>?,
    val orderClause: OrderClause<T>?,
    val groupClause: GroupClause<T>?,
    val havingClause: HavingClause<T>?
) {

    inline fun select(selection: (T) -> Sequence<Projection>): SelectStatement<T> {
        return SelectStatement(
            selection(
                subject.table
            ),
            subject,
            whereClause,
            orderClause,
            limit,
            this,
            groupClause,
            havingClause
        )
    }
}

class Offset2Clause<T : Table, T2 : Table>(
    val offset: Int,
    val limit2Clause: Limit2Clause<T, T2>,
    val joinOn2Clause: JoinOn2Clause<T, T2>,
    val whereClause: Where2Clause<T, T2>?,
    val orderClause: Order2Clause<T, T2>?,
    val group2Clause: Group2Clause<T, T2>?,
    val having2Clause: Having2Clause<T, T2>?
) {

    inline fun select(selection: (T, T2) -> Sequence<Projection>): Select2Statement<T, T2> {
        return Select2Statement(
            selection(
                joinOn2Clause.subject.table,
                joinOn2Clause.table2
            ),
            joinOn2Clause,
            whereClause,
            orderClause,
            limit2Clause,
            this,
            group2Clause,
            having2Clause
        )
    }

}

class Offset3Clause<T : Table, T2 : Table, T3 : Table>(
    val offset: Int,
    val limit3Clause: Limit3Clause<T, T2, T3>,
    val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    val where3Clause: Where3Clause<T, T2, T3>?,
    val order3Clause: Order3Clause<T, T2, T3>?,
    val group3Clause: Group3Clause<T, T2, T3>?,
    val having3Clause: Having3Clause<T, T2, T3>?
) {

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
            limit3Clause,
            this,
            group3Clause,
            having3Clause
        )
    }
}

class Offset4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table>(
    val offset: Int,
    val limit4Clause: Limit4Clause<T, T2, T3, T4>,
    val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    val where4Clause: Where4Clause<T, T2, T3, T4>?,
    val order4Clause: Order4Clause<T, T2, T3, T4>?,
    val group4Clause: Group4Clause<T, T2, T3, T4>?,
    val having4Clause: Having4Clause<T, T2, T3, T4>?
) {

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
            limit4Clause,
            this,
            group4Clause,
            having4Clause
        )
    }
}