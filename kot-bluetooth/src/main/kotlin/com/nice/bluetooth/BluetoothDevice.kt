@file:Suppress("MissingPermission")

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

internal class ConnectionHandler(
    private val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    onClose: () -> Unit
) {

    init {
        callback.invokeOnDisconnected(onClose)
    }

    private val mutex = Mutex()

    val services: List<DiscoveredService>
        get() = bluetoothGatt.services.map { it.toDiscoveredService() }

    fun connect() = bluetoothGatt.connect()

    fun disconnect() = bluetoothGatt.disconnect()

    fun close() = bluetoothGatt.close()

    /**
     * Executes specified [BluetoothGatt] action.
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially. In other words, the
     * response for an action must be received before another action can be performed. Additionally, the Android BLE
     * stack can be unstable if I/O isn't performed on a dedicated thread.
     *
     * These requirements are fulfilled by ensuring that all action's are performed behind a [Mutex]. On Android pre-O
     * a single threaded [CoroutineDispatcher] is used, Android O and newer a [CoroutineDispatcher] backed by an Android
     * `Handler` is used (and is also used in the Android BLE [Callback]).
     *
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     */
    suspend inline fun <T> execute(
        crossinline action: suspend BluetoothGatt.() -> Boolean,
        crossinline response: suspend Callback.() -> T
    ): T = mutex.withLock {
        withContext(dispatcher) {
            bluetoothGatt.action() || throw GattRequestRejectedException()
        }

        callback.response()
    }


    /**
     * Executes specified [BluetoothGatt] action.
     *
     * @throws GattRequestRejectedException if [action] cause an exception.
     */
    suspend inline fun <T> tryExecute(
        crossinline action: suspend BluetoothGatt.() -> Unit,
        crossinline response: suspend Callback.() -> T
    ): T = mutex.withLock {
        withContext(dispatcher) {
            try {
                bluetoothGatt.action()
            } catch (t: Throwable) {
                throw GattRequestRejectedException(cause = t)
            }
        }

        callback.response()
    }

    suspend fun collectCharacteristicChanges(observers: PeripheralObservers) {
        callback.onCharacteristicChanged.consumeAsFlow()
            .map { (bluetoothGattCharacteristic, value) ->
                PeripheralEvent.CharacteristicChange(
                    bluetoothGattCharacteristic.toCharacteristic(),
                    value
                )
            }.collect {
                observers.emit(it)
            }
    }

    fun setCharacteristicNotification(characteristic: DiscoveredCharacteristic, enabled: Boolean) {
        bluetoothGatt.setCharacteristicNotification(
            characteristic.bluetoothGattCharacteristic,
            enabled
        ) || throw GattRequestRejectedException()
    }

}

/**
 * @param defaultTransport is only used on API level >= 23.
 * @param defaultPhy is only used on API level >= 26.
 * @param phy is only used on API level >= 26
 */
internal fun BluetoothDevice.connect(
    context: Context,
    autoConnect: Boolean,
    defaultTransport: Transport,
    defaultPhy: Phy,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    phy: MutableStateFlow<PreferredPhy?>,
    onClose: () -> Unit
): ConnectionHandler? {
    // Explicitly set Connecting state so when Peripheral is suspending until Connected, it doesn't incorrectly see
    // Disconnected before the connection request has kicked off the Connecting state (via Callback).
    state.value = ConnectionState.Connecting.Device

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectApi26(context, autoConnect, defaultTransport, defaultPhy, state, mtu, phy, onClose)
    } else {
        connectApi23(context, autoConnect, defaultTransport, state, mtu, onClose)
    }
}

/**
 * @param defaultTransport is only used on API level >= 23.
 */
private fun BluetoothDevice.connectApi23(
    context: Context,
    autoConnect: Boolean,
    defaultTransport: Transport,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    onClose: () -> Unit
): ConnectionHandler? {
    val callback = Callback(state, mtu)
    val bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectGatt(context, autoConnect, callback, defaultTransport.intValue)
    } else {
        connectGatt(context, autoConnect, callback)
    } ?: return null

    val dispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, threadName).apply { isDaemon = true }
    }.asCoroutineDispatcher()
    return ConnectionHandler(
        bluetoothGatt,
        dispatcher,
        callback,
        onClose = { dispatcher.close(); onClose.invoke() }
    )
}

@TargetApi(Build.VERSION_CODES.O)
private fun BluetoothDevice.connectApi26(
    context: Context,
    autoConnect: Boolean,
    defaultTransport: Transport,
    defaultPhy: Phy,
    state: MutableStateFlow<ConnectionState>,
    mtu: MutableStateFlow<Int?>,
    phy: MutableStateFlow<PreferredPhy?>,
    onClose: () -> Unit
): ConnectionHandler? {
    val thread = HandlerThread(threadName).apply { start() }
    try {
        val handler = Handler(thread.looper)
        val dispatcher = handler.asCoroutineDispatcher()
        val callback = Callback(state, mtu, phy)

        val bluetoothGatt = connectGatt(
            context,
            autoConnect,
            callback,
            defaultTransport.intValue,
            defaultPhy.intValue,
            handler
        ) ?: return null

        return ConnectionHandler(
            bluetoothGatt,
            dispatcher,
            callback,
            onClose = { thread.quit(); onClose.invoke() }
        )
    } catch (t: Throwable) {
        thread.quit()
        throw t
    }
}

private val BluetoothDevice.threadName: String
    get() = "Gatt@$this"
