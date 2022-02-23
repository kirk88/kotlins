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

fun Characteristic(
    uuid: String,
    serviceUuid: String,
    properties: Array<CharacteristicProperty> = emptyArray(),
    permissions: Array<CharacteristicPermission> = emptyArray(),
    descriptors: List<Descriptor> = emptyList()
): Characteristic = LazyCharacteristic(
    uuid = UUID.fromString(uuid),
    serviceUuid = UUID.fromString(serviceUuid),
    properties = properties,
    permissions = permissions,
    descriptors = descriptors
)

fun Characteristic(
    uuid: UUID,
    serviceUuid: UUID,
    properties: Array<CharacteristicProperty> = emptyArray(),
    permissions: Array<CharacteristicPermission> = emptyArray(),
    descriptors: List<Descriptor> = emptyList()
): Characteristic = LazyCharacteristic(
    uuid = uuid,
    serviceUuid = serviceUuid,
    properties = properties,
    permissions = permissions,
    descriptors = descriptors
)

interface Characteristic {
    val uuid: UUID
    val serviceUuid: UUID
    val properties: Array<CharacteristicProperty>
    val permissions: Array<CharacteristicPermission>
    val descriptors: List<Descriptor>

    var value: ByteArray?
    var writeType: WriteType

    fun hasProperty(property: CharacteristicProperty): Boolean {
        return properties.contains(property)
    }

    fun hasPermission(permission: CharacteristicPermission): Boolean {
        return permissions.contains(permission)
    }
}

private data class LazyCharacteristic(
    override val uuid: UUID,
    override val serviceUuid: UUID,
    override val properties: Array<CharacteristicProperty>,
    override val permissions: Array<CharacteristicPermission>,
    override val descriptors: List<Descriptor>
) : Characteristic {

    override var value: ByteArray? = null
    override var writeType: WriteType = WriteType.WithResponse

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyCharacteristic

        if (serviceUuid != other.serviceUuid) return false
        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + uuid.hashCode()
        return result
    }
}

private data class DiscoveredCharacteristic(
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic {

    override val uuid: UUID = bluetoothGattCharacteristic.uuid

    override val serviceUuid: UUID = bluetoothGattCharacteristic.service.uuid

    override val properties: Array<CharacteristicProperty> =
        CharacteristicProperty.values().filter {
            bluetoothGattCharacteristic.properties and it.value != 0
        }.toTypedArray()

    override val permissions: Array<CharacteristicPermission> =
        CharacteristicPermission.values().filter {
            bluetoothGattCharacteristic.permissions and it.value != 0
        }.toTypedArray()

    override val descriptors: List<Descriptor> = bluetoothGattCharacteristic.descriptors.map { it.toDescriptor() }


    override var value: ByteArray?
        get() = bluetoothGattCharacteristic.value
        set(value) {
            bluetoothGattCharacteristic.value = value
        }

    override var writeType: WriteType
        get() = if (bluetoothGattCharacteristic.writeType == WRITE_TYPE_DEFAULT) WriteType.WithResponse else WriteType.WithoutResponse
        set(value) {
            bluetoothGattCharacteristic.writeType = value.intValue
        }

    override fun toString(): String =
        "DiscoveredCharacteristic(serviceUuid=$serviceUuid, characteristicUuid=$uuid, descriptors=$descriptors)"

}

fun Characteristic.findDescriptor(descriptorUuid: UUID): Descriptor? {
    return descriptors.find { it.uuid == descriptorUuid }
}

fun Characteristic.findDescriptor(predicate: (Descriptor) -> Boolean): Descriptor? {
    return descriptors.find(predicate)
}

operator fun Characteristic.get(descriptorUuid: UUID): Descriptor {
    return descriptors.first { it.uuid == descriptorUuid }
}

internal fun Characteristic.toBluetoothGattCharacteristic(): BluetoothGattCharacteristic {
    if (this is DiscoveredCharacteristic) {
        return bluetoothGattCharacteristic
    }
    var propertiesFlag = 0
    for (property in properties) {
        propertiesFlag = propertiesFlag or property.value
    }

    var permissionsFlag = 0
    for (permission in permissions) {
        permissionsFlag = permissionsFlag or permission.value
    }

    val characteristic = BluetoothGattCharacteristic(uuid, propertiesFlag, permissionsFlag)

    for (descriptor in descriptors) {
        characteristic.addDescriptor(descriptor.toBluetoothGattDescriptor())
    }
    return characteristic
}

internal fun BluetoothGattCharacteristic.toCharacteristic(): Characteristic = DiscoveredCharacteristic(this)