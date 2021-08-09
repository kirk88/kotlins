package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Assignment
import com.nice.sqlite.core.ddl.Assignments
import com.nice.sqlite.core.ddl.ConflictAlgorithm
import com.nice.sqlite.core.ddl.Statement

class InsertStatement<T : Table>(
    val subject: Subject<T>,
    val assignments: Sequence<Assignment>,
    val conflictAlgorithm: ConflictAlgorithm
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class BatchInsertStatement<T : Table>(
    val subject: Subject<T>,
    val batchAssignments: Sequence<Assignments>,
    val conflictAlgorithm: ConflictAlgorithm
) : Statement {

    private val iterator = batchAssignments.iterator()
    private val sqlCaches = mutableMapOf<Int, String>()

    private lateinit var nextAssignments: Assignments
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

    fun next(dialect: Dialect): Pair<String, Assignments> {
        return toString(dialect) to nextAssignments
    }

}