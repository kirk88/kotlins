package com.nice.bluetooth

import com.nice.bluetooth.common.*

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
