package com.nice.bluetooth

import android.bluetooth.BluetoothGattService
import com.nice.bluetooth.common.*
import java.util.*

internal data class AndroidGattService(
    override val serviceUuid: UUID,
    val bluetoothGattService: BluetoothGattService,
    val characteristics: List<AndroidGattCharacteristic>
) : Service

internal fun AndroidGattService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() }
)

internal fun BluetoothGattService.toAndroidGattService(): AndroidGattService {
    val serviceUuid = uuid
    val characteristics = characteristics
        .map { characteristic -> characteristic.toAndroidGattCharacteristic() }

    return AndroidGattService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this
    )
}

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<AndroidGattService>.getCharacteristic(
    characteristic: Characteristic
): AndroidGattCharacteristic =
    getCharacteristic(
        serviceUuid = characteristic.serviceUuid,
        characteristicUuid = characteristic.characteristicUuid
    )

/** @throws NoSuchElementException if service or characteristic is not found. */
private fun List<AndroidGattService>.getCharacteristic(
    serviceUuid: UUID,
    characteristicUuid: UUID
): AndroidGattCharacteristic =
    first(serviceUuid)
        .characteristics
        .first(characteristicUuid)

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
internal fun List<AndroidGattService>.getDescriptor(
    descriptor: Descriptor
): AndroidGattDescriptor =
    getDescriptor(
        serviceUuid = descriptor.serviceUuid,
        characteristicUuid = descriptor.characteristicUuid,
        descriptorUuid = descriptor.descriptorUuid
    )

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
private fun List<AndroidGattService>.getDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID,
    descriptorUuid: UUID
): AndroidGattDescriptor =
    getCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid
    ).descriptors.first(descriptorUuid)
