package com.sandra.calendearlife

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sandra.calendearlife.util.Notification


class MyBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {

        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            Notification.countdownNotify()

        }
    }
}