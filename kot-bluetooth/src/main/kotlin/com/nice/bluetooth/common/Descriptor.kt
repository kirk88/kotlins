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

fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String,
    permissions: Array<DescriptorPermission> = emptyArray()
): Descriptor = Descriptor(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    descriptorUuid = UUID.fromString(descriptor),
    permissions = permissions
)

interface Descriptor {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val descriptorUuid: UUID
    val permissions: Array<DescriptorPermission>

    fun hasPermission(permission: DescriptorPermission): Boolean {
        return permissions.contains(permission)
    }

    companion object {
        operator fun invoke(
            serviceUuid: UUID,
            characteristicUuid: UUID,
            descriptorUuid: UUID,
            permissions: Array<DescriptorPermission> = emptyArray()
        ): Descriptor = LazyDescriptor(serviceUuid, characteristicUuid, descriptorUuid, permissions)
    }
}

private data class LazyDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID,
    override val permissions: Array<DescriptorPermission>
) : Descriptor {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LazyDescriptor

        if (serviceUuid != other.serviceUuid) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (descriptorUuid != other.descriptorUuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + descriptorUuid.hashCode()
        return result
    }
}

data class DiscoveredDescriptor internal constructor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID,
    internal val bluetoothGattDescriptor: BluetoothGattDescriptor
) : Descriptor {

    override val permissions: Array<DescriptorPermission> = bluetoothGattDescriptor.permissionValues

    override fun toString(): String =
        "DiscoveredDescriptor(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptorUuid=$descriptorUuid)"

}

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: UUID
): T = firstOrNull { it.descriptorUuid == descriptorUuid }
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")

private val BluetoothGattDescriptor.permissionValues: Array<DescriptorPermission>
    get() = DescriptorPermission.values().filter { permissions and it.value != 0 }.toTypedArray()