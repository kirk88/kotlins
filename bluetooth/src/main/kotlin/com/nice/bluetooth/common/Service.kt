package com.nice.bluetooth.common

import java.util.*
import kotlin.NoSuchElementException

interface Service {
    val serviceUuid: UUID
}

data class DiscoveredService internal constructor(
    override val serviceUuid: UUID,
    val characteristics: List<DiscoveredCharacteristic>
) : Service

/** @throws IOException if service is not found. */
internal fun <T : Service> List<T>.first(
    serviceUuid: UUID
): T = firstOrNull { it.serviceUuid == serviceUuid }
    ?: throw NoSuchElementException("Service $serviceUuid not found")
