package com.example.healthyme.ui.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.healthyme.R
import com.example.healthyme.data.HabitRepository
import com.example.healthyme.databinding.FragmentHomeBinding
import java.time.LocalDate

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Load user profile data
        updateProfileSection()

        // Update today's progress
        updateProgressForDate(LocalDate.now())

        // Set up profile button click
        binding.profileContainer.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment)
        }

        // Register for habit updates
        requireActivity().registerReceiver(
            habitUpdateReceiver,
            IntentFilter("com.example.healthyme.ACTION_UPDATE_WIDGET"),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data and progress when returning to the screen
        updateProfileSection()
        updateProgressForDate(LocalDate.now())
    }

    private fun updateProfileSection() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)

        // Load user data
        val userName = sharedPreferences.getString("user_name", "") ?: ""
        val profilePicUri = sharedPreferences.getString("profile_picture_uri", "")

        // Set username
        binding.tvUsername.text = userName

        // Load profile image using Glide
        Glide.with(requireContext())
            .load(profilePicUri.takeIf { !it.isNullOrEmpty() } ?: R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.ivProfile)
    }

    fun updateProgressForDate(date: LocalDate) {
        val repository = HabitRepository.getInstance(requireContext())
        val habits = repository.getHabits()
        val habitsForDate = habits.filter { it.startDate <= date.toEpochDay() }
        val completed = habitsForDate.count { (it.completionHistory[date] ?: 0) > 0 }
        val total = habitsForDate.size

        val percent = if (total > 0) (completed.toFloat() / total.toFloat() * 100).toInt() else 0
        binding.progressBar.progress = percent
        binding.tvProgressPercent.text = "$percent%"
    }

    private val habitUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.healthyme.ACTION_UPDATE_WIDGET") {
                updateProgressForDate(LocalDate.now())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireActivity().unregisterReceiver(habitUpdateReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        _binding = null
    }
}
