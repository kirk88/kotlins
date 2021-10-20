@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import android.database.Cursor
import com.nice.sqlite.core.Dialect

class UnionStatement(
    val statement1: QueryStatement,
    val statement2: QueryStatement,
    val all: Boolean
) : QueryStatement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

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