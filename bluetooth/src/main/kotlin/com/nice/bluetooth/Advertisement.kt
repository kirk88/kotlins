package com.nice.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.os.ParcelUuid
import com.nice.bluetooth.common.Advertisement
import com.nice.bluetooth.common.AndroidScanResult
import com.nice.bluetooth.common.BondState
import com.nice.bluetooth.common.ManufacturerData
import java.util.*

internal class AndroidAdvertisement(
    private val scanResult: AndroidScanResult
) : Advertisement {

    override val device: BluetoothDevice
        get() = scanResult.device

    override val name: String
        get() = device.name.orEmpty()

    override val address: String
        get() = device.address.orEmpty()

    override val bondState: BondState
        get() = when (device.bondState) {
            BOND_NONE -> BondState.None
            BOND_BONDING -> BondState.Bonding
            BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${device.bondState}")
        }

    override val rssi: Int
        get() = scanResult.rssi

    override val txPower: Int?
        get() = scanResult.scanRecord?.txPowerLevel

    override val uuids: List<UUID>
        get() = scanResult.scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()

    override val manufacturerData: ManufacturerData?
        get() = scanResult.scanRecord?.manufacturerSpecificData?.takeIf { it.size() > 0 }?.let {
            ManufacturerData(
                it.keyAt(0),
                it.valueAt(0)
            )
        }

    override fun serviceData(uuid: UUID): ByteArray? =
        scanResult.scanRecord?.getServiceData(ParcelUuid(uuid))

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        scanResult.scanRecord?.getManufacturerSpecificData(companyIdentifierCode)


    override fun toString(): String =
        "Advertisement(name=$name, bluetoothDevice=$device, rssi=$rssi, txPower=$txPower)"
}