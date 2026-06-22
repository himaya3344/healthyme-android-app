package com.example.healthyme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.healthyme.R
import com.example.healthyme.models.Mood
import com.example.healthyme.models.MoodState
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.LocalDate
import java.util.UUID

class AddMoodDialogFragment : DialogFragment() {

    private var listener: MoodDialogListener? = null
    private var existingMood: Mood? = null

    interface MoodDialogListener {
        fun onMoodSaved(mood: Mood)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingBar = view.findViewById<RatingBar>(R.id.mood_rating_bar)
        val noteEditText = view.findViewById<EditText>(R.id.et_mood_note)
        val saveButton = view.findViewById<Button>(R.id.btn_save_mood)
        val cancelButton = view.findViewById<Button>(R.id.btn_cancel_mood)
        val moodNameTextView = view.findViewById<TextView>(R.id.tv_selected_mood_name)
        val suggestionChipGroup = view.findViewById<ChipGroup>(R.id.mood_suggestion_chip_group)

        existingMood = arguments?.getParcelable(ARG_MOOD)
        existingMood?.let {
            ratingBar.rating = it.moodState.rating.toFloat()
            noteEditText.setText(it.note)
            moodNameTextView.text = it.moodState.displayName
            updateSuggestionChips(suggestionChipGroup, it.moodState, noteEditText)
        }

        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            val moodState = MoodState.fromRating(rating.toInt())
            moodNameTextView.text = moodState.displayName
            updateSuggestionChips(suggestionChipGroup, moodState, noteEditText)
        }

        saveButton.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val moodState = MoodState.fromRating(rating)
            val note = noteEditText.text.toString()

            val mood = existingMood?.copy(
                moodState = moodState,
                note = note,
                date = LocalDate.now() // Always update date on edit
            ) ?: Mood(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now(),
                moodState = moodState,
                note = note
            )

            listener?.onMoodSaved(mood)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSuggestionChips(chipGroup: ChipGroup, moodState: MoodState, noteEditText: EditText) {
        chipGroup.removeAllViews()
        val suggestions = getSuggestionsForMood(moodState)
        suggestions.forEach { suggestion ->
            val chip = Chip(context)
            chip.text = suggestion
            chip.setOnClickListener {
                val currentText = noteEditText.text.toString()
                noteEditText.setText(if (currentText.isEmpty()) suggestion else "$currentText, $suggestion")
            }
            chipGroup.addView(chip)
        }
    }

    private fun getSuggestionsForMood(moodState: MoodState): List<String> {
        return when (moodState) {
            MoodState.GREAT -> listOf("Productive day", "Feeling energized", "Achieved a goal", "Spent time with loved ones")
            MoodState.GOOD -> listOf("Feeling happy", "Good weather", "Relaxing", "Enjoyed a meal")
            MoodState.OKAY -> listOf("Just a normal day", "Feeling neutral", "A bit tired")
            MoodState.BAD -> listOf("Feeling sad", "Stressed", "Didn\'t sleep well", "Headache")
            MoodState.AWFUL -> listOf("Feeling anxious", "Overwhelmed", "Sick", "Tough day at work")
        }
    }

    fun setListener(listener: MoodDialogListener) {
        this.listener = listener
    }

    companion object {
        private const val ARG_MOOD = "arg_mood"

        fun newInstance(mood: Mood? = null): AddMoodDialogFragment {
            val fragment = AddMoodDialogFragment()
            val args = Bundle()
            mood?.let { args.putParcelable(ARG_MOOD, it) }
            fragment.arguments = args
            return fragment
        }
    }
}
