package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.common.applicationContext
import com.nice.sqlite.core.Table

object TestTable : Table("test2") {
    val id = IntColumn("id")
    val name = StringColumn("name")
    val age = IntColumn("age")
    val flag = BooleanColumn("flag")
}

object DB : com.nice.sqlite.ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("newdb.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(1) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            db.pageSize = 1024 * 32
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        }
    }

}