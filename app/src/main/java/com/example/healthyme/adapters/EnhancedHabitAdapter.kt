package com.example.healthyme.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.models.Habit
import java.time.LocalDate

class EnhancedHabitAdapter(
    private val listener: HabitInteractionListener
) : RecyclerView.Adapter<EnhancedHabitAdapter.ViewHolder>() {

    private var habits: List<Habit> = emptyList()
    private var currentDate: LocalDate = LocalDate.now()
    private var currentCategory: String? = null
    private var context: Context? = null

    fun updateData(newHabits: List<Habit>, date: LocalDate, category: String?) {
        currentDate = date
        currentCategory = category
        habits = when (category) {
            null, "All" -> newHabits
            else -> newHabits.filter { it.category.displayName == category }
        }
        notifyDataSetChanged()

        // Notify about completion status changes
        context?.sendBroadcast(Intent("com.example.healthyme.ACTION_UPDATE_WIDGET"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_enhanced, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.habitCheckbox)
        private val nameText: TextView = itemView.findViewById(R.id.habitName)
        private val descriptionText: TextView = itemView.findViewById(R.id.habitDescription)
        private val streakText: TextView = itemView.findViewById(R.id.streakCount)
        private val noteText: TextView = itemView.findViewById(R.id.tv_note)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btn_share)
        private val menuButton: ImageButton = itemView.findViewById(R.id.btn_more)

        fun bind(habit: Habit) {
            nameText.text = habit.name
            descriptionText.text = "${habit.category.emoji} ${habit.category.displayName}"
            checkbox.isChecked = (habit.completionHistory[currentDate] ?: 0) > 0

            // Show streak if exists
            if (habit.currentStreak > 0) {
                streakText.visibility = View.VISIBLE
                streakText.text = "🔥 ${habit.currentStreak} day streak"
            } else {
                streakText.visibility = View.GONE
            }

            // Show note if exists
            if (!habit.note.isNullOrBlank()) {
                noteText.visibility = View.VISIBLE
                noteText.text = habit.note
            } else {
                noteText.visibility = View.GONE
            }

            // Show share button if note exists and set click listener
            if (!habit.note.isNullOrBlank()) {
                btnShare.visibility = View.VISIBLE
                btnShare.setOnClickListener {
                    listener.onShare(habit)
                }
            } else {
                btnShare.visibility = View.GONE
                btnShare.setOnClickListener(null)
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                listener.onHabitChecked(habit, isChecked)
            }

            itemView.setOnClickListener {
                listener.onHabitClicked(habit)
            }

            menuButton.setOnClickListener { view ->
                showPopupMenu(view, habit)
            }
        }

        private fun showPopupMenu(view: View, habit: Habit) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_habit_item, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        listener.onHabitClicked(habit) // Reusing onHabitClicked for edit
                        true
                    }
                    R.id.action_delete -> {
                        listener.onHabitDelete(habit)
                        true
                    }
                    R.id.action_add_note -> {
                        listener.onAddNote(habit)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    interface HabitInteractionListener {
        fun onHabitChecked(habit: Habit, isChecked: Boolean)
        fun onHabitClicked(habit: Habit)
        fun onHabitDelete(habit: Habit)
        fun onShare(habit: Habit)
        fun onAddNote(habit: Habit)
    }
}
