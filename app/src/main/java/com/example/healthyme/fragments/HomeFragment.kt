package com.example.healthyme.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.healthyme.R
import com.example.healthyme.adapters.EnhancedHabitAdapter
import com.example.healthyme.data.HabitRepository
import com.example.healthyme.databinding.FragmentHomeBinding
import com.example.healthyme.models.Habit
import com.example.healthyme.models.HabitFrequency
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: HabitRepository
    private lateinit var habitsAdapter: EnhancedHabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = HabitRepository.getInstance(requireContext())

        // Set up profile section
        setupProfileSection()

        // Set up RecyclerView
        setupRecyclerView()

        // Observe habits from the repository
        viewLifecycleOwner.lifecycleScope.launch {
            repository.habits.collect { allHabits ->
                updateUi(allHabits)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh all data when coming back to the screen
        setupProfileSection()
        viewLifecycleOwner.lifecycleScope.launch {
            updateUi(repository.habits.value)
        }
    }

    private fun setupRecyclerView() {
        habitsAdapter = EnhancedHabitAdapter(object : EnhancedHabitAdapter.HabitInteractionListener {
            override fun onHabitChecked(habit: Habit, isChecked: Boolean) {
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.toggleHabitCompletion(habit.id, LocalDate.now())
                }
            }

            override fun onHabitClicked(habit: Habit) {
                // No edit action from home screen
            }

            override fun onHabitDelete(habit: Habit) {
                // No delete action from home screen
            }
            
            override fun onShare(habit: Habit) {
                // No share action from home screen
            }

            override fun onAddNote(habit: Habit) {
                // No note action from home screen
            }
        })
        binding.rvHabitsHome.layoutManager = LinearLayoutManager(context)
        binding.rvHabitsHome.adapter = habitsAdapter
    }

    private fun setupProfileSection() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        
        // Load user data
        val userName = sharedPreferences.getString("user_name", "") ?: ""
        val profileImageUri = sharedPreferences.getString("user_profile_image_uri", "")

        // Set username
        binding.tvUsername.text = userName

        // Load profile image
        Glide.with(this)
            .load(profileImageUri.takeIf { !it.isNullOrEmpty() } ?: R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.ivProfile)

        // Set click listener for profile section
        binding.profileContainer.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment)
        }
    }

    private fun updateUi(allHabits: List<Habit>) {
        val today = LocalDate.now()
        val todaysHabits = allHabits.filter { isHabitForDate(it, today) }

        // Update adapter and progress
        habitsAdapter.updateData(todaysHabits, today, null)
        updateProgress(todaysHabits, today)
    }

    private fun updateProgress(todaysHabits: List<Habit>, date: LocalDate) {
        if (todaysHabits.isEmpty()) {
            binding.progressBar.progress = 0
            binding.tvProgressPercent.text = "0%"
            return
        }

        val completed = todaysHabits.count { (it.completionHistory[date] ?: 0) > 0 }
        val percent = (completed * 100) / todaysHabits.size
        binding.progressBar.progress = percent
        binding.tvProgressPercent.text = "$percent%"
    }

    private fun isHabitForDate(habit: Habit, date: LocalDate): Boolean {
        val habitStartDate = LocalDate.ofEpochDay(habit.startDate)
        // Always include habits created today
        if (habitStartDate == date) return true

        // Don't show habits created in the future
        if (habitStartDate.isAfter(date)) return false

        return when (habit.repeatInterval) {
            "Daily" -> true
            "Weekly" -> habitStartDate.dayOfWeek == date.dayOfWeek
            "Monthly" -> habitStartDate.dayOfMonth == date.dayOfMonth
            else -> !habit.isRecurring // If not recurring, only show on start date
        }
    }
}
