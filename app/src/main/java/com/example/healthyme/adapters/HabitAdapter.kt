package com.example.healthyme.adapters

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
import java.time.format.DateTimeFormatter

class HabitAdapter(
    private val items: MutableList<Habit>,
    private val listener: Listener,
    private var currentDate: LocalDate = LocalDate.now() // Add current date
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface Listener {
        fun onToggleCompleted(habit: Habit, position: Int)
        fun onEdit(habit: Habit, position: Int)
        fun onDelete(habit: Habit, position: Int)
        fun onShare(habit: Habit)
        fun onAddNote(habit: Habit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = items[position]
        holder.bind(habit)
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = (habit.completionHistory[currentDate] ?: 0) > 0 // Correct completion check
        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            listener.onToggleCompleted(habit, position)
        }

        holder.btnShare.setOnClickListener { listener.onShare(habit) }

        holder.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_habit_item, popup.menu)
            popup.menu.add(0, R.id.action_add_note, 3, "Add Note")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> listener.onEdit(habit, position)
                    R.id.action_delete -> listener.onDelete(habit, position)
                    R.id.action_add_note -> listener.onAddNote(habit)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Habit>, date: LocalDate) {
        currentDate = date
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cb_completed)
        val tvName: TextView = itemView.findViewById(R.id.tv_habit_name)
        val tvTarget: TextView = itemView.findViewById(R.id.tv_habit_target)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvNote: TextView = itemView.findViewById(R.id.tv_note)
        val btnShare: ImageButton = itemView.findViewById(R.id.btn_share)
        val btnMore: ImageButton = itemView.findViewById(R.id.btn_more)

        fun bind(habit: Habit) {
            tvName.text = "${habit.category.emoji} ${habit.name}"
            // Correctly display habit schedule
            tvTarget.text = if (habit.isRecurring) {
                habit.repeatInterval ?: "Recurring"
            } else {
                "One-time"
            }

            if (habit.currentStreak > 0) {
                tvTarget.append(" 🔥 ${habit.currentStreak}")
            }

            // Show date if available
            tvDate.text = habit.lastUpdated?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: ""

            // Show/hide note if available
            if (!habit.note.isNullOrBlank()) {
                tvNote.visibility = View.VISIBLE
                tvNote.text = habit.note
            } else {
                tvNote.visibility = View.GONE
            }

            // Show share button if has note
            btnShare.visibility = if (!habit.note.isNullOrBlank()) View.VISIBLE else View.GONE

            cbCompleted.isChecked = (habit.completionHistory[currentDate] ?: 0) > 0 // Correct completion check
        }
    }
}
