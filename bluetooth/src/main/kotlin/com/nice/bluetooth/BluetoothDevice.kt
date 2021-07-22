package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.nice.bluetooth.common.*
import com.nice.bluetooth.gatt.Callback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

internal class ConnectionClient(
    val bluetoothGatt: BluetoothGatt,
    val dispatcher: CoroutineDispatcher,
    val callback: Callback,
    onClose: () -> Unit
) {

    init {
        callback.invokeOnDisconnected(onClose)
    }

    fun disconnect() = bluetoothGatt.disconnect()

    fun close() = bluetoothGatt.disconnect()

}

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
    onClose: () -> Unit
): ConnectionClient? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectApi26(context, defaultTransport, defaultPhy, state, mtu, phy, onClose)
    } else {
        connectDefault(context, defaultTransport, state, mtu, onClose)
    }

/**
 * @param defaultTransport is only used on API level >= 23.
 */
private fun BluetoothDevice.connectDefault(
    context: Context,
    defaultTransport: Transport,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    onClose: () -> Unit
): ConnectionClient? {
    val callback = Callback(state, mtu)
    val bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectGatt(context, false, callback, defaultTransport.intValue)
    } else {
        connectGatt(context, false, callback)
    } ?: return null

    // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
    // Disconnected before the connection request has kicked off the Connecting state (via Callback).
    state.value = ConnectionState.Connecting

    val dispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, threadName).apply { isDaemon = true }
    }.asCoroutineDispatcher()
    return ConnectionClient(
        bluetoothGatt,
        dispatcher,
        callback
    ) {
        dispatcher.close()
        onClose.invoke()
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
    onClose: () -> Unit
): ConnectionClient? {
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

        return ConnectionClient(
            bluetoothGatt,
            dispatcher,
            callback
        ) {
            thread.quit()
            onClose.invoke()
        }
    } catch (t: Throwable) {
        thread.quit()
        throw t
    }
}

private val BluetoothDevice.threadName: String
    get() = "Gatt@$this"
