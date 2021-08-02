package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.common.applicationContext
import com.nice.sqlite.ClassParserConstructor
import com.nice.sqlite.ManagedSQLiteOpenHelper
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.alter
import com.nice.sqlite.core.create
import com.nice.sqlite.core.ddl.index
import com.nice.sqlite.core.offer
import com.nice.sqlite.statementExecutor

data class DBTest @ClassParserConstructor constructor(
    val id: Long,
    val name: String,
    val age: Int,
    val flag: Boolean,
    val number: Int
)

object TestTable : Table("test") {
    val id = LongColumn("id").primaryKey()
    val name = StringColumn("name").default("jack")
    val age = IntColumn("age")
    val flag = BooleanColumn("flag")
    val number = IntColumn("number").default(10)
}

object DB : ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("test_db.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(6) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            db.pageSize = 1024 * 32
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            offer(TestTable).create(db.statementExecutor) {
                it.id + it.name + it.age + it.flag + it.number + index(it.id, it.name)
            }
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            offer(TestTable).alter(db.statementExecutor) {
                it.number + index(it.id, it.name).ifNotExists()
            }
        }
    }

}