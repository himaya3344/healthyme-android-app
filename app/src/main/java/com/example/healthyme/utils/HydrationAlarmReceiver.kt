package com.example.healthyme.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver invoked by AlarmManager to show hydration notification.
 */
class HydrationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val helper = NotificationHelper(context)
        helper.createChannel()
        helper.showHydrationNotification()
    }
}