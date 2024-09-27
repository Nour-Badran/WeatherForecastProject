package com.example.mvvm.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mvvm.AlarmReceiver
import com.example.mvvm.databinding.FragmentAlertBinding // Make sure to import the correct binding class
import java.util.*

class AlertFragment : Fragment() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var binding: FragmentAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val radioGroupAlertType: RadioGroup = binding.radioGroupAlertType
        binding.btnSetAlert.setOnClickListener {
            val selectedAlertType = view.findViewById<RadioButton>(radioGroupAlertType.checkedRadioButtonId)

            if (selectedAlertType != null) {
                setAlert(selectedAlertType.text.toString())
            } else {
                Toast.makeText(requireContext(), "Please select an alert type", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancelAlert.setOnClickListener {
            cancelAlert()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlert(alertType: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALERT_TYPE", if (alertType == "Alarm") "ALARM" else "NOTIFICATION")
        }

        pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use Calendar to set the alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.datePicker.year)
            set(Calendar.MONTH, binding.datePicker.month)
            set(Calendar.DAY_OF_MONTH, binding.datePicker.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, binding.startTimePicker.hour)
            set(Calendar.MINUTE, binding.startTimePicker.minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(requireContext(), "Alert set for ${binding.startTimePicker.hour}:${binding.startTimePicker.minute}", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAlert() {
        if (::pendingIntent.isInitialized) {
            alarmManager.cancel(pendingIntent)
            Toast.makeText(requireContext(), "Alert canceled", Toast.LENGTH_SHORT).show()
        }
    }
}
