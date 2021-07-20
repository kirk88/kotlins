package com.nice.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import com.nice.bluetooth.common.*
import java.util.*

internal class AndroidAdvertisement(
    private val scanResult: BluetoothScanResult
) : Advertisement {

    private val scanRecord: ScanRecord?
        get() = scanResult.scanRecord

    override val bluetoothDevice: BluetoothDevice
        get() = scanResult.device

    override val name: String
        get() = bluetoothDevice.name.orEmpty()

    override val address: String
        get() = bluetoothDevice.address.orEmpty()

    override val bondState: BondState
        get() = when (bluetoothDevice.bondState) {
            BOND_NONE -> BondState.None
            BOND_BONDING -> BondState.Bonding
            BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${bluetoothDevice.bondState}")
        }

    override val rssi: Int
        get() = scanResult.rssi

    override val txPower: Int?
        get() = scanRecord?.txPowerLevel

    override val uuids: List<UUID>
        get() = scanRecord?.serviceUuids.orEmpty()

    override val manufacturerData: ManufacturerData?
        get() = scanRecord?.manufacturerSpecificData?.find { it.data.isNotEmpty() }

    override fun serviceData(uuid: UUID): ServiceData? =
        scanRecord?.getServiceData(uuid)

    override fun manufacturerData(companyIdentifierCode: Int): ManufacturerData? =
        scanRecord?.getManufacturerSpecificData(companyIdentifierCode)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndroidAdvertisement

        if (bluetoothDevice != other.bluetoothDevice) return false

        return true
    }

    override fun hashCode(): Int {
        return bluetoothDevice.hashCode()
    }

    override fun toString(): String =
        "Advertisement(name=$name, address=$address, rssi=$rssi, txPower=$txPower)"

}