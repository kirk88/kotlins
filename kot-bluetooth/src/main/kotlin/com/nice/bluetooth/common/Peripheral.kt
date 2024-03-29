@file:Suppress("UNUSED")

package com.nice.bluetooth.common

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

internal typealias PeripheralBuilderAction = PeripheralBuilder.() -> Unit
internal typealias OnSubscriptionAction = suspend () -> Unit

data class PreferredPhy(
    val txPhy: Phy,
    val rxPhy: Phy
)

enum class PhyOptions {
    NoPreferred,
    S2,
    S8
}

enum class WriteType {
    WithResponse,
    WithoutResponse
}

enum class ConnectionPriority {
    Low,
    Balanced,
    High
}

internal val Int.phy: Phy
    get() = when (this) {
        BluetoothDevice.PHY_LE_1M -> Phy.Le1M
        BluetoothDevice.PHY_LE_2M -> Phy.Le2M
        BluetoothDevice.PHY_LE_CODED -> Phy.LeCoded
        else -> error("Unknown phy: $this")
    }

internal val ConnectionPriority.intValue: Int
    get() = when (this) {
        ConnectionPriority.Low -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
        ConnectionPriority.Balanced -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        ConnectionPriority.High -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    }

internal val WriteType.intValue: Int
    get() = when (this) {
        WriteType.WithResponse -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        WriteType.WithoutResponse -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    }

internal val PhyOptions.intValue: Int
    @TargetApi(Build.VERSION_CODES.O)
    get() = when (this) {
        PhyOptions.NoPreferred -> BluetoothDevice.PHY_OPTION_NO_PREFERRED
        PhyOptions.S2 -> BluetoothDevice.PHY_OPTION_S2
        PhyOptions.S8 -> BluetoothDevice.PHY_OPTION_S8
    }

interface Readable {

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun read(
        characteristic: Characteristic
    ): ByteArray

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun read(
        descriptor: Descriptor
    ): ByteArray

}

interface Writable {

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse
    )

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    )

}

interface Peripheral : Readable, Writable {

    /**
     * Provides a conflated [Flow] of the [Peripheral]'s [ConnectionState].
     *
     * After [connect] is called, the [state] will typically transition through the following [states][ConnectionState]:
     *
     * ```
     *     connect()
     *         :
     *         v
     *   .------------.       .-----------.
     *   | Connecting | ----> | Connected |
     *   '------------'       '-----------'
     *                              :
     *                       disconnect() or
     *                       connection drop
     *                              :
     *                              v
     *                      .---------------.       .--------------.
     *                      | Disconnecting | ----> | Disconnected |
     *                      '---------------'       '--------------'
     * ```
     */
    val state: Flow<ConnectionState>

    /**
     * [Flow] of the most recently negotiated MTU. The MTU will change upon a successful request to change the MTU
     * (via [requestMtu]), or if the peripheral initiates an Phy change. [Flow]'s `value` will be `null` until MTU
     * is negotiated.
     */
    val mtu: Flow<Int?>

    /**
     * [Flow] of the most recently negotiated PHY. The PHY will change upon a successful request to change the PHY
     * (via [setPreferredPhy]), or if the peripheral initiates an PHY change. [Flow]'s `value` will be `null` until PHY
     * is negotiated.
     */
    val phy: Flow<PreferredPhy?>

    /** @return discovered [services][Service], or `null` until a [connection][connect] has been established. */
    val services: List<Service>

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent invocations will all
     * suspend until connected (or failure occurs). If already connected, then returns immediately.
     *
     * @throws ConnectionRejectedException when a connection request is rejected by the system (e.g. bluetooth hardware unavailable).
     * @throws IllegalStateException if [Peripheral]'s Coroutine scope has been cancelled.
     *
     * @param autoConnect Whether to directly connect to the remote device (false) or to
     * automatically connect as soon as the remote device becomes available (true).
     *
     */
    suspend fun connect(autoConnect: Boolean = false)

    /**
     * Disconnects the active connection, or cancels an in-flight [connection][connect] attempt, suspending until
     * [Peripheral] has settled on a [disconnected][ConnectionState.Disconnected] state.
     *
     * Multiple concurrent invocations will all suspend until disconnected (or failure occurs).
     */
    suspend fun disconnect()

    /**
     * Executes a reliable write transaction.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun reliableWrite(action: suspend Writable.() -> Unit)

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun readRssi(): Int

    /**
     * Request a specific connection priority
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority

    /**
     * Requests that the current connection's MTU be changed. Suspends until the MTU changes, or failure occurs. The
     * negotiated MTU value is returned, which may not be [mtu] value requested if the remote peripheral negotiated an
     * alternate MTU.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun requestMtu(mtu: Int): Int

    /**
     * Set the preferred connection PHY for this app. Please note that this is just a
     * recommendation, whether the PHY change will happen depends on other applications preferences,
     * local and remote controller capabilities. Controller can override these settings.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy

    /**
     * Read the current transmitter PHY and receiver PHY of the connection
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    suspend fun readPhy(): PreferredPhy

    /**
     * Observes changes to the specified [Characteristic].
     *
     * Observations can be setup ([observe] can be called) prior to a [connection][connect] being established. Once
     * connected, the observation will automatically start emitting changes. If connection is lost, [Flow] will remain
     * active, once reconnected characteristic changes will begin emitting again.
     *
     * If characteristic has a Client Characteristic Configuration descriptor (CCCD), then based on bits in the
     * [characteristic] properties, observe will be configured (CCCD will be written to) as **notification** or
     * **indication** (if [characteristic] supports both notifications and indications, then only **notification** is
     * used).
     *
     * Failures related to notifications are propagated via the returned [observe] [Flow], for example, if the specified
     * [characteristic] is invalid or cannot be found then a [NoSuchElementException] is propagated via the returned
     * [Flow].
     *
     * The optional [onSubscription] parameter is functionally identical to using the
     * [onSubscription][kotlinx.coroutines.flow.onSubscription] operator on the returned [Flow] except it has the
     * following special properties:
     *
     * - It will be executed whenever [connection][connect] is established (while the returned [Flow] is active); and
     * - It will be executed _after_ the observation is spun up (i.e. after enabling notifications or indications)
     *
     * The [onSubscription] action is useful in situations where an initial operation is needed when starting an
     * observation (such as writing a configuration to the peripheral and expecting the response to come back in the
     * form of a characteristic change). The [onSubscription] is invoked for every new subscriber; if it is desirable to
     * only invoke the [onSubscription] once per connection (for the specified [characteristic]) then you can either
     * use the [shareIn][kotlinx.coroutines.flow.shareIn] [Flow] operator on the returned [Flow], or call [observe]
     * again with the same [characteristic] and without specifying an [onSubscription] action.
     *
     * If multiple [observations][observe] are created for the same [characteristic] but with different [onSubscription]
     * actions, then the [onSubscription] actions will be executed in the order in which the returned [Flow]s are
     * subscribed to.
     */
    fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {}
    ): Flow<ByteArray>

}

fun Peripheral.findService(serviceUuid: UUID): Service? {
    return services.find { it.uuid == serviceUuid }
}

fun Peripheral.findService(predicate: (Service) -> Boolean): Service? {
    return services.find(predicate)
}

fun Peripheral.findCharacteristic(
    serviceUuid: UUID,
    characteristicUuid: UUID
): Characteristic? {
    return findService(serviceUuid)?.findCharacteristic(characteristicUuid)
}

fun Peripheral.findDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID,
    descriptorUuid: UUID
): Descriptor? {
    return findService(serviceUuid)?.findCharacteristic(characteristicUuid)?.findDescriptor(descriptorUuid)
}

operator fun Peripheral.get(serviceUuid: UUID): Service {
    return services.first { it.uuid == serviceUuid }
}

operator fun Peripheral.get(serviceUuid: UUID, characteristicUuid: UUID): Characteristic {
    return get(serviceUuid)[characteristicUuid]
}

operator fun Peripheral.get(serviceUuid: UUID, characteristicUuid: UUID, descriptorUuid: UUID): Descriptor {
    return get(serviceUuid, characteristicUuid)[descriptorUuid]
}