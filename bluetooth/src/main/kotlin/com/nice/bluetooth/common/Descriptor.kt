package com.nice.bluetooth.common

import java.util.*
import kotlin.NoSuchElementException

fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String
): Descriptor = LazyDescriptor(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    descriptorUuid = UUID.fromString(descriptor)
)

interface Descriptor {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val descriptorUuid: UUID
}

data class LazyDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID
) : Descriptor

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: UUID
): T = firstOrNull(descriptorUuid)
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")

internal fun <T : Descriptor> List<T>.firstOrNull(
    descriptorUuid: UUID
): T? = firstOrNull { it.descriptorUuid == descriptorUuid }
