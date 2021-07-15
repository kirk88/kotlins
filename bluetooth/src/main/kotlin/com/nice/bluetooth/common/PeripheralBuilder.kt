package com.nice.bluetooth.common

import android.annotation.TargetApi
import android.os.Build

interface ServicesDiscoveredPeripheral {
    suspend fun read(
        characteristic: Characteristic
    ): ByteArray

    suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse
    )

    suspend fun read(
        descriptor: Descriptor
    ): ByteArray

    suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    )

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestConnectionPriority(priority: Priority): Boolean

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestMtu(mtu: Int): Int
}

internal typealias ServicesDiscoveredAction = suspend ServicesDiscoveredPeripheral.() -> Unit

interface PeripheralBuilder {
    var transport: Transport

    var phy: Phy

    fun onServicesDiscovered(action: ServicesDiscoveredAction)
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