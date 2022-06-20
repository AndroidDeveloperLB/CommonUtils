package com.lb.common_utils.custom_views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox

//https://stackoverflow.com/a/27391245/878126
class CheckBox : MaterialCheckBox {
    private var listener: OnCheckedChangeListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
        super.setOnCheckedChangeListener(listener)
    }

    fun setChecked(checked: Boolean, alsoNotify: Boolean) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null)
            super.setChecked(checked)
            super.setOnCheckedChangeListener(listener)
            return
        }
        super.setChecked(checked)
    }

    fun toggle(alsoNotify: Boolean) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null)
            super.toggle()
            super.setOnCheckedChangeListener(listener)
            return
        }
        super.toggle()
    }
}
