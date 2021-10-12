@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*

class InsertStatement<T : Table>(
    val subject: Subject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Sequence<Assignment>
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class InsertBatchStatement<T : Table>(
    val subject: Subject<T>,
    insertSpecs: Sequence<InsertSpec>
) : Statement {

    private val iterator = insertSpecs.iterator()
    private val sqlCaches = mutableMapOf<InsertSpec, String>()

    private lateinit var nextSql: String
    private lateinit var nextInsert: InsertSpec

    override fun toString(dialect: Dialect): String {
        val nextStatement = InsertStatement(subject, nextInsert.conflictAlgorithm, nextInsert.assignments)
        return dialect.build(nextStatement)
    }

    fun moveToNext(dialect: Dialect): Boolean {
        val hasNext = iterator.hasNext()
        if (hasNext) {
            nextInsert = iterator.next()
            nextSql = sqlCaches.getOrPut(nextInsert) {
                toString(dialect)
            }
        }
        return hasNext
    }

    fun next(): Executable = Executable(nextSql, nextInsert.assignments)

}

class InsertBatchBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val subject: Subject<T>
) : Sequence<InsertSpec> {

    @PublishedApi
    internal val insertSpecs = mutableListOf<InsertSpec>()

    inline fun item(buildAction: InsertSpecBuilder<T>.() -> Unit) {
        insertSpecs.add(InsertSpecBuilder(subject).apply(buildAction).build())
    }

    override fun iterator(): Iterator<InsertSpec> = insertSpecs.iterator()

}

data class InsertSpec(
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Assignments
)

class InsertSpecBuilder<T : Table>(
    @PublishedApi internal val subject: Subject<T>
) {

    private lateinit var assignments: Assignments

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    fun assignments(assignments: (T) -> Sequence<Assignment>) {
        this.assignments = Assignments(assignments(subject.table))
    }

    @PublishedApi
    internal fun build(): InsertSpec = InsertSpec(conflictAlgorithm, assignments)

}