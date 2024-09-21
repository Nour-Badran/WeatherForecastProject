package com.example.mvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm.databinding.FragmentSettingsBinding
import com.example.mvvm.model.SettingsLocalDataSource
import com.example.mvvm.model.SettingsRepository
import com.example.mvvm.viewmodel.SettingsViewModel
import com.example.mvvm.viewmodel.SettingsViewModelFactory

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize the repository
        repository = SettingsRepository(SettingsLocalDataSource(requireContext()))
        // Initialize the ViewModel using the factory
        val factory = SettingsViewModelFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        // Observe ViewModel LiveData and set up listeners for UI components
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.location.observe(viewLifecycleOwner) { location ->
            setDefaultSelection(binding.locationRadioGroup, location)
        }

        viewModel.language.observe(viewLifecycleOwner) { language ->
            setDefaultSelection(binding.languageRadioGroup, language)
        }

        viewModel.windSpeed.observe(viewLifecycleOwner) { windSpeed ->
            setDefaultSelection(binding.windSpeedRadioGroup, windSpeed)
        }

        viewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            setDefaultSelection(binding.temperatureRadioGroup, temperature)
        }

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.notificationsSwitch.isChecked = enabled
        }

        // Set listeners for UI components
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateLocation(selectedButton.text.toString())
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateLanguage(selectedButton.text.toString())
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateWindSpeed(selectedButton.text.toString())
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = binding.root.findViewById<RadioButton>(checkedId)
            viewModel.updateTemperature(selectedButton.text.toString())
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }
    }

    private fun setDefaultSelection(group: RadioGroup, value: String?) {
        value?.let {
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.text.toString() == value) {
                    radioButton.isChecked = true
                    break
                }
            }
        }
    }
}
