package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MedicineTimeAdapter(
    private val times: MutableList<LocalTime>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MedicineTimeAdapter.ViewHolder>() {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(times[position], position)
    }

    override fun getItemCount() = times.size

    fun getTimes(): MutableList<LocalTime> {
        return times
    }

    fun updateTimes(newTimes: List<LocalTime>) {
        times.clear()
        times.addAll(newTimes)
        notifyDataSetChanged()
    }

    fun addTime(time: LocalTime) {
        times.add(time)
        notifyItemInserted(times.size - 1)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.tv_time)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(time: LocalTime, position: Int) {
            timeText.text = time.format(timeFormatter)
            deleteButton.setOnClickListener { onDelete(position) }
        }
    }
}