package com.lb.common_utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.annotation.IdRes
import androidx.annotation.IntDef
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
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

fun View?.removeFromParent() = (this?.parent as? ViewGroup?)?.removeView(this)

fun ImageView.setImageDrawableOrHide(newDrawable: Drawable?,
                                     @ViewUtils.Visibility visibilityWhenEmpty: Int = View.GONE) {
    if (newDrawable == null) {
        visibility = visibilityWhenEmpty
        setImageDrawable(null)
    } else {
        isVisible = true
        setImageDrawable(newDrawable)
    }
}

fun RatingBar.setRatingOrHide(newRating: Float?, @ViewUtils.Visibility visibilityWhenEmpty: Int = View.GONE) {
    if (newRating == null) {
        visibility = visibilityWhenEmpty
        rating = 0f
    } else {
        isVisible = true
        rating = newRating
    }
}


fun TextView.setTextOrHide(textToSet: CharSequence?, @ViewUtils.Visibility visibilityWhenEmpty: Int = View.GONE) {
    if (textToSet.isNullOrEmpty()) {
        visibility = visibilityWhenEmpty
        text = null
    } else {
        isVisible = true
        text = textToSet
    }
}

fun ViewAnimator.setViewToSwitchTo(viewToSwitchTo: View, animate: Boolean = true): Boolean {
    if (currentView === viewToSwitchTo)
        return false
    this.forEachIndexed { i, view ->
        if (view != viewToSwitchTo)
            return@forEachIndexed
        if (animate)
            displayedChild = i
        else {
            val outAnimation = this.outAnimation
            val inAnimation = this.inAnimation
            this.inAnimation = null
            this.outAnimation = null
            displayedChild = i
            this.inAnimation = inAnimation
            this.outAnimation = outAnimation
        }
        return true
    }
    return false
}

fun ViewAnimator.setViewToSwitchTo(@IdRes viewIdToSwitchTo: Int, animate: Boolean = true): Boolean {
    if (currentView.id == viewIdToSwitchTo)
        return false
    this.forEachIndexed { i, view ->
        if (view.id != viewIdToSwitchTo)
            return@forEachIndexed
        if (animate)
            displayedChild = i
        else {
            val outAnimation = this.outAnimation
            val inAnimation = this.inAnimation
            this.inAnimation = null
            this.outAnimation = null
            displayedChild = i
            this.inAnimation = inAnimation
            this.outAnimation = outAnimation
        }
        return true
    }
    return false
}

object ViewUtils {
    @IntDef(View.VISIBLE, View.INVISIBLE, View.GONE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Visibility

    /**returns the location of the child view in a LinearLayout, using Gravity values
     * Fill - view takes entire space
     * no-gravity - view not found
     * center-vertical/horizontal - in the middle of multiple other views
     * the rest are quite obvious:top/bottom/left/right*/
    @SuppressLint("RtlHardcoded")
    fun findLocationOfChildInLinearLayout(childView: View, container: LinearLayout): Int {
        val childCount = container.children.count()
        if (childCount == 0)
            return Gravity.NO_GRAVITY
        if (childCount == 1) {
            if (container.children.first() == childView)
                return Gravity.FILL
            return Gravity.NO_GRAVITY
        }
        //handle cases of at least 2 children:
        if (container.orientation == LinearLayout.HORIZONTAL) {
            //horizontal
            for ((index, child) in container.children.withIndex()) {
                if (child != childView)
                    continue
                val isLeftToRight = container.layoutDirection == View.LAYOUT_DIRECTION_LTR
                //                        TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR
                return when (index) {
                    0 -> {
                        if (isLeftToRight) Gravity.LEFT else Gravity.RIGHT
                    }

                    childCount - 1 -> {
                        if (isLeftToRight) Gravity.RIGHT else Gravity.LEFT
                    }

                    else -> Gravity.CENTER_HORIZONTAL
                }
            }
            return Gravity.FILL
        }
        else {
            //vertical
            for ((index, child) in container.children.withIndex()) {
                if (child != childView)
                    continue
                return when (index) {
                    0 -> Gravity.TOP
                    childCount - 1 -> Gravity.BOTTOM
                    else -> Gravity.CENTER_VERTICAL
                }
            }
        }
        return Gravity.NO_GRAVITY
    }
}
