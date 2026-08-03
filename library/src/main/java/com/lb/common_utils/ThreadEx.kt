package com.lb.common_utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.os.HandlerCompat
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

val uiHandler = Handler(Looper.getMainLooper())

fun isUiThread() =
        Looper.getMainLooper().isCurrentThread

fun interface UiRunnable : Runnable {
    @UiThread
    override fun run()
}

fun interface WorkerRunnable : Runnable {
    @WorkerThread
    override fun run()
}

/**if on UI thread, performs the runnable right away. If not, posts it to the UI thread, without waiting for it*/
fun runOnUiThread(runnable: UiRunnable) {
    if (isUiThread()) runnable.run()
    else uiHandler.post(runnable)
}

fun Handler.postDelayedWithToken(runnable: Runnable, token: Any?, delayMillis: Long) =
        HandlerCompat.postDelayed(this, runnable, token, delayMillis)

@AnyThread
fun runAndWaitForUiThread(runnable: UiRunnable) {
    if (isUiThread()) {
        @SuppressLint("ThreadConstraint")
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

@AnyThread
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

