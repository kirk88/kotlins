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
) : Statement, Iterable<Assignments> {

    internal var currentAssignments: Assignments = assignments.first()

    private val caches = mutableMapOf<Int, String>()

    override fun toString(dialect: Dialect): String {
        return caches.getOrPut(currentAssignments.id) {
            dialect.build(this)
        }
    }

    override fun iterator(): Iterator<Assignments> {
        return object : Iterator<Assignments> {
            val iterator = assignments.iterator()

            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): Assignments {
                currentAssignments = iterator.next()
                return currentAssignments
            }
        }
    }

}