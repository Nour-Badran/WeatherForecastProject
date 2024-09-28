package com.example.mvvm.weather.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvm.R
import com.example.mvvm.databinding.HomeFragmentBinding
import com.example.mvvm.settings.model.SettingsLocalDataSource
import com.example.mvvm.settings.model.SettingsRepository
import com.example.mvvm.networkBrodcastReciever.NetworkUtil
import com.example.mvvm.weather.model.utilities.capitalizeFirstLetter
import com.example.mvvm.weather.model.utilities.customizeSnackbar
import com.example.mvvm.weather.model.utilities.setIcon
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import com.example.mvvm.settings.viewmodel.SettingsViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import android.provider.Settings
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mvvm.weather.viewmodel.ApiState
import com.example.mvvm.weather.viewmodel.HomeViewModel
import com.example.mvvm.weather.viewmodel.HomeViewModelFactory
import com.example.mvvm.weather.model.db.WeatherDatabase
import com.example.mvvm.weather.model.localdatasource.WeatherLocalDataSource
import com.example.mvvm.weather.model.remotedatasource.WeatherRemoteDataSource
import com.example.mvvm.weather.model.repo.WeatherRepository
import java.util.Locale

class HomeFragment : Fragment() {

    lateinit var binding: HomeFragmentBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private var selectedLanguage: String = "en"
    private var selectedTemp: String = "metric"
    private var selectedLocation: String = "GPS"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        val weatherDao = WeatherDatabase.getDatabase(requireActivity().application).weatherDao()
        val weatherRepository = WeatherRepository(
            localDataSource = WeatherLocalDataSource(weatherDao),
            remoteDataSource = WeatherRemoteDataSource()
        )
        val factory = HomeViewModelFactory(requireActivity().application, weatherRepository)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        val settingsLocalDataSource = SettingsLocalDataSource(requireActivity().application)
        val settingsRepository = SettingsRepository(settingsLocalDataSource)

        val settingsFactory = SettingsViewModelFactory(settingsRepository)
        settingsViewModel = ViewModelProvider(requireActivity(), settingsFactory).get(
            SettingsViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupUI()
        observeWeatherData()
        observeDaily()
        observeHourly()
        // Fetch location and weather data
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtil.isNetworkConnected(requireContext()))
            {
                fetchWeatherData()
                observeWeatherData()
                observeDaily()
                observeHourly()
            }
            else
            {
                val snackbar = Snackbar.make(binding.root,R.string.no_network, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().resources.getColor(android.R.color.holo_red_dark))
                    .setTextColor(requireContext().resources.getColor(android.R.color.white))
                snackbar.setDuration(2000)
                customizeSnackbar(snackbar, requireContext())
                snackbar.show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchWeatherData()
    }
    private fun fetchWeatherData() {
        showOverallLoading()

        // Get the selected location method
        val selectedLocationId = settingsViewModel.getLocationId()
        selectedLocation = when (selectedLocationId) {
            R.id.gps_radio_button -> "GPS"
            R.id.map_radio_button -> "Map"
            else -> "GPS"
        }

        // If GPS is selected, fetch the current location
        if (selectedLocation == "GPS") {
            getCurrentLocation { lat, lon ->
                if (lat != null && lon != null) {
                    fetchWeatherAndForecast(lat, lon)
                } else {
                    Toast.makeText(requireContext(), R.string.unable, Toast.LENGTH_SHORT).show()
                }
            }
        }
        // If Map is selected, fetch the stored lat/lon from SharedPreferences
        else {
            val (lat, lon) = settingsViewModel.getLatLon()

            // Check if valid lat/lon values were retrieved
            if (lat != 0.0 && lon != 0.0) {
                fetchWeatherAndForecast(lat, lon)
            } else {
                Toast.makeText(requireContext(), R.string.invalid, Toast.LENGTH_SHORT).show()
            }
        }

        hideOverallLoading()
    }

    private fun fetchWeatherAndForecast(lat: Double, lon: Double) {
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

        // Fetch both weather and forecast data using the latitude and longitude
        viewModel.fetchForecastData(lat, lon, "477840c0a8b416725948f965ee5450ec", selectedTemp, selectedLanguage,false)
        viewModel.fetchWeatherData(lat, lon, "477840c0a8b416725948f965ee5450ec", selectedTemp, selectedLanguage,false)
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
            viewModel.dailyWeather.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> showDailyLoading()
                    is ApiState.Success -> {
                        hideDailyLoading()
                        val dailyWeatherList = apiState.data
                        if (dailyWeatherList.isNotEmpty()) {
                            val updatedList = dailyWeatherList.drop(1).take(5)
                            forecastAdapter.submitList(updatedList)
                        }
                    }
                    is ApiState.Error -> {
                        hideDailyLoading()
                        //Toast.makeText(requireContext(), R.string.daily, Toast.LENGTH_SHORT).show()
                    }
                }
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
            viewModel.hourlyWeather.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> showHourlyLoading()
                    is ApiState.Success -> {
                        hideHourlyLoading()
                        val hourlyWeatherList = apiState.data
                        val limitedHourlyWeatherList = hourlyWeatherList.take(15)
                        hourlyAdapter.submitList(limitedHourlyWeatherList)
                    }
                    is ApiState.Error -> {
                        hideHourlyLoading()
                        //Toast.makeText(requireContext(), R.string.hourly, Toast.LENGTH_SHORT).show()
                    }
                }
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
            viewModel.weatherData.collect { apiState ->
                when (apiState) {
                    is ApiState.Loading -> {
                        showOverallLoading()
                    }
                    is ApiState.Success -> {
                        hideOverallLoading()
                        val weather = apiState.data
                        weather?.let {
                            val timestamp = it.dt * 1000L // Convert seconds to milliseconds
                            val formattedDateTime = SimpleDateFormat("EEE, dd MMM yyyy \nhh:mm a", Locale.getDefault())
                                .format(Date(timestamp))
                            binding.tvDate.text = formattedDateTime
                            binding.tvCityName.text = it.cityName

                            // Determine temperature unit
                            val temperatureUnit = when (selectedTemp) {
                                "metric" -> "C"
                                "imperial" -> "F"
                                "standard" -> "K"
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

                            val weatherDescription = it.description
                            val capitalizedDescription = capitalizeFirstLetter(weatherDescription)
                            binding.tvWeatherDescription.text = capitalizedDescription

                            binding.tvHumidity.text = if (selectedLanguage == "en") {
                                "Humidity: ${numberFormat.format(it.humidity)}%"
                            } else {
                                "الرطوبة: ${numberFormat.format(it.humidity)}%"
                            }

                            val selectedWindSpeedId = settingsViewModel.getWindSpeedId()
                            val windSpeedInMps = it.windSpeed
                            val selectedWindSpeed = when (selectedWindSpeedId) {
                                R.id.meter_sec_radio_button -> "Meters/Second"
                                R.id.mile_hour_radio_button -> "Miles/Hour"
                                else -> "Meters/Second"
                            }

                            val windSpeedToDisplay = if (selectedWindSpeed == "Miles/Hour") {
                                windSpeedInMps * 2.23694 // Convert from m/s to mph
                            } else {
                                windSpeedInMps // Keep the value in m/s
                            }

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
                    is ApiState.Error -> {
                        hideOverallLoading()
                        //Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isLocalDataUsed.collect { isLocal ->
                if (isLocal && isAdded && view != null) {
                    val rootView = requireActivity().window.decorView.findViewById<View>(android.R.id.content)
                    val snackbar2 = Snackbar.make(rootView, R.string.check_snackbar, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                        .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                        .setDuration(2000)

                    customizeSnackbar(snackbar2, requireContext())
                    snackbar2.show()
                }
            }
        }


    }


    private fun getCurrentLocation(callback: (Double?, Double?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location.latitude, location.longitude)
            } else {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10000L
                ).apply {
                    setWaitForAccurateLocation(false)
                    setMinUpdateIntervalMillis(5000L)
                    setMaxUpdateDelayMillis(10000L)
                }.build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            callback(it.latitude, it.longitude)
                        } ?: callback(null, null)
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchWeatherData()
            } else {
                val snackbar = Snackbar.make(
                    requireView(),
                    R.string.location_permission,
                    Snackbar.LENGTH_LONG
                )

                // Customize the text size of the Snackbar message
                val snackbarView = snackbar.view
                val snackbarTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarTextView.textSize = 18f  // Increase text size
                snackbarTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Text color
                snackbarTextView.maxLines = 3 // Allow multiple lines if needed

                // Add action to open app settings
                snackbar.setAction("Settings") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                snackbar.show()
            }
        }
    }

}
