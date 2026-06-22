package com.example.healthyme.models

import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val description: String,
    val category: HabitCategory = HabitCategory.OTHER,
    val startDate: Long = LocalDate.now().toEpochDay(), // Store as epoch day for easy comparison
    val isRecurring: Boolean = false,
    val repeatInterval: String? = null, // e.g., "daily", "weekly", "monthly"
    val completionHistory: Map<LocalDate, Int> = emptyMap(),
    var currentStreak: Int = 0,
    val note: String? = null,
    val lastUpdated: LocalDate? = null,
    val isArchived: Boolean = false // Added isArchived
)
