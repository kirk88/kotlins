package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class SelectStatement<T: Table>(
    val projections: Sequence<Projection>,
    val subject: Subject<T>,
    val whereClause: WhereClause<T>?,
    val orderClause: OrderClause<T>?,
    val limitClause: LimitClause<T>?,
    val offsetClause: OffsetClause<T>?,
    val groupClause: GroupClause<T>?,
    val havingClause: HavingClause<T>?) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}

class Select2Statement<T: Table, T2: Table>(
    val projections: Sequence<Projection>,
    val joinOn2Clause: JoinOn2Clause<T, T2>,
    val where2Clause: Where2Clause<T, T2>?,
    val order2Clause: Order2Clause<T, T2>?,
    val limit2Clause: Limit2Clause<T, T2>?,
    val offset2Clause: Offset2Clause<T, T2>?,
    val group2Clause: Group2Clause<T, T2>?,
    val having2Clause: Having2Clause<T, T2>?) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}

class Select3Statement<T: Table, T2: Table, T3: Table>(
    val projections: Sequence<Projection>,
    val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    val where3Clause: Where3Clause<T, T2, T3>?,
    val order3Clause: Order3Clause<T, T2, T3>?,
    val limit3Clause: Limit3Clause<T, T2, T3>?,
    val offset3Clause: Offset3Clause<T, T2, T3>?,
    val group3Clause: Group3Clause<T, T2, T3>?,
    val having3Clause: Having3Clause<T, T2, T3>?) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}


class Select4Statement<T: Table, T2: Table, T3: Table, T4: Table>(
    val projections: Sequence<Projection>,
    val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    val where4Clause: Where4Clause<T, T2, T3, T4>?,
    val order4Clause: Order4Clause<T, T2, T3, T4>?,
    val limit4Clause: Limit4Clause<T, T2, T3, T4>?,
    val offset4Clause: Offset4Clause<T, T2, T3, T4>?,
    val group4Clause: Group4Clause<T, T2, T3, T4>?,
    val having4Clause: Having4Clause<T, T2, T3, T4>?) {

    fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }
}