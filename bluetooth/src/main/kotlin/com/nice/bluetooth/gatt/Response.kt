package com.nice.bluetooth.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.*
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.nice.bluetooth.common.Phy
import com.nice.bluetooth.external.*

internal sealed class Response {

    abstract val status: GattStatus

    data class OnPhyUpdate(
        val phy: PreferredPhy,
        override val status: GattStatus
    ) : Response()

    data class OnPhyRead(
        val phy: PreferredPhy,
        override val status: GattStatus
    ) : Response()

    data class OnReadRemoteRssi(
        val rssi: Int,
        override val status: GattStatus
    ) : Response()

    data class OnServicesDiscovered(
        override val status: GattStatus
    ) : Response()

    data class OnMtuChanged(
        val mtu: Int,
        override val status: GattStatus
    ) : Response()

    data class OnCharacteristicRead(
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray?,
        override val status: GattStatus
    ) : Response() {
        override fun toString(): String =
            "OnCharacteristicRead(characteristic=${characteristic.uuid}, value=${value?.size ?: 0} bytes, status=$status)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnCharacteristicRead

            if (characteristic != other.characteristic) return false
            if (value != null) {
                if (other.value == null) return false
                if (!value.contentEquals(other.value)) return false
            } else if (other.value != null) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = characteristic.hashCode()
            result = 31 * result + (value?.contentHashCode() ?: 0)
            result = 31 * result + status.hashCode()
            return result
        }
    }

    data class OnCharacteristicWrite(
        val characteristic: BluetoothGattCharacteristic,
        override val status: GattStatus
    ) : Response() {
        override fun toString(): String =
            "OnCharacteristicWrite(characteristic=${characteristic.uuid}, status=$status)"
    }

    data class OnDescriptorRead(
        val descriptor: BluetoothGattDescriptor,
        val value: ByteArray?,
        override val status: GattStatus
    ) : Response() {
        override fun toString(): String =
            "OnDescriptorRead(descriptor=${descriptor.uuid}, value=${value?.size ?: 0} bytes, status=$status)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnDescriptorRead

            if (descriptor != other.descriptor) return false
            if (value != null) {
                if (other.value == null) return false
                if (!value.contentEquals(other.value)) return false
            } else if (other.value != null) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = descriptor.hashCode()
            result = 31 * result + (value?.contentHashCode() ?: 0)
            result = 31 * result + status.hashCode()
            return result
        }
    }

    data class OnDescriptorWrite(
        val descriptor: BluetoothGattDescriptor,
        override val status: GattStatus
    ) : Response() {
        override fun toString(): String =
            "OnDescriptorWrite(descriptor=${descriptor.uuid}, status=$status)"
    }

    data class OnReliableWriteCompleted(
        override val status: GattStatus
    ) : Response()
}

data class PreferredPhy(
    val txPhy: Phy,
    val rxPhy: Phy
)

/**
 * Represents the possible GATT statuses as defined in [BluetoothGatt]:
 *
 * - [BluetoothGatt.GATT_SUCCESS]
 * - [BluetoothGatt.GATT_READ_NOT_PERMITTED]
 * - [BluetoothGatt.GATT_WRITE_NOT_PERMITTED]
 * - [BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION]
 * - [BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED]
 * - [BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION]
 * - [BluetoothGatt.GATT_INVALID_OFFSET]
 * - [BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH]
 * - [BluetoothGatt.GATT_CONNECTION_CONGESTED]
 * - [BluetoothGatt.GATT_FAILURE]
 */
@JvmInline
internal value class GattStatus(private val value: Int) {
    override fun toString(): String = when (value) {
        GATT_SUCCESS -> "GATT_SUCCESS"
        GATT_INVALID_HANDLE -> "GATT_INVALID_HANDLE"
        GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED"
        GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED"
        GATT_INVALID_PDU -> "GATT_INVALID_PDU"
        GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION"
        GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED"
        GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET"
        GATT_INSUF_AUTHORIZATION -> "GATT_INSUF_AUTHORIZATION"
        GATT_PREPARE_Q_FULL -> "GATT_PREPARE_Q_FULL"
        GATT_NOT_FOUND -> "GATT_NOT_FOUND"
        GATT_NOT_LONG -> "GATT_NOT_LONG"
        GATT_INSUF_KEY_SIZE -> "GATT_INSUF_KEY_SIZE"
        GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE"
        GATT_ERR_UNLIKELY -> "GATT_ERR_UNLIKELY"
        GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION"
        GATT_UNSUPPORT_GRP_TYPE -> "GATT_UNSUPPORT_GRP_TYPE"
        GATT_INSUF_RESOURCE -> "GATT_INSUF_RESOURCE"
        GATT_ILLEGAL_PARAMETER -> "GATT_ILLEGAL_PARAMETER"
        GATT_NO_RESOURCES -> "GATT_NO_RESOURCES"
        GATT_INTERNAL_ERROR -> "GATT_INTERNAL_ERROR"
        GATT_WRONG_STATE -> "GATT_WRONG_STATE"
        GATT_DB_FULL -> "GATT_DB_FULL"
        GATT_BUSY -> "GATT_BUSY"
        GATT_ERROR -> "GATT_ERROR"
        GATT_CMD_STARTED -> "GATT_CMD_STARTED"
        GATT_PENDING -> "GATT_PENDING"
        GATT_AUTH_FAIL -> "GATT_AUTH_FAIL"
        GATT_MORE -> "GATT_MORE"
        GATT_INVALID_CFG -> "GATT_INVALID_CFG"
        GATT_SERVICE_STARTED -> "GATT_SERVICE_STARTED"
        GATT_ENCRYPED_NO_MITM -> "GATT_ENCRYPED_NO_MITM"
        GATT_NOT_ENCRYPTED -> "GATT_NOT_ENCRYPTED"
        GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED"
        GATT_CCC_CFG_ERR -> "GATT_CCC_CFG_ERR"
        GATT_PRC_IN_PROGRESS -> "GATT_PRC_IN_PROGRESS"
        GATT_OUT_OF_RANGE -> "GATT_OUT_OF_RANGE"
        GATT_FAILURE -> "GATT_FAILURE"
        else -> "GATT_UNKNOWN"
    }.let { name -> "$name($value)" }
}
