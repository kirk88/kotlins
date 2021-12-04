package com.hao.reader.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.hao.reader.applicationContext
import com.nice.sqlite.ClassParserConstructor
import com.nice.sqlite.ManagedSupportSQLiteOpenHelper
import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.limit
import com.nice.sqlite.core.dml.selectDistinct
import com.nice.sqlite.core.dml.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DBTest @ClassParserConstructor constructor(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val flag: Boolean,
    val number: Int,
    val data: ByteArray,
    val time: String = ""
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
    val id = LongColumn("id") + PrimaryKey()
    val name = StringColumn("name") + Default("jack")
    val age = IntColumn("age") + Default(20)
    val flag = BooleanColumn("flag") + Default(false)
    val number = IntColumn("number") + Default(10)
    val data = BlobColumn("data") + Default(byteArrayOf(1, 2, 3, 4, 5))
    val time = DatetimeColumn("time") + Default(defined("CURRENT_TIMESTAMP"))

    val idNameIndex = NonuniqueIndex(id, name)
}

object TestTable2 : Table("test2") {

    val id = IntColumn("id") + PrimaryKey()

    val pid = IntColumn("pid") + References(TestTable.id) + OnDelete(ConstraintAction.Cascade)

    val name = StringColumn("name")

    val age = IntColumn("age")

}

val TestView = View("test_view") {
    offer(TestTable)
        .orderBy { desc(TestTable.id) }
        .limit { 10 }
        .selectDistinct()
}

private val TestTrigger = Trigger.Builder<TestTable>("test_trigger")
    .at(TriggerTime.After, TriggerType.Update)
    .on(TestTable)
    .trigger {
        offer(TestTable)
            .where { TestTable.id eq old(TestTable.id) }
            .update { TestTable.time(datetime("now")) }
    }.build()

private val SQLiteOpenHelperCallback = object : SupportSQLiteOpenHelper.Callback(22) {
    override fun onConfigure(db: SupportSQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
        db.pageSize = 2000
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        offer(TestTable).create(db) {
            TestTable.id + TestTable.name + TestTable.age + TestTable.flag +
                    TestTable.number + TestTable.data + TestTable.time + TestTable.idNameIndex
        }

        offer(TestView).create(db)

        offer(TestTable2).create(db) {
            TestTable2.id + TestTable2.pid + TestTable2.name + TestTable2.age
        }

        offer(TestTrigger).create(db)
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        offer(TestTable).alter(db) {
            TestTable.number + TestTable.data + TestTable.time + TestTable.idNameIndex
        }

        offer(TestView).create(db)

        offer(TestTable2).alter(db) {
            TestTable2.name + TestTable2.age
        }

        offer(TestTable2).create(db) {
            TestTable2.id + TestTable2.pid + TestTable2.name + TestTable2.age
        }

        offer(TestTrigger).create(db)
    }
}

val AppDatabase: ManagedSupportSQLiteOpenHelper by lazy {
    ManagedSupportSQLiteOpenHelper(
        applicationContext,
        "test_db.db",
        SQLiteOpenHelperCallback
    )
}

fun ManagedSupportSQLiteOpenHelper.executeAsync(
    scope: CoroutineScope,
    action: SupportSQLiteDatabase.() -> Unit
): Job = scope.launch(Dispatchers.IO) {
    execute(action)
}

fun ManagedSupportSQLiteOpenHelper.transactionAsync(
    scope: CoroutineScope,
    action: SupportSQLiteDatabase.() -> Unit
): Job = scope.launch(Dispatchers.IO) {
    transaction(action = action)
}