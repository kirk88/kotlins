@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject

class InsertStatement<T : Table>(
    val subject: TableSubject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Sequence<Value>
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class InsertBatchStatement<T : Table>(
    val subject: TableSubject<T>,
    insertParts: Sequence<InsertPart>
) : Statement {

    private val iterator = insertParts.iterator()
    private val sqlCaches = mutableMapOf<InsertPart, String>()

    private lateinit var nextSql: String
    private lateinit var nextInsert: InsertPart

    override fun toString(dialect: Dialect): String {
        val nextStatement =
            InsertStatement(subject, nextInsert.conflictAlgorithm, nextInsert.values)
        return dialect.build(nextStatement)
    }

    fun next(dialect: Dialect): Executable {
        nextInsert = iterator.next()
        nextSql = sqlCaches.getOrPut(nextInsert) {
            toString(dialect)
        }
        return Executable(nextSql, nextInsert.values)
    }

    fun hasNext(): Boolean = iterator.hasNext()

}

class InsertBatchBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val subject: TableSubject<T>
) : Sequence<InsertPart> {

    @PublishedApi
    internal val insertSpecs = mutableListOf<InsertPart>()

    inline fun item(buildAction: InsertPartBuilder<T>.() -> Unit) {
        insertSpecs.add(InsertPartBuilder(subject).apply(buildAction).build())
    }

    override fun iterator(): Iterator<InsertPart> = insertSpecs.iterator()

}

class InsertPart(
    val conflictAlgorithm: ConflictAlgorithm,
    val values: Sequence<Value>
) {

    private val id: Int = values.joinToString(prefix = "$conflictAlgorithm, ") {
        it.column.name
    }.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InsertPart

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

}

class InsertPartBuilder<T : Table>(
    @PublishedApi internal val subject: TableSubject<T>
) {

    private lateinit var values: Sequence<Value>

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    fun values(values: (T) -> Sequence<Value>) {
        this.values = values.invoke(subject.table)
    }

    @PublishedApi
    internal fun build(): InsertPart = InsertPart(conflictAlgorithm, values)

}