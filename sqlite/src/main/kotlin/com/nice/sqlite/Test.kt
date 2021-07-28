package com.nice.sqlite

import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.ConstraintAction
import com.nice.sqlite.core.dml.plus

object TestTable : Table("test") {

    val id = IntColumn("id")

    val name = StringColumn("name")

    val flag = BooleanColumn("flag")

    val number = IntColumn("number")

}

fun main() {
    println(over(TestTable).create {
        define(it.id).primaryKey(true)
        define(it.name).notNull()
        define(it.flag, false)
        define(it.number, 0)
    }.toString(SQLiteDialect))

    println(over(TestTable).alert {
        define(it.id).notNull().primaryKey(true).onDelete(ConstraintAction.SetNull).onUpdate(ConstraintAction.SetNull)
        define(it.name)
    }.toString(SQLiteDialect))

    println(into(TestTable).insert(Conflict.Replace) {
        it.id(1) + it.name("jack") + it.flag(true) + it.number(30)
    }.toString(SQLiteDialect))

    println(from(TestTable).update(Conflict.Replace) {
        it.name("tom") + it.flag(false)
    }.toString(SQLiteDialect))

    println(from(TestTable).where {
        (it.id ne 1) and (it.name eq "jack") and (it.flag eq true)
    }.orderBy {
        it.name.asc + it.flag.desc
    }.select {
        it.id + it.name + it.flag
    }.toString(SQLiteDialect))
}