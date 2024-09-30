package com.example.mvvm.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mvvm.R
import com.example.mvvm.databinding.FragmentSettingsBinding
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import com.example.mvvm.settings.viewmodel.SettingsViewModelFactory
import com.example.mvvm.settings.model.SettingsLocalDataSource
import com.example.mvvm.settings.model.SettingsRepository

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository
    private var isInitialLoad = true  // Flag to track initial load

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        repository = SettingsRepository(SettingsLocalDataSource(requireContext()))

        val factory = SettingsViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory).get(SettingsViewModel::class.java)

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.location.observe(viewLifecycleOwner) { location ->
            setDefaultSelection(binding.locationRadioGroup, location, viewModel.getLocationId())
        }

        viewModel.language.observe(viewLifecycleOwner) { language ->
            setDefaultSelection(binding.languageRadioGroup, language, viewModel.getLanguageId())
        }

        viewModel.windSpeed.observe(viewLifecycleOwner) { windSpeed ->
            setDefaultSelection(binding.windSpeedRadioGroup, windSpeed, viewModel.getWindSpeedId())
        }

        viewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            setDefaultSelection(binding.temperatureRadioGroup, temperature, viewModel.getTemperatureId())
        }

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { isEnabled ->
            if (isInitialLoad) {
                binding.notificationsSwitch.isChecked = isEnabled
            }
        }

        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isInitialLoad) {
                val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
                if(selectedButton.id != R.id.map_radio_button)
                {
                    viewModel.updateLocation(selectedButton.text.toString(), checkedId)
                }

                if (selectedButton.id == R.id.map_radio_button) {
                    findNavController().navigate(R.id.action_SettingsFragment_to_mapFragmentSettings2)
                }
            }
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateLanguage(selectedButton.text.toString(), checkedId)
            viewModel.updateLanguage(selectedButton.text.toString())
            updateUIForLanguage(selectedButton.text.toString())
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateWindSpeed(selectedButton.text.toString(), checkedId)
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateTemperature(selectedButton.text.toString(), checkedId)
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        binding.root.post {
            isInitialLoad = false
        }
    }

    private fun setDefaultSelection(group: RadioGroup, value: String?, selectedId: Int) {
        value?.let {
            group.check(selectedId)
        }
    }

    private fun updateUIForLanguage(language: String) {
        when (language) {
            "English", "الإنجليزية" -> {
                binding.tvTitle.text = "Settings"
                binding.tvLocation.text = "Location"
                binding.tvLanguage.text = "Language"
                binding.tvWindSpeed.text = "Wind Speed"
                binding.tvTemperature.text = "Temperature"
                binding.tvNotifications.text = "Notifications"
                binding.arabicRadioButton.text = "Arabic"
                binding.englishRadioButton.text = "English"
                binding.gpsRadioButton.text = "GPS"
                binding.mapRadioButton.text = "Map"
                binding.meterSecRadioButton.text = "Meters/Second"
                binding.mileHourRadioButton.text = "Miles/Hour"
                binding.celsiusRadioButton.text = "Celsius"
                binding.kelvinRadioButton.text = "Kelvin"
                binding.fahrenheitRadioButton.text = "Fahrenheit"
                binding.notificationsSwitch.text = "Enable"
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_LTR

            }
            "Arabic", "العربية" -> {
                binding.tvTitle.text = "الإعدادات"
                binding.tvLocation.text = "الموقع"
                binding.tvLanguage.text = "اللغة"
                binding.tvWindSpeed.text = "سرعة الرياح"
                binding.tvTemperature.text = "درجة الحرارة"
                binding.tvNotifications.text = "الإشعارات"
                binding.gpsRadioButton.text = "نظام تحديد المواقع"
                binding.mapRadioButton.text = "خريطة"
                binding.arabicRadioButton.text = "العربية"
                binding.englishRadioButton.text = "الإنجليزية"
                binding.meterSecRadioButton.text = "متر/ثانية"
                binding.mileHourRadioButton.text = "ميل/ساعة"
                binding.celsiusRadioButton.text = "سيلسيوس"
                binding.kelvinRadioButton.text = "كلفن"
                binding.fahrenheitRadioButton.text = "فهرنهايت"
                binding.notificationsSwitch.text = "تفعيل"
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
        }
    }
}

