package com.nice.bluetooth.common

import java.util.*

fun characteristicOf(
    service: String,
    characteristic: String
): Characteristic = LazyCharacteristic(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic)
)

interface Characteristic {
    val serviceUuid: UUID
    val characteristicUuid: UUID
}

data class LazyCharacteristic internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID
) : Characteristic

data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<DiscoveredDescriptor>
) : Characteristic, Iterable<DiscoveredDescriptor>{

    override fun iterator(): Iterator<DiscoveredDescriptor> {
        return descriptors.iterator()
    }

}

fun DiscoveredCharacteristic.findDescriptor(descriptorUuid: UUID): DiscoveredDescriptor? {
    return descriptors.find { it.descriptorUuid == descriptorUuid }
}

internal fun <T : Characteristic> List<T>.first(
    characteristicUuid: UUID
): T = firstOrNull { it.characteristicUuid == characteristicUuid }
    ?: throw NoSuchElementException("Characteristic $characteristicUuid not found")
