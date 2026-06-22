package com.example.healthyme.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.healthyme.R
import com.example.healthyme.data.HabitRepository
import com.example.healthyme.models.Habit
import com.example.healthyme.MainActivity // Corrected import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.healthyme.ACTION_UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = HabitRepository.getInstance(context)

            CoroutineScope(Dispatchers.Main).launch {
                val habits = repository.getHabits()
                val views = RemoteViews(context.packageName, R.layout.widget_habit)
                views.removeAllViews(R.id.widget_habit_list)

                // Set up the intent that starts the main activity
                val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                    .let { intent ->
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    }
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)


                val today = LocalDate.now()
                val todaysHabits = habits.filter { it.startDate <= today.toEpochDay() && (!it.isRecurring || shouldShowRecurringHabit(it, today)) }

                for (habit in todaysHabits) {
                    val habitView = RemoteViews(context.packageName, R.layout.item_widget_habit)
                    habitView.setTextViewText(R.id.widget_habit_name, "${habit.category.emoji} ${habit.name}")

                    // Correctly check if the habit is completed for the current date
                    val isCompleted = (habit.completionHistory[today] ?: 0) > 0
                    habitView.setImageViewResource(R.id.widget_habit_checkbox, if (isCompleted) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
                    views.addView(R.id.widget_habit_list, habitView)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun shouldShowRecurringHabit(habit: Habit, date: LocalDate): Boolean {
            val habitStartDate = LocalDate.ofEpochDay(habit.startDate)
            return when (habit.repeatInterval) {
                "Daily" -> true
                "Weekly" -> habitStartDate.dayOfWeek == date.dayOfWeek
                "Monthly" -> habitStartDate.dayOfMonth == date.dayOfMonth
                else -> false
            }
        }
    }
}
