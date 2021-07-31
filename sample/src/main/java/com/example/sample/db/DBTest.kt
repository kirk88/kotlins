package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.common.applicationContext
import com.nice.sqlite.ClassParserConstructor
import com.nice.sqlite.ManagedSQLiteOpenHelper
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.create
import com.nice.sqlite.core.offer
import com.nice.sqlite.statementExecutor

data class DBTest @ClassParserConstructor constructor(
    val id: Long,
    val name: String,
    val age: Int,
    val flag: Boolean
)

object TestTable : Table("test2") {
    val id = LongColumn("id")
    val name = StringColumn("name")
    val age = IntColumn("age")
    val flag = BooleanColumn("flag")
}

object DB : ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("test_db.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(1) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            db.pageSize = 1024 * 32
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            offer(TestTable).create(db.statementExecutor) {
                define(it.id).primaryKey()
                define(it.name)
                define(it.age)
                define(it.flag)

                define(it.id, it.name).ifNotExists()
            }
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        }
    }

}