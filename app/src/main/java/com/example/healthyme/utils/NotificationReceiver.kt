package com.example.healthyme.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)
        helper.createChannels()
        
        when(intent.action) {
            NotificationHelper.ACTION_MEDICINE_ALARM -> helper.showMedicineReminder()
            NotificationHelper.ACTION_SLEEP_ALARM -> helper.showSleepReminder()
        }
    }
}