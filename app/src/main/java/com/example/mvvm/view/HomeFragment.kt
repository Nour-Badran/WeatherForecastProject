package com.example.mvvm.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mvvm.viewmodel.HomeViewModel
import com.example.mvvm.R
import com.example.mvvm.databinding.HomeFragmentBinding
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.model.SettingsLocalDataSource
import com.example.mvvm.model.SettingsRepository
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import com.example.mvvm.utilities.capitalizeFirstLetter
import com.example.mvvm.utilities.setIcon
import com.example.mvvm.viewmodel.HomeViewModelFactory
import com.example.mvvm.viewmodel.SettingsViewModel
import com.example.mvvm.viewmodel.SettingsViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var binding: HomeFragmentBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var hourlyAdapter: HourlyForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        val view = inflater.inflate(R.layout.home_fragment, container, false)
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

// Create SettingsViewModelFactory using the settings repository
        val settingsFactory = SettingsViewModelFactory(requireActivity().application, settingsRepository)
        settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupUI()
        observeWeatherData()
        observeDaily()
        observeHourly()
        // Fetch location and weather data
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchWeatherData()
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
        getCurrentLocation { lat, lon ->
            if (lat != null && lon != null) {
                viewModel.fetchForecastData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
                viewModel.fetchWeatherData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
            hideOverallLoading()
        }
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
        forecastAdapter = WeatherForecastAdapter()
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }

        hourlyAdapter = HourlyForecastAdapter()
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
                    val date = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(it.dt * 1000L))

                    binding.tvDate.text = date
                    binding.tvCityName.text = it.cityName
                    binding.tvTemperature.text = "${it.temperature}°C"
                    binding.tvVisibility.text = "Visibility: ${it.visibility} m"

                    val weatherDescription = weather.description
                    val capitalizedDescription = capitalizeFirstLetter(weatherDescription)

                    binding.tvWeatherDescription.text = capitalizedDescription
                    binding.tvHumidity.text = "Humidity: ${it.humidity}%"
                    binding.tvWindSpeed.text = "Wind Speed: ${it.windSpeed} m/s"
                    binding.tvPressure.text = "Pressure: ${it.pressure} hPa"
                    binding.tvClouds.text = "Clouds: ${it.clouds}%"

                    binding.weatherIcon.setImageResource(setIcon(it.icon))
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
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
