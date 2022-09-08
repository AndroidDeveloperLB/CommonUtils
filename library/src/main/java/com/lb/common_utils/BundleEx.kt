package com.lb.common_utils

import android.content.Intent
import android.os.*
import android.os.Build.VERSION_CODES
import java.io.Serializable


fun Intent.getBooleanExtraOrNull(key: String): Boolean? =
    if (this.hasExtra(key)) getBooleanExtra(key, false) else null

fun Bundle.getIntOrNull(key: String): Int? = if (this.containsKey(key)) getInt(key) else null
fun Intent.getIntExtraOrNull(key: String): Int? =
    if (this.hasExtra(key)) getIntExtra(key, 0) else null

fun Intent.getLongExtraOrNull(key: String): Long? =
    if (this.hasExtra(key)) getLongExtra(key, 0L) else null

fun Bundle.getLongOrNull(key: String): Long? =
    if (this.containsKey(key)) this.getLong(key) else null

inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T?
}

inline fun <reified T> Bundle.getParcelableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T?
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T>? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
    }


inline fun <reified T : java.io.Serializable> Intent.getSerializableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T?
}

inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T?
}

inline fun <reified EnumType : Enum<EnumType>> enumValueOfOrNull(name: String?): EnumType? =
    name?.runCatching { enumValueOf<EnumType>(this) }
        ?.getOrNull()


inline fun <reified EnumType : Enum<EnumType>> enumValueOrDefault(
    name: String?,
    defaultValue: () -> EnumType
): EnumType = enumValueOfOrNull<EnumType>(name)
    ?: defaultValue.invoke()
//
//object BundleEx {
//}
