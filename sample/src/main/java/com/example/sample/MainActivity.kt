package com.example.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DBTest
import com.example.sample.db.Database
import com.example.sample.db.TestTable
import com.nice.bluetooth.Bluetooth
import com.nice.bluetooth.Scanner
import com.nice.bluetooth.ScannerLevel
import com.nice.bluetooth.common.Advertisement
import com.nice.bluetooth.common.BluetoothState
import com.nice.bluetooth.peripheral
import com.nice.common.adapter.ItemViewHolder
import com.nice.common.adapter.SimpleRecyclerAdapter
import com.nice.common.app.NiceViewModelActivity
import com.nice.common.app.PocketActivityResultLauncher
import com.nice.common.app.launch
import com.nice.common.event.FlowEventBus.subscribeEvent
import com.nice.common.event.NamedEvent
import com.nice.common.helper.doOnClick
import com.nice.common.helper.setContentView
import com.nice.common.helper.string
import com.nice.common.helper.viewBindings
import com.nice.common.widget.*
import com.nice.sqlite.Transaction
import com.nice.sqlite.asMapSequence
import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.ConflictAlgorithm
import com.nice.sqlite.core.ddl.aliased
import com.nice.sqlite.core.ddl.asc
import com.nice.sqlite.core.ddl.date
import com.nice.sqlite.core.dml.select
import com.nice.sqlite.core.dml.update
import com.nice.sqlite.statementExecutor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


class MainActivity : NiceViewModelActivity<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val progressView: ProgressView by progressViews()

    override val statefulView: StatefulView by lazy {
        StatefulFrameLayout.wrap(binding.contentView)
    }

    private val binding: ActivityMainBinding by viewBindings()

    override val tipView: TipView by tipViews()

    private val permissionRequestLauncher =
        PocketActivityResultLauncher(ActivityResultContracts.RequestMultiplePermissions())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        permissionRequestLauncher.register(this)

        title = "Home"

        binding.fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "value"
            ) {
            }
        }

        subscribeEvent({ it is NamedEvent }) {
            Log.e(TAG, "what event: " + it.toString())
        }


        testDB()
//        initBle()

    }

    private fun testDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            Database.use(Transaction.Exclusive) {
                val beans = mutableListOf<DBTest>()
                repeat(5) { index ->
                    val bean = DBTest(
                        id = index.toLong(),
                        name = "jack",
                        age = index,
                        flag = true,
                        number = -index,
                        data = byteArrayOf(1, 2, 3)
                    )
                    beans.add(bean)
                }

                var start = System.currentTimeMillis()
                offer(TestTable).insertBatch(statementExecutor) {
                    for (bean in beans) {
                        item {
                            conflictAlgorithm = ConflictAlgorithm.Replace

                            values {
                                it.id(bean.id) + it.name(bean.name) + it.age(bean.age) +
                                        it.flag(bean.flag) + it.number(bean.number) + it.data(bean.data)
                            }
                        }
                    }
                }
                Log.e("TAGTAG", "insert time: ${System.currentTimeMillis() - start}")

                Log.e("TAGTAG", "==============================")

                offer(TestTable).select(statementExecutor) {
                    it.id + it.name + it.age + date(it.time)
                }.asMapSequence().forEach {
                    Log.e("TAGTAG", it.toString())
                }

                Thread.sleep(2000)

                offer(TestTable).where { it.id eq 0 }.update(statementExecutor) {
                    it.name("tom")
                }

                Log.e("TAGTAG", "==============================")

                offer(TestTable).orderBy { it.time.asc }.select(statementExecutor) {
                    it.id + it.name + it.age + it.time.local.aliased("time")
                }.asMapSequence().forEach {
                    Log.e("TAGTAG", it.toString())
                }

//                val start2 = System.currentTimeMillis()
//                offer(TestTable).updateBatch(statementExecutor) {
//                    for (bean in beans) {
//                        item {
//                            conflictAlgorithm = ConflictAlgorithm.Replace
//
//                            where {
//                                it.age gt 100
//                            }
//
//                            values {
//                                it.id(bean.id) + it.name(bean.name) + it.age(bean.age) +
//                                        it.flag(bean.flag) + it.number(bean.number) + it.data(bean.data)
//                            }
//                        }
//                    }
//                }
//                Log.e("TAGTAG", "update time: ${System.currentTimeMillis() - start2}")
//
//
//                offer(TestTable2).insertBatch(statementExecutor) {
//                    for (index in 0..4) {
//                        item {
//                            conflictAlgorithm = ConflictAlgorithm.Replace
//
//                            values {
//                                it.id(index) + it.pid(index) + it.name("tom") + it.age(13)
//                            }
//                        }
//                    }
//                }

//                offer(TestTable).where { it.id eq 3 }.delete(statementExecutor)

//                offer(TestTable).select(statementExecutor).asMapSequence().forEach {
//                    Log.e("TAGTAG", it.toString())
//                }
//
//                Log.e("TAGTAG", "==============================")
//
//                offer(TestTable2).select(statementExecutor).asMapSequence().forEach {
//                    Log.e("TAGTAG", it.toString())
//                }

//                Log.e("TAGTAG", "==============================")
//
//                offer(TestTable2).innerJoin(TestTable).on { testTable2, testTable ->
//                    testTable2.pid eq testTable.id
//                }.selectDistinct(statementExecutor) { testTable2, testTable ->
//                    testTable2.name + testTable2.pid + testTable2.age + testTable.number
//                }.asMapSequence().forEach {
//                    Log.e("TAGTAG", it.toString())
//                }
            }

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initBle() {
        val adapter = BleAdapter(this).also {
            binding.recyclerView.adapter = it
        }

        val scan = {
            val channel = Channel<Advertisement>(Channel.UNLIMITED)

            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, throwable.message, throwable)
            }) {
                Scanner(ScannerLevel.Low).advertisements.scan(mutableSetOf<Advertisement>()) { accumulator, value ->
                    if (accumulator.add(value)) {
                        withContext(Dispatchers.Main) {
                            adapter.addItem(value)
                        }
                        channel.send(value)
                    }
                    accumulator
                }.collect()
            }

            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, "connect error", throwable)
            }) {
                channel.consumeAsFlow().collect {
                    launch {
                        val peripheral = peripheral(it) {
                            onConnected {
                            }
                        }

                        launch {
                            peripheral.state.collect { state ->
                                Log.e(TAG, "${it.address}  state: $state")
                            }
                        }

                        peripheral.connect()

                        peripheral.services.forEach { service ->
                            Log.e(TAG, "service: $service")
                        }
                    }
                }

            }
        }

        val startScan = {
            permissionRequestLauncher.launch(Bluetooth.permissions) {
                if (it.all { entry -> entry.value }) {
                    scan()
                } else {
                    tipView.show("没有权限")
                }
            }
        }

        Bluetooth.state.onEach {
            Log.e(TAG, "state: $it")
            if (it == BluetoothState.Opened) {
                startScan()
            }
        }.launchIn(lifecycleScope)

        if (!Bluetooth.isEnabled) {
            Bluetooth.isEnabled = true
        }
    }

    private class BleAdapter(context: Context) :
        SimpleRecyclerAdapter<Advertisement>(context, android.R.layout.simple_list_item_2) {

        override fun onBindItemViewHolder(
            holder: ItemViewHolder,
            item: Advertisement,
            payloads: List<Any>
        ) {
            holder.findViewById<TextView>(android.R.id.text1).string = item.name
            holder.findViewById<TextView>(android.R.id.text2).string = item.address
        }

    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

}