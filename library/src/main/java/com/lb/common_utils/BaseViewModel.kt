package com.lb.common_utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel

/**usage: class MyViewModel(application: Application) : BaseViewModel(application)
 * getting instance:    private lateinit var viewModel: MyViewModel
 * viewModel=ViewModelProvider(this).get(MyViewModel::class.java)*/
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    @Suppress("MemberVisibilityCanBePrivate")
    var isCleared = false
    val onClearedListeners = ArrayList<Runnable>()

    @SuppressLint("StaticFieldLeak")
    @Suppress("LeakingThis")
    val applicationContext: Context = application.applicationContext
    val handler = Handler(Looper.getMainLooper())

    override fun onCleared() {
        super.onCleared()
        isCleared = true
        onClearedListeners.forEach { it.run() }
    }
}
