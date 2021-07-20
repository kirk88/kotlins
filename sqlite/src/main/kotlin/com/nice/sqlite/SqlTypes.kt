@file:Suppress("unused", "FunctionName")

package com.nice.sqlite

interface SqlType {
    val name: String

    fun render(): String
    operator fun plus(m: SqlTypeModifier): SqlType

    companion object {
        fun create(name: String): SqlType = SqlTypeImpl(name)
    }
}

interface SqlTypeModifier {
    val modifier: String

    companion object {
        fun create(modifier: String): SqlTypeModifier = SqlTypeModifierImpl(modifier)
    }
}

val NULL: SqlType = SqlTypeImpl("NULL")
val INTEGER: SqlType = SqlTypeImpl("INTEGER")
val REAL: SqlType = SqlTypeImpl("REAL")
val TEXT: SqlType = SqlTypeImpl("TEXT")
val BLOB: SqlType = SqlTypeImpl("BLOB")

enum class ConstraintActions {
    SET_NULL,
    SET_DEFAULT,
    SET_RESTRICT,
    CASCADE,
    NO_ACTION;

    override fun toString(): String {
        return name.replace("_", " ")
    }
}

fun ON_UPDATE(constraintActions: ConstraintActions): SqlTypeModifier {
    return SqlTypeModifierImpl("ON UPDATE $constraintActions")
}

fun ON_DELETE(constraintActions: ConstraintActions): SqlTypeModifier {
    return SqlTypeModifierImpl("ON DELETE $constraintActions")
}

fun FOREIGN_KEY(
        columnName: String,
        referenceTable: String,
        referenceColumn: String,
        vararg actions: SqlTypeModifier
): SqlColumnProperty {
    return "" of SqlTypeImpl(
            "FOREIGN KEY($columnName) REFERENCES $referenceTable($referenceColumn)${
                actions.map { it.modifier }.joinToString("") { " $it" }
            }"
    )
}

val PRIMARY_KEY: SqlTypeModifier = SqlTypeModifierImpl("PRIMARY KEY")
val NOT_NULL: SqlTypeModifier = SqlTypeModifierImpl("NOT NULL")
val AUTOINCREMENT: SqlTypeModifier = SqlTypeModifierImpl("AUTOINCREMENT")
val UNIQUE: SqlTypeModifier = SqlTypeModifierImpl("UNIQUE")

fun UNIQUE(conflictClause: ConflictClause): SqlTypeModifier {
    return SqlTypeModifierImpl("UNIQUE ON CONFLICT $conflictClause")
}

enum class ConflictClause {
    ROLLBACK,
    ABORT,
    FAIL,
    IGNORE,
    REPLACE
}

fun DEFAULT(value: String): SqlTypeModifier = SqlTypeModifierImpl("DEFAULT $value")

fun DEFAULT(value: Number): SqlTypeModifier = SqlTypeModifierImpl("DEFAULT $value")

private open class SqlTypeImpl(override val name: String, val modifiers: String? = null) : SqlType {
    override fun render() = if (modifiers == null) name else "$name $modifiers"

    override fun plus(m: SqlTypeModifier): SqlType {
        return SqlTypeImpl(name, if (modifiers == null) m.modifier else "$modifiers ${m.modifier}")
    }
}

private class SqlTypeModifierImpl(override val modifier: String) : SqlTypeModifier