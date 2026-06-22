package com.example.healthyme.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthyme.R
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_HYDRATION = "hydration_channel"
        const val CHANNEL_SLEEP = "sleep_channel"
        const val CHANNEL_MEDICINE = "medicine_channel"

        const val NOTIF_ID_HYDRATION = 1001
        const val NOTIF_ID_SLEEP = 1002
        const val NOTIF_ID_MEDICINE = 1003

        const val ACTION_HYDRATION_ALARM = "com.example.healthyme.action.HYDRATION_ALARM"
        const val ACTION_SLEEP_ALARM = "com.example.healthyme.action.SLEEP_ALARM"
        const val ACTION_MEDICINE_ALARM = "com.example.healthyme.action.MEDICINE_ALARM"

        const val REQUEST_CODE_MEDICINE = 2001
        const val REQUEST_CODE_SLEEP = 3001
        private const val MAX_MEDICINE_REMINDERS = 10
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannel() {
        // For backward compatibility, delegate to createChannels
        createChannels()
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }
            val sleepChannel = NotificationChannel(
                CHANNEL_SLEEP,
                "Sleep Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for bedtime"
            }
            val medicineChannel = NotificationChannel(
                CHANNEL_MEDICINE,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take medicine"
            }
            manager.createNotificationChannels(listOf(hydrationChannel, sleepChannel, medicineChannel))
        }
    }

    fun showHydrationNotification() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, CHANNEL_HYDRATION)
            .setContentTitle("Stay Hydrated!")
            .setContentText("💧 Time to drink water!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIF_ID_HYDRATION, notif)
    }

    fun showSleepReminder() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(
            context,
            NOTIF_ID_SLEEP,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SLEEP)
            .setContentTitle("Time for Bed!")
            .setContentText("😴 It's bedtime! Get some rest!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        manager.notify(NOTIF_ID_SLEEP, notification)
    }

    fun showMedicineReminder() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(
            context,
            NOTIF_ID_MEDICINE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MEDICINE)
            .setContentTitle("Medicine Reminder")
            .setContentText("💊 Time to take your medicine!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        manager.notify(NOTIF_ID_MEDICINE, notification)
    }

    fun scheduleHydrationAlarm(intervalMinutes: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = ACTION_HYDRATION_ALARM
        }
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes.toLong())
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            TimeUnit.MINUTES.toMillis(intervalMinutes.toLong()),
            pending
        )
    }

    fun cancelHydrationAlarm() {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = ACTION_HYDRATION_ALARM
        }
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pending)
    }

    fun scheduleMedicineReminders(times: List<LocalTime>) {
        cancelMedicineReminders()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()

        times.forEachIndexed { index, time ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_MEDICINE_ALARM
            }

            val requestCode = REQUEST_CODE_MEDICINE + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            calendar.apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    fun cancelMedicineReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (i in 0 until MAX_MEDICINE_REMINDERS) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_MEDICINE_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MEDICINE + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleSleepReminder(time: LocalTime) {
        cancelSleepReminder()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SLEEP_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SLEEP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelSleepReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SLEEP_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SLEEP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
