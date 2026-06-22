package com.example.healthyme.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Mood(
    val id: String,
    val date: LocalDate,
    val moodState: MoodState,
    val note: String? = null
) : Parcelable
