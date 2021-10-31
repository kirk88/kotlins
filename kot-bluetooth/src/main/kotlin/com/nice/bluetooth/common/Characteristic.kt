@file:Suppress("UNUSED")

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
): Characteristic = Characteristic(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic)
)

interface Characteristic {
    val serviceUuid: UUID
    val characteristicUuid: UUID

    companion object {
        operator fun invoke(
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
    internal val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic {

    val properties: Array<CharacteristicProperty> =
        CharacteristicProperty.values().filter { hasProperty(it) }.toTypedArray()

    val permissions: Array<CharacteristicPermission> =
        CharacteristicPermission.values().filter { hasPermission(it) }.toTypedArray()

    fun hasProperty(property: CharacteristicProperty): Boolean {
        return bluetoothGattCharacteristic.properties and property.value != 0
    }

    fun hasPermission(permission: CharacteristicPermission): Boolean {
        bluetoothGattCharacteristic.permissions
        return bluetoothGattCharacteristic.permissions and permission.value != 0
    }

    override fun toString(): String =
        "DiscoveredCharacteristic(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptors=$descriptors)"

}

fun DiscoveredCharacteristic.findDescriptor(descriptorUuid: UUID): DiscoveredDescriptor? {
    return descriptors.find { it.descriptorUuid == descriptorUuid }
}

fun DiscoveredCharacteristic.findDescriptor(predicate: (DiscoveredDescriptor) -> Boolean): DiscoveredDescriptor? {
    return descriptors.find(predicate)
}

operator fun DiscoveredCharacteristic.get(descriptorUuid: UUID): DiscoveredDescriptor {
    return descriptors.first(descriptorUuid)
}

internal fun <T : Characteristic> List<T>.first(
    characteristicUuid: UUID
): T = firstOrNull { it.characteristicUuid == characteristicUuid }
    ?: throw NoSuchElementException("Characteristic $characteristicUuid not found")

internal fun BluetoothGattCharacteristic.toCharacteristic() = Characteristic(
    serviceUuid = service.uuid,
    characteristicUuid = uuid
)
