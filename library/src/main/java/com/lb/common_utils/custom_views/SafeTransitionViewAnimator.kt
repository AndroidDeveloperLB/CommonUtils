package com.lb.common_utils.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ViewAnimator

/**
 * A ViewAnimator that can block touch events for a set duration after a transition.
 * Use this to prevent accidental clicks immediately after something replaces a loading screen, for example
 */
class SafeTransitionViewAnimator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewAnimator(context, attrs) {
    private var transitionTime: Long = 0
    private var isBlockActive: Boolean = false

    // Set this to true when the ad is about to load/display
    var blockInteractionsOnTransition: Boolean = false

    // 500ms is a standard 'safe' window to prevent muscle-memory accidental clicks
    var blockDurationMs: Long = 500

    override fun showNext() {
        if (childCount <= 1)
            return
        markTransition()
        super.showNext()
    }

    override fun setDisplayedChild(whichChild: Int) {
        if (displayedChild == whichChild)
            return
        markTransition()
        super.setDisplayedChild(whichChild)
    }

    private fun markTransition() {
        if (blockInteractionsOnTransition) {
            transitionTime = System.currentTimeMillis()
            isBlockActive = true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isBlockActive) {
            if (System.currentTimeMillis() - transitionTime < blockDurationMs) {
                // Swallow the event to prevent children from being clicked
                return true
            } else {
                isBlockActive = false
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return isBlockActive && (System.currentTimeMillis() - transitionTime < blockDurationMs) || super.onTouchEvent(event)
    }
}
