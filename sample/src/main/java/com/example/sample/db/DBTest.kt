package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.kotlins.app.applicationContext
import com.nice.kotlins.sqlite.db.*

object TestTable {
    val TABLE_NAME = "Test"

    val ID = "id" of INTEGER + PRIMARY_KEY

    val NAME = "name" of TEXT

    val AGE = "age" of INTEGER

    val NUMBER = "number" of INTEGER + UNIQUE

    val DATA = "data" of TEXT

    val TIP = "tip" of TEXT + NOT_NULL + DEFAULT("wow")

    val PP = "pp" of TEXT

    val JJ = "jj" of TEXT + DEFAULT("heheheheh")
}

class Test(
    @JvmField
    var id: Long = 0L,
    @JvmField
    var name: String? = null,
    @JvmField
    var age: Int = 0,
    @JvmField
    var number: Int = 0,
    @JvmField
    @field:Column("data", C::class)
    var dataList: List<String>? = null,
    var tip: String = "",
    var pp: String? = null,
    var jj: String? = null
) {

    @IgnoreOnTable
    var text: String = "2343"

    override fun toString(): String {
        return "Test(id=$id, name=$name, age=$age, number=$number, dataList=$dataList, text=$text, tip=$tip, pp=$pp, jj=$jj)"
    }


}

class C : ColumnValueConverter<List<String>, String> {

    override fun fromValue(value: List<String>): String {
        return value.joinToString(",")
    }

    override fun toValue(value: String): List<String> {
        return value.split(",")
    }

}

object DB : ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("testdemo.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(5) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.createTable(
                TestTable.TABLE_NAME,
                true,
                TestTable.ID,
                TestTable.NAME,
                TestTable.AGE,
                TestTable.NUMBER,
                TestTable.DATA,
                TestTable.TIP,
                TestTable.PP,
                TestTable.JJ
            )
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.TIP)
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.PP)
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.JJ)
        }
    }

}