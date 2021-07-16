package com.nice.bluetooth

import android.bluetooth.BluetoothGattService
import com.nice.bluetooth.common.*
import java.util.*

internal data class AndroidGattService(
    override val serviceUuid: UUID,
    val bluetoothGattService: BluetoothGattService,
    val characteristics: List<AndroidCharacteristic>
) : Service

internal fun AndroidGattService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() }
)

internal fun BluetoothGattService.toAndroidGattService(): AndroidGattService {
    val serviceUuid = uuid
    val characteristics = characteristics
        .map { characteristic -> characteristic.toAndroidCharacteristic() }

    return AndroidGattService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this
    )
}

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<AndroidGattService>.findCharacteristic(
    characteristic: Characteristic
): AndroidCharacteristic =
    findCharacteristic(
        serviceUuid = characteristic.serviceUuid,
        characteristicUuid = characteristic.characteristicUuid
    )

/** @throws NoSuchElementException if service or characteristic is not found. */
private fun List<AndroidGattService>.findCharacteristic(
    serviceUuid: UUID,
    characteristicUuid: UUID
): AndroidCharacteristic =
    first(serviceUuid)
        .characteristics
        .first(characteristicUuid)

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
internal fun List<AndroidGattService>.findDescriptor(
    descriptor: DiscoveredDescriptor
): AndroidDescriptor =
    findDescriptor(
        serviceUuid = descriptor.serviceUuid,
        characteristicUuid = descriptor.characteristicUuid,
        descriptorUuid = descriptor.descriptorUuid
    )

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
private fun List<AndroidGattService>.findDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID,
    descriptorUuid: UUID
): AndroidDescriptor =
    findCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid
    ).descriptors.first(descriptorUuid)
