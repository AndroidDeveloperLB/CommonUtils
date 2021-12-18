package com.lb.common_utils

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

fun DialogFragment.showAllowStateLoss(fragmentManager: FragmentManager, tag: String? = null) = fragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

fun DialogFragment.showAllowStateLoss(activity: FragmentActivity, tag: String? = null) = activity.supportFragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

fun DialogFragment.showAllowStateLoss(fragment: Fragment, tag: String? = null) = fragment.childFragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss() >= 0

abstract class DialogFragmentCompatEx : androidx.fragment.app.DialogFragment() {
    private var isDismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isDismissed = true
        if (isAdded)
            onDismiss()
    }

    private fun onDismiss() {}

    override fun onDestroyView() {
        //workaround for this issue: https://code.google.com/p/android/issues/detail?id=17423 (unable to retain instance after configuration change)
        //note that it doesn't seem to work if using the DialogFragment of the support library
        if (dialog != null && retainInstance)
            dialog!!.setDismissMessage(null)
        super.onDestroyView()
    }

    fun show(activity: FragmentActivity, tag: String) {
        show(activity.supportFragmentManager, tag)
    }


    fun show(fragment: Fragment, tag: String) {
        show(fragment.childFragmentManager, tag)
    }

}
