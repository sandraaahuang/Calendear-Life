package com.sandra.calendearlife.calendar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class CountdownWorker (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Notification.countdownNotify()

        return Result.success()
    }
}

class ReminderWorker (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Notification.reminderNotify()

        return Result.success()
    }
}

class TestWorker (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Log.d("sandraaa", "testtttttttttttt")

        return Result.success()
    }
}

class MyBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {

        }
    }
}