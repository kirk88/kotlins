package com.nice.bluetooth

import android.bluetooth.BluetoothGattDescriptor
import com.nice.bluetooth.common.Descriptor
import java.util.*

internal data class AndroidGattDescriptor(
    override val serviceUuid: UUID,
    override val characteristicUuid: UUID,
    override val descriptorUuid: UUID,
    val bluetoothGattDescriptor: BluetoothGattDescriptor
) : Descriptor

internal fun AndroidGattDescriptor.toDescriptor() = Descriptor.create(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = descriptorUuid
)

internal fun BluetoothGattDescriptor.toAndroidGattDescriptor(
    serviceUuid: UUID,
    characteristicUuid: UUID
) = AndroidGattDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = uuid,
    bluetoothGattDescriptor = this
)
