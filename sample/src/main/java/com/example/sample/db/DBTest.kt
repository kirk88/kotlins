package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.common.applicationContext
import com.nice.sqlite.ManagedSQLiteOpenHelper
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.create
import com.nice.sqlite.core.offer
import com.nice.sqlite.statementExecutor

object TestTable : Table("test2") {
    val id = IntColumn("id")
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