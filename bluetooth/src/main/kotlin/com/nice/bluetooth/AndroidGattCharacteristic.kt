package com.nice.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import com.nice.bluetooth.common.Characteristic
import com.nice.bluetooth.common.DiscoveredCharacteristic
import java.util.*

internal data class AndroidGattCharacteristic(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<AndroidGattDescriptor>,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic

internal fun AndroidGattCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toDescriptor() }
)

internal fun BluetoothGattCharacteristic.toAndroidGattCharacteristic(): AndroidGattCharacteristic {
    val androidDescriptors = descriptors.map { descriptor ->
        descriptor.toAndroidGattDescriptor(service.uuid, uuid)
    }

    return AndroidGattCharacteristic(
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