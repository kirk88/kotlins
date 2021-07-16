package com.nice.bluetooth

import com.nice.bluetooth.common.*

class AndroidServicesDiscoveredPeripheral internal constructor(
    private val peripheral: AndroidPeripheral
) : ServicesDiscoveredPeripheral {

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = peripheral.read(characteristic)

    override suspend fun read(
        descriptor: DiscoveredDescriptor
    ): ByteArray = peripheral.read(descriptor)

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) = peripheral.write(characteristic, data, writeType)

    override suspend fun write(
        descriptor: DiscoveredDescriptor,
        data: ByteArray
    ) = peripheral.write(descriptor, data)

    override suspend fun requestConnectionPriority(priority: Priority): Boolean {
        return peripheral.requestConnectionPriority(priority)
    }

    override suspend fun requestMtu(mtu: Int): Boolean {
        return peripheral.requestMtu(mtu)
    }

    override suspend fun setPreferredPhy(txPhy: Phy, rxPhy: Phy, options: PhyOptions): Boolean {
        return peripheral.setPreferredPhy(txPhy, rxPhy, options)
    }
}

class AndroidPeripheralBuilder internal constructor() : PeripheralBuilder {

    internal var onServicesDiscovered: ServicesDiscoveredAction = {}
    override fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        onServicesDiscovered = action
    }

    /** Preferred transport for GATT connections to remote dual-mode devices. */
    override var transport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    override var phy: Phy = Phy.Le1M

}
