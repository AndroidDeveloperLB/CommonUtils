package com.lb.common_utils

import android.view.View
import android.widget.FrameLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils

//    https://github.com/material-components/material-components-android/issues/3860#issuecomment-1822276005
@androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
fun BadgeDrawable.attachToView(anchor: View, customBadgeParent: FrameLayout?) {
    BadgeUtils.attachBadgeDrawable(this, anchor, customBadgeParent)
    anchor.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        updateBadgeCoordinates(anchor, customBadgeParent)
    }
}

object ViewUtils {

}
