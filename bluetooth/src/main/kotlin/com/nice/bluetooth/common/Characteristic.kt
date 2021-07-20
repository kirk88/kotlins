@file:Suppress("unused")

package com.nice.bluetooth.common

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import java.util.*

enum class CharacteristicProperty(internal val value: Int) {
    Broadcast(PROPERTY_BROADCAST),
    Read(PROPERTY_READ),
    Write(PROPERTY_WRITE),
    WriteWithoutResponse(PROPERTY_WRITE_NO_RESPONSE),
    Notify(PROPERTY_NOTIFY),
    Indicate(PROPERTY_INDICATE),
    SignedWrite(PROPERTY_SIGNED_WRITE),
    ExtendedProps(PROPERTY_EXTENDED_PROPS)
}

enum class CharacteristicPermission(internal val value: Int) {
    Read(PERMISSION_READ),
    ReadEncrypted(PERMISSION_READ_ENCRYPTED),
    ReadEncryptedMitm(PERMISSION_READ_ENCRYPTED_MITM),
    Write(PERMISSION_WRITE),
    WriteEncrypted(PERMISSION_WRITE_ENCRYPTED),
    WriteEncryptedMitm(PERMISSION_WRITE_ENCRYPTED_MITM),
    WriteSigned(PERMISSION_WRITE_SIGNED),
    WriteSignedMitm(PERMISSION_WRITE_SIGNED_MITM)
}

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
        ): Characteristic = LazyCharacteristic(serviceUuid, characteristicUuid)
    }
}

private data class LazyCharacteristic(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID
) : Characteristic

data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<DiscoveredDescriptor>,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic, Iterable<DiscoveredDescriptor> {

    val id: Int
        get() = bluetoothGattCharacteristic.instanceId

    val value: ByteArray?
        get() = bluetoothGattCharacteristic.value

    val properties: Array<CharacteristicProperty> = mutableListOf<CharacteristicProperty>().apply {
        for (property in CharacteristicProperty.values()) {
            if (hasProperty(property)) add(property)
        }
    }.toTypedArray()

    val permissions: Array<CharacteristicPermission> = mutableListOf<CharacteristicPermission>().apply {
        for (permission in CharacteristicPermission.values()) {
            if (hasPermission(permission)) add(permission)
        }
    }.toTypedArray()

    fun hasProperty(property: CharacteristicProperty) = bluetoothGattCharacteristic.properties and property.value != 0

    fun hasPermission(permission: CharacteristicPermission) = bluetoothGattCharacteristic.permissions and permission.value != 0

    override fun iterator(): Iterator<DiscoveredDescriptor> {
        return descriptors.iterator()
    }

    override fun toString(): String =
        "DiscoveredCharacteristic(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptors=$descriptors)"

}

fun DiscoveredCharacteristic.findDescriptor(descriptorUuid: UUID): DiscoveredDescriptor? {
    return descriptors.find { it.descriptorUuid == descriptorUuid }
}

operator fun DiscoveredCharacteristic.get(descriptorUuid: UUID): DiscoveredDescriptor {
    return descriptors.first(descriptorUuid)
}

internal fun <T : Characteristic> List<T>.first(
    characteristicUuid: UUID
): T = firstOrNull { it.characteristicUuid == characteristicUuid }
    ?: throw NoSuchElementException("Characteristic $characteristicUuid not found")

internal fun BluetoothGattCharacteristic.toDiscoveredCharacteristic(): DiscoveredCharacteristic {
    val androidDescriptors = descriptors.map { descriptor ->
        descriptor.toDiscoveredDescriptor(service.uuid, uuid)
    }

    return DiscoveredCharacteristic(
        serviceUuid = service.uuid,
        characteristicUuid = uuid,
        descriptors = androidDescriptors,
        bluetoothGattCharacteristic = this
    )
}

internal fun BluetoothGattCharacteristic.toCharacteristic() = Characteristic.create(
    serviceUuid = service.uuid,
    characteristicUuid = uuid
)
