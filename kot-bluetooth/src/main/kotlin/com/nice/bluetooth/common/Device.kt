package com.nice.bluetooth.common

import java.util.*

interface Device {

    /**
     * The name in the Advertisement.
     * The same as, or a shortened version of, the local name assigned to the device
     */
    val name: String?

    /**
     * The alias in the Advertisement.
     */
    val alias: String?

    /**
     * The hardware address in the Advertisement
     */
    val address: String

    /**
     * The bond state of the Advertisement
     */
    val bondState: BondState

    /**
     * A list of Service or Service Class UUIDs.
     * According to the BLE specification, GAP and GATT service UUIDs should not be included here.
     */
    val uuids: List<UUID>

    /**
     * the type of the Advertisement
     */
    val type: Int

}