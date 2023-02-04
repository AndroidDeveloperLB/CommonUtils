package com.lb.common_utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**Same as the internal class of androidx.lifecycle.EmptyActivityLifecycleCallbacks, but public https://issuetracker.google.com/issues/207842543*/
open class EmptyActivityLifecycleCallbacksEx : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
