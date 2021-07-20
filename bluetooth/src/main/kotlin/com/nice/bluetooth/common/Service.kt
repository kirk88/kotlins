@file:Suppress("unused")

package com.nice.bluetooth.common

import android.bluetooth.BluetoothGattService
import java.util.*
import kotlin.NoSuchElementException

interface Service {
    val serviceUuid: UUID
}

data class DiscoveredService internal constructor(
    override val serviceUuid: UUID,
    val characteristics: List<DiscoveredCharacteristic>,
    val bluetoothGattService: BluetoothGattService
) : Service, Iterable<DiscoveredCharacteristic> {

    val id: Int
        get() = bluetoothGattService.instanceId

    val type: Int
        get() = bluetoothGattService.type

    override fun iterator(): Iterator<DiscoveredCharacteristic> {
        return characteristics.iterator()
    }

    override fun toString(): String =
        "DiscoveredService(serviceUuid=$serviceUuid, characteristics=$characteristics)"

}

fun DiscoveredService.findCharacteristic(characteristicUuid: UUID): DiscoveredCharacteristic? {
    return characteristics.find { it.characteristicUuid == characteristicUuid }
}

fun DiscoveredService.findDescriptor(characteristicUuid: UUID, descriptorUuid: UUID): DiscoveredDescriptor? {
    return findCharacteristic(characteristicUuid)?.findDescriptor(descriptorUuid)
}

operator fun DiscoveredService.get(characteristicUuid: UUID): DiscoveredCharacteristic {
    return characteristics.first(characteristicUuid)
}

operator fun DiscoveredService.get(characteristicUuid: UUID, descriptorUuid: UUID): DiscoveredDescriptor {
    return get(characteristicUuid)[descriptorUuid]
}

internal fun <T : Service> List<T>.first(
    serviceUuid: UUID
): T = firstOrNull { it.serviceUuid == serviceUuid }
    ?: throw NoSuchElementException("Service $serviceUuid not found")

internal fun BluetoothGattService.toDiscoveredService(): DiscoveredService {
    val serviceUuid = uuid
    val characteristics = characteristics
        .map { characteristic -> characteristic.toDiscoveredCharacteristic() }

    return DiscoveredService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this
    )
}

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<DiscoveredService>.getCharacteristic(
    characteristic: Characteristic
): DiscoveredCharacteristic =
    getCharacteristic(
        serviceUuid = characteristic.serviceUuid,
        characteristicUuid = characteristic.characteristicUuid
    )

/** @throws NoSuchElementException if service or characteristic is not found. */
private fun List<DiscoveredService>.getCharacteristic(
    serviceUuid: UUID,
    characteristicUuid: UUID
): DiscoveredCharacteristic =
    first(serviceUuid)
        .characteristics
        .first(characteristicUuid)

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
internal fun List<DiscoveredService>.getDescriptor(
    descriptor: Descriptor
): DiscoveredDescriptor =
    getDescriptor(
        serviceUuid = descriptor.serviceUuid,
        characteristicUuid = descriptor.characteristicUuid,
        descriptorUuid = descriptor.descriptorUuid
    )

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
private fun List<DiscoveredService>.getDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID,
    descriptorUuid: UUID
): DiscoveredDescriptor =
    getCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid
    ).descriptors.first(descriptorUuid)
