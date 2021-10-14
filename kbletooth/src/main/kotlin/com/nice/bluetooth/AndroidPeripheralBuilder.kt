package com.nice.bluetooth

import com.nice.bluetooth.common.*

internal class AndroidServicesDiscoveredPeripheral(
    private val connection: PeripheralConnection
) : ServicesDiscoveredPeripheral {
    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = connection.read(characteristic)

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = connection.read(descriptor)

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) = connection.write(characteristic, data, writeType)

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) = connection.write(descriptor, data)

    override suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        connection.reliableWrite(action)
    }
}

internal class AndroidConnectedPeripheral(
    private val connection: PeripheralConnection
) : ConnectedPeripheral {
    override suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority {
        return connection.requestConnectionPriority(priority)
    }

    override suspend fun requestMtu(mtu: Int): Int {
        return connection.requestMtu(mtu)
    }

    override suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return connection.setPreferredPhy(phy, options)
    }

    override suspend fun readPhy(): PreferredPhy {
        return connection.readPhy()
    }

    override suspend fun readRssi(): Int {
        return connection.readRssi()
    }
}

class AndroidPeripheralBuilder internal constructor() : PeripheralBuilder {

    internal var onConnected: ConnectedAction = {}
    internal var onServicesDiscovered: ServicesDiscoveredAction = {}

    override fun onConnected(action: ConnectedAction) {
        onConnected = action
    }

    override fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        onServicesDiscovered = action
    }

    /** Preferred transport for GATT connections to remote dual-mode devices. */
    override var defaultTransport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    override var defaultPhy: Phy = Phy.Le1M

}
