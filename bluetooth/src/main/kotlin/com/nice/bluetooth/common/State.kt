package com.nice.bluetooth.common

enum class BluetoothState {
    Opened,
    Opening,
    Closed,
    Closing
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
     * @param status represents status (cause) of [Disconnected].
     */
    data class Disconnected(val status: Status? = null) : ConnectionState() {

        override fun toString(): String {
            return if (status != null) {
                "Disconnected.Status.$status"
            } else {
                "Disconnected"
            }
        }

        /**
         * State statuses translated from their respective platforms:
         *
         * https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#106
         */
        sealed class Status {

            /**
             *  `GATT_CONN_TERMINATE_PEER_USER`
             */
            object PeripheralDisconnected : Status() {
                override fun toString(): String = "PeripheralDisconnected"
            }

            /**
             *  `GATT_CONN_FAIL_ESTABLISH`
             */
            object Failed : Status() {
                override fun toString(): String = "Failed"
            }

            /**
             *  `GATT_CONN_TIMEOUT`
             */
            object Timeout : Status() {
                override fun toString(): String = "Timeout"
            }


            /**
             *  `GATT_CONN_CANCEL`
             */
            object Cancelled : Status() {
                override fun toString(): String = "Cancelled"
            }


            /** Catch-all for any statuses that are unknown. */
            data class Unknown(val status: Int) : Status() {
                override fun toString(): String = "Unknown($status)"
            }

        }
    }
}
