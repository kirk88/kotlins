package com.example.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DB
import com.example.sample.db.DBTest
import com.example.sample.db.TestTable
import com.example.sample.db.TestView
import com.nice.bluetooth.Bluetooth
import com.nice.bluetooth.Scanner
import com.nice.bluetooth.ScannerType
import com.nice.bluetooth.common.*
import com.nice.bluetooth.peripheral
import com.nice.common.adapter.ItemViewHolder
import com.nice.common.adapter.SimpleRecyclerAdapter
import com.nice.common.app.NiceViewModelActivity
import com.nice.common.app.PocketActivityResultLauncher
import com.nice.common.app.launch
import com.nice.common.helper.doOnClick
import com.nice.common.helper.setContentView
import com.nice.common.helper.string
import com.nice.common.helper.viewBindings
import com.nice.common.widget.ProgressView
import com.nice.common.widget.TipView
import com.nice.common.widget.progressViews
import com.nice.common.widget.tipViews
import com.nice.sqlite.Transaction
import com.nice.sqlite.asMapSequence
import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.ConflictAlgorithm
import com.nice.sqlite.statementExecutor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


class MainActivity : NiceViewModelActivity<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val progressView: ProgressView by progressViews()

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
                Log.e(TAG, "" + it.component1() + " " + it.component2())
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            DB.use(Transaction.Exclusive) {
                val beans = mutableListOf<DBTest>()
                repeat(10000) { index ->
                    val bean = DBTest(id = index.toLong(), name = "jack", age = index, flag = true, number = -index, data = byteArrayOf(1, 2, 3))
                    beans.add(bean)
                }

                var start = System.currentTimeMillis()
                offer(TestTable).insertBatch(statementExecutor) {
                    for (bean in beans) {
                        item {
                            conflictAlgorithm = ConflictAlgorithm.Replace

                            assignments {
                                it.id(bean.id) + it.name(bean.name) + it.age(bean.age) +
                                        it.flag(bean.flag) + it.number(bean.number) + it.data(bean.data)
                            }
                        }
                    }
                }.also {
                    Log.e(TAG, "insert last row id: $it")
                }
                Log.e(TAG, "insert: ${System.currentTimeMillis() - start}")

                start = System.currentTimeMillis()
                offer(TestTable).updateBatch(statementExecutor){
                    for (bean in beans){
                        item {
                            conflictAlgorithm = ConflictAlgorithm.Replace

                            where {
                                it.id eq bean.id
                            }

                            assignments {
                                it.id(bean.id) + it.name(bean.name) + it.age(bean.age) +
                                        it.flag(bean.flag) + it.number(bean.number) + it.data(bean.data)
                            }
                        }
                    }
                }.also {
                    Log.e(TAG, "update number of rows: $it")
                }
                Log.e(TAG, "update: ${System.currentTimeMillis() - start}")

                start = System.currentTimeMillis()
                var count = 0
                for (row in offer(TestView).select(statementExecutor).asMapSequence()) {
                    if (count <= 10) {
                        Log.e(TAG, row.toString())
                    } else {
                        break
                    }
                    count++
                }
                Log.e(TAG, "query: ${System.currentTimeMillis() - start}")

            }

        }

//        initBle()
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
                Scanner(ScannerType.Low).advertisements.scan(mutableSetOf<Advertisement>()) { accumulator, value ->
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
                            val state = peripheral.state.drop(1)
                                .first { it is ConnectionState.Disconnected }
                            Log.e(TAG, "${it.address}  disconnected: $state")
                        }

                        Log.e(TAG, "${it.address}  connecting")
                        peripheral.connect()

                        Log.e(TAG, "${it.address}  connected")

                        val characteristic = peripheral.findService { service ->
                            service.serviceUuid.toString().contains("ffe0", true)
                        }?.findCharacteristic { characteristic ->
                            characteristic.characteristicUuid.toString().contains("ffe1", true)
                        }

                        if (characteristic != null) {
                            Log.e(TAG, "observe...")

                            peripheral.observe(characteristic) {
                                Log.e(TAG, "onSubscription")
                            }.collect {
                                Log.e(TAG, "data : ${it.contentToString()}")
                            }
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
            payloads: MutableList<Any>
        ) {
            holder.findViewById<TextView>(android.R.id.text1).string = item.name
            holder.findViewById<TextView>(android.R.id.text2).string = item.address

        }

    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

}