package com.example.healthyme.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.healthyme.models.Habit
import com.example.healthyme.models.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalTime

class SharedPreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("healthyme_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "prefs_habits"
        private const val KEY_MOODS = "prefs_moods"
        private const val KEY_HYDRATION_INTERVAL_MIN = "prefs_hydration_interval"
        private const val KEY_MEDICINE_TIMES = "prefs_medicine_times"
        private const val KEY_MEDICINE_REMINDER_ENABLED = "prefs_medicine_reminder_enabled"
        private const val KEY_SLEEP_TIME = "prefs_sleep_time"
        private const val KEY_SLEEP_REMINDER_ENABLED = "prefs_sleep_reminder_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "prefs_onboarding_completed"
    }

    // Habits
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Moods
    fun saveMoods(moods: List<Mood>) {
        val json = gson.toJson(moods)
        prefs.edit().putString(KEY_MOODS, json).apply()
    }

    fun loadMoods(): MutableList<Mood> {
        val json = prefs.getString(KEY_MOODS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Mood>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Hydration interval
    fun setHydrationIntervalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_HYDRATION_INTERVAL_MIN, minutes).apply()
    }

    fun getHydrationIntervalMinutes(): Int {
        return prefs.getInt(KEY_HYDRATION_INTERVAL_MIN, 120)
    }

    // Medicine Reminders
    fun setMedicineTimes(times: List<LocalTime>) {
        val timeStrings = times.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_MEDICINE_TIMES, timeStrings).apply()
    }

    fun getMedicineTimes(): List<LocalTime> {
        val timeStrings = prefs.getStringSet(KEY_MEDICINE_TIMES, emptySet()) ?: emptySet()
        return timeStrings.map { LocalTime.parse(it) }.sorted()
    }

    fun setMedicineReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MEDICINE_REMINDER_ENABLED, enabled).apply()
    }

    fun getMedicineReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_MEDICINE_REMINDER_ENABLED, false)
    }

    // Sleep Reminders
    fun setSleepTime(time: LocalTime) {
        prefs.edit().putString(KEY_SLEEP_TIME, time.toString()).apply()
    }

    fun getSleepTime(): LocalTime? {
        val timeString = prefs.getString(KEY_SLEEP_TIME, null)
        return timeString?.let { LocalTime.parse(it) }
    }

    fun setSleepReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SLEEP_REMINDER_ENABLED, enabled).apply()
    }

    fun getSleepReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_SLEEP_REMINDER_ENABLED, false)
    }

    // Onboarding
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}
