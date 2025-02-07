package com.lb.common_utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationExitInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.system.OsConstants
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.R)
fun ApplicationExitInfo.wasKilledByLowMemory(): Boolean {
    return if (ActivityManager.isLowMemoryKillReportSupported()) reason == ApplicationExitInfo.REASON_LOW_MEMORY
    else reason == ApplicationExitInfo.REASON_SIGNALED && status == OsConstants.SIGKILL
}

/**uses application context to make sure it will avoid memory leaks*/
inline fun <reified T : Any> Context.getSystemServiceCompat(): T =
        ContextCompat.getSystemService(applicationContext, T::class.java)!!

fun PackageManager.queryIntentActivitiesCompat(
        intent: Intent,
        flags: Long = 0L
): MutableList<ResolveInfo> {
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

fun PackageManager.getActivityInfoCompat(
        componentName: ComponentName,
        flags: Long = 0L
): ActivityInfo? {
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
    fun getActivityLabel(
            packageManager: PackageManager, packageName: String, inputActivityInfo: ActivityInfo?,
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

    @Throws(ArithmeticException::class)
    @JvmStatic
    fun getHeapMemStats(): String {
        val maxMemInBytes: Long = getMaxMemInBytes()
        val availableMemInBytes: Long = getAvailableMemInBytes()
        val usedMemInBytes: Long = maxMemInBytes - availableMemInBytes
        val usedMemInPercentage: Long = usedMemInBytes * 100 / maxMemInBytes
        try {
            return "used: " + StringsUtil.bytesIntoHumanReadable(usedMemInBytes, isMetric = false) + " / " +
                    StringsUtil.bytesIntoHumanReadable(maxMemInBytes, isMetric = false) + " (" + usedMemInPercentage + "%)"
        } catch (e: java.lang.ArithmeticException) {
            //For some reason this occurs on some old devices (mostly Android 7)
            throw ArithmeticException("failed to format heap stats: maxMemInBytes:$maxMemInBytes availableMemInBytes:$availableMemInBytes usedMemInBytes:$usedMemInBytes usedMemInPercentage:$usedMemInPercentage $e")
        }
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
        return@lazy (
                // Android SDK emulator
                Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
                        ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                                && Build.FINGERPRINT.endsWith(":user/release-keys")
                                && Build.PRODUCT.startsWith("sdk_gphone_")
                                && Build.MODEL.startsWith("sdk_gphone_"))
                                //alternative
                                || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_") && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys")
                                || (Build.FINGERPRINT.endsWith(":user/release-keys")) && Build.PRODUCT.startsWith("sdk_gphone64_")
                                && Build.MODEL.startsWith("sdk_gphone64_")))
                                //Google Play Games emulator https://play.google.com/googleplaygames https://developer.android.com/games/playgames/emulator#other-downloads
                                || (Build.MODEL == "HPE device" &&
                                Build.FINGERPRINT.startsWith("google/kiwi_") && Build.FINGERPRINT.endsWith(":user/release-keys")
                                && Build.BOARD == "kiwi" && Build.PRODUCT.startsWith("kiwi_"))
                                )
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

    /**returns a list of all current locales, or null if not needed as there aren't at least 2 (if it's one, it's the default locale anyway).
     * @param haveFirstAsCurrentLocale when true, makes sure the first locale is also the default one. If not, it depends on what you've set as locale*/
    //        https://stackoverflow.com/a/77000208/878126
    fun getLocalesList(haveFirstAsCurrentLocale: Boolean = true): ArrayList<Locale>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (haveFirstAsCurrentLocale)
                return arrayListOf(Locale.getDefault())
            return null
        }
        val localeList = Resources.getSystem().configuration.locales
        if (localeList.size() <= 1)
            return null
        val defaultLocale =
                if (haveFirstAsCurrentLocale) Locale.getDefault()
                else null
        val result = ArrayList<Locale>(localeList.size())
        if (defaultLocale != null)
            result.add(defaultLocale)
        for (i in 0 until localeList.size()) {
            val locale = localeList[i]!!
            if (locale == defaultLocale)
                continue
            result.add(locale)
        }
        return result
    }


    /**used for Admob test ads and constent testing:
     *  Admob test ads usage :
     *  https://developers.google.com/admob/android/test-ads
     *
     *  val deviceIds = arrayListOf(AdRequest.DEVICE_ID_EMULATOR)
     *  getDeviceHashedId(context)?.let { deviceIds.add(it) }
     *  MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(deviceIds).build())
     *
     *  ads consent testing:
     *  https://developers.google.com/admob/android/privacy
     *
     *  val debugSettings = ConsentDebugSettings.Builder(app)
     *           .setDebugGeography(ConsentDebugSettings.DebugGeography....)
     *           .addTestDeviceHashedId(getDeviceHashedId(app)!!)
     *           .build()
     * */
    @SuppressLint("HardwareIds")
    fun getDeviceHashedId(context: Application): String? {
        val md5 = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        try {
            val md = MessageDigest.getInstance("MD5")
            val array = md.digest(md5.toByteArray())
            val sb = StringBuilder()
            for (i in array.indices)
                sb.append(Integer.toHexString(array[i].toInt() and 0xFF or 0x100).substring(1, 3))
            //            Log.d("AppLog", "getDeviceIdForAdMobTestAds:$sb")
            return "$sb".uppercase(Locale.ENGLISH)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }
}
