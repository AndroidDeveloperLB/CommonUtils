package com.lb.common_utils

sealed class StatefulData<T> {
    class Success<T>(val data: T) : StatefulData<T>()
    class Error<T>(val throwable: Throwable? = null) : StatefulData<T>()
    class Loading<T>(val loadingData: Any? = null) : StatefulData<T>()
}
