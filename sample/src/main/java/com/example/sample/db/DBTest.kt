package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.common.applicationContext
import com.nice.sqlite.ClassParserConstructor
import com.nice.sqlite.ManagedSQLiteOpenHelper
import com.nice.sqlite.SQLiteDialect
import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.desc
import com.nice.sqlite.core.ddl.index
import com.nice.sqlite.core.dml.*
import com.nice.sqlite.statementExecutor

data class DBTest @ClassParserConstructor constructor(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val flag: Boolean,
    val number: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DBTest

        if (id != other.id) return false
        if (name != other.name) return false
        if (age != other.age) return false
        if (flag != other.flag) return false
        if (number != other.number) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + age
        result = 31 * result + flag.hashCode()
        result = 31 * result + number
        result = 31 * result + data.contentHashCode()
        return result
    }
}

val TestView = View("test_view")

object TestTable : Table("test") {
    val id = LongColumn("id").primaryKey()
    val name = StringColumn("name").default("jack")
    val age = IntColumn("age").default(20)
    val flag = BooleanColumn("flag").default(false)
    val number = IntColumn("number").default(10)
    val data = BlobColumn("data").default(byteArrayOf(1, 2, 3, 4, 5))
}

object DB : ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("test_db.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(15) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            offer(TestTable).create(db.statementExecutor) {
                it.id + it.name + it.age + it.flag + it.number + it.data + index(it.id, it.name)
            }

            offer(TestView).create(db.statementExecutor) {
                offer(TestTable).orderBy {
                    desc(it.id)
                }.limit { 10 }.selectDistinct()
            }
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            offer(TestTable).alter(db.statementExecutor) {
                it.number + it.data + index(it.id, it.name).ifNotExists()
            }

            offer(TestView).create(db.statementExecutor) {
                offer(TestTable).orderBy {
                    desc(it.id)
                }.limit { 10 }.selectDistinct()
            }
        }
    }

}

object Table2 : Table("table2"){


    val id = IntColumn("id")

}

fun main() {
    println(offer(TestTable).join(Table2).using { testTable, table2 ->
        testTable.id + testTable.name + table2.id
    }.on { testTable, table2 ->
        table2.id eq testTable.id
    }.select().toString(SQLiteDialect))
}