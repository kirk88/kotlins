@file:Suppress("MissingPermission")

package com.nice.bluetooth.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.os.Build
import com.nice.bluetooth.common.*
import java.util.*

internal class AndroidAdvertisement(
    private val scanResult: BluetoothScanResult
) : Advertisement {

    private val device: BluetoothDevice
        get() = scanResult.device

    private val scanRecord: ScanRecord?
        get() = scanResult.scanRecord

    override val name: String?
        get() = device.name

    override val alias: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            device.alias
        } else {
            null
        }

    override val address: String
        get() = device.address.orEmpty()

    override val bondState: BondState
        get() = when (device.bondState) {
            BOND_NONE -> BondState.None
            BOND_BONDING -> BondState.Bonding
            BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${device.bondState}")
        }

    override val uuids: List<UUID>
        get() = device.uuids?.map { it.uuid }.orEmpty()

    override val type: Int
        get() = device.type

    override val rssi: Int
        get() = scanResult.rssi

    override val txPower: Int?
        get() = scanRecord?.txPowerLevel

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

        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }

    override fun toString(): String =
        "Advertisement(name=$name, address=$address, rssi=$rssi, txPower=$txPower)"

}