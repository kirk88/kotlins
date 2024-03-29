package com.nice.bluetooth.common

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.os.Build
import kotlin.coroutines.cancellation.CancellationException

interface ServicesDiscoveredPeripheral : Readable, Writable {

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun reliableWrite(action: suspend Writable.() -> Unit)

}

interface ConnectedPeripheral {

    suspend fun readRssi(): Int

    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority

    suspend fun requestMtu(mtu: Int): Int

    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy

    suspend fun readPhy(): PreferredPhy

}

internal typealias ServicesDiscoveredAction = suspend ServicesDiscoveredPeripheral.() -> Unit
internal typealias ConnectedAction = suspend ConnectedPeripheral.() -> Unit

interface PeripheralBuilder {

    fun onConnected(action: ConnectedAction)

    fun onServicesDiscovered(action: ServicesDiscoveredAction)

    var defaultTransport: Transport

    var defaultPhy: Phy

}

/** Preferred transport for GATT connections to remote dual-mode devices. */
enum class Transport {

    /** No preference of physical transport for GATT connections to remote dual-mode devices. */
    Auto,

    /** Prefer BR/EDR transport for GATT connections to remote dual-mode devices. */
    BrEdr,

    /** Prefer LE transport for GATT connections to remote dual-mode devices. */
    Le,
}

/** Preferred Physical Layer (PHY) for connections to remote LE devices. */
enum class Phy {

    /** Bluetooth LE 1M PHY. */
    Le1M,

    /**
     * Bluetooth LE 2M PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b25):
     * "The new LE 2M PHY allows the physical layer to operate at 2 Ms/s and thus enables higher data rates than LE 1M
     * and Bluetooth 4."
     */
    Le2M,

    /**
     * Bluetooth LE Coded PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b26):
     * "The LE Coded PHY allows range to be quadrupled (approximately), compared to Bluetooth® 4 and this has been
     * accomplished without increasing the transmission power required."
     */
    LeCoded,
}

internal val Transport.intValue: Int
    @TargetApi(Build.VERSION_CODES.M)
    get() = when (this) {
        Transport.Auto -> BluetoothDevice.TRANSPORT_AUTO
        Transport.BrEdr -> BluetoothDevice.TRANSPORT_BREDR
        Transport.Le -> BluetoothDevice.TRANSPORT_LE
    }

internal val Phy.intValue: Int
    @TargetApi(Build.VERSION_CODES.O)
    get() = when (this) {
        Phy.Le1M -> BluetoothDevice.PHY_LE_1M
        Phy.Le2M -> BluetoothDevice.PHY_LE_2M
        Phy.LeCoded -> BluetoothDevice.PHY_LE_CODED
    }