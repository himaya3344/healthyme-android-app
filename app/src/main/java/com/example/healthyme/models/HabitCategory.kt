package com.example.healthyme.models

enum class HabitCategory(val emoji: String, val displayName: String) {
    HEALTH("🏥", "Health"),
    FITNESS("💪", "Fitness"),
    MINDFULNESS("🧘", "Mindfulness"),
    LEARNING("📚", "Learning"),
    NUTRITION("🥗", "Nutrition"),
    SLEEP("😴", "Sleep"),
    SOCIAL("👥", "Social"),
    PRODUCTIVITY("⚡", "Productivity"),
    OTHER("📌", "Other");

    companion object {
        fun fromString(value: String): HabitCategory = 
            values().find { it.name == value } ?: OTHER
    }
}