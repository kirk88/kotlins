package com.nice.bluetooth

import android.bluetooth.BluetoothDevice
import com.nice.bluetooth.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction = {}
): Peripheral = peripheral(advertisement.address, builderAction)

fun CoroutineScope.peripheral(
    address: String,
    builderAction: PeripheralBuilderAction = {}
): Peripheral = peripheral(Bluetooth.adapter.getRemoteDevice(address), builderAction)

private fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {}
): Peripheral {
    val builder = AndroidPeripheralBuilder()
    builder.builderAction()
    return AndroidPeripheral(
        coroutineContext,
        bluetoothDevice,
        builder.defaultTransport,
        builder.defaultPhy,
        builder.onConnected,
        builder.onServicesDiscovered
    )
}

class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    bluetoothDevice: BluetoothDevice,
    defaultTransport: Transport,
    defaultPhy: Phy,
    onConnected: ConnectedAction,
    onServicesDiscovered: ServicesDiscoveredAction
) : Peripheral {

    private val connection = PeripheralConnection(
        parentCoroutineContext,
        bluetoothDevice,
        defaultTransport,
        defaultPhy,
        onConnected,
        onServicesDiscovered
    )

    override val state: Flow<ConnectionState> = connection.state.asStateFlow()

    override val mtu: Flow<Int?> = connection.mtu.asStateFlow()

    override val phy: Flow<PreferredPhy?> = connection.phy.asStateFlow()

    override val services: List<DiscoveredService>
        get() = connection.services

    override suspend fun connect(autoConnect: Boolean) {
        connection.connect(autoConnect)
    }

    override suspend fun disconnect() {
        connection.disconnect()
    }

    override suspend fun readRssi(): Int = connection.readRssi()

    override suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority =
        connection.requestConnectionPriority(priority)

    override suspend fun requestMtu(mtu: Int): Int = connection.requestMtu(mtu)

    override suspend fun readPhy(): PreferredPhy = connection.readPhy()

    override suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return connection.setPreferredPhy(phy, options)
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) = connection.write(characteristic, data, writeType)

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = connection.read(characteristic)

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) = connection.write(descriptor, data)

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = connection.read(descriptor)

    override suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        connection.reliableWrite(action)
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = connection.observe(characteristic, onSubscription)

}