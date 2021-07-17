package com.nice.bluetooth.common

import java.util.*
import kotlin.NoSuchElementException

interface Service {
    val serviceUuid: UUID
}

data class DiscoveredService internal constructor(
    override val serviceUuid: UUID,
    val characteristics: List<DiscoveredCharacteristic>
) : Service, Iterable<DiscoveredCharacteristic> {

    override fun iterator(): Iterator<DiscoveredCharacteristic> {
        return characteristics.iterator()
    }

}

fun DiscoveredService.findCharacteristic(characteristicUuid: UUID): DiscoveredCharacteristic? {
    return characteristics.find { it.characteristicUuid == characteristicUuid }
}

fun DiscoveredService.findDescriptor(characteristicUuid: UUID, descriptorUuid: UUID): Descriptor? {
    return findCharacteristic(characteristicUuid)?.findDescriptor(descriptorUuid)
}

operator fun DiscoveredService.get(characteristicUuid: UUID): DiscoveredCharacteristic {
    return characteristics.first(characteristicUuid)
}

operator fun DiscoveredService.get(characteristicUuid: UUID, descriptorUuid: UUID): Descriptor {
    return get(characteristicUuid)[descriptorUuid]
}

internal fun <T : Service> List<T>.first(
    serviceUuid: UUID
): T = firstOrNull { it.serviceUuid == serviceUuid }
    ?: throw NoSuchElementException("Service $serviceUuid not found")
