package com.example.sample

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DB
import com.example.sample.db.Test
import com.example.sample.db.TestTable
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.*
import com.nice.kotlins.http.OkFaker
import com.nice.kotlins.http.asFlow
import com.nice.kotlins.sqlite.db.*
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.regex.Pattern

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    private val tipView: TipView by tipViews()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        val titleBar = binding.titleBar
        val fab = binding.fab

        fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "value"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
        }

        fab.doOnLongClick {
            true
        }
        val liveEvent = MutableLiveEvent<String>()
        liveEvent.observe(this) {
            Log.e("TAGTAG", "event: $it")
        }

        liveEvent += "event1"
        liveEvent += "event2"

        lifecycleScope.launch(Dispatchers.IO) {
            DB.use(true) {
                var start = System.currentTimeMillis()
                for (index in 0..10000) {
                    val test = Test(index.toLong(), "jack$index", 20, index, listOf("A", "B", "C", "D"), "lalalalal", "")

                    insert(TestTable.TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, test.toColumnElements())
                }

                Log.e("TAGTAG", "insert: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()

                updateBuilder(TestTable.TABLE_NAME)
                    .values(TestTable.NAME + "jack100")
                    .where(TestTable.NAME.equal("jack1") or TestTable.NAME.equal("jack2"))
                    .execute()

                updateBuilder(TestTable.TABLE_NAME)
                    .values(TestTable.NAME + "jack101")
                    .where(TestTable.NAME.equal("jack3") or TestTable.NAME.equal("jack4"))
                    .execute()

                Log.e("TAGTAG", "update: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()

                val result = queryBuilder(TestTable.TABLE_NAME)
                    .selection(TestTable.ID.notBetween(3, 6))
                    .groupBy(TestTable.NAME, TestTable.JJ)
                    .parseList<Test>()

                Log.e("TAGTAG", "query: ${System.currentTimeMillis() - start}")

                Log.e("TAGTAG", "result: ${result.size}  " + result.toString())
            }
        }

        lifecycleScope.launch {

            OkFaker.get<String>().client(OkHttpClient()).url("https://www.baidu.com")
                .mapResponse {
                    throw IllegalStateException("hhhhhhhhhhhhhhhhhhhhhh")
                }.build().asFlow().catch {
                    Log.e("TAGTAG", "error: " + it)
                }.collect {
                    Log.e("TAGTAG", "result1: " + it)
                }

        }


        val job = step {
            add(Dispatchers.IO) {
                val result =
                    OkFaker.get<String>().client(OkHttpClient()).url("https://www.baidu.com")
                        .mapResponse {
                            it.body!!.string()
                        }.execute()


                Log.e("TAGTAG", "result2: " + result)
            }

            add {
                delay(3000)
                Log.e("TAGTAG", "step2")
            }

            add {
                Log.e("TAGTAG", "step3")
            }
        }.launchIn(lifecycleScope)

//        lifecycleScope.launch {
//            delay(2000)
//            job.cancel()
//        }
    }

}

fun main() {
    val str = "https://www.baidu.com"

    val pattern = Pattern.compile("http(s)?://www\\..+\\..+")
    println(str.matches(pattern.toRegex()))

    val pattern2 = Pattern.compile(".+")
}

