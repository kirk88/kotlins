package com.nice.bluetooth

import android.bluetooth.BluetoothGattDescriptor
import com.nice.bluetooth.common.DiscoveredDescriptor
import com.nice.bluetooth.common.LazyDescriptor
import java.util.*

internal data class AndroidDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID,
    val bluetoothGattDescriptor: BluetoothGattDescriptor
) : DiscoveredDescriptor

internal fun AndroidDescriptor.toLazyDescriptor() = LazyDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = descriptorUuid
)

internal fun BluetoothGattDescriptor.toAndroidDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID
) = AndroidDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = uuid,
    bluetoothGattDescriptor = this
)
