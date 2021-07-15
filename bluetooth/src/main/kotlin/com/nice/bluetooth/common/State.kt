package com.nice.bluetooth.common

sealed class State {

    object Connecting : State()

    object Connected : State()

    object Disconnecting : State()

    /**
     * Triggered either after an established connection has dropped or after a connection attempt has failed.
     *
     * @param status represents status (cause) of [Disconnected] [State]. Always `null` for Javascript target.
     */
    data class Disconnected(val status: Status? = null) : State() {

        /**
         * State statuses translated from their respective platforms:
         *
         * - Android: https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#106
         * - Apple: `CBError.h` from the Core Bluetooth framework headers
         */
        sealed class Status {

            /**
             * - Android: `GATT_CONN_TERMINATE_PEER_USER`
             * - Apple: `CBErrorPeripheralDisconnected`
             */
            object PeripheralDisconnected : Status()

            /**
             * - Android: `GATT_CONN_FAIL_ESTABLISH`
             * - Apple: `CBErrorConnectionFailed`
             */
            object Failed : Status()

            /**
             * - Android: `GATT_CONN_TIMEOUT`
             * - Apple: `CBErrorConnectionTimeout`
             */
            object Timeout : Status()

            /**
             * - Apple: `CBErrorUnknownDevice`
             */
            object UnknownDevice : Status()

            /**
             * - Android: `GATT_CONN_CANCEL`
             * - Apple: `CBErrorOperationCancelled`
             */
            object Cancelled : Status()

            /**
             * - Apple: `CBErrorConnectionLimitReached`
             */
            object ConnectionLimitReached : Status()

            /**
             * - Apple: `CBErrorEncryptionTimedOut`
             */
            object EncryptionTimedOut : Status()

            /** Catch-all for any statuses that are unknown for a platform. */
            data class Unknown(val status: Int) : Status()
        }
    }
}
