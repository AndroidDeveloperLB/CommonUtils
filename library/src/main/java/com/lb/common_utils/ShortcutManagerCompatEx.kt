package com.lb.common_utils

import android.content.Context
import android.content.IntentSender
import androidx.annotation.WorkerThread
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat

object ShortcutManagerCompatEx {
    /**this exists only because we don't have an annotation of WorkerThread for the real function of ShortcutManagerCompat.requestPinShortcut, and it's not even documented:
     * https://issuetracker.google.com/issues/484770612*/
    @WorkerThread
    fun requestPinShortcut(context: Context,
                           shortcut: ShortcutInfoCompat, callback: IntentSender?): Boolean {
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, callback)
    }
}
