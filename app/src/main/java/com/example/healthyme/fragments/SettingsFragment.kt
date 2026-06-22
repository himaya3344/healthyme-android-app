package com.example.healthyme.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.adapters.MedicineTimeAdapter
import com.example.healthyme.utils.NotificationHelper
import com.example.healthyme.utils.SharedPreferencesHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.LocalTime
import java.util.Calendar

class SettingsFragment : Fragment() {
    private lateinit var prefs: SharedPreferencesHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var medicineTimeAdapter: MedicineTimeAdapter
    private lateinit var medicineSwitch: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPreferencesHelper(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        setupMedicineReminders(view)
        setupHydrationReminder(view)
        setupSleepReminder(view)
    }

    private fun setupSleepReminder(view: View) {
        val sleepSwitch = view.findViewById<SwitchMaterial>(R.id.switch_sleep)
        val tvSleepTime = view.findViewById<TextView>(R.id.tv_sleep_time)

        // Set initial state
        sleepSwitch.isChecked = prefs.getSleepReminderEnabled()
        prefs.getSleepTime()?.let { time ->
            tvSleepTime.text = "Bedtime: ${formatTime(time)}"
        }

        // Handle sleep time selection
        tvSleepTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    val selectedTime = LocalTime.of(selectedHour, selectedMinute)
                    prefs.setSleepTime(selectedTime)
                    tvSleepTime.text = "Bedtime: ${formatTime(selectedTime)}"
                    if (sleepSwitch.isChecked) {
                        notificationHelper.scheduleSleepReminder(selectedTime)
                    }
                },
                currentHour,
                currentMinute,
                false
            ).show()
        }

        // Handle switch changes
        sleepSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setSleepReminderEnabled(isChecked)
            if (isChecked) {
                prefs.getSleepTime()?.let { time ->
                    notificationHelper.scheduleSleepReminder(time)
                } ?: run {
                    // If no time is set, show time picker
                    tvSleepTime.performClick()
                }
            } else {
                notificationHelper.cancelSleepReminder()
            }
        }
    }

    private fun formatTime(time: LocalTime): String {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
    }

    private fun setupMedicineReminders(view: View) {
        val rvMedicineTimes = view.findViewById<RecyclerView>(R.id.rv_medicine_times)
        medicineSwitch = view.findViewById(R.id.switch_medicine)
        val btnAddMedicineTime = view.findViewById<MaterialButton>(R.id.btn_add_medicine_time)

        val savedTimes = prefs.getMedicineTimes()

        medicineTimeAdapter = MedicineTimeAdapter(savedTimes.toMutableList()) { position ->
            val times = medicineTimeAdapter.getTimes()
            times.removeAt(position)
            prefs.setMedicineTimes(times)
            medicineTimeAdapter.notifyItemRemoved(position)
            updateMedicineNotifications()
        }

        rvMedicineTimes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = medicineTimeAdapter
        }

        medicineSwitch.isChecked = prefs.getMedicineReminderEnabled()
        medicineSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setMedicineReminderEnabled(isChecked)
            updateMedicineNotifications()
        }

        btnAddMedicineTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val newTime = LocalTime.of(selectedHour, selectedMinute)
                medicineTimeAdapter.addTime(newTime)
                prefs.setMedicineTimes(medicineTimeAdapter.getTimes())
                updateMedicineNotifications()
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun updateMedicineNotifications() {
        if (medicineSwitch.isChecked) {
            val times = medicineTimeAdapter.getTimes()
            notificationHelper.scheduleMedicineReminders(times)
        } else {
            notificationHelper.cancelMedicineReminders()
        }
    }

    private fun setupHydrationReminder(view: View) {
        val etInterval = view.findViewById<EditText>(R.id.et_hydration_interval)
        val btnSave = view.findViewById<Button>(R.id.btn_save_hydration)
        etInterval.setText(prefs.getHydrationIntervalMinutes().toString())

        btnSave.setOnClickListener {
            val intervalText = etInterval.text.toString()
            if (intervalText.isNotBlank()) {
                try {
                    val interval = intervalText.toInt()
                    if (interval > 0) {
                        prefs.setHydrationIntervalMinutes(interval)

                        notificationHelper.cancelHydrationAlarm()
                        notificationHelper.createChannel()
                        notificationHelper.scheduleHydrationAlarm(interval)

                        Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Interval must be positive", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
