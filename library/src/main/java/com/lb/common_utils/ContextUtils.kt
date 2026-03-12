package com.lb.common_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

inline fun <reified T : Activity> Fragment.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(context!!, T::class.java)
    block(intent)
    startActivity(intent)
}

fun AppCompatActivity.tryMoveToBack(onBackPressedCallback: OnBackPressedCallback, navController: NavController?) {
    val succeededMovingToBack: Boolean =
            when {
                navController?.previousBackStackEntry != null -> false
                VERSION.SDK_INT >= VERSION_CODES.O -> moveTaskToBack(false)
                else -> {
                    try {
                        //workaround of some weird NPE on Android 7.0 https://play.google.com/apps/publish/?account=7235900666503041581#AndroidMetricsErrorsPlace:p=com.lb.app_manager&appid=4975755565900227263&appVersion=PRODUCTION&clusterName=apps/com.lb.app_manager/clusters/bb35697e&detailsAppVersion=PRODUCTION&detailsSpan=7
                        moveTaskToBack(false)
                    } catch (_: Exception) {
                        false
                    }
                }
            }
    if (!succeededMovingToBack) {
        onBackPressedCallback.isEnabled = false
        onBackPressedDispatcher.onBackPressed()
        onBackPressedCallback.isEnabled = true
    }
}

fun Context.getPackageInfo(flags: Int = 0): PackageInfo =
        packageManager.getPackageInfo(packageName, flags)!!

fun Fragment.isNotAddedOrActivityFinishingOrDestroyed() =
        !isAdded || isActivityFinishingOrDestroyed()

fun Fragment.isActivityFinishingOrDestroyed(): Boolean = activity.isFinishedOrFinishing()

fun Activity?.isFinishedOrFinishing(): Boolean = this == null || isFinishing || isDestroyed

fun interface OnActivityResultSuccessCallback {
    fun onSuccess(result: ActivityResult)
}

@MainThread
fun Fragment.registerForActivityResultSuccess(onActivityResultSuccessCallback: OnActivityResultSuccessCallback): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK)
            onActivityResultSuccessCallback.onSuccess(it)
    }
}

fun Context.registerPreferenceListener(lifecycle: Lifecycle, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    val defaultSharedPreferences = PreferenceUtil.getDefaultSharedPreferences(this)
    defaultSharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    })
}

object ContextUtils {
}
