@file:Suppress("unused")

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
    descriptor: String
): Descriptor = Descriptor.create(
    serviceUuid = UUID.fromString(service),
    characteristicUuid = UUID.fromString(characteristic),
    descriptorUuid = UUID.fromString(descriptor)
)

interface Descriptor {
    val serviceUuid: UUID
    val characteristicUuid: UUID
    val descriptorUuid: UUID

    companion object {
        fun create(
            serviceUuid: UUID,
            characteristicUuid: UUID,
            descriptorUuid: UUID
        ): Descriptor = LazyDescriptor(serviceUuid, characteristicUuid, descriptorUuid)
    }
}

private data class LazyDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID
) : Descriptor

data class DiscoveredDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID,
    val bluetoothGattDescriptor: BluetoothGattDescriptor
) : Descriptor {

    val value: ByteArray?
        get() = bluetoothGattDescriptor.value

    val permissions: Array<DescriptorPermission> = mutableListOf<DescriptorPermission>().apply {
        for (permission in DescriptorPermission.values()) {
            if (hasPermission(permission)) add(permission)
        }
    }.toTypedArray()

    fun hasPermission(permission: DescriptorPermission) = bluetoothGattDescriptor.permissions and permission.value != 0

    override fun toString(): String =
        "DiscoveredDescriptor(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, descriptorUuid=$descriptorUuid)"

}

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: UUID
): T = firstOrNull { it.descriptorUuid == descriptorUuid }
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")


internal fun BluetoothGattDescriptor.toDiscoveredDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID
) = DiscoveredDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = uuid,
    bluetoothGattDescriptor = this
)
