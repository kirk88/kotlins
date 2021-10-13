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
    insertParts: Sequence<InsertPart>
) : Statement {

    private val iterator = insertParts.iterator()
    private val sqlCaches = mutableMapOf<InsertPart, String>()

    private lateinit var nextSql: String
    private lateinit var nextInsert: InsertPart

    override fun toString(dialect: Dialect): String {
        val nextStatement =
            InsertStatement(subject, nextInsert.conflictAlgorithm, nextInsert.assignments)
        return dialect.build(nextStatement)
    }

    fun next(dialect: Dialect): Executable {
        nextInsert = iterator.next()
        nextSql = sqlCaches.getOrPut(nextInsert) {
            toString(dialect)
        }
        return Executable(nextSql, nextInsert.assignments)
    }

    fun hasNext(): Boolean = iterator.hasNext()

}

class InsertBatchBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val subject: Subject<T>
) : Sequence<InsertPart> {

    @PublishedApi
    internal val insertSpecs = mutableListOf<InsertPart>()

    inline fun item(buildAction: InsertPartBuilder<T>.() -> Unit) {
        insertSpecs.add(InsertPartBuilder(subject).apply(buildAction).build())
    }

    override fun iterator(): Iterator<InsertPart> = insertSpecs.iterator()

}

data class InsertPart(
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Assignments
)

class InsertPartBuilder<T : Table>(
    @PublishedApi internal val subject: Subject<T>
) {

    private lateinit var assignments: Assignments

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    fun assignments(assignments: (T) -> Sequence<Assignment>) {
        this.assignments = Assignments(assignments(subject.table))
    }

    @PublishedApi
    internal fun build(): InsertPart = InsertPart(conflictAlgorithm, assignments)

}