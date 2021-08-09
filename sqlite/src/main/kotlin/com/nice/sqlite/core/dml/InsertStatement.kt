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
    val conflictAlgorithm: ConflictAlgorithm,
    val bindValues: Boolean = true
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
    private lateinit var nextSql: String

    override fun toString(dialect: Dialect): String {
        val nextStatement = InsertStatement(subject, nextAssignments, conflictAlgorithm, false)
        return dialect.build(nextStatement)
    }

    fun moveToNext(dialect: Dialect): Boolean {
        val hasNext = iterator.hasNext()
        if (hasNext) {
            nextAssignments = iterator.next()
            nextSql = sqlCaches.getOrPut(nextAssignments.id) {
                toString(dialect)
            }
        }
        return hasNext
    }

    fun next(): InsertExecutable = InsertExecutable(nextSql, nextAssignments)

}

class InsertExecutable(
    val sql: String,
    val assignments: Sequence<Assignment>
)