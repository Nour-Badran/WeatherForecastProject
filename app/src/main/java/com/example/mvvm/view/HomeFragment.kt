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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.viewmodel.HomeViewModel
import com.example.mvvm.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupUI(view)
        observeWeatherData()

        // Fetch location and weather data
        getCurrentLocation { lat, lon ->
            if (lat != null && lon != null) {
                viewModel.fetchWeatherData(lat, lon, "477840c0a8b416725948f965ee5450ec", "metric")
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun setupUI(view: View) {
        forecastAdapter = WeatherForecastAdapter()
        view.findViewById<RecyclerView>(R.id.rv_weather_forecast).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = forecastAdapter
        }
    }

    private fun observeWeatherData() {
        viewModel.weatherData.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                view?.findViewById<TextView>(R.id.tv_city_name)?.text = weather.cityName
                view?.findViewById<TextView>(R.id.tv_temperature)?.text = "${weather.temperature}°C"
                view?.findViewById<TextView>(R.id.tv_weather_description)?.text =
                    weather.description
                view?.findViewById<TextView>(R.id.tv_humidity)?.text =
                    "Humidity: ${weather.humidity}%"
                view?.findViewById<TextView>(R.id.tv_wind_speed)?.text =
                    "Wind Speed: ${weather.windSpeed} m/s"
                view?.findViewById<TextView>(R.id.tv_pressure)?.text =
                    "Pressure: ${weather.pressure} hPa"
                view?.findViewById<TextView>(R.id.tv_clouds)?.text = "Clouds: ${weather.clouds}%"

                val weatherIconView = view?.findViewById<ImageView>(R.id.weather_icon)
                weatherIconView?.setImageResource(weather.iconResId)

                forecastAdapter.submitList(weather.forecast)
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
            // Request location permissions
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
                callback(null, null)
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
