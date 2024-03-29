@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject

class InsertStatement<T : Table>(
    val subject: TableSubject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val values: Shell<ColumnValue>,
    val nativeBindValues: Boolean = false
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}

class InsertBatchStatement<T : Table>(
    val subject: TableSubject<T>,
    insertParts: Shell<InsertPart>
) : Statement {

    private val iterator = insertParts.iterator()
    private val sqlCaches = mutableMapOf<InsertPart, String>()

    private lateinit var nextSql: String
    private lateinit var nextInsert: InsertPart

    override fun toString(dialect: Dialect): String {
        val nextStatement = InsertStatement(
            subject,
            nextInsert.conflictAlgorithm,
            nextInsert.values,
            true
        )
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
) : Shell<InsertPart> {

    @PublishedApi
    internal val insertParts = mutableListOf<InsertPart>()

    inline fun item(buildAction: InsertPartBuilder<T>.() -> Unit) {
        insertParts.add(InsertPartBuilder(subject).apply(buildAction).build())
    }

    override val size: Int get() = insertParts.size
    override fun iterator(): Iterator<InsertPart> = insertParts.iterator()

}

class InsertPart(
    val conflictAlgorithm: ConflictAlgorithm,
    val values: Shell<ColumnValue>
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

    @PublishedApi
    internal lateinit var values: Shell<ColumnValue>

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    inline fun values(crossinline values: (T) -> Shell<ColumnValue>) {
        this.values = values(subject.table)
    }

    @PublishedApi
    internal fun build(): InsertPart = InsertPart(conflictAlgorithm, values)

}