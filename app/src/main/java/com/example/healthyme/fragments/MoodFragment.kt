package com.example.healthyme.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyme.R
import com.example.healthyme.adapters.MoodAdapter
import com.example.healthyme.data.HabitRepository
import com.example.healthyme.databinding.FragmentMoodBinding
import com.example.healthyme.models.Mood
import com.example.healthyme.models.MoodState
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

class MoodFragment : Fragment(), AddMoodDialogFragment.MoodDialogListener {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodAdapter: MoodAdapter
    private lateinit var repository: HabitRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = HabitRepository.getInstance(requireContext())

        setupPieChart()
        setupRecyclerView()

        binding.fabAddMood.setOnClickListener { addMood() }

        // Observe moods from the repository
        viewLifecycleOwner.lifecycleScope.launch {
            repository.moods.collect { moods ->
                moodAdapter.updateMoods(moods)
                updateMoodChart(moods)
            }
        }
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            mutableListOf(),
            onEdit = { mood -> editMood(mood) },
            onDelete = { mood -> deleteMood(mood) }
        )
        binding.rvMoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }
    }

    private fun setupPieChart() {
        binding.moodPieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            legend.isEnabled = true
        }
    }

    private fun updateMoodChart(moods: List<Mood>) {
        val today = LocalDate.now()
        val todaysMoods = moods.filter { it.date == today }

        val moodCounts = MoodState.values().associateWith { moodState ->
            todaysMoods.count { it.moodState == moodState }.toFloat()
        }

        val entries = moodCounts
            .filter { it.value > 0 }
            .map { (moodState, count) -> PieEntry(count, moodState.emoji) }

        val dataSet = PieDataSet(entries, "Today's Moods").apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = MoodState.values().map { ContextCompat.getColor(requireContext(), it.colorRes) }
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.moodPieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }

        binding.moodPieChart.apply {
            data = pieData
            highlightValues(null)
            invalidate()
            centerText = if (todaysMoods.isEmpty()) "No moods today" else "Today's Moods"
        }
    }

    private fun addMood() {
        val dialog = AddMoodDialogFragment.newInstance()
        dialog.setListener(this)
        dialog.show(parentFragmentManager, "AddMoodDialog")
    }

    private fun editMood(mood: Mood) {
        val dialog = AddMoodDialogFragment.newInstance(mood)
        dialog.setListener(this)
        dialog.show(parentFragmentManager, "EditMoodDialog")
    }

    private fun deleteMood(mood: Mood) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteMood(mood.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onMoodSaved(mood: Mood) {
        lifecycleScope.launch {
            if (repository.moods.value.any { it.id == mood.id }) {
                repository.updateMood(mood)
            } else {
                repository.addMood(mood)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
