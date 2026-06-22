package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.models.Mood
import com.example.healthyme.models.MoodState
import java.time.format.DateTimeFormatter

class MoodAdapter(
    private val moods: MutableList<Mood>,
    private val onEdit: (Mood) -> Unit,
    private val onDelete: (Mood) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount() = moods.size

    fun updateMoods(newMoods: List<Mood>) {
        moods.clear()
        moods.addAll(newMoods)
        notifyDataSetChanged()
    }

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        private val moodNote: TextView = itemView.findViewById(R.id.tv_mood_note)
        private val moodDate: TextView = itemView.findViewById(R.id.tv_mood_date)
        private val menuButton: ImageButton = itemView.findViewById(R.id.btn_mood_menu)

        fun bind(mood: Mood) {
            moodEmoji.text = mood.moodState.emoji
            moodNote.text = mood.note
            moodDate.text = mood.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

            menuButton.setOnClickListener { showMenu(it, mood) }
        }

        private fun showMenu(anchor: View, mood: Mood) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_mood_item, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_mood -> onEdit(mood)
                    R.id.action_delete_mood -> onDelete(mood)
                }
                true
            }
            popup.show()
        }
    }
}
