package com.example.mvvm.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mvvm.viewmodel.HomeViewModel
import com.example.mvvm.R
import com.example.mvvm.capitalizeFirstLetter
import com.example.mvvm.setIcon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupUI(view)
        observeWeatherData()
        observeDaily()
        observeHourly()
        // Fetch location and weather data

        swipeRefreshLayout.setOnRefreshListener {
            getCurrentLocation { lat, lon ->
                if (lat != null && lon != null) {
                    viewModel.fetchForecastData(
                        lat,
                        lon,
                        "477840c0a8b416725948f965ee5450ec",
                        "metric"
                    )
                    viewModel.fetchWeatherData(
                        lat,
                        lon,
                        "477840c0a8b416725948f965ee5450ec",
                        "metric"
                    )
                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT)
                        .show()
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentLocation { lat, lon ->
            if (lat != null && lon != null) {
                viewModel.fetchForecastData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
                viewModel.fetchWeatherData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeWeatherData()

    }
    private fun setupUI(view: View) {
        forecastAdapter = WeatherForecastAdapter()
        view.findViewById<RecyclerView>(R.id.rv_daily_forecast).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }
        hourlyAdapter = HourlyForecastAdapter()
        view.findViewById<RecyclerView>(R.id.rv_hourly_forecast).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }
    }
    private fun observeDaily() {
        lifecycleScope.launch {
            viewModel.dailyWeather.collect { dailyWeatherList ->
                if (dailyWeatherList.isNotEmpty()) {
                    val updatedList = dailyWeatherList.drop(1).take(5)
                    forecastAdapter.submitList(updatedList)
                }
            }
        }
    }

    private fun observeHourly() {
        lifecycleScope.launch {
            viewModel.hourlyWeather.collect { hourlyWeatherList ->
                val limitedHourlyWeatherList = hourlyWeatherList.take(15)
                hourlyAdapter.submitList(limitedHourlyWeatherList)
            }
        }
    }

    private fun observeWeatherData() {
        lifecycleScope.launch {
            viewModel.weatherData.collect { weather ->
                if (weather != null) {
                    val date = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(weather.dt * 1000L))

                    view?.findViewById<TextView>(R.id.tv_date)?.text = date
                    view?.findViewById<TextView>(R.id.tv_city_name)?.text = weather.cityName
                    view?.findViewById<TextView>(R.id.tv_temperature)?.text = "${weather.temperature}°C"
                    view?.findViewById<TextView>(R.id.tv_visibility)?.text = "Visibility: ${weather.visibility} m"

                    val weatherDescription = weather.description
                    val capitalizedDescription = capitalizeFirstLetter(weatherDescription)

                    view?.findViewById<TextView>(R.id.tv_weather_description)?.text = capitalizedDescription
                    view?.findViewById<TextView>(R.id.tv_humidity)?.text = "Humidity: ${weather.humidity}%"
                    view?.findViewById<TextView>(R.id.tv_wind_speed)?.text = "Wind Speed: ${weather.windSpeed} m/s"
                    view?.findViewById<TextView>(R.id.tv_pressure)?.text = "Pressure: ${weather.pressure} hPa"
                    view?.findViewById<TextView>(R.id.tv_clouds)?.text = "Clouds: ${weather.clouds}%"

                    val weatherIconView = view?.findViewById<ImageView>(R.id.weather_icon)
                    weatherIconView?.setImageResource(setIcon(weather.icon))
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
                // Request a fresh location update if lastLocation is null
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, get the location
                getCurrentLocation { lat, lon ->
                    if (lat != null && lon != null) {
                        viewModel.fetchWeatherData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Permission denied, handle appropriately
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
