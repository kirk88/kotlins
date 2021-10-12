package com.nice.bluetooth.common

enum class BluetoothState {
    Opened,
    Opening,
    Closed,
    Closing
}

sealed class ConnectionState {

    sealed class Connecting : ConnectionState() {
        /**
         * [Peripheral] has initiated the process of connecting, via BluetoothDevice.
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) will throw [NotReadyException]
         * while in this state.
         */
        object Device : Connecting() {
            override fun toString(): String = "Connecting.Device"
        }

        /**
         * [Peripheral] has connected, but has not yet discovered services.
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) will throw [NotReadyException]
         * while in this state.
         */
        object Services : Connecting() {
            override fun toString(): String = "Connecting.Services"
        }

        /**
         * [Peripheral] is wiring up [PeripheralObservers][Peripheral.observe].
         *
         * I/O operations (e.g. [write][Peripheral.write] and [read][Peripheral.read]) are permitted while in this state.
         */
        object Observes : Connecting() {
            override fun toString(): String = "Connecting.Observes"
        }

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
             *  `GATT_CONN_TERMINATE_LOCAL_HOST`
             */
            object CentralDisconnected : Status() {
                override fun toString(): String = "CentralDisconnected"
            }

            /**
             *  `GATT_CONN_FAIL_ESTABLISH`
             */
            object Failed : Status() {
                override fun toString(): String = "Failed"
            }

            /**
             *  `GATT_CONN_L2C_FAILURE`
             */
            object L2CapFailure : Status() {
                override fun toString(): String = "L2CapFailure"
            }

            /**
             *  `GATT_CONN_TIMEOUT`
             */
            object Timeout : Status() {
                override fun toString(): String = "Timeout"
            }

            /**
             *  `GATT_CONN_LMP_TIMEOUT`
             */
            object LinkManagerProtocolTimeout : Status() {
                override fun toString(): String = "LinkManagerProtocolTimeout"
            }

            /**
             * `CBErrorOperationCancelled`
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

/**
 * Returns `true` if `this` is at least in state [T], where [ConnectionState]s are ordered:
 * - [ConnectionState.Disconnected] (smallest)
 * - [ConnectionState.Disconnecting]
 * - [ConnectionState.Connecting.Device]
 * - [ConnectionState.Connecting.Services]
 * - [ConnectionState.Connecting.Observes]
 * - [ConnectionState.Connected] (largest)
 */
internal inline fun <reified T : ConnectionState> ConnectionState.isAtLeast(): Boolean {
    val currentState = when (this) {
        is ConnectionState.Disconnected -> 0
        ConnectionState.Disconnecting -> 1
        ConnectionState.Connecting.Device -> 2
        ConnectionState.Connecting.Services -> 3
        ConnectionState.Connecting.Observes -> 4
        ConnectionState.Connected -> 5
    }
    val targetState = when (T::class) {
        ConnectionState.Disconnected::class -> 0
        ConnectionState.Disconnecting::class -> 1
        ConnectionState.Connecting.Device::class -> 2
        ConnectionState.Connecting.Services::class -> 3
        ConnectionState.Connecting.Observes::class -> 4
        ConnectionState.Connected::class -> 5
        else -> error("Unreachable.")
    }
    return currentState >= targetState
}
