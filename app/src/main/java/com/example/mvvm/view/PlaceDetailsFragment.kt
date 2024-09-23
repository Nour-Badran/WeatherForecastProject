package com.example.mvvm.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.databinding.FragmentPlaceDetailsBinding
import com.example.mvvm.databinding.HomeFragmentBinding
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.model.SettingsLocalDataSource
import com.example.mvvm.model.SettingsRepository
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.NetworkUtil
import com.example.mvvm.network.WeatherRemoteDataSource
import com.example.mvvm.utilities.capitalizeFirstLetter
import com.example.mvvm.utilities.customizeSnackbar
import com.example.mvvm.utilities.setIcon
import com.example.mvvm.viewmodel.HomeViewModel
import com.example.mvvm.viewmodel.HomeViewModelFactory
import com.example.mvvm.viewmodel.SettingsViewModel
import com.example.mvvm.viewmodel.SettingsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PlaceDetailsFragment : Fragment() {

    lateinit var binding: FragmentPlaceDetailsBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private var selectedLanguage: String = "en"
    private var selectedTemp: String = "metric"
    private var selectedWindSpeed: String = "Meters/Second"
    lateinit var place: FavoritePlaces
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_placeDetailsFragment_to_FavouritesFragment)
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        val weatherDao = WeatherDatabase.getDatabase(requireActivity().application).weatherDao()
        val weatherRepository = WeatherRepository(
            context = requireActivity().application,
            localDataSource = WeatherLocalDataSource(weatherDao),
            remoteDataSource = WeatherRemoteDataSource()
        )
        val factory = HomeViewModelFactory(requireActivity().application, weatherRepository)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        val settingsLocalDataSource = SettingsLocalDataSource(requireActivity().application)
        val settingsRepository = SettingsRepository(settingsLocalDataSource)

        val settingsFactory = SettingsViewModelFactory(requireActivity().application, settingsRepository)
        settingsViewModel = ViewModelProvider(requireActivity(), settingsFactory).get(SettingsViewModel::class.java)
        setupUI()
        observeWeatherData()
        observeDaily()
        observeHourly()
        // Fetch location and weather data
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtil.isNetworkConnected(requireContext()))
            {
                fetchWeatherData()
            }
            else
            {
                val snackbar = Snackbar.make(binding.root, "No Network", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().resources.getColor(android.R.color.holo_red_dark))
                    .setTextColor(requireContext().resources.getColor(android.R.color.white))
                snackbar.setDuration(4000)
                customizeSnackbar(snackbar, requireContext())
                snackbar.show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
        // Retrieve the passed argument using Safe Args
        place = PlaceDetailsFragmentArgs.fromBundle(requireArguments()).favouritePlace

        //Toast.makeText(requireContext(), place.cityName, Toast.LENGTH_SHORT).show()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchWeatherData()
    }
    private fun fetchWeatherData() {
        showOverallLoading()
        // Fetch the language setting based on the selected RadioButton ID
        val selectedLanguageId = settingsViewModel.getLanguageId()
        selectedLanguage = when (selectedLanguageId) {
            R.id.arabic_radio_button -> "ar"
            R.id.english_radio_button -> "en"
            else -> "null"
        }
        val selectedTempId = settingsViewModel.getTemperatureId()
        selectedTemp = when (selectedTempId) {
            R.id.celsius_radio_button -> "metric"
            R.id.kelvin_radio_button -> "standard"
            R.id.fahrenheit_radio_button -> "imperial"
            else -> "null"
        }
        viewModel.fetchForecastData(place.lat, place.lon, "477840c0a8b416725948f965ee5450ec", selectedTemp, selectedLanguage,true)
        viewModel.fetchWeatherData(place.lat, place.lon, "477840c0a8b416725948f965ee5450ec", selectedTemp, selectedLanguage,true)

        hideOverallLoading()
    }

    private fun showOverallLoading() {
        binding.progressBarWeather.visibility = View.VISIBLE
    }

    private fun hideOverallLoading() {
        binding.progressBarWeather.visibility = View.GONE
    }
    override fun onResume() {
        super.onResume()
        observeWeatherData()

    }
    private fun setupUI() {
        val selectedLanguageId = settingsViewModel.getLanguageId()
        selectedLanguage = when (selectedLanguageId) {
            R.id.arabic_radio_button -> "ar"
            R.id.english_radio_button -> "en"
            else -> "null"
        }

        val selectedTempId = settingsViewModel.getTemperatureId()
        selectedTemp = when (selectedTempId) {
            R.id.celsius_radio_button -> "metric"
            R.id.kelvin_radio_button -> "standard"
            R.id.fahrenheit_radio_button -> "imperial"
            else -> "null"
        }
        forecastAdapter = WeatherForecastAdapter(selectedLanguage,selectedTemp)
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }

        hourlyAdapter = HourlyForecastAdapter(selectedLanguage,selectedTemp)
        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
    }
    private fun observeDaily() {
        lifecycleScope.launch {
            viewModel.dailyWeather.collect { dailyWeatherList ->
                showDailyLoading()
                if (dailyWeatherList.isNotEmpty()) {
                    val updatedList = dailyWeatherList.drop(1).take(5)
                    forecastAdapter.submitList(updatedList)
                }
                hideDailyLoading()
            }
        }
    }

    private fun showDailyLoading() {
        binding.progressBarDaily.visibility = View.VISIBLE
    }

    private fun hideDailyLoading() {
        binding.progressBarDaily.visibility = View.GONE
    }

    private fun observeHourly() {
        lifecycleScope.launch {
            viewModel.hourlyWeather.collect { hourlyWeatherList ->
                showHourlyLoading()
                val limitedHourlyWeatherList = hourlyWeatherList.take(15)
                hourlyAdapter.submitList(limitedHourlyWeatherList)
                hideHourlyLoading()
            }
        }
    }

    private fun showHourlyLoading() {
        binding.progressBarHourly.visibility = View.VISIBLE
    }

    private fun hideHourlyLoading() {
        binding.progressBarHourly.visibility = View.GONE
    }
    private fun observeWeatherData() {
        lifecycleScope.launch {
            viewModel.weatherData.collect { weather ->
                weather?.let {
                    val currentDateTime = java.text.SimpleDateFormat("EEE, dd MMM yyyy \nhh:mm a", java.util.Locale.getDefault())
                        .format(java.util.Date())

                    binding.tvDate.text = currentDateTime


                    binding.tvCityName.text = it.cityName

                    // Determine temperature unit
                    val temperatureUnit = when (selectedTemp) {
                        "metric" -> "C"       // Celsius
                        "imperial" -> "F"     // Fahrenheit
                        "standard" -> "K"     // Kelvin
                        else -> "C"
                    }

                    // Format temperature
                    val temperature = NumberFormat.getInstance(if (selectedLanguage == "ar") Locale("ar") else Locale.ENGLISH)
                        .format(it.temperature.toInt())
                    binding.tvTemperature.text = "$temperature°$temperatureUnit"

                    // Format visibility, humidity, wind speed, pressure, and clouds
                    val locale = if (selectedLanguage == "ar") Locale("ar") else Locale.ENGLISH
                    val numberFormat = NumberFormat.getInstance(locale)

                    binding.tvVisibility.text = if (selectedLanguage == "en") {
                        "Visibility: ${numberFormat.format(it.visibility)} m"
                    } else {
                        "الرؤية: ${numberFormat.format(it.visibility)} م"
                    }

                    val weatherDescription = weather.description
                    val capitalizedDescription = capitalizeFirstLetter(weatherDescription)

                    binding.tvWeatherDescription.text = capitalizedDescription
                    binding.tvHumidity.text = if (selectedLanguage == "en") {
                        "Humidity: ${numberFormat.format(it.humidity)}%"
                    } else {
                        "الرطوبة: ${numberFormat.format(it.humidity)}%"
                    }

                    //////////////////
                    val selectedWindSpeedId = settingsViewModel.getWindSpeedId()
                    val windSpeedInMps = it.windSpeed

                    selectedWindSpeed = when (selectedWindSpeedId) {
                        R.id.meter_sec_radio_button -> "Meters/Second"
                        R.id.mile_hour_radio_button -> "Miles/Hour"
                        else -> "Meters/Second"
                    }

                    // Convert wind speed to miles per hour if "m/h" is selected
                    val windSpeedToDisplay = if (selectedWindSpeed == "Miles/Hour") {
                        windSpeedInMps * 2.23694  // Convert from m/s to mph
                    } else {
                        windSpeedInMps  // Keep the value in m/s
                    }



                    // Set wind speed text dynamically based on language and unit selection
                    binding.tvWindSpeed.text = if (selectedLanguage == "en") {
                        "Wind Speed: ${numberFormat.format(windSpeedToDisplay)} $selectedWindSpeed"
                    } else {
                        "سرعة الرياح: ${numberFormat.format(windSpeedToDisplay)} ${if (selectedWindSpeed == "Meters/Second") "متر/ثانية" else "ميل/ساعة"}"
                    }

                    binding.tvPressure.text = if (selectedLanguage == "en") {
                        "Pressure: ${numberFormat.format(it.pressure)} hPa"
                    } else {
                        "الضغط: ${numberFormat.format(it.pressure)} هكتوباسكال"
                    }

                    binding.tvClouds.text = if (selectedLanguage == "en") {
                        "Clouds: ${numberFormat.format(it.clouds)}%"
                    } else {
                        "الغيوم: ${numberFormat.format(it.clouds)}%"
                    }

                    binding.weatherIcon.setImageResource(setIcon(it.icon))
                }
            }
        }
    }

}
