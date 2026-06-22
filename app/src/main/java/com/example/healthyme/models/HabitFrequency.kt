package com.example.healthyme.models

import java.time.DayOfWeek

sealed class HabitFrequency {
    data class Daily(val timesPerDay: Int = 1) : HabitFrequency()
    data class Weekly(val daysOfWeek: Set<DayOfWeek>) : HabitFrequency()
    data class Custom(
        val interval: Int, // every X days
        val timesPerOccurrence: Int = 1
    ) : HabitFrequency()

    fun toJson(): String = when (this) {
        is Daily -> """{"type":"daily","timesPerDay":$timesPerDay}"""
        is Weekly -> """{"type":"weekly","days":"${daysOfWeek.joinToString(",")}"}"""
        is Custom -> """{"type":"custom","interval":$interval,"timesPerOccurrence":$timesPerOccurrence}"""
    }

    companion object {
        fun fromJson(json: String): HabitFrequency {
            // Simple JSON parsing for SharedPreferences storage
            return when {
                json.contains("\"type\":\"daily\"") -> {
                    val times = Regex("\"timesPerDay\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    Daily(times)
                }
                json.contains("\"type\":\"weekly\"") -> {
                    val daysStr = Regex("\"days\":\"([\\w,]+)\"").find(json)?.groupValues?.get(1) ?: ""
                    val days = daysStr.split(",").mapNotNull { day ->
                        try { DayOfWeek.valueOf(day) } catch (e: Exception) { null }
                    }.toSet()
                    Weekly(days)
                }
                json.contains("\"type\":\"custom\"") -> {
                    val interval = Regex("\"interval\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    val times = Regex("\"timesPerOccurrence\":(\\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                    Custom(interval, times)
                }
                else -> Daily()
            }
        }
    }
}