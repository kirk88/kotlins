package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.Projection

sealed class ConstraintAction(
    private val name: String
) {
    object SetNull : ConstraintAction("SET NULL")

    object SetDefault : ConstraintAction("SET DEFAULT")

    object SetRestrict : ConstraintAction("SET DEFAULT")

    object Cascade : ConstraintAction("CASCADE")

    object NoAction : ConstraintAction("NO ACTION")

    override fun toString(): String {
        return name
    }
}

sealed class Constraint {

    class PrimaryKey(val autoIncrement: Boolean) : Constraint()

    class ForeignKey(val references: Projection.Column) : Constraint()

    class Unique(val conflict: Conflict) : Constraint()

    object NotNull : Constraint()

}