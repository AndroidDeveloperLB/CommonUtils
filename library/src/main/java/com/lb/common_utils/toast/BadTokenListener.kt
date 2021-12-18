package com.lb.common_utils.toast

import android.widget.Toast

/**
 * @author drakeet
 */
interface BadTokenListener {
    fun onBadTokenCaught(toast: Toast)
}
