package com.example.sample.db

import androidx.room.*
import com.nice.common.applicationContext

@Entity(tableName = "dbb")
class DBB(
    id: Long = 0,
    name: String,
    age: Int,
    flag: Boolean,
    number: Int,
    data: ByteArray,
    time: String = ""
){

    @PrimaryKey(autoGenerate = true)
    val id: Long = id
    val name: String = name
    val age: Int = age
    val flag: Boolean = flag
    val number: Int = number
    val data: ByteArray = data
    val time: String = time

}

@Dao
interface TestDao{

    @Update
    fun insert(list: List<DBB>)

}

@Database(version = 1, exportSchema = false, entities = [DBB::class])
abstract class TestDatabase : RoomDatabase() {
    val testDao: TestDao by lazy { createTestDao() }

    abstract fun createTestDao(): TestDao
}

val RoomDB: TestDatabase by lazy {
    Room.databaseBuilder(applicationContext, TestDatabase::class.java, "Test")
        .build()
}