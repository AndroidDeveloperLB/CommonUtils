package com.lb.common_utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * https://issuetracker.google.com/issues/588513767
 * Subclass of [LinearProgressIndicator] that automatically prevents the Material 3 wave animation memory leak.
 *
 * When wave animation (app:waveSpeed != 0) is configured, Material Components' internal
 * DeterminateDrawable registers a continuous frame callback with Choreographer.
 * If the view is detached, hidden, or inside an unselected ViewSwitcher/ViewAnimator tab,
 * Choreographer retains the animator callback and its Context (Activity).
 *
 * This custom view automatically sets waveSpeed to 0 whenever the view is detached or non-visible,
 * and restores the configured waveSpeed when the view is attached and visible.
 */
class LinearProgressIndicatorEx @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.linearProgressIndicatorStyle
) : LinearProgressIndicator(context, attrs, defStyleAttr) {

    private var originalWaveSpeed: Int = waveSpeed

    override fun setWaveSpeed(waveSpeed: Int) {
        super.setWaveSpeed(waveSpeed)
        if (waveSpeed != 0) {
            originalWaveSpeed = waveSpeed
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (originalWaveSpeed != 0) {
            super.setWaveSpeed(originalWaveSpeed)
        }
    }

    override fun onDetachedFromWindow() {
        if (originalWaveSpeed != 0) {
            super.setWaveSpeed(0)
        }
        try {
            progressDrawable?.hideNow()
            indeterminateDrawable?.hideNow()
        } catch (_: Exception) {
        }
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE && originalWaveSpeed != 0) {
            super.setWaveSpeed(0)
            try {
                progressDrawable?.hideNow()
                indeterminateDrawable?.hideNow()
            } catch (_: Exception) {
            }
        } else if (visibility == VISIBLE && originalWaveSpeed != 0 && isAttachedToWindow) {
            super.setWaveSpeed(originalWaveSpeed)
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != VISIBLE && originalWaveSpeed != 0) {
            super.setWaveSpeed(0)
            try {
                progressDrawable?.hideNow()
                indeterminateDrawable?.hideNow()
            } catch (_: Exception) {
            }
        } else if (visibility == VISIBLE && originalWaveSpeed != 0 && isAttachedToWindow) {
            super.setWaveSpeed(originalWaveSpeed)
        }
    }
}
