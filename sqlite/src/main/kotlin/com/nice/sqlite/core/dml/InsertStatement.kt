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
    val assignments: Sequence<Assignments>,
    val conflict: Conflict
) : Statement {

    private var nextAssignments: Assignments = assignments.first()

    private val iterator = assignments.iterator()
    private val caches = mutableMapOf<Int, String>()

    val currentAssignments: Assignments
        get() = nextAssignments

    override fun toString(dialect: Dialect): String {
        return caches.getOrPut(currentAssignments.id) {
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