package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Assignments
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.Statement

class InsertStatement<T : Table>(
    val subject: Subject<T>,
    val assignments: Sequence<Assignment>,
    val conflict: Conflict
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class BatchInsertStatement<T : Table>(
    val subject: Subject<T>,
    val batchAssignments: Sequence<Assignments>,
    val conflict: Conflict
) : Statement {

    private val iterator = batchAssignments.iterator()
    private val sqlCaches = mutableMapOf<Int, String>()

    private var nextAssignments: Assignments = batchAssignments.first()
    val assignments: Assignments
        get() = nextAssignments

    override fun toString(dialect: Dialect): String {
        return sqlCaches.getOrPut(nextAssignments.id) {
            dialect.build(this)
        }
    }

    fun moveToNext(): Boolean {
        val hasNext = iterator.hasNext()
        if (hasNext) {
            nextAssignments = iterator.next()
        }
        return hasNext
    }

}