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

## 使用方式

初始化

```kotlin
//不是必需操作
OkHttpConfig.Setter()
    .baseUrl("https://www.baidu.com")
    .client(OkHttpClient())
    .apply()
```

GET请求

```kotlin
httpCallFlow<String> {

    url("/s")

    queryParameters {
        "wd" += "hello word"
    }

}.onStart {
    Log.e("OkFaker", "onStart")
}.onEach {
    Log.e("OkFaker", "onEach: $it")
}.onCompletion {
    Log.e("OkFaker", "onCompletion")
}.catch {
    Log.e("OkFaker", "onError: $it")
}.launchIn(lifecycleScope)
```

POST请求

```kotlin
httpCallFlow<String> {

    method(OkRequestMethod.POST)

    url("/s")

    queryParameters {
        "wd" += "hello word"
    }

}.onStart {
    Log.e("OkFaker", "onStart")
}.onEach {
    Log.e("OkFaker", "onEach: $it")
}.onCompletion {
    Log.e("OkFaker", "onCompletion")
}.catch {
    Log.e("OkFaker", "onError: $it")
}.launchIn(lifecycleScope)
```

转换成任意数据格式

```kotlin
httpCallFlow<String> {

    method(OkRequestMethod.POST)

    url("/s")

    queryParameters {
        "wd" += "hello word"
    }

    //默认已经使用gson实现了
    mapResponse { response ->
        //转换成任意格式
        JSONObject(response.body!!.string())
    }

}
```

下载

```kotlin
httpCallFlow<File> {
    client(
        //注意 HttpLoggingInterceptor.Level < BODY
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()
    )

    url("http://www.baidu.com")

    extension(
        DefaultOkDownloadExtension("../path.html", true) { readBytes, totalBytes ->
            //Main Thread
        }
    )
}.collect { file ->

}
```

***

# sqlite

SQLite的kotlin封装

## 定义表

```kotlin
object TestTable : Table("test") {
    val Id = LongColumn("id") + PrimaryKey()
    val Name = StringColumn("name", "jack")
    val Age = IntColumn("age")
    val Flag = BooleanColumn("flag")
    val Number = IntColumn("number") + Default(10)
    val CreateTime = DatetimeColumn("create_time") + Default(datetime("now", "localtime"))
    val UpdateTime = DatetimeColumn("update_time") + Default(datetime("now", "localtime"))
    
    val NameAgeIndex = UniqueIndex(Name, Age)
}
```

## 创建数据库

```kotlin
private val TestUpdateTrigger = Trigger.Builder<TestTable>("test_update_time_trigger")
    .at(TriggerTime.After, TriggerType.Update)
    .on(TestTable)
    .trigger {
        offer(TestTable)
            .where { it.Id eq old(it.Id) }
            .update { it.UpdateTime(datetime("now", "localtime")) }
    }.build()

private val SQLiteOpenHelperCallback = object : SupportSQLiteOpenHelper.Callback(1) {
    override fun onCreate(db: SupportSQLiteDatabase) {
        //创建表
        offer(TestTable).create(db) {
            it.Id + it.Name + it.Age + it.Flag + it.Number + it.CreateTime + it.UpdateTime + it.NameAgeIndex
        }

        //创建触发器
        offer(TestUpdateTrigger).create(db)
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}

val Database: ManagedSupportSQLiteOpenHelper by lazy {
    ManagedSupportSQLiteOpenHelper(applicationContext, "test", SQLiteOpenHelperCallback)
}
```
## 删除表

```kotlin
offer(TestTable).drop(db)
```

删除索引

```kotlin
offer(TestTable).drop(db) { it.NameAgeIndex }
```

## 插入数据

```kotlin
offer(TestTable).insert(db) {
    it.Id(1) + it.Name("jack") + it.Age(20) + it.Flag(true) + it.Number(100)
}
```

批量插入

```kotlin
offer(TestTable).batchInsert(db, Conflict.Replace) {
    for (bean in beans) {
        values {
            it.Id(bean.id) + it.Name(bean.name) + it.Age(bean.age) +
                    it.Flag(bean.flag) + it.Number(bean.number)
        }
    }
}
```

## 删除数据

```kotlin
offer(TestTable).where { it.Id eq 1 }.delete()
```

## 查询数据

```kotlin
offer(TestTable).where {
    (it.Name like "%jack%") and (it.Id gt 100)
}.select {
    it.Id + it.Name + it.Age + it.Flag + it.Number
}

//or
data class TestBean @ClassParserConstructor constructor(
    val id: Long,
    val name: String,
    val age: Int,
    val flag: Boolean
)

val cursor = offer(TestTable)
    .where { it.Flag eq true }
    .groupBy { it.Id + it.Name }
    .having { it.Age gt 20 }
    .orderBy { desc(it.Id) }
    .limit { 10 }
    .offset { 10 }
    .select(db) {
        it.id + it.name + it.age + it.flag
    }

//Sequence<Map<String, ColumnValue>>
for (row in cursor.rowSequence()) {
    //do something
}

//直接解析为bean
for (bean in cursor.toList<TestBean>()) {
    //do something
}
```

## 更新数据

```kotlin
offer(TestTable).where {
    it.Name eq "jack"
}.update {
    it.Id(100) + it.Name("tom")
}
```





