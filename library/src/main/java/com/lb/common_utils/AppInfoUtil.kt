package com.lb.common_utils

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

@SuppressLint("QueryPermissionsNeeded")
fun PackageManager.getInstalledPackagesCompat(flags: Int = 0): MutableList<PackageInfo> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        return getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
    return getInstalledPackages(flags)
}

fun ApplicationInfo.isSystemApp() = this.flags and ApplicationInfo.FLAG_SYSTEM != 0

fun PackageInfo.isSystemApp() = this.applicationInfo!!.isSystemApp()

fun PackageInfo.versionCodeCompat(): Long = PackageInfoCompat.getLongVersionCode(this)


object AppInfoUtil {
}
