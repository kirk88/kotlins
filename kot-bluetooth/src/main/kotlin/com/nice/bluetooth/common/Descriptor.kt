@file:Suppress("UNUSED")

package com.nice.bluetooth.common

import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.*
import java.util.*

enum class DescriptorPermission(internal val value: Int) {
    Read(PERMISSION_READ),
    ReadEncrypted(PERMISSION_READ_ENCRYPTED),
    ReadEncryptedMitm(PERMISSION_READ_ENCRYPTED_MITM),
    Write(PERMISSION_WRITE),
    WriteEncrypted(PERMISSION_WRITE_ENCRYPTED),
    WriteEncryptedMitm(PERMISSION_WRITE_ENCRYPTED_MITM),
    WriteSigned(PERMISSION_WRITE_SIGNED),
    WriteSignedMitm(PERMISSION_WRITE_SIGNED_MITM)
}

fun Descriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID,
    descriptorUuid: UUID,
    permissions: Array<DescriptorPermission> = emptyArray()
): Descriptor = LazyDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    uuid = descriptorUuid,
    permissions = permissions
)

fun Descriptor(
    serviceUuid: String,
    characteristicUuid: String,
    descriptorUuid: String,
    permissions: Array<DescriptorPermission> = emptyArray()
): Descriptor = LazyDescriptor(
    serviceUuid = UUID.fromString(serviceUuid),
    characteristicUuid = UUID.fromString(characteristicUuid),
    uuid = UUID.fromString(descriptorUuid),
    permissions = permissions
)

interface Descriptor {
    val uuid: UUID
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val permissions: Array<DescriptorPermission>

    fun hasPermission(permission: DescriptorPermission): Boolean {
        return permissions.contains(permission)
    }
}

private data class LazyDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val uuid: UUID,
    override val permissions: Array<DescriptorPermission>
) : Descriptor {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyDescriptor

        if (serviceUuid != other.serviceUuid) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + uuid.hashCode()
        return result
    }
}

private data class DiscoveredDescriptor(
    val bluetoothGattDescriptor: BluetoothGattDescriptor
) : Descriptor {

    override val uuid: UUID = bluetoothGattDescriptor.uuid

    override val serviceUuid: UUID = bluetoothGattDescriptor.characteristic.service.uuid

    override val characteristicUuid: UUID = bluetoothGattDescriptor.characteristic.uuid

    override val permissions: Array<DescriptorPermission> =
        DescriptorPermission.values().filter { bluetoothGattDescriptor.permissions and it.value != 0 }.toTypedArray()

    override fun toString(): String =
        "DiscoveredDescriptor(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptorUuid=$uuid)"

}

internal fun Descriptor.toBluetoothGattDescriptor(): BluetoothGattDescriptor {
    if (this is DiscoveredDescriptor) {
        return bluetoothGattDescriptor
    }
    var permissionsFlag = 0
    for (permission in permissions) {
        permissionsFlag = permissionsFlag or permission.value
    }
    return BluetoothGattDescriptor(uuid, permissionsFlag)
}

internal fun BluetoothGattDescriptor.toDescriptor(): Descriptor = DiscoveredDescriptor(this)