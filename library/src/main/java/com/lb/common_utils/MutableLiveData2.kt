package com.lb.common_utils

import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData

//https://proandroiddev.com/improving-livedata-nullability-in-kotlin-45751a2bafb7
@Suppress("UNCHECKED_CAST")
open class MutableLiveData2<T>(value: T) : LiveData<T>(value) {

    override fun getValue(): T = super.getValue() as T

    @UiThread
    public override fun setValue(value: T) = super.setValue(value)

    @AnyThread
    public override fun postValue(value: T) = super.postValue(value)
}
