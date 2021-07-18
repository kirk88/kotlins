package com.nice.bluetooth

import com.nice.bluetooth.common.*
import com.nice.bluetooth.gatt.PreferredPhy

class AndroidServicesDiscoveredPeripheral internal constructor(
    private val peripheral: AndroidPeripheral
) : ServicesDiscoveredPeripheral {

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = peripheral.read(characteristic)

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = peripheral.read(descriptor)

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) = peripheral.write(characteristic, data, writeType)

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) = peripheral.write(descriptor, data)

}

class AndroidConnectedPeripheral internal constructor(
    private val peripheral: AndroidPeripheral
): ConnectedPeripheral {
    override suspend fun requestConnectionPriority(priority: Priority): Boolean {
        return peripheral.requestConnectionPriority(priority)
    }

    override suspend fun requestMtu(mtu: Int): Int {
        return peripheral.requestMtu(mtu)
    }

    override suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return peripheral.setPreferredPhy(phy, options)
    }

    override suspend fun readPhy(): PreferredPhy {
        return peripheral.readPhy()
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
    override var transport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    override var phy: Phy = Phy.Le1M

}
