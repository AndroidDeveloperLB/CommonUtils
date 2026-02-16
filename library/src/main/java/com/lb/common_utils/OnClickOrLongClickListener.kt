package com.lb.common_utils

import android.view.View
import androidx.annotation.UiThread

fun View.setClickOrLongClickListener(listener: OnClickOrLongClickListener) {
    setOnLongClickListener(listener)
    setOnClickListener(listener)
}

abstract class OnClickOrLongClickListener : View.OnClickListener, View.OnLongClickListener {
    @UiThread
    abstract fun onClickOrLongClick(v: View, isClicked: Boolean)

    @UiThread
    override fun onClick(v: View) {
        onClickOrLongClick(v, true)
    }

    @UiThread
    override fun onLongClick(v: View): Boolean {
        onClickOrLongClick(v, false)
        return true
    }
}
