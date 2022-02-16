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
    characteristic: String,
    properties: Array<CharacteristicProperty> = emptyArray(),
    permissions: Array<CharacteristicPermission> = emptyArray()
): Characteristic = Characteristic(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    properties = properties,
    permissions = permissions
)

interface Characteristic {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val properties: Array<CharacteristicProperty>
    val permissions: Array<CharacteristicPermission>

    fun hasProperty(property: CharacteristicProperty): Boolean {
        return properties.contains(property)
    }

    fun hasPermission(permission: CharacteristicPermission): Boolean {
        return permissions.contains(permission)
    }

    companion object {
        operator fun invoke(
            serviceUuid: UUID,
            characteristicUuid: UUID,
            properties: Array<CharacteristicProperty> = emptyArray(),
            permissions: Array<CharacteristicPermission> = emptyArray()
        ): Characteristic = LazyCharacteristic(serviceUuid, characteristicUuid, properties, permissions)
    }
}

private data class LazyCharacteristic(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val properties: Array<CharacteristicProperty>,
    override val permissions: Array<CharacteristicPermission>
) : Characteristic {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyCharacteristic

        if (serviceUuid != other.serviceUuid) return false
        if (characteristicUuid != other.characteristicUuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + characteristicUuid.hashCode()
        return result
    }
}

data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<DiscoveredDescriptor>,
    internal val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic {

    override val properties: Array<CharacteristicProperty> = bluetoothGattCharacteristic.propertyValues

    override val permissions: Array<CharacteristicPermission> = bluetoothGattCharacteristic.permissionValues


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
    characteristicUuid = uuid,
    properties = propertyValues,
    permissions = permissionValues
)

private val BluetoothGattCharacteristic.propertyValues: Array<CharacteristicProperty>
    get() = CharacteristicProperty.values().filter { properties and it.value != 0 }.toTypedArray()

private val BluetoothGattCharacteristic.permissionValues: Array<CharacteristicPermission>
    get() = CharacteristicPermission.values().filter { permissions and it.value != 0 }.toTypedArray()