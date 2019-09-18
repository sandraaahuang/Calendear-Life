package com.sandra.calendearlife.calendar.notification

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotifyWorker (appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Notification.reminderNotify()

        return Result.success()
    }
}