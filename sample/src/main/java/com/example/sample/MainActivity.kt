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
import com.nice.bluetooth.Bluetooth
import com.nice.bluetooth.Scanner
import com.nice.bluetooth.common.Advertisement
import com.nice.bluetooth.common.BluetoothState
import com.nice.bluetooth.common.CharacteristicProperty
import com.nice.bluetooth.common.ConnectionState
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


class MainActivity : NiceViewModelActivity<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    override val tipView: TipView by tipViews()

    private val permissionRequestLauncher = PocketActivityResultLauncher(ActivityResultContracts.RequestMultiplePermissions())

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

//        lifecycleScope.launch(Dispatchers.IO) {
//            DB.use(true) {
//                var start = System.currentTimeMillis()
//                for (index in 0..1400) {
//                    val test = Test(
//                        index.toLong(),
//                        "jack$index",
//                        20,
//                        index,
//                        "lalalalal",
//                        "",
//                        null,
//                        true
//                    )
//
//                    insert(
//                        TestTable.TABLE_NAME,
//                        SQLiteDatabase.CONFLICT_REPLACE,
//                        test.toColumnElements()
//                    )
//                }
//
//                Log.e(TAG, "insert: ${System.currentTimeMillis() - start}")
//                start = System.currentTimeMillis()
//                updateBuilder(TestTable.TABLE_NAME)
//                    .values(TestTable.NAME + "jack100")
//                    .where(TestTable.ID.lessThan(10000))
//                    .execute()
//
//                updateBuilder(TestTable.TABLE_NAME)
//                    .values(TestTable.NAME + "jack101")
//                    .where(TestTable.NAME.equal("jack3") or TestTable.NAME.equal("jack4"))
//                    .execute()
//
//                Log.e(TAG, "update: ${System.currentTimeMillis() - start}")
//                start = System.currentTimeMillis()
//
//                val result = queryBuilder(TestTable.TABLE_NAME)
//                    .parseList<Test>()
//
//                Log.e(TAG, "query: ${System.currentTimeMillis() - start}  size: ${result.size}")
//            }
//        }

        initBle()
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
                Scanner().advertisements.scan(mutableSetOf<Advertisement>()) { accumulator, value ->
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

                        if(it.name.startsWith("SimpleBLE")) {
                            launch {
                                val state = peripheral.state.drop(1).first { it is ConnectionState.Disconnected }
                                Log.e(TAG, "${it.address}  disconnected: $state")
                            }

                            Log.e(TAG, "${it.address}  connecting")
                            peripheral.connect()

                            Log.e(TAG, "${it.address}  connected")

                            val service = peripheral.services.find { service ->
                                service.serviceUuid.toString().contains("ffe0", true)
                            }

                            val characteristic = service?.characteristics?.find { characteristic ->
                                characteristic.characteristicUuid.toString().contains("ffe1", true)
                            }

                            if(characteristic != null){
                                Log.e(TAG, "observe...")

                                peripheral.observe(characteristic){
                                    Log.e(TAG, "onSubscription")
                                }.collect {
                                    Log.e(TAG, "data : ${it.contentToString()}")
                                }
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

    private class BleAdapter(context: Context) : SimpleRecyclerAdapter<Advertisement>(context, android.R.layout.simple_list_item_2) {

        override fun onBindItemViewHolder(holder: ItemViewHolder, item: Advertisement, payloads: MutableList<Any>) {
            holder.findViewById<TextView>(android.R.id.text1).string = item.name
            holder.findViewById<TextView>(android.R.id.text2).string = item.address
        }

    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

}