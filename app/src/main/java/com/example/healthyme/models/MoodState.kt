package com.example.healthyme.models

import com.example.healthyme.R

enum class MoodState(val emoji: String, val displayName: String, val rating: Int, val colorRes: Int) {
    AWFUL("😢", "Awful", 1, R.color.mood_awful),
    BAD("😞", "Bad", 2, R.color.mood_bad),
    OKAY("😐", "Okay", 3, R.color.mood_okay),
    GOOD("😊", "Good", 4, R.color.mood_good),
    GREAT("😁", "Great", 5, R.color.mood_great);

    companion object {
        fun fromRating(rating: Int): MoodState {
            return values().find { it.rating == rating } ?: OKAY
        }
    }
}
