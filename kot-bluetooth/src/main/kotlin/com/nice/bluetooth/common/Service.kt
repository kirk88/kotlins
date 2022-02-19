@file:Suppress("UNUSED")

package com.nice.bluetooth.common

import android.bluetooth.BluetoothGattService
import java.util.*

interface Service {
    val uuid: UUID
    val serviceType: Int
    val characteristics: List<Characteristic>
}

fun Service(
    uuid: String,
    serviceType: Int = -1,
    characteristics: List<Characteristic> = emptyList()
): Service = LazyService(
    uuid = UUID.fromString(uuid),
    serviceType = serviceType,
    characteristics = characteristics
)

fun Service(
    uuid: UUID,
    serviceType: Int = -1,
    characteristics: List<Characteristic> = emptyList()
): Service = LazyService(
    uuid = uuid,
    serviceType = serviceType,
    characteristics = characteristics
)

private data class LazyService(
    override val uuid: UUID,
    override val serviceType: Int,
    override val characteristics: List<Characteristic>
) : Service


private data class DiscoveredService(
    val bluetoothGattService: BluetoothGattService
) : Service {

    override val uuid: UUID = bluetoothGattService.uuid

    override val serviceType: Int = bluetoothGattService.type

    override val characteristics: List<Characteristic> =
        bluetoothGattService.characteristics.map { it.toCharacteristic() }

    override fun toString(): String = "DiscoveredService(serviceUuid=$uuid, characteristics=$characteristics)"

}

fun Service.findCharacteristic(characteristicUuid: UUID): Characteristic? {
    return characteristics.find { it.uuid == characteristicUuid }
}

fun Service.findCharacteristic(predicate: (Characteristic) -> Boolean): Characteristic? {
    return characteristics.find(predicate)
}

fun Service.findDescriptor(characteristicUuid: UUID, descriptorUuid: UUID): Descriptor? {
    return findCharacteristic(characteristicUuid)?.findDescriptor(descriptorUuid)
}

operator fun Service.get(characteristicUuid: UUID): Characteristic {
    return characteristics.first { it.uuid == characteristicUuid }
}

operator fun Service.get(characteristicUuid: UUID, descriptorUuid: UUID): Descriptor {
    return get(characteristicUuid)[descriptorUuid]
}

internal fun Service.toBluetoothGattService(): BluetoothGattService {
    if (this is DiscoveredService) {
        return bluetoothGattService
    }
    val service = BluetoothGattService(uuid, serviceType)
    for (characteristic in characteristics) {
        service.addCharacteristic(characteristic.toBluetoothGattCharacteristic())
    }
    return service
}

internal fun BluetoothGattService.toService(): Service = DiscoveredService(this)