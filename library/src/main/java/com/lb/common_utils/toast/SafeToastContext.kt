package com.lb.common_utils.toast

import android.content.Context
import android.content.ContextWrapper
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.Toast

/**
 * @author drakeet
 */
internal class SafeToastContext(base: Context, private val toast: Toast) : ContextWrapper(base) {
    override fun getApplicationContext(): Context {
        return ApplicationContextWrapper(baseContext.applicationContext)
    }

    private inner class ApplicationContextWrapper constructor(base: Context) :
        ContextWrapper(base) {
        override fun getSystemService(name: String): Any {
            return if (WINDOW_SERVICE == name) {
                WindowManagerWrapper(baseContext.getSystemService(name) as WindowManager)
            } else super.getSystemService(name)
        }
    }

    private inner class WindowManagerWrapper constructor(private val base: WindowManager) :
        WindowManager {
        @Deprecated("Deprecated in Java")
        override fun getDefaultDisplay(): Display {
            return base.defaultDisplay
        }

        override fun removeViewImmediate(view: View) {
            base.removeViewImmediate(view)
        }

        override fun addView(view: View, params: ViewGroup.LayoutParams) {
            try {
                base.addView(view, params)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }

        override fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
            base.updateViewLayout(view, params)
        }

        override fun removeView(view: View) {
            base.removeView(view)
        }

    }
}
