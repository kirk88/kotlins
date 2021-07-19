package com.nice.bluetooth.common

import java.util.*

fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String
): Descriptor = Descriptor.create(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    descriptorUuid = UUID.fromString(descriptor)
)

interface Descriptor {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val descriptorUuid: UUID


    companion object {

        fun create(
            serviceUuid: UUID,
            characteristicUuid: UUID,
            descriptorUuid: UUID
        ): Descriptor = DescriptorImpl(serviceUuid, characteristicUuid, descriptorUuid)

    }
}

private data class DescriptorImpl(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID
) : Descriptor {
    override fun toString(): String {
        return "Descriptor(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptorUuid=$descriptorUuid)"
    }
}

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: UUID
): T = firstOrNull { it.descriptorUuid == descriptorUuid }
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")
