package com.nice.bluetooth

import android.util.Log
import com.nice.bluetooth.common.Characteristic
import com.nice.bluetooth.common.NotReadyException
import com.nice.bluetooth.common.OnSubscriptionAction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

internal sealed class PeripheralEvent {

    abstract val characteristic: Characteristic

    data class CharacteristicChange(
        override val characteristic: Characteristic,
        val data: ByteArray
    ) : PeripheralEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharacteristicChange

            if (characteristic != other.characteristic) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = characteristic.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    data class Error(
        override val characteristic: Characteristic,
        val cause: Throwable
    ) : PeripheralEvent()

}

/**
 * Manages observations for the specified [connection].
 *
 * The [characteristicChanges] property is expected to be fed with all characteristic changes associated with the
 * [connection]. The changes are then fanned out to individual [Flow]s created via [acquire] (associated with a specific
 * characteristic).
 *
 * For example, if you have a sequence of characteristic changes represented by characteristic A, B and C with their
 * corresponding change uniquely identified by a change number postfix (in other words: characteristic A emitting 3
 * different changes would be represented as A1, A2 and A3):
 *
 * ```
 *                                                       .--- acquire(A) --> A1, A2, A3
 *                             .----------------------. /
 *  A1, B1, C1, A2, A3, B2 --> | characteristicChange | ----- acquire(B) --> B1, B2
 *                             '----------------------' \
 *                                                       '--- acquire(C) --> C1
 * ```
 *
 * @param connection to perform notification actions against to enable/disable the observations.
 */
internal class PeripheralObservers(
    private val connection: PeripheralConnection
) {

    private val characteristicChanges = MutableSharedFlow<PeripheralEvent>()
    private val observations = Observations()

    suspend fun send(event: PeripheralEvent) {
        characteristicChanges.emit(event)
    }

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = characteristicChanges
        .onSubscription {
            connection.suspendUntilReady()
            if (observations.add(characteristic, onSubscription) == 1) {
                connection.startObservation(characteristic)
            }
            onSubscription()
        }
        .filter {
            it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                    it.characteristic.serviceUuid == characteristic.serviceUuid
        }
        .map {
            when (it) {
                is PeripheralEvent.CharacteristicChange -> it.data
                is PeripheralEvent.Error -> throw it.cause
            }
        }
        .onCompletion {
            if (observations.remove(characteristic, onSubscription) == 0) {
                try {
                    connection.stopObservation(characteristic)
                } catch (e: NotReadyException) {
                    // Silently ignore as it is assumed that failure is due to connection drop, in which case Android
                    // will clear the notifications.
                    Log.d(TAG, "Stop notification failure ignored.")
                }
            }
        }

    suspend fun rewire() {
        observations.forEach { characteristic, onSubscriptionActions ->
            try {
                connection.startObservation(characteristic)
                onSubscriptionActions.forEach { it() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (t: Throwable) {
                characteristicChanges.emit(PeripheralEvent.Error(characteristic, t))
            }
        }
    }
}

private class Observations {

    private val lock = Mutex()
    private val observations = mutableMapOf<Characteristic, MutableList<OnSubscriptionAction>>()

    suspend inline fun forEach(
        action: (Characteristic, List<OnSubscriptionAction>) -> Unit
    ) = lock.withLock {
        observations.forEach { (characteristic, onSubscriptionActions) ->
            action(characteristic, onSubscriptionActions)
        }
    }

    suspend fun add(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Int = lock.withLock {
        val actions = observations[characteristic]
        if (actions == null) {
            val newActions = mutableListOf(onSubscription)
            observations[characteristic] = newActions
            1
        } else {
            actions += onSubscription
            actions.count()
        }
    }

    suspend fun remove(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Int = lock.withLock {
        val actions = observations[characteristic]
        when {
            actions == null -> -1 // No previous observation existed for characteristic.
            actions.count() == 1 -> {
                observations -= characteristic
                0
            }
            else -> {
                actions -= onSubscription
                actions.count()
            }
        }
    }

}
