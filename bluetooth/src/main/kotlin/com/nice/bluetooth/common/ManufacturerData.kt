package com.nice.bluetooth.common

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
