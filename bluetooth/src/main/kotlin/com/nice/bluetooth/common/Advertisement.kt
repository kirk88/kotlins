package com.nice.bluetooth.common

import android.bluetooth.BluetoothDevice
import java.util.*

enum class BondState {
    None,
    Bonding,
    Bonded,
}

interface Advertisement {
    val bluetoothDevice: BluetoothDevice

    /**
     * The name in the Advertisement.
     * The same as, or a shortened version of, the local name assigned to the device
     */
    val name: String

    /**
     * The hardware address in the Advertisement
     */
    val address: String

    /**
     * The bond state of the remote device
     */
    val bondState: BondState

    /**
     * The received signal strength, in dBm, of the packet received.
     */
    val rssi: Int

    /**
     * The TX Power Level data type indicates the transmitted/radiated power level of the Advertisement packet.
     * The path loss on a received packet may be calculated using the following equation:
     * `pathloss = txPower – rssi`
     */
    val txPower: Int?

    /**
     * A list of Service or Service Class UUIDs.
     * According to the BLE specification, GAP and GATT service UUIDs should not be included here.
     */
    val uuids: List<UUID>

    /**
     * The Manufacturer Specific Data, or null if none provided in the Advertisement packet.
     */
    val manufacturerData: ManufacturerData?

    /**
     * Lookup the data associated with a Service
     *
     * @param uuid the Service UUID
     * @return the data associated with the service or `null` if not found
     */
    fun serviceData(uuid: UUID): ServiceData?

    /**
     * Lookup the Manufacturer Specific Data by
     * [Company Identifier Code][https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/]
     *
     * @param companyIdentifierCode the two-octet code identifying the manufacturer
     * @return the Manufacturer Data for the given code (does not include the leading two identifier octets),
     * or `null` if not found
     */
    fun manufacturerData(companyIdentifierCode: Int): ManufacturerData?

}
