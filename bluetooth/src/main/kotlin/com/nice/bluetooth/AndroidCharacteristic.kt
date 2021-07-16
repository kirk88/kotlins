package com.nice.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import com.nice.bluetooth.common.Characteristic
import com.nice.bluetooth.common.DiscoveredCharacteristic
import com.nice.bluetooth.common.LazyCharacteristic
import java.util.*

internal data class AndroidCharacteristic(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    val descriptors: List<AndroidDescriptor>,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic
) : Characteristic

internal fun AndroidCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toLazyDescriptor() }
)

internal fun BluetoothGattCharacteristic.toAndroidCharacteristic(): AndroidCharacteristic {
    val platformDescriptors = descriptors.map { descriptor ->
        descriptor.toAndroidDescriptor(service.uuid, uuid)
    }

    return AndroidCharacteristic(
        serviceUuid = service.uuid,
        characteristicUuid = uuid,
        descriptors = platformDescriptors,
        bluetoothGattCharacteristic = this
    )
}

internal fun BluetoothGattCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid,
    characteristicUuid = uuid
)
