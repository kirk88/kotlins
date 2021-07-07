package com.example.sample

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DB
import com.example.sample.db.Test
import com.example.sample.db.TestTable
import com.nice.kotlins.app.NiceViewModelActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.plusAssign
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.sqlite.db.*
import com.nice.kotlins.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : NiceViewModelActivity<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    override val tipView: TipView by tipViews()

    override val statefulView: StatefulView by lazy {
        StatefulFrameLayout.wrap(binding.contentView)
            .setOnErrorActionListener {
                viewModel.start()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        binding.fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "value"
            ) {
                Log.e(TAG, "" + it.component1() + " " + it.component2())
            }
        }

        val liveEvent = MutableLiveEvent<String>()
        liveEvent.observe(this) {
            Log.e(TAG, "event: $it")
        }

        liveEvent += "event1"
        liveEvent += "event2"

        lifecycleScope.launch(Dispatchers.IO) {
            DB.use(true) {
                var start = System.currentTimeMillis()
                for (index in 0..10000) {
                    val test = Test(
                        index.toLong(),
                        "jack$index",
                        20,
                        index,
                        "lalalalal",
                        "",
                        null,
                        true
                    )

                    insert(
                        TestTable.TABLE_NAME,
                        SQLiteDatabase.CONFLICT_REPLACE,
                        test.toColumnElements()
                    )
                }

                Log.e(TAG, "insert: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()
                updateBuilder(TestTable.TABLE_NAME)
                    .values(TestTable.NAME + "jack100")
                    .where(TestTable.ID.lessThan(10000))
                    .execute()

                updateBuilder(TestTable.TABLE_NAME)
                    .values(TestTable.NAME + "jack101")
                    .where(TestTable.NAME.equal("jack3") or TestTable.NAME.equal("jack4"))
                    .execute()

                Log.e(TAG, "update: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()

                val result = queryBuilder(TestTable.TABLE_NAME)
                    .parseList<Test>()

                Log.e(TAG, "query: ${System.currentTimeMillis() - start}  size: ${result.size}")
            }
        }

        viewModel.start()
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}