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

SQLite的kotlin封装（直接生成sql语句，性能比SQLiteDatabase中的增删查改略好）

## 定义表

```kotlin
object TestTable : Table("test") {
    val id = IntColumn("id")
    val name = StringColumn("name")
    val age = IntColumn("age")
    val flag = BooleanColumn("flag")
}
```

## 创建表

```kotlin
offer(TestTable).create(db.statementExecutor) {
    define(it.id).primaryKey()
    define(it.name).default("jack")
    define(it.age)
    define(it.flag)

    define(it.id, it.name, name = "indexName").unique().ifNotExists() //索引，索引名称默认为id_name
}
```

## 删除表

```kotlin
offer(TestTable).drop()
```

如果要删除索引

```kotlin
offer(TestTable).drop {
    define(it.id, it.name).ifExists()
    //or
    define(name = "indexName").ifExists()
}
```

## 插入数据

```kotlin
offer(TestTable).insert {
    it.id(1) + it.name("jack") + it.age(20) + it.flag(true)
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
```

## 更新数据

```kotlin
offer(TestTable).where {
    it.name eq "jack"
}.update {
    it.id(100) + it.name("tom") + it.age(20) + it.flag(false)
}
```





