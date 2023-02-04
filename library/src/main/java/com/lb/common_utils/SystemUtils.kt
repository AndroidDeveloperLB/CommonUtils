package com.lb.common_utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.*
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager
import android.os.*
import android.provider.Settings
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.io.File
import java.util.regex.Pattern
import kotlin.math.max

/**uses application context to make sure it will avoid memory leaks*/
inline fun <reified T : Any> Context.getSystemServiceCompat(): T =
    ContextCompat.getSystemService(applicationContext, T::class.java)!!

fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Long = 0L): MutableList<ResolveInfo> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        return queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags))
    @Suppress("DEPRECATION")
    return queryIntentActivities(intent, flags.toInt())
}

fun PackageManager.resolveActivityCompat(intent: Intent, flags: Long = 0L): ResolveInfo? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        return resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags))
    @Suppress("DEPRECATION")
    return resolveActivity(intent, flags.toInt())
}

fun PackageManager.getActivityInfoCompat(componentName: ComponentName, flags: Long = 0L): ActivityInfo? {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return getActivityInfo(componentName, PackageManager.ComponentInfoFlags.of(flags))
        @Suppress("DEPRECATION")
        return getActivityInfo(componentName, flags.toInt())
    } catch (_: NameNotFoundException) {
    }
    return null
}

@Suppress("unused")
object SystemUtils {
    /**
     * returns the label of the specified activity.
     * Will first try using the activityInfo, and then the path to it.
     */
    fun getActivityLabel(packageManager: PackageManager, packageName: String, inputActivityInfo: ActivityInfo?,
        fullPathToActivity: String?
    ): String? {
        var activityInfo: ActivityInfo? = inputActivityInfo
        var label: String? = null
        if (fullPathToActivity != null && activityInfo == null) {
            try {
                activityInfo =
                    packageManager.getActivityInfoCompat(
                        ComponentName(packageName, fullPathToActivity),
                        0
                    )
            } catch (_: NameNotFoundException) {
            }
        }
        if (activityInfo != null) {
            label = activityInfo.loadLabel(packageManager).toString()
        }
        return label
    }

    fun isDevMode(context: Context) =
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0

    /** returns the max size of the heap, in bytes     */
    private fun getMaxMemInBytes(): Long = Runtime.getRuntime().maxMemory()

    /** returns the currently free memory in bytes (of the heap)     */
    private fun getAvailableMemInBytes(): Long {
        // find available memory
        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        val maxHeapSize = runtime.maxMemory()
        return maxHeapSize - usedMem
    }

    @JvmStatic
    fun getHeapMemStats(): String {
        val maxMemInBytes = getMaxMemInBytes()
        val availableMemInBytes = getAvailableMemInBytes()
        val usedMemInBytes = maxMemInBytes - availableMemInBytes
        val usedMemInPercentage = usedMemInBytes * 100 / maxMemInBytes
        return "used: " + StringsUtil.bytesIntoHumanReadable(
            usedMemInBytes,
            isMetric = false
        ) + " / " +
                StringsUtil.bytesIntoHumanReadable(
                    maxMemInBytes,
                    isMetric = false
                ) + " (" + usedMemInPercentage + "%)"
    }

    fun setAppComponentEnabled(context: Context, componentClass: Class<*>, enable: Boolean) {
        val pm = context.packageManager
        val enableFlag =
            if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        pm.setComponentEnabledSetting(
            ComponentName(context, componentClass), enableFlag,
            PackageManager.DONT_KILL_APP
        )
    }

    @RequiresPermission(allOf = [android.Manifest.permission.WAKE_LOCK])
    fun wakeUp(activity: Activity) {
        @Suppress("DEPRECATION") activity.window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        val power: PowerManager = activity.getSystemServiceCompat()
        val lock =
            power.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                activity.packageName + ":wakeup!"
            )
        lock.acquire(1000)
        lock.release()
    }

    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_NETWORK_STATE])
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager: ConnectivityManager = context.getSystemServiceCompat()
            connectivityManager.activeNetworkInfo?.isConnected == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * return the number of cores of the device.<br></br>
     * based on : http://stackoverflow.com/a/10377934/878126
     */
    val coresCount: Int by lazy {
        return@lazy kotlin.runCatching {
            val dir = File("/sys/devices/system/cpu/")
            val files = dir.listFiles { pathname -> Pattern.matches("cpu[0-9]+", pathname.name) }
            max(1, files?.size ?: 1)
        }.getOrDefault(1)
    }

    //    https://stackoverflow.com/a/21505193/878126
    val isProbablyRunningOnEmulator: Boolean by lazy {
        // Android SDK emulator
        return@lazy ((Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
                ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                        && Build.FINGERPRINT.endsWith(":user/release-keys")
                        && Build.PRODUCT.startsWith("sdk_gphone_")
                        && Build.MODEL.startsWith("sdk_gphone_"))
                        //alternative
                        || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_")
                        && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys") || Build.FINGERPRINT.endsWith(":user/release-keys"))
                        && Build.PRODUCT.startsWith("sdk_gphone64_")
                        && Build.MODEL.startsWith("sdk_gphone64_"))))
                //
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
                //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build")
                //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                // another Android SDK emulator check
                || SystemProperties.getProp("ro.kernel.qemu") == "1")
    }

    /**@return true iff we've detected that MIUI OS has MIUI optimization enabled. Returns null when failed to detect anything about it*/
    @SuppressLint("PrivateApi")
    fun isMiuiOptimizationEnabled(): Boolean? {
        try {
            val miuiOptimizationEnabled: String =
                SystemProperties.getProp("persist.sys.miui_optimization")
            if (miuiOptimizationEnabled.isNotEmpty()) return miuiOptimizationEnabled == "true"
            val clazz = Class.forName("android.miui.AppOpsUtils")
            val isOptedOutOfMiuiOptimization = clazz.getMethod("isXOptMode").invoke(null) as Boolean
            return !isOptedOutOfMiuiOptimization
        } catch (e: Exception) {
            return null
        }
    }

    fun hasRootManagerSystemApp(context: Context): Boolean {
        val rootAppsPackageNames =
            arrayOf(
                "com.topjohnwu.magisk",
                "eu.chainfire.supersu",
                "com.koushikdutta.superuser",
                "com.noshufou.android.su",
                "me.phh.superuser"
            )
        rootAppsPackageNames.forEach { rootAppPackageName ->
            try {
                context.packageManager.getApplicationInfo(rootAppPackageName, 0)
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun hasSuBinary(): Boolean {
        return try {
            findBinary("su")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun findBinary(binaryName: String): Boolean {
        val paths = System.getenv("PATH")
        if (!paths.isNullOrBlank()) {
            val systemPlaces: List<String> = paths.split(":")
            return systemPlaces.firstOrNull { File(it, binaryName).exists() } != null
        }
        val places = arrayOf(
            "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/",
            "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"
        )
        return places.firstOrNull { File(it, binaryName).exists() } != null
    }

    fun getPerformanceClassValue(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Build.VERSION.MEDIA_PERFORMANCE_CLASS
            else -1
        } catch (e: Throwable) {
            e.printStackTrace()
            -1
        }
    }

    fun getArchitecture() = kotlin.runCatching { System.getProperty("os.arch") }.getOrNull() ?: ""
}
