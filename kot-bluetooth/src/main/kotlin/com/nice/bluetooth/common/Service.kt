@file:Suppress("UNUSED")

package com.nice.bluetooth.common

import android.bluetooth.BluetoothGattService
import java.util.*

interface Service {
    val serviceUuid: UUID
}

data class DiscoveredService internal constructor(
    override val serviceUuid: UUID,
    val characteristics: List<DiscoveredCharacteristic>,
    internal val bluetoothGattService: BluetoothGattService
) : Service {

    override fun toString(): String =
        "DiscoveredService(serviceUuid=$serviceUuid, characteristics=$characteristics)"

}

fun DiscoveredService.findCharacteristic(characteristicUuid: UUID): DiscoveredCharacteristic? {
    return characteristics.find { it.characteristicUuid == characteristicUuid }
}

fun DiscoveredService.findCharacteristic(predicate: (DiscoveredCharacteristic) -> Boolean): DiscoveredCharacteristic? {
    return characteristics.find(predicate)
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

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<DiscoveredService>.getCharacteristic(
    characteristic: Characteristic
): DiscoveredCharacteristic = first(characteristic.serviceUuid)
    .characteristics
    .first(characteristic.characteristicUuid)

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
internal fun List<DiscoveredService>.getDescriptor(
    descriptor: Descriptor
): DiscoveredDescriptor = first(descriptor.serviceUuid)
    .characteristics
    .first(descriptor.characteristicUuid)
    .descriptors
    .first(descriptor.descriptorUuid)

internal fun BluetoothGattService.toDiscoveredService(): DiscoveredService {
    val serviceUuid = uuid
    val characteristics = characteristics.map { characteristic ->
        val descriptors = characteristic.descriptors.map { descriptor ->
            DiscoveredDescriptor(
                serviceUuid = serviceUuid,
                characteristicUuid = characteristic.uuid,
                descriptorUuid = descriptor.uuid,
                bluetoothGattDescriptor = descriptor
            )
        }

        DiscoveredCharacteristic(
            serviceUuid = serviceUuid,
            characteristicUuid = characteristic.uuid,
            descriptors = descriptors,
            bluetoothGattCharacteristic = characteristic
        )
    }
    return DiscoveredService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this
    )
}
