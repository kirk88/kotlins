package com.nice.sqlite

import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.ColumnConstraintAction
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.dml.plus

object TestTable : Table("test") {

    val id = IntColumn("id")

    val name = StringColumn("name")

    val flag = BooleanColumn("flag")

    val number = IntColumn("number")

}

object TestTable2 : Table("test2") {

    val id = IntColumn("id")

    val name = StringColumn("name")

}

fun main() {
    println(over(TestTable).create {
        define(it.id).primaryKey(true)
        define(it.name).notNull().foreignKey(TestTable2.name)
        define(it.flag, false)
        define(it.number, 0)
        define(it.id, it.name).unique().ifNotExists()
    }.toString(SQLiteDialect))

    println(over(TestTable).drop().toString(SQLiteDialect))

    println(over(TestTable).drop{
        define(it.id, it.name).ifExists()
    }.toString(SQLiteDialect))

    println(over(TestTable).alert {
        define(it.id).notNull().primaryKey(true).onDelete(ColumnConstraintAction.SetNull)
            .onUpdate(ColumnConstraintAction.SetNull)
        define(it.name)
    }.toString(SQLiteDialect))

    println(into(TestTable).insert(Conflict.Replace) {
        it.id(1) + it.name("jack") + it.flag(true) + it.number(30)
    }.toString(SQLiteDialect))

    println(from(TestTable).update(Conflict.Replace) {
        it.name("tom") + it.flag(false)
    }.toString(SQLiteDialect))

    println(from(TestTable).join(TestTable2).on { testTable, testTable2 ->
        (testTable.id eq 10) and (testTable2.name eq "tom")
    }.select().toString(SQLiteDialect))

    println(from(TestTable).outerJoin(TestTable2).on { testTable, testTable2 ->
        (testTable.id eq 10) and (testTable2.name eq "tom")
    }.select().toString(SQLiteDialect))

    println(from(TestTable).where {
        it.id.ne(0) and it.name.eq("jack") or it.flag.eq(true)
    }.groupBy {
        it.id + it.name
    }.having {
        it.name ne "tom"
    }.orderBy {
        it.name.asc + it.flag.desc
    }.select {
        it.id + it.name + it.number
    }.toString(SQLiteDialect))

    println(from(TestTable).delete().toString(SQLiteDialect))


}