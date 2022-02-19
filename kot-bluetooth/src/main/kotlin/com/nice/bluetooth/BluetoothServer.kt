@file:Suppress("MissingPermission")

package com.nice.bluetooth

import android.bluetooth.BluetoothGattServer
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import com.nice.bluetooth.common.AdvertiseState
import com.nice.bluetooth.common.BluetoothServer
import com.nice.bluetooth.common.Service
import com.nice.bluetooth.common.toBluetoothGattService
import com.nice.bluetooth.gatt.ServerCallback
import com.nice.bluetooth.gatt.ServerEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BluetoothServerBuilder internal constructor() {

    private val _services by lazy { mutableListOf<Service>() }
    internal val services: List<Service>
        get() = _services

    fun addService(service: Service) {
        _services.add(service)
    }

    var settings: AdvertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setConnectable(true)
        .build()

    var advertiseData: AdvertiseData = AdvertiseData.Builder().build()


    var scanResponse: AdvertiseData = AdvertiseData.Builder().build()

}

fun BluetoothServer(buildAction: BluetoothServerBuilder.() -> Unit): BluetoothServer {
    val builder = BluetoothServerBuilder().apply(buildAction)
    return DefaultBluetoothServer(
        builder.settings,
        builder.advertiseData,
        builder.scanResponse,
        builder.services
    )
}


private class DefaultBluetoothServer(
    private val settings: AdvertiseSettings,
    private val advertiseData: AdvertiseData,
    private val scanResponse: AdvertiseData,
    private val services: List<Service>
) : BluetoothServer {

    private val bluetoothManager = Bluetooth.manager
    private val bluetoothAdapter = Bluetooth.adapter

    private val bluetoothGattServer: BluetoothGattServer by lazy {
        bluetoothManager.openGattServer(applicationContext, bluetoothGattServerCallback)
    }

    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        requireNotNull(bluetoothAdapter.bluetoothLeAdvertiser) {
            "Bluetooth is disabled or Bluetooth LE Advertising is not supported on this device"
        }
    }

    @Volatile
    private var isStarted = false

    private val _advertiseState = Channel<AdvertiseState>()
    override val advertiseState: Flow<AdvertiseState> = _advertiseState.consumeAsFlow()

    private val _serverEvent = Channel<ServerEvent>()
    override val serverEvent: Flow<ServerEvent> = _serverEvent.consumeAsFlow()

    private val bluetoothGattServerCallback = ServerCallback(_serverEvent)

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            _advertiseState.trySendBlocking(AdvertiseState.Success(settingsInEffect))
        }

        override fun onStartFailure(errorCode: Int) {
            _advertiseState.trySendBlocking(AdvertiseState.Failure(errorCode))
        }
    }

    override fun addService(service: Service) {
        bluetoothGattServer.addService(service.toBluetoothGattService())
    }

    override fun start() {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        for (service in services) {
            addService(service)
        }

        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponse, advertiseCallback)

        isStarted = true
    }

    override fun stop() {
        if (isStarted) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
            bluetoothGattServer.close()
        }
    }
}