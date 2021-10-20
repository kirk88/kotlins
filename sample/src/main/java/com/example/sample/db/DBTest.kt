package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import com.nice.common.applicationContext
import com.nice.sqlite.ClassParserConstructor
import com.nice.sqlite.ManagedSQLiteOpenHelper
import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.ColumnConstraintAction
import com.nice.sqlite.core.ddl.defined
import com.nice.sqlite.core.ddl.desc
import com.nice.sqlite.core.ddl.index
import com.nice.sqlite.core.dml.limit
import com.nice.sqlite.core.dml.selectDistinct
import com.nice.sqlite.statementExecutor

data class DBTest @ClassParserConstructor constructor(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val flag: Boolean,
    val number: Int,
    val data: ByteArray,
    val time: Long
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
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + age
        result = 31 * result + flag.hashCode()
        result = 31 * result + number
        result = 31 * result + data.contentHashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}

data class DBTest2 @ClassParserConstructor constructor(
    val id: Long = 0,
    val pid: Long,
    val name: String,
    val age: Int
)

object TestTable : Table("test") {
    val id = LongColumn("id").primaryKey()
    val name = StringColumn("name").default("jack")
    val age = IntColumn("age").default(20)
    val flag = BooleanColumn("flag").default(false)
    val number = IntColumn("number").default(10)
    val data = BlobColumn("data").default(byteArrayOf(1, 2, 3, 4, 5))
    val time = TimestampColumn("time")
        .default(defined("CURRENT_TIMESTAMP"))
}

object TestTable2 : Table("test2") {

    val id = IntColumn("id").primaryKey()

    val pid = IntColumn("pid").references(TestTable.id).onDelete(ColumnConstraintAction.Cascade)

    val name = StringColumn("name")

    val age = IntColumn("age")

}

val TestView = View("test_view") {
    offer(TestTable)
        .orderBy { desc(it.id) }
        .limit { 10 }
        .selectDistinct()
}

val TestTrigger = Trigger.Builder<TestTable>("test_trigger")
    .event { TriggerTime.After + TriggerType.Update }
    .on(TestTable)
    .action {
        offer(it).where {  }
    }.build()

object DB : ManagedSQLiteOpenHelper(
    applicationContext,
    "test_db.db",
    21
) {

    override fun onConfigure(db: SupportSQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        offer(TestTable).create(db.statementExecutor) {
            it.id + it.name + it.age + it.flag + it.number + it.data + it.time + index(
                it.id,
                it.name
            )
        }

        offer(TestView).create(db.statementExecutor)

        offer(TestTable2).create(db.statementExecutor) {
            it.id + it.pid + it.name + it.age
        }
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        offer(TestTable).alter(db.statementExecutor) {
            it.number + it.data + it.time + index(it.id, it.name).ifNotExists()
        }

        offer(TestView).create(db.statementExecutor)

        offer(TestTable2).alter(db.statementExecutor) {
            it.name + it.age
        }

        offer(TestTable2).create(db.statementExecutor) {
            it.id + it.pid + it.name + it.age
        }
    }

}