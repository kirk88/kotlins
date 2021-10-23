@file:Suppress("UNUSED")

package com.nice.bluetooth.common

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

internal object BluetoothUuid {
    private val BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")

    /**
     * Length of bytes for 16 bit UUID
     */
    const val UUID_BYTES_16_BIT = 2

    /**
     * Length of bytes for 32 bit UUID
     */
    const val UUID_BYTES_32_BIT = 4

    /**
     * Length of bytes for 128 bit UUID
     */
    const val UUID_BYTES_128_BIT = 16

    /**
     * Parse UUID from bytes. The `uuidBytes` can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return [ParcelUuid] parsed from bytes.
     * @throws IllegalArgumentException If the `uuidBytes` cannot be parsed.
     */
    fun parseUuidFrom(uuidBytes: ByteArray?): UUID {
        requireNotNull(uuidBytes) { "uuidBytes cannot be null" }
        val length = uuidBytes.size
        require(!(length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT && length != UUID_BYTES_128_BIT)) {
            "uuidBytes length invalid - $length"
        }

        // Construct a 128 bit UUID.
        if (length == UUID_BYTES_128_BIT) {
            val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
            val msb = buf.getLong(8)
            val lsb = buf.getLong(0)
            return UUID(msb, lsb)
        }

        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        var shortUuid: Long
        if (length == UUID_BYTES_16_BIT) {
            shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
            shortUuid += (uuidBytes[1].toInt() and 0xFF shl 8).toLong()
        } else {
            shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
            shortUuid += (uuidBytes[1].toInt() and 0xFF shl 8).toLong()
            shortUuid += (uuidBytes[2].toInt() and 0xFF shl 16).toLong()
            shortUuid += (uuidBytes[3].toInt() and 0xFF shl 24).toLong()
        }
        val msb = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
        val lsb = BASE_UUID.uuid.leastSignificantBits
        return UUID(msb, lsb)
    }
}

class ManufacturerData(
    /**
     * Two-octet [Company Identifier Code][https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/]
     */
    val code: Int,

    /**
     * the Manufacturer Data (not including the leading two identifier octets)
     */
    val data: ByteArray
)

class ServiceData(
    /**
     * Service Data Uuid
     */
    val uuid: UUID,

    /**
     * The Service Data
     */
    val data: ByteArray
)

class ScanRecord private constructor(
    val serviceUuids: List<UUID>?,
    val serviceSolicitationUuids: List<UUID>?,
    val manufacturerSpecificData: List<ManufacturerData>?,
    val serviceData: List<ServiceData>?,
    // Flags of the advertising data.
    val advertiseFlags: Int,
    // Transmission power level(in dB).
    val txPowerLevel: Int,
    // Local name of the Bluetooth LE device.
    val deviceName: String?,
    // Raw bytes of scan record.
    val bytes: ByteArray
) {

    fun getManufacturerSpecificData(manufacturerId: Int): ManufacturerData? {
        return manufacturerSpecificData?.find { it.code == manufacturerId }
    }

    fun getServiceData(serviceDataUuid: UUID): ServiceData? {
        return serviceData?.find { it.uuid == serviceDataUuid }
    }

    override fun toString(): String {
        return ("ScanRecord [mAdvertiseFlags=" + advertiseFlags
                + ", mServiceUuids=" + serviceUuids
                + ", mServiceSolicitationUuids=" + serviceSolicitationUuids
                + ", mManufacturerSpecificData=" + manufacturerSpecificData
                + ", mServiceData=" + serviceData
                + ", mTxPowerLevel=" + txPowerLevel
                + ", mDeviceName=" + deviceName + "]")
    }

    internal companion object {
        private const val TAG = "ScanRecord"

        // The following data type values are assigned by Bluetooth SIG.
        // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
        private const val DATA_TYPE_FLAGS = 0x01
        private const val DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02
        private const val DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03
        private const val DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04
        private const val DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05
        private const val DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06
        private const val DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07
        private const val DATA_TYPE_LOCAL_NAME_SHORT = 0x08
        private const val DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09
        private const val DATA_TYPE_TX_POWER_LEVEL = 0x0A
        private const val DATA_TYPE_SERVICE_DATA_16_BIT = 0x16
        private const val DATA_TYPE_SERVICE_DATA_32_BIT = 0x20
        private const val DATA_TYPE_SERVICE_DATA_128_BIT = 0x21
        private const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT = 0x14
        private const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT = 0x1F
        private const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT = 0x15
        private const val DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF

        fun parseFromBytes(scanRecord: ByteArray?): ScanRecord? {
            if (scanRecord == null) {
                return null
            }
            var currentPos = 0
            var advertiseFlag = -1
            var localName: String? = null
            var txPowerLevel = Int.MIN_VALUE
            var serviceUuids: MutableList<UUID>? = mutableListOf()
            val serviceSolicitationUuids: MutableList<UUID> = mutableListOf()
            val serviceData: MutableList<ServiceData> = mutableListOf()
            val manufacturerData: MutableList<ManufacturerData> = mutableListOf()
            return try {
                while (currentPos < scanRecord.size) {
                    // length is unsigned int.
                    val length: Int = scanRecord[currentPos++].toInt() and 0xFF
                    if (length == 0) {
                        break
                    }
                    // Note the length includes the length of the field type itself.
                    val dataLength = length - 1
                    // fieldType is unsigned int.
                    when (val fieldType: Int = scanRecord[currentPos++].toInt() and 0xFF) {
                        DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toInt() and 0xFF
                        DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos,
                            dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids
                        )
                        DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids
                        )
                        DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids
                        )
                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT -> parseServiceSolicitationUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_16_BIT, serviceSolicitationUuids
                        )
                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT -> parseServiceSolicitationUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_32_BIT, serviceSolicitationUuids
                        )
                        DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT -> parseServiceSolicitationUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_128_BIT, serviceSolicitationUuids
                        )
                        DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName =
                            String(
                                extractBytes(scanRecord, currentPos, dataLength)
                            )
                        DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                        DATA_TYPE_SERVICE_DATA_16_BIT, DATA_TYPE_SERVICE_DATA_32_BIT, DATA_TYPE_SERVICE_DATA_128_BIT -> {
                            var serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT
                            if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                                serviceUuidLength = BluetoothUuid.UUID_BYTES_32_BIT
                            } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                                serviceUuidLength = BluetoothUuid.UUID_BYTES_128_BIT
                            }
                            val serviceDataUuidBytes = extractBytes(
                                scanRecord, currentPos,
                                serviceUuidLength
                            )
                            val serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes
                            )
                            val serviceDataArray = extractBytes(
                                scanRecord,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength
                            )
                            serviceData.add(ServiceData(serviceDataUuid, serviceDataArray))
                        }
                        DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                            // The first two bytes of the manufacturer specific data are
                            // manufacturer ids in little endian.
                            val manufacturerId: Int =
                                ((scanRecord[currentPos + 1].toInt() and 0xFF shl 8)
                                        + (scanRecord[currentPos].toInt() and 0xFF))
                            val manufacturerDataBytes = extractBytes(
                                scanRecord, currentPos + 2,
                                dataLength - 2
                            )
                            manufacturerData.add(ManufacturerData(manufacturerId, manufacturerDataBytes))
                        }
                        else -> {
                        }
                    }
                    currentPos += dataLength
                }
                if (serviceUuids!!.isEmpty()) {
                    serviceUuids = null
                }
                ScanRecord(
                    serviceUuids, serviceSolicitationUuids, manufacturerData,
                    serviceData, advertiseFlag, txPowerLevel, localName, scanRecord
                )
            } catch (e: Exception) {
                Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord))
                ScanRecord(
                    null, null, null,
                    null, -1, Int.MIN_VALUE, null, scanRecord
                )
            }
        }

        // Parse service UUIDs.
        private fun parseServiceUuid(
            scanRecord: ByteArray,
            currentPos: Int,
            dataLength: Int,
            uuidLength: Int,
            serviceUuids: MutableList<UUID>?
        ): Int {
            var position = currentPos
            var length = dataLength
            while (length > 0) {
                val uuidBytes = extractBytes(
                    scanRecord, position,
                    uuidLength
                )
                serviceUuids!!.add(BluetoothUuid.parseUuidFrom(uuidBytes))
                length -= uuidLength
                position += uuidLength
            }
            return position
        }

        /**
         * Parse service Solicitation UUIDs.
         */
        private fun parseServiceSolicitationUuid(
            scanRecord: ByteArray,
            currentPos: Int,
            dataLength: Int,
            uuidLength: Int,
            serviceSolicitationUuids: MutableList<UUID>
        ): Int {
            var position = currentPos
            var length = dataLength
            while (length > 0) {
                val uuidBytes = extractBytes(scanRecord, position, uuidLength)
                serviceSolicitationUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes))
                length -= uuidLength
                position += uuidLength
            }
            return position
        }

        // Helper method to extract bytes from byte array.
        private fun extractBytes(scanRecord: ByteArray, start: Int, length: Int): ByteArray {
            val bytes = ByteArray(length)
            System.arraycopy(scanRecord, start, bytes, 0, length)
            return bytes
        }
    }
}

internal data class BluetoothScanResult(
    val device: BluetoothDevice,
    val rssi: Int,
    val scanRecord: ScanRecord?
)

internal fun BluetoothScanResult(
    device: BluetoothDevice,
    rssi: Int,
    scanRecord: ByteArray? = null
) = BluetoothScanResult(device, rssi, ScanRecord.parseFromBytes(scanRecord))

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal fun BluetoothScanResult(
    scanResult: ScanResult
) = BluetoothScanResult(scanResult.device, scanResult.rssi, scanResult.scanRecord?.bytes)