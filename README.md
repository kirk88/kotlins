# kotlins

纯kotlin编写的各种实用库
***

# common

kotlin一些通用的工具

1. RecyclerView适配器
2. Activity、Fragment基类封装
4. 一些常用的工具扩展类
5. ViewModel相关扩展，ViewModel -> UI 事件发送
6. 常用的自定义控件

***

# bluetooth

Kotlin中低能耗蓝牙的使用api，支持最低版本Android4.4

Kable的纯android版[https://github.com/JuulLabs/kable]
***

# atomic

Kotlin中使用原子操作的api
***

# gson

Gson的kotlin扩展
***

# okfaker

OkHttp的kotlin封装
***

# sqlite

SQLite的kotlin封装

## 定义表

```kotlin
object TestTable : Table("test") {
    val id = LongColumn("id").primaryKey()
    val name = StringColumn("name").default("jack")
    val age = IntColumn("age")
    val flag = BooleanColumn("flag")
    val number = IntColumn("number").default(10)
}
```

## 创建表

```kotlin
object DB : ManagedSQLiteOpenHelper(
    SupportSQLiteOpenHelper.Configuration.builder(applicationContext)
        .name("test_db.db")
        .callback(Callback())
        .build()
) {

    private class Callback : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            //创建表
            offer(TestTable).create(db.statementExecutor) {
                it.id + it.name + it.age + it.flag + it.number + index(it.id, it.name, name = "indexName")
            }
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            //添加列 或者 索引
            offer(TestTable).alter(db.statementExecutor) {
                it.number + index(it.id, it.name)
            }
        }
    }

}
```

## 删除表

```kotlin
offer(TestTable).drop()
```

如果要删除索引 (不支持删除列)

```kotlin
offer(TestTable).drop {
    index(it.id, it.name).ifExists()
    //or
    index(name = "indexName").ifExists()
}
```

## 插入数据

```kotlin
offer(TestTable).insert {
    it.id(1) + it.name("jack") + it.age(20) + it.flag(true)
}
```

批量插入

```kotlin
offer(TestTable).batchInsert(statementExecutor, Conflict.Replace) {
    for (bean in beans){
        assignments {
            it.id(bean.id) + it.name(bean.name) + it.age(bean.age) +
                    it.flag(bean.flag) + it.number(bean.number)
        }
    }
}
```

## 删除数据

```kotlin
offer(TestTable).where {
    it.id eq 1
}.delete()
```

## 查询数据

```kotlin
offer(TestTable).where {
    (it.name like "%jack%") and (it.id gt 100)
}.select {
    it.id + it.name + it.age + it.flag
}

//or
data class TestBean @ClassParserConstructor constructor(
    val id: Long,
    val name: String,
    val age: Int,
    val flag: Boolean
)

DB.use {
    val cursor = offer(TestTable)
        .where { it.flag eq true }
        .groupBy { it.id + it.name }
        .having { it.age gt 20 }
        .orderBy { desc(it.id) }
        .limit { 10 }
        .offset { 10 }
        .select(statementExecutor) {
            it.id + it.name + it.age + it.flag
        }

    //Sequence<Map<String, ColumnValue>>
    for (row in cursor.asMapSequence()) {
        //do something
    }

    //直接解析为bean
    for (bean in cursor.parseList(classParser<TestBean>())) {
        //do something
    }
}
```

## 更新数据

```kotlin
offer(TestTable).where {
    it.name eq "jack"
}.update {
    it.id(100) + it.name("tom") + it.age(20) + it.flag(false)
}
```





