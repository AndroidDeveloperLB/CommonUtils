package com.lb.common_utils

import android.content.Context
import androidx.annotation.*
import androidx.work.*
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**reason to use this: https://issuetracker.google.com/issues/115575872 https://commonsware.com/blog/2018/11/24/workmanager-app-widgets-side-effects.html*/
object WorkerManagerUtils {
    fun interface OnGotWorkerManager {
        @WorkerThread
        fun onGotWorkerManager(workerManager: WorkManager)
    }

    @WorkerThread
    fun getWorkerManager(context: Context, onGotWorkerManager: OnGotWorkerManager) {
        val workManager = WorkManager.getInstance(context)
        val dummyWorkers = workManager.getWorkInfosByTag(DummyWorker.DUMMY_WORKER_TAG).get()
        val hasPendingDummyWorker =
            (dummyWorkers?.indexOfFirst { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING } ?: -1) >= 0
        if (!hasPendingDummyWorker) {
            DummyWorker.schedule(context)
        }
        onGotWorkerManager.onGotWorkerManager(workManager)
    }

    class DummyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
        override fun doWork(): Result {
            schedule(applicationContext)
            return Result.success()
        }

        companion object {
            const val DUMMY_WORKER_TAG = "DummyWorker"

            @AnyThread
            fun schedule(context: Context) {
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.Builder(
                    DummyWorker::class.java).addTag(DUMMY_WORKER_TAG).setInitialDelay(10L * 365L, TimeUnit.DAYS)
                    .build())
            }
        }
    }
}
