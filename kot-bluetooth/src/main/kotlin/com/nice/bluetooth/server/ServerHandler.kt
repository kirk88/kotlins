@file:SuppressLint("MissingPermission")

package com.nice.bluetooth.server

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattServer
import android.os.Build
import androidx.annotation.RequiresApi
import com.nice.bluetooth.common.*

interface ServerHandler {

    val services: List<Service>

    fun connect(device: Device, autoConnect: Boolean)

    fun disconnect(device: Device)

    @RequiresApi(Build.VERSION_CODES.O)
    fun readPhy(device: Device)

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPreferredPhy(device: Device, phy: PreferredPhy, options: PhyOptions)

    fun notifyCharacteristicChanged(device: Device, characteristic: Characteristic, confirm: Boolean)

    fun sendResponse(device: Device, requestId: Int, status: Int, offset: Int, value: ByteArray)

}


internal class DefaultServerHandler(
    private val server: BluetoothGattServer
): ServerHandler{

    override val services: List<Service>
        get() = server.services?.map { it.toService() }.orEmpty()

    override fun connect(device: Device, autoConnect: Boolean) {
        server.connect(device.toBluetoothDevice(), autoConnect)
    }

    override fun disconnect(device: Device) {
        server.cancelConnection(device.toBluetoothDevice())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun readPhy(device: Device) {
        server.readPhy(device.toBluetoothDevice())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setPreferredPhy(device: Device, phy: PreferredPhy, options: PhyOptions) {
        server.setPreferredPhy(device.toBluetoothDevice(), phy.rxPhy.intValue, phy.txPhy.intValue, options.intValue)
    }

    override fun notifyCharacteristicChanged(device: Device, characteristic: Characteristic, confirm: Boolean) {
       server.notifyCharacteristicChanged(device.toBluetoothDevice(), characteristic.toBluetoothGattCharacteristic(), confirm)
    }

    override fun sendResponse(device: Device, requestId: Int, status: Int, offset: Int, value: ByteArray) {
        server.sendResponse(device.toBluetoothDevice(), requestId, status, offset, value)
    }
}