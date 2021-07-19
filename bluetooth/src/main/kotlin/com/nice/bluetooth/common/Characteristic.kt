package com.nice.bluetooth.common

import java.util.*

fun characteristicOf(
    service: String,
    characteristic: String
): Characteristic = Characteristic.create(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic)
)

interface Characteristic {
    val serviceUuid: UUID
    val characteristicUuid: UUID

    companion object {

        fun create(
            serviceUuid: UUID,
            characteristicUuid: UUID
        ): Characteristic = CharacteristicImpl(serviceUuid, characteristicUuid)

    }

}

private data class CharacteristicImpl(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID
) : Characteristic {
    override fun toString(): String {
        return "Characteristic(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid)"
    }
}

data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<Descriptor>
) : Characteristic, Iterable<Descriptor> {

    override fun iterator(): Iterator<Descriptor> {
        return descriptors.iterator()
    }

}

fun DiscoveredCharacteristic.findDescriptor(descriptorUuid: UUID): Descriptor? {
    return descriptors.find { it.descriptorUuid == descriptorUuid }
}

operator fun DiscoveredCharacteristic.get(descriptorUuid: UUID): Descriptor {
    return descriptors.first(descriptorUuid)
}

internal fun <T : Characteristic> List<T>.first(
    characteristicUuid: UUID
): T = firstOrNull { it.characteristicUuid == characteristicUuid }
    ?: throw NoSuchElementException("Characteristic $characteristicUuid not found")
