package com.example.healthyme.data

import android.content.Context
import android.content.SharedPreferences
import com.example.healthyme.models.Habit
import com.example.healthyme.models.Mood
import com.example.healthyme.utils.LocalDateAdapter
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class HabitRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _moods = MutableStateFlow<List<Mood>>(emptyList())
    val moods: StateFlow<List<Mood>> = _moods.asStateFlow()

    init {
        loadHabits()
        loadMoods()
    }

    private fun loadHabits() {
        val habitsJson = prefs.getString(KEY_HABITS, null)
        if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                _habits.value = gson.fromJson(habitsJson, type) ?: emptyList()
            } catch (e: JsonSyntaxException) {
                _habits.value = emptyList()
                prefs.edit().remove(KEY_HABITS).apply()
            }
        } else {
            _habits.value = emptyList()
        }
    }

    private fun loadMoods() {
        val moodsJson = prefs.getString(KEY_MOODS, null)
        if (moodsJson != null) {
            try {
                val type = object : TypeToken<List<Mood>>() {}.type
                _moods.value = gson.fromJson(moodsJson, type) ?: emptyList()
            } catch (e: JsonSyntaxException) {
                _moods.value = emptyList()
                prefs.edit().remove(KEY_MOODS).apply()
            }
        } else {
            _moods.value = emptyList()
        }
    }

    private suspend fun saveHabits(habits: List<Habit>) = withContext(Dispatchers.IO) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, habitsJson).apply()
        _habits.value = habits
    }

    private suspend fun saveMoods(moods: List<Mood>) = withContext(Dispatchers.IO) {
        val moodsJson = gson.toJson(moods)
        prefs.edit().putString(KEY_MOODS, moodsJson).apply()
        _moods.value = moods
    }

    // Habit Operations
    suspend fun addHabit(habit: Habit) {
        val currentHabits = _habits.value.toMutableList()
        currentHabits.add(habit)
        saveHabits(currentHabits)
    }

    suspend fun updateHabit(updatedHabit: Habit) {
        val currentHabits = _habits.value.toMutableList()
        val index = currentHabits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            currentHabits[index] = updatedHabit
            saveHabits(currentHabits)
        }
    }

    suspend fun deleteHabit(habitId: String) {
        val currentHabits = _habits.value.toMutableList()
        currentHabits.removeAll { it.id == habitId }
        saveHabits(currentHabits)
    }

    suspend fun toggleHabitCompletion(habitId: String, date: LocalDate) {
        val currentHabits = _habits.value.toMutableList()
        val index = currentHabits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = currentHabits[index]
            val newHistory = habit.completionHistory.toMutableMap()
            val currentCount = newHistory[date] ?: 0
            newHistory[date] = if (currentCount > 0) 0 else 1
            currentHabits[index] = habit.copy(completionHistory = newHistory)
            saveHabits(currentHabits)
        }
    }

    fun getHabits(): List<Habit> = _habits.value

    // Mood Operations
    suspend fun addMood(mood: Mood) {
        val currentMoods = _moods.value.toMutableList()
        currentMoods.add(mood)
        saveMoods(currentMoods)
    }

    suspend fun updateMood(updatedMood: Mood) {
        val currentMoods = _moods.value.toMutableList()
        val index = currentMoods.indexOfFirst { it.id == updatedMood.id }
        if (index != -1) {
            currentMoods[index] = updatedMood
            saveMoods(currentMoods)
        }
    }

    suspend fun deleteMood(moodId: String) {
        val currentMoods = _moods.value.toMutableList()
        currentMoods.removeAll { it.id == moodId }
        saveMoods(currentMoods)
    }

    companion object {
        private const val PREFS_NAME = "healthy_me_habits"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOODS = "moods"

        @Volatile
        private var INSTANCE: HabitRepository? = null

        fun getInstance(context: Context): HabitRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
