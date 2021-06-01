package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.core.database.getStringOrNull
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DB
import com.example.sample.db.Test
import com.example.sample.db.TestTable
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.plusAssign
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.sqlite.db.*
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
        val pdfView = binding.pdfView

        fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "value"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
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
                    val startN = System.nanoTime()
                    replace(TestTable.TABLE_NAME,
                        "id" with index.toLong(), "name" with "jack${index}",
                        "age" with 20, "number" with index, "data" with listOf("A", "B", "C", "D").joinToString(","),
                        "tip" with "lalalalal", "pp" with "")
                    Log.e("TAGTAG", "each: ${System.nanoTime() - startN}")
                }

                Log.e("TAGTAG", "insert: ${System.currentTimeMillis() - start}")

                start = System.currentTimeMillis()

                update(TestTable.TABLE_NAME).values(TestTable.NAME + "jack100").where(
                    TestTable.NAME.equal("jack1") or TestTable.NAME.equal("jack2")
                ).execute()

                update(TestTable.TABLE_NAME).values(TestTable.NAME + "jack101").where(
                    TestTable.NAME.equal("jack3") or TestTable.NAME.equal("jack4")
                ).execute()

                Log.e("TAGTAG", "update: ${System.currentTimeMillis() - start}")

                start = System.currentTimeMillis()

                val result: List<Test> =
                    query(TestTable.TABLE_NAME).where(TestTable.ID.notBetween(3, 6))
                        .execute {
                            mutableListOf<Test>().apply {
                                moveToFirst()
                                while (moveToNext()) {
                                    add(Test(
                                        getLong(0),
                                        getString(1),
                                        getInt(2),
                                        getInt(3),
                                        getString(4).split(","),
                                        getString(5),
                                        getStringOrNull(6)
                                    ))
                                }
                            }
                        }

                Log.e("TAGTAG", "query: ${System.currentTimeMillis() - start}")

                Log.e("TAGTAG", "size: ${result.size}  " + result.toString())
            }
        }

    }


}

