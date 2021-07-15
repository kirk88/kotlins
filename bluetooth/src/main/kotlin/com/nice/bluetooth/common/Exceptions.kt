package com.nice.bluetooth.common

typealias IOException = java.io.IOException

/** Failure occurred with the underlying Bluetooth system. */
open class BluetoothException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class BluetoothDisabledException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : BluetoothException(message, cause)

class ConnectionRejectedException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)

class NotReadyException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)

class GattStatusException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)

class ConnectionLostException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)

/**
 * Thrown when underlying [BluetoothGatt] method call returns `false`. This can occur under the following conditions:
 *
 * - Request isn't allowed (e.g. reading a non-readable characteristic)
 * - Underlying service or client interface is missing or invalid (e.g. `mService == null || mClientIf == 0`)
 * - Associated [BluetoothDevice] is unavailable
 * - Device is busy (i.e. a previous request is still in-progress)
 * - An Android internal failure occurred (i.e. an underlying [RemoteException] was thrown)
 */
class GattRequestRejectedException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : BluetoothException(message, cause)
