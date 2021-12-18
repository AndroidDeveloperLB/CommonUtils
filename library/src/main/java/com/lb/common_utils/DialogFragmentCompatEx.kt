package com.lb.common_utils

import android.os.Bundle
import androidx.fragment.app.*

val DialogFragment.argumentsSafe: Bundle
    get() = arguments ?: Bundle().also { arguments = it }

fun DialogFragment.showAllowStateLoss(fragmentManager: FragmentManager, tag: String? = null) = fragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

fun DialogFragment.showAllowStateLoss(activity: FragmentActivity, tag: String? = null) = activity.supportFragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

fun DialogFragment.showAllowStateLoss(fragment: Fragment, tag: String? = null) = fragment.childFragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

fun DialogFragment.show(activity: FragmentActivity, tag: String) {
    show(activity.supportFragmentManager, tag)
}

fun DialogFragment.show(fragment: Fragment, tag: String) {
    show(fragment.childFragmentManager, tag)
}

