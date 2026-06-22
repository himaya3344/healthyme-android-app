package com.example.healthyme.calendar

import android.view.View
import android.widget.TextView
import com.example.healthyme.R
import com.google.android.material.card.MaterialCardView
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayViewContainer(view: View) : ViewContainer(view) {
    private val dateText: TextView = view.findViewById(R.id.dateText)
    private val dayText: TextView = view.findViewById(R.id.dayText)
    private val dateCard: MaterialCardView = view.findViewById(R.id.dateCard)

    fun bind(day: WeekDay, isSelected: Boolean) {
        dateText.text = day.date.format(DateTimeFormatter.ofPattern("d"))
        dayText.text = day.date.format(DateTimeFormatter.ofPattern("EEE"))
        
        dateCard.isChecked = isSelected
        dateCard.strokeWidth = if (isSelected) 2 else 0
        
        // Highlight today
        if (day.date == LocalDate.now()) {
            dateCard.setCardBackgroundColor(view.context.getColor(R.color.primaryVariant))
            dateText.setTextColor(view.context.getColor(R.color.white))
            dayText.setTextColor(view.context.getColor(R.color.white))
        } else {
            dateCard.setCardBackgroundColor(view.context.getColor(R.color.white))
            dateText.setTextColor(view.context.getColor(R.color.textPrimary))
            dayText.setTextColor(view.context.getColor(R.color.textPrimary))
        }
    }
}
