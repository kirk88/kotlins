package com.nice.bluetooth.common

sealed class BluetoothState {

    object Opened : BluetoothState() {
        override fun toString(): String = "Opened"
    }

    object Opening : BluetoothState() {
        override fun toString(): String = "Opening"
    }

    object Closed : BluetoothState() {
        override fun toString(): String = "Closed"
    }

    object Closing : BluetoothState() {
        override fun toString(): String = "Closing"
    }

}

sealed class ConnectionState {

    object Connecting : ConnectionState() {
        override fun toString(): String = "Connecting"
    }

    object Connected : ConnectionState() {
        override fun toString(): String = "Connected"
    }

    object Disconnecting : ConnectionState() {
        override fun toString(): String = "Disconnecting"
    }

    /**
     * Triggered either after an established connection has dropped or after a connection attempt has failed.
     *
     * @param status represents status (cause) of [Disconnected] [ConnectionState]. Always `null` for Javascript target.
     */
    data class Disconnected(val status: Status? = null) : ConnectionState() {

        override fun toString(): String {
            return "Disconnected.Status.$status"
        }

        /**
         * State statuses translated from their respective platforms:
         *
         * - Android: https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#106
         */
        sealed class Status {

            /**
             * - Android: `GATT_CONN_TERMINATE_PEER_USER`
             */
            object PeripheralDisconnected : Status() {
                override fun toString(): String = "PeripheralDisconnected"
            }

            /**
             * - Android: `GATT_CONN_FAIL_ESTABLISH`
             */
            object Failed : Status() {
                override fun toString(): String = "Failed"
            }

            /**
             * - Android: `GATT_CONN_TIMEOUT`
             */
            object Timeout : Status() {
                override fun toString(): String = "Timeout"
            }


            /**
             * - Android: `GATT_CONN_CANCEL`
             */
            object Cancelled : Status() {
                override fun toString(): String = "Cancelled"
            }


            /** Catch-all for any statuses that are unknown for a platform. */
            data class Unknown(val status: Int) : Status() {
                override fun toString(): String = "Unknown($status)"
            }

        }
    }
}
