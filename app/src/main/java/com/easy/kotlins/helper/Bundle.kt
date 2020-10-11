package com.easy.kotlins.helper

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Create by LiZhanPing on 2020/9/23
 */

@Suppress("UNCHECKED_CAST")
fun <T: Serializable> Bundle.requireSerializable(key: String): T = getSerializable(key) as T

fun <T: Parcelable> Bundle.requireParcelable(key: String): T = getParcelable<T>(key) as T

@Suppress("UNCHECKED_CAST")
fun <T: Serializable> Intent.requireSerializableExtra(key: String): T = getSerializableExtra(key) as T

fun <T: Parcelable> Intent.requireParcelableExtra(key: String): T = getParcelableExtra(key) as T