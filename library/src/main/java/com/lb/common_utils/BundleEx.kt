package com.lb.common_utils

import android.content.Intent
import android.os.*
import android.os.Build.VERSION_CODES

inline fun <reified EnumType : Enum<EnumType>> enumValueOfOrNull(name: String?): EnumType? =
    name?.runCatching { enumValueOf<EnumType>(this) }
        ?.getOrNull()

//https://stackoverflow.com/a/73311814/878126  https://issuetracker.google.com/issues/243986223
inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T?
}

inline fun <reified T> Bundle.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T?
}

inline fun <reified EnumType : Enum<EnumType>> enumValueOrDefault(
    name: String?,
    defaultValue: () -> EnumType
): EnumType = enumValueOfOrNull<EnumType>(name)
    ?: defaultValue.invoke()

object BundleEx {

}
