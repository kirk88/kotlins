@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import android.database.Cursor
import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.dml.*

interface QueryStatement : Statement

class SelectStatement<T : Table>(
    val subject: TableSubject<T>,
    val definitions: Sequence<Definition>,
    val whereClause: WhereClause<T>? = null,
    val orderClause: OrderClause<T>? = null,
    val limitClause: LimitClause<T>? = null,
    val offsetClause: OffsetClause<T>? = null,
    val groupClause: GroupClause<T>? = null,
    val havingClause: HavingClause<T>? = null,
    val distinct: Boolean = false
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class Select2Statement<T : Table, T2 : Table>(
    val definitions: Sequence<Definition>,
    val joinOn2Clause: JoinOn2Clause<T, T2>,
    val where2Clause: Where2Clause<T, T2>? = null,
    val order2Clause: Order2Clause<T, T2>? = null,
    val limit2Clause: Limit2Clause<T, T2>? = null,
    val offset2Clause: Offset2Clause<T, T2>? = null,
    val group2Clause: Group2Clause<T, T2>? = null,
    val having2Clause: Having2Clause<T, T2>? = null,
    val distinct: Boolean = false
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class Select3Statement<T : Table, T2 : Table, T3 : Table>(
    val definitions: Sequence<Definition>,
    val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    val where3Clause: Where3Clause<T, T2, T3>? = null,
    val order3Clause: Order3Clause<T, T2, T3>? = null,
    val limit3Clause: Limit3Clause<T, T2, T3>? = null,
    val offset3Clause: Offset3Clause<T, T2, T3>? = null,
    val group3Clause: Group3Clause<T, T2, T3>? = null,
    val having3Clause: Having3Clause<T, T2, T3>? = null,
    val distinct: Boolean = false
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}


class Select4Statement<T : Table, T2 : Table, T3 : Table, T4 : Table>(
    val definitions: Sequence<Definition>,
    val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    val where4Clause: Where4Clause<T, T2, T3, T4>? = null,
    val order4Clause: Order4Clause<T, T2, T3, T4>? = null,
    val limit4Clause: Limit4Clause<T, T2, T3, T4>? = null,
    val offset4Clause: Offset4Clause<T, T2, T3, T4>? = null,
    val group4Clause: Group4Clause<T, T2, T3, T4>? = null,
    val having4Clause: Having4Clause<T, T2, T3, T4>? = null,
    val distinct: Boolean = false
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class UnionStatement(
    val statement1: QueryStatement,
    val statement2: QueryStatement,
    val all: Boolean
) : QueryStatement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

inline infix fun QueryStatement.union(other: () -> QueryStatement): UnionStatement {
    return UnionStatement(this, other(), false)
}

inline fun QueryStatement.union(
    executor: StatementExecutor,
    other: () -> QueryStatement
): Cursor {
    return executor.executeQuery(union(other))
}

inline fun QueryStatement.unionAll(other: () -> QueryStatement): UnionStatement {
    return UnionStatement(this, other(), false)
}

inline fun QueryStatement.unionAll(
    executor: StatementExecutor,
    other: () -> QueryStatement
): Cursor {
    return executor.executeQuery(unionAll(other))
}