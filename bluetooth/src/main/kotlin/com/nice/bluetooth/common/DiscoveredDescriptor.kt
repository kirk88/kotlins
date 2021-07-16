package com.nice.bluetooth.common

import java.util.*
import kotlin.NoSuchElementException

fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String
): DiscoveredDescriptor = LazyDescriptor(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    descriptorUuid = UUID.fromString(descriptor)
)

data class LazyDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID
) : DiscoveredDescriptor

interface DiscoveredDescriptor {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val descriptorUuid: UUID
}

internal fun <T : DiscoveredDescriptor> List<T>.first(
    descriptorUuid: UUID
): T = firstOrNull(descriptorUuid)
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")

internal fun <T : DiscoveredDescriptor> List<T>.firstOrNull(
    descriptorUuid: UUID
): T? = firstOrNull { it.descriptorUuid == descriptorUuid }
