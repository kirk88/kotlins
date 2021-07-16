package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.nice.bluetooth.common.ConnectionState
import com.nice.bluetooth.common.Phy
import com.nice.bluetooth.common.Transport
import com.nice.bluetooth.common.intValue
import com.nice.bluetooth.gatt.Callback
import com.nice.bluetooth.gatt.PreferredPhy
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

/**
 * @param defaultTransport is only used on API level >= 23.
 * @param defaultPhy is only used on API level >= 26.
 * @param phy is only used on API level >= 26
 */
internal fun BluetoothDevice.connect(
    context: Context,
    defaultTransport: Transport,
    defaultPhy: Phy,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    phy: MutableStateFlow<PreferredPhy?>,
    invokeOnClose: () -> Unit
): Connection? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectApi26(context, defaultTransport, defaultPhy, state, mtu, phy, invokeOnClose)
    } else {
        connectApi21(context, defaultTransport, state, mtu, invokeOnClose)
    }

/**
 * @param defaultTransport is only used on API level >= 23.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private fun BluetoothDevice.connectApi21(
    context: Context,
    defaultTransport: Transport,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    invokeOnClose: () -> Unit
): Connection? {
    val callback = Callback(state, mtu)
    val bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectGatt(context, false, callback, defaultTransport.intValue)
    } else {
        connectGatt(context, false, callback)
    } ?: return null

    // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
    // Disconnected before the connection request has kicked off the Connecting state (via Callback).
    state.value = ConnectionState.Connecting

    val dispatcher = Executors.newScheduledThreadPool(1) { runnable ->
        Thread(runnable, threadName).apply { isDaemon = true }
    }.asCoroutineDispatcher()
    return Connection(
        bluetoothGatt,
        dispatcher,
        callback
    ) {
        dispatcher.close()
        invokeOnClose.invoke()
    }
}

@TargetApi(Build.VERSION_CODES.O)
private fun BluetoothDevice.connectApi26(
    context: Context,
    defaultTransport: Transport,
    defaultPhy: Phy,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    phy: MutableStateFlow<PreferredPhy?>,
    invokeOnClose: () -> Unit
): Connection? {
    val thread = HandlerThread(threadName).apply { start() }
    try {
        val handler = Handler(thread.looper)
        val dispatcher = handler.asCoroutineDispatcher()
        val callback = Callback(state, mtu, phy)

        val bluetoothGatt =
            connectGatt(context, false, callback, defaultTransport.intValue, defaultPhy.intValue, handler)
                ?: return null

        // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
        // Disconnected before the connection request has kicked off the Connecting state (via Callback).
        state.value = ConnectionState.Connecting

        return Connection(
            bluetoothGatt,
            dispatcher,
            callback
        ) {
            thread.quit()
            invokeOnClose.invoke()
        }
    } catch (t: Throwable) {
        thread.quit()
        throw t
    }
}

private val BluetoothDevice.threadName: String
    get() = "Gatt@$this"
