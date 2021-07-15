package com.example.sample.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.nice.kotlins.applicationContext
import com.nice.kotlins.sqlite.db.*

object TestTable {
    const val TABLE_NAME = "Test"

    val ID = "id" of INTEGER + PRIMARY_KEY

    val NAME = "name" of TEXT

    val AGE = "age" of INTEGER

    val NUMBER = "number" of INTEGER + UNIQUE

//    val DATA = "data" of TEXT

    val TIP = "tip" of TEXT + NOT_NULL + DEFAULT("wow")

    val PP = "pp" of TEXT

    val JJ = "jj" of TEXT + DEFAULT("heheheheh")

    val BOOL = "bool" of INTEGER + DEFAULT(1)
}

class Test constructor(
    @JvmField
    var id: Long = 0L,
    @JvmField
    var name: String? = null,
    @JvmField
    var age: Int = 0,
    @JvmField
    var number: Int = 0,
//    @JvmField
//    @field:Column("data", C::class)
//    @param:Column("data", C::class)
//    var dataList: List<String>? = null,
    var tip: String = "",
    var pp: String? = null,
    var jj: String? = null,
    var bool: Boolean = false
) {

    @IgnoreOnTable
    var text: String = "2343"

    override fun toString(): String {
        return "Test(id=$id, name=$name, age=$age, number=$number, text=$text, tip=$tip, pp=$pp, jj=$jj, bool=$bool)"
    }


}

class C : ColumnValueConverter<List<String>, String> {

    override fun toDatabaseValue(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    override fun toPropertyValue(value: String?): List<String>? {
        return value?.split(",")
    }

}

object DB : ManagedSQLiteOpenHelper(
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
            db.createTable(
                TestTable.TABLE_NAME,
                true,
                TestTable.ID,
                TestTable.NAME,
                TestTable.AGE,
                TestTable.NUMBER,
                TestTable.TIP,
                TestTable.PP,
                TestTable.JJ,
                TestTable.BOOL
            )
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.TIP)
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.PP)
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.JJ)
            db.addColumn(TestTable.TABLE_NAME, true, TestTable.BOOL)
        }
    }

}