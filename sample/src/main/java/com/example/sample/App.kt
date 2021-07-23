package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.common.app.ScreenCompatAdapter
import com.nice.common.app.ScreenCompatStrategy
import com.nice.common.helper.isTabletDevice


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = if (isTabletDevice) ScreenCompatStrategy.BASE_ON_HEIGHT
        else ScreenCompatStrategy.BASE_ON_WIDTH

}

fun main() {
//    val bytes = byteArrayOf(
//        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
//        0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
//        0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19,
//        0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
//        0x20, 0x21
//    )
//
//    val header = byteArrayOf(bytes[0])
//    val type = byteArrayOf(bytes[1])
//    val length = byteArrayOf(bytes[2])
//    val data = bytes.asList().subList(3, bytes.size - 1).toByteArray()
//    val crc = bytes.asList().subList(bytes.size - 1, bytes.size).toByteArray()
//
//    println(header.contentToString())
//    println(type.contentToString())
//    println(length.contentToString())
//    println(data.contentToString())
//    println(crc.contentToString())
}