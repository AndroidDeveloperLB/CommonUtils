package com.lb.common_utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.UiThread

/**Same as the internal class of androidx.lifecycle.EmptyActivityLifecycleCallbacks, but public https://issuetracker.google.com/issues/207842543*/
open class EmptyActivityLifecycleCallbacksEx : Application.ActivityLifecycleCallbacks {
    @UiThread
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    @UiThread
    override fun onActivityStarted(activity: Activity) {
    }

    @UiThread
    override fun onActivityResumed(activity: Activity) {
    }

    @UiThread
    override fun onActivityPaused(activity: Activity) {
    }

    @UiThread
    override fun onActivityStopped(activity: Activity) {
    }

    @UiThread
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    @UiThread
    override fun onActivityDestroyed(activity: Activity) {
    }
}
