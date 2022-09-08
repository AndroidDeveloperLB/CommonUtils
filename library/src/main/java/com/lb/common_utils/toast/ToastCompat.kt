package com.lb.common_utils.toast

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author drakeet
 */
class ToastCompat
/**
 * Construct an empty Toast object.  You must call [.setView] before you
 * can call [.show].
 *
 * @param context The context to use.  Usually your [Application]
 * or [Activity] object.
 * @param base    The base toast
 */
private constructor(context: Context, val baseToast: Toast) : Toast(context) {
    override fun show() {
        baseToast.show()
    }

    override fun setDuration(duration: Int) {
        baseToast.duration = duration
    }

    override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        baseToast.setGravity(gravity, xOffset, yOffset)
    }

    override fun setMargin(horizontalMargin: Float, verticalMargin: Float) {
        baseToast.setMargin(horizontalMargin, verticalMargin)
    }

    override fun setText(resId: Int) {
        baseToast.setText(resId)
    }

    override fun setText(s: CharSequence) {
        baseToast.setText(s)
    }

    @Deprecated("Deprecated in Java")
    override fun setView(view: View) {
        baseToast.view = view
        setContextCompat(view, SafeToastContext(view.context, this))
    }

    override fun getHorizontalMargin(): Float {
        return baseToast.horizontalMargin
    }

    override fun getVerticalMargin(): Float {
        return baseToast.verticalMargin
    }

    override fun getDuration(): Int {
        return baseToast.duration
    }

    override fun getGravity(): Int {
        return baseToast.gravity
    }

    override fun getXOffset(): Int {
        return baseToast.xOffset
    }

    override fun getYOffset(): Int {
        return baseToast.yOffset
    }

    @Deprecated("Deprecated in Java")
    override fun getView(): View? {
        return baseToast.view
    }

    companion object {
        /**
         * Make a standard toast that just contains a text view.
         *
         * @param context  The context to use.  Usually your [android.app.Application]
         * or [android.app.Activity] object.
         * @param text     The text to show.  Can be formatted text.
         * @param duration How long to display the message.  Either [.LENGTH_SHORT] or
         * [.LENGTH_LONG]
         */
        fun makeText(context: Context, text: CharSequence?, duration: Int): Toast {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1)
                return Toast.makeText(context, text, duration)
            // We cannot pass the SafeToastContext to Toast.makeText() because
            // the View will unwrap the base context and we are in vain.
            @SuppressLint("ShowToast") val toast = Toast.makeText(context, text, duration)
            setContextCompat(toast.view!!, SafeToastContext(context, toast))
            return ToastCompat(context, toast)
        }

        /**
         * Make a standard toast that just contains a text view with the text from a resource.
         *
         * @param context  The context to use.  Usually your [android.app.Application]
         * or [android.app.Activity] object.
         * @param resId    The resource id of the string resource to use.  Can be formatted text.
         * @param duration How long to display the message.  Either [.LENGTH_SHORT] or
         * [.LENGTH_LONG]
         * @throws Resources.NotFoundException if the resource can't be found.
         */
        @Throws(NotFoundException::class)
        fun makeText(context: Context, @StringRes resId: Int, duration: Int): Toast {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1)
                return Toast.makeText(context, resId, duration)
            return makeText(context, context.resources.getText(resId), duration)
        }

        private fun setContextCompat(view: View, context: Context) {
            if (Build.VERSION.SDK_INT == 25) {
                try {
                    @SuppressLint("DiscouragedPrivateApi") val field =
                        View::class.java.getDeclaredField("mContext")
                    field.isAccessible = true
                    field[view] = context
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }
    }
}
