package com.example.healthyme.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.adapters.EnhancedHabitAdapter
import com.example.healthyme.calendar.DayViewContainer
import com.example.healthyme.data.HabitRepository
import com.example.healthyme.models.Habit
import com.example.healthyme.models.HabitCategory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class HabitFragment : Fragment() {
    private lateinit var adapter: EnhancedHabitAdapter
    private lateinit var repository: HabitRepository
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedCategory: String? = "All" // Default to All category

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = HabitRepository.getInstance(requireContext())

        setupRecyclerView(view)
        setupCalendarView(view)
        setupCategoryTabs(view)

        // Initial load of habits
        lifecycleScope.launch {
            repository.habits.collect { habits ->
                updateUI(habits)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_habit).setOnClickListener {
            showAddDialog()
        }
    }

    private fun updateUI(habits: List<Habit>) {
        if (::adapter.isInitialized) {
            // Filter habits by selected date
            val dateFilteredHabits = habits.filter { habit ->
                // Include if the habit's start date is on or before the selected date
                habit.startDate <= selectedDate.toEpochDay() &&
                // And if it's a recurring habit that should appear on this date
                (!habit.isRecurring || shouldShowRecurringHabit(habit, selectedDate))
            }

            // Filter by category if not "All"
            val categoryFilteredHabits = if (selectedCategory == "All") {
                dateFilteredHabits
            } else {
                dateFilteredHabits.filter { it.category.displayName == selectedCategory }
            }

            adapter.updateData(categoryFilteredHabits, selectedDate, selectedCategory)
        }
    }

    private fun shouldShowRecurringHabit(habit: Habit, date: LocalDate): Boolean {
        val habitStartDate = LocalDate.ofEpochDay(habit.startDate)
        return when (habit.repeatInterval) {
            "Daily" -> true
            "Weekly" -> habitStartDate.dayOfWeek == date.dayOfWeek
            "Monthly" -> habitStartDate.dayOfMonth == date.dayOfMonth
            else -> false
        }
    }

    private fun setupRecyclerView(view: View) {
        val rvHabits = view.findViewById<RecyclerView>(R.id.rv_habits)
        rvHabits.layoutManager = LinearLayoutManager(context)

        // Initialize adapter with filtered data
        adapter = EnhancedHabitAdapter(object : EnhancedHabitAdapter.HabitInteractionListener {
            override fun onHabitChecked(habit: Habit, isChecked: Boolean) {
                lifecycleScope.launch {
                    repository.toggleHabitCompletion(habit.id, selectedDate)
                }
            }

            override fun onHabitClicked(habit: Habit) {
                showEditDialog(habit)
            }

            override fun onHabitDelete(habit: Habit) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete '${habit.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            repository.deleteHabit(habit.id)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            override fun onShare(habit: Habit) {
                if (!habit.note.isNullOrBlank()) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${habit.name}: ${habit.note}")
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                }
            }

            override fun onAddNote(habit: Habit) {
                showNoteDialog(habit)
            }
        })

        rvHabits.adapter = adapter
    }

    private fun setupCalendarView(view: View) {
        val calendarView = view.findViewById<WeekCalendarView>(R.id.weekCalendarView)

        calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.bind(data, data.date == selectedDate)
                container.view.setOnClickListener {
                    if (selectedDate != data.date) {
                        val oldDate = selectedDate
                        selectedDate = data.date
                        calendarView.notifyDateChanged(oldDate)
                        calendarView.notifyDateChanged(data.date)
                        updateUI(repository.habits.value)
                    }
                }
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        calendarView.setup(startMonth.atDay(1), endMonth.atDay(endMonth.lengthOfMonth()), daysOfWeek().first())
        calendarView.scrollToDate(LocalDate.now())
    }

    private fun setupCategoryTabs(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.categoryChipGroup)

        addFilterChip(chipGroup, "All", "All")

        HabitCategory.values().forEach { category ->
            addFilterChip(chipGroup, category.displayName, "${category.emoji} ${category.displayName}")
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedCategory = if (checkedId == View.NO_ID) "All" else group.findViewById<Chip>(checkedId)?.tag as? String
            updateUI(repository.habits.value)
        }
    }

    private fun addFilterChip(chipGroup: ChipGroup, tag: String?, text: String) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.tag = tag
        chip.isCheckable = true
        if (tag == "All") chip.isChecked = true
        chipGroup.addView(chip)
    }

    private fun showAddDialog() {
        val context = requireContext()
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val etName = layout.findViewById<EditText>(R.id.et_habit_name)
        val etDescription = layout.findViewById<EditText>(R.id.et_habit_target)
        val categorySpinner = layout.findViewById<Spinner>(R.id.spinner_category)
        val recurringCheckbox = layout.findViewById<CheckBox>(R.id.cb_recurring)
        val repeatSpinner = layout.findViewById<Spinner>(R.id.spinner_repeat)

        // Category Spinner
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, HabitCategory.values().map { "${it.emoji} ${it.displayName}" })
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Repeat Spinner
        val repeatAdapter = ArrayAdapter.createFromResource(context, R.array.repeat_options, android.R.layout.simple_spinner_item)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatSpinner.adapter = repeatAdapter

        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            repeatSpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        AlertDialog.Builder(context)
            .setTitle("Add Habit")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val description = etDescription.text.toString()
                if (name.isNotBlank()) {
                    val selectedCategory = HabitCategory.values()[categorySpinner.selectedItemPosition]
                    val isRecurring = recurringCheckbox.isChecked
                    val repeatInterval = if (isRecurring) repeatSpinner.selectedItem.toString() else null

                    val habit = Habit(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        category = selectedCategory,
                        startDate = selectedDate.toEpochDay(),
                        isRecurring = isRecurring,
                        repeatInterval = repeatInterval
                    )
                    lifecycleScope.launch {
                        repository.addHabit(habit)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoteDialog(habit: Habit) {
        val context = requireContext()
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_add_mood, null)
        val etNote = layout.findViewById<EditText>(R.id.et_mood_note)

        etNote.setText(habit.note)

        AlertDialog.Builder(context)
            .setTitle("Add Note")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val note = etNote.text.toString()
                lifecycleScope.launch {
                    repository.updateHabit(habit.copy(
                        note = if (note.isBlank()) null else note,
                        lastUpdated = LocalDate.now()
                    ))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(habit: Habit) {
        val context = requireContext()
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val etName = layout.findViewById<EditText>(R.id.et_habit_name)
        val etDescription = layout.findViewById<EditText>(R.id.et_habit_target)
        val categorySpinner = layout.findViewById<Spinner>(R.id.spinner_category)
        val recurringCheckbox = layout.findViewById<CheckBox>(R.id.cb_recurring)
        val repeatSpinner = layout.findViewById<Spinner>(R.id.spinner_repeat)

        etName.setText(habit.name)
        etDescription.setText(habit.description)

        // Category Spinner
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, HabitCategory.values().map { "${it.emoji} ${it.displayName}" })
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(habit.category.ordinal)

        // Repeat Spinner
        val repeatAdapter = ArrayAdapter.createFromResource(context, R.array.repeat_options, android.R.layout.simple_spinner_item)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatSpinner.adapter = repeatAdapter
        habit.repeatInterval?.let {
            val position = (repeatSpinner.adapter as ArrayAdapter<String>).getPosition(it)
            repeatSpinner.setSelection(position)
        }

        recurringCheckbox.isChecked = habit.isRecurring
        repeatSpinner.visibility = if (habit.isRecurring) View.VISIBLE else View.GONE

        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            repeatSpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Habit")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString()
                val description = etDescription.text.toString()
                if (name.isNotBlank()) {
                    val selectedCategory = HabitCategory.values()[categorySpinner.selectedItemPosition]
                    val isRecurring = recurringCheckbox.isChecked
                    val repeatInterval = if (isRecurring) repeatSpinner.selectedItem.toString() else null

                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        category = selectedCategory,
                        isRecurring = isRecurring,
                        repeatInterval = repeatInterval,
                        lastUpdated = LocalDate.now()
                    )
                    lifecycleScope.launch {
                        repository.updateHabit(updatedHabit)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
