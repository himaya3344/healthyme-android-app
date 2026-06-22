package com.example.healthyme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.healthyme.R

class OnboardingFragment : Fragment() {
    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt(ARG_POSITION) ?: 0

        val illustrationImage = view.findViewById<ImageView>(R.id.illustrationImage)
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val descriptionText = view.findViewById<TextView>(R.id.descriptionText)

        when (position) {
            0 -> {
                titleText.text = "Welcome to HealthyMe"
                descriptionText.text = "Build healthier habits, stay on track, and take care of your mind and body."
                illustrationImage.setImageResource(R.drawable.ic_launcher_foreground) // Replace with actual welcome illustration
            }
            1 -> {
                titleText.text = "Track & Remind"
                descriptionText.text = "Track your steps, sleep, workouts, and get gentle reminders for hydration, sleep, and medicine."
                illustrationImage.setImageResource(R.drawable.ic_launcher_foreground) // Replace with actual tracking illustration
            }
            2 -> {
                titleText.text = "Mood & Insights"
                descriptionText.text = "Log your moods and see daily summaries with easy-to-understand charts."
                illustrationImage.setImageResource(R.drawable.ic_launcher_foreground) // Replace with actual insights illustration
            }
        }
    }
}