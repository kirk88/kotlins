@file:Suppress("MissingPermission")

package com.nice.bluetooth.server

import android.bluetooth.BluetoothDevice
import android.os.Build
import com.nice.bluetooth.Bluetooth
import com.nice.bluetooth.common.BondState
import com.nice.bluetooth.common.Device
import java.util.*

internal class AndroidDevice(
    val device: BluetoothDevice
) : Device {

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
            BluetoothDevice.BOND_NONE -> BondState.None
            BluetoothDevice.BOND_BONDING -> BondState.Bonding
            BluetoothDevice.BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${device.bondState}")
        }

    override val uuids: List<UUID>
        get() = device.uuids?.map { it.uuid }.orEmpty()

    override val type: Int
        get() = device.type

}

internal fun Device.toBluetoothDevice(): BluetoothDevice {
    if (this is AndroidDevice) {
        return device
    }
    return Bluetooth.adapter.getRemoteDevice(address)
}

internal fun BluetoothDevice.toDevice(): Device = AndroidDevice(this)