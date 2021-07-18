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
import com.nice.bluetooth.peripheral
import com.nice.kotlins.adapter.ItemViewHolder
import com.nice.kotlins.adapter.SimpleRecyclerAdapter
import com.nice.kotlins.app.NiceViewModelActivity
import com.nice.kotlins.app.PocketActivityResultLauncher
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.string
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis


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


    @OptIn(FlowPreview::class)
    private fun initBle() {
        val adapter = BleAdapter(this).also {
            binding.recyclerView.adapter = it
        }

        val scan = {
            val channel = Channel<Advertisement>(Channel.UNLIMITED)

            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, throwable.message, throwable)
            }) {
                Scanner().advertisements.collect {
                    withContext(Dispatchers.Main){
                        adapter.addItem(it)
                    }

                    channel.send(it)
                }
            }

            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, throwable.message, throwable)
            }){
                channel.consumeAsFlow().collect {
                    val peripheral = peripheral(it) {

                        onServicesDiscovered {
                        }

                    }
                    Log.e(TAG, "connecting")
                    peripheral.connect()

                    peripheral.services.forEach { service ->
                        Log.e(TAG, "service: $service")

                        service.forEach { c ->
                            peripheral.observe(c).collect { b ->
                                Log.e(TAG, "observe: $b")
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

        if (!Bluetooth.isEnabled) {
            Bluetooth.isEnabled = true

            Bluetooth.state.onEach {
                Log.e(TAG, "state: $it")
                if(it == BluetoothState.Opened){
                    startScan()
                }
            }.launchIn(lifecycleScope)

            return
        }

        startScan()


    }

    private class BleAdapter(context: Context) : SimpleRecyclerAdapter<Advertisement>(context, android.R.layout.simple_list_item_1) {

        override fun onBindItemViewHolder(holder: ItemViewHolder, item: Advertisement, payloads: MutableList<Any>) {
            holder.findViewById<TextView>(android.R.id.text1).string = item.name + "\n" + item.address
        }

    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

}