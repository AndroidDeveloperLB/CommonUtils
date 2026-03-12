package com.lb.common_utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

val uiHandler = Handler(Looper.getMainLooper())

fun isUiThread() =
        Looper.getMainLooper().isCurrentThread

@AnyThread
fun runAndWaitForUiThread(runnable: Runnable) {
    if (isUiThread()) {
        runnable.run()
        return
    }
    val countDownLatch = CountDownLatch(1)
    uiHandler.post {
        runnable.run()
        countDownLatch.countDown()
    }
    countDownLatch.await()
}

@SuppressLint("WrongThread")
@AnyThread
fun <T> MutableLiveData<T>.setValueAndWait(value: T) {
    if (isUiThread())
        setValue(value)
    else runAndWaitForUiThread { postValue(value) }
}


@SuppressLint("WrongThread")
@AnyThread
fun <T> MutableLiveData2<T>.setValueAndWait(value: T) {
    if (isUiThread())
        setValue(value)
    else runAndWaitForUiThread { postValue(value) }
}

fun interface ResultCallback<T> {
    fun getResult(): T
}

fun <T> runOnUiThreadWithResult(callback: ResultCallback<T>): T {
    if (isUiThread())
        return callback.getResult()
    val countDownLatch = CountDownLatch(1)
    val resultRef = AtomicReference<T>()
    uiHandler.post {
        resultRef.set(callback.getResult())
        countDownLatch.countDown()
    }
    countDownLatch.await()
    return resultRef.get()
}

object ThreadEx {
}
