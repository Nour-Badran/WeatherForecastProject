package com.example.mvvm.alert.view

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.alert.reciever.AlarmReceiver
import com.example.mvvm.alert.model.AlertEntity
import com.example.mvvm.alert.model.AlertRepository
import com.example.mvvm.alert.AlertViewModel.AlertViewModel
import com.example.mvvm.alert.AlertViewModel.AlertViewModelFactory
import com.example.mvvm.databinding.FragmentAlertBinding
import com.example.mvvm.weather.model.db.WeatherDatabase
import com.example.mvvm.weather.viewmodel.ApiState
import kotlinx.coroutines.launch
import java.util.*

class AlertFragment : Fragment() {

    private lateinit var binding: FragmentAlertBinding

    companion object {
        const val REQUEST_OVERLAY_PERMISSION_CODE = 1001
    }

    private lateinit var database: WeatherDatabase
    private lateinit var viewModel: AlertViewModel

    private lateinit var alertAdapter: AlertAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        binding.datePicker.minDate = System.currentTimeMillis() - 1000

        database = WeatherDatabase.getDatabase(requireContext())

        val factory = AlertViewModelFactory(AlertRepository(database.weatherDao(),requireContext()))
        viewModel = ViewModelProvider(requireActivity(), factory).get(AlertViewModel::class.java)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alerts.collect { alerts ->
                    when (alerts) {
                        is ApiState.Loading->{}
                        is ApiState.Success->{
                            alertAdapter.submitList(alerts.data)
                            //updateUI(alerts.data.isEmpty())
                        }
                        else ->{}
                    }
                }
            }
        }

        binding.btnSetAlert.setOnClickListener {
            val selectedAlertType = view.findViewById<RadioButton>(binding.radioGroupAlertType.checkedRadioButtonId)

            if (selectedAlertType != null) {
                if (selectedAlertType.text.toString() == "Alarm" || selectedAlertType.text.toString() == "منبه") {
                    requestOverlayPermission()
                } else {
                    setAlert("NOTIFICATION")
                }
            } else {
                Toast.makeText(requireContext(), R.string.please_select_alert_type, Toast.LENGTH_SHORT).show()
            }
        }

        binding.fabShowAlert.setOnClickListener {
            if (binding.alertScrollView.visibility == View.GONE) {
                binding.alertScrollView.visibility = View.VISIBLE
                binding.recyclerViewAlerts.visibility = View.GONE
                binding.alert.visibility = View.GONE
                binding.fabShowAlert.setImageResource(R.drawable.close_24dp_e8eaed_fill0_wght400_grad0_opsz24)
            } else {
                binding.alertScrollView.visibility = View.GONE
                binding.alert.visibility = View.VISIBLE
                binding.recyclerViewAlerts.visibility = View.VISIBLE
                binding.fabShowAlert.setImageResource(R.drawable.add_24dp_e8eaed_fill0_wght400_grad0_opsz24)
            }
        }

        viewModel.loadAlerts()
    }

    private fun setupRecyclerView() {
        alertAdapter = AlertAdapter { alert ->
            cancelAlert(alert)
            //updateUi()
        }

        binding.recyclerViewAlerts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alertAdapter
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION_CODE)
        } else {
            setAlert("ALARM")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION_CODE) {
            if (Settings.canDrawOverlays(requireContext())) {
                setAlert("ALARM")
            } else {
                Toast.makeText(requireContext(), R.string.overlay_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlert(alertType: String) {
        val calendar = Calendar.getInstance().apply {
            set(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                binding.startTimePicker.hour,
                binding.startTimePicker.minute
            )
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(
                requireContext(),
                R.string.selected_time_passed,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val endTimeCalendar = Calendar.getInstance().apply {
            set(
                binding.datePicker.year,
                binding.datePicker.month,
                binding.datePicker.dayOfMonth,
                binding.endTimePicker.hour,
                binding.endTimePicker.minute
            )
        }

        if (endTimeCalendar.timeInMillis <= calendar.timeInMillis) {
            Toast.makeText(
                requireContext(),
                R.string.end_time_after_start_time,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val alert = AlertEntity(
            id = createID(),
            alertType = alertType,
            year = binding.datePicker.year,
            month = binding.datePicker.month,
            day = binding.datePicker.dayOfMonth,
            hour = binding.startTimePicker.hour,
            minute = binding.startTimePicker.minute,
            isActive = true
        )

        val delay = endTimeCalendar.timeInMillis - calendar.timeInMillis

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALERT_TYPE", alertType)
            putExtra("ID", alert.id)
            putExtra("DELAY", delay)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alert.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        viewModel.setAlert(alert, pendingIntent)

        val hourFormatted = String.format("%02d", binding.startTimePicker.hour)
        val minuteFormatted = String.format("%02d", binding.startTimePicker.minute)

        Toast.makeText(
            requireContext(),
            getString(R.string.alert_set_for, hourFormatted, minuteFormatted),
            Toast.LENGTH_SHORT
        ).show()


    }


    fun createID():Int{
        var id =requireActivity().getSharedPreferences("nextid", MODE_PRIVATE).getInt("nextid",0)
        id++
        if (id==Int.MAX_VALUE){
            id=0
        }
        requireActivity().getSharedPreferences("nextid", MODE_PRIVATE).edit().putInt("nextid",id+1).apply()
        return id
    }

    private fun cancelAlert(alert: AlertEntity) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alert.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        viewModel.cancelAlert(pendingIntent, alert.id)
        Toast.makeText(requireContext(), R.string.alert_canceled, Toast.LENGTH_SHORT).show()
    }
//    private fun updateUI(isListEmpty: Boolean) {
//        if (isListEmpty) {
//            binding.recyclerViewAlerts.visibility= View.GONE
//            binding.emptyView.visibility = View.VISIBLE
//        } else {
//            binding.recyclerViewAlerts.visibility = View.VISIBLE
//            binding.emptyView.visibility = View.GONE
//        }
//    }
}
