package com.lb.common_utils

import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.lifecycle.MediatorLiveData

fun <T, R> MutableLiveData2<T>.mapNonNull(transform: (T) -> R): MediatorLiveData2<R> {
    val result = MediatorLiveData2(transform(getValue()))
    result.addSource(this) { x ->
        result.value = transform(x)
    }
    return result
}

@Suppress("UNCHECKED_CAST")
open class MediatorLiveData2<T>(initialValue: T) : MediatorLiveData<T>() {
    init {
        value = initialValue
    }

    override fun getValue(): T = super.getValue() as T

    @UiThread
    override fun setValue(value: T) = super.setValue(value)

    @AnyThread
    override fun postValue(value: T) = super.postValue(value)
}
