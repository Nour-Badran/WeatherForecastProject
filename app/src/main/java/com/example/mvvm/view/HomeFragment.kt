package com.example.mvvm.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.viewmodel.HomeViewModel
import com.example.mvvm.R


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var forecastAdapter: WeatherForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupUI(view)
        observeWeatherData()

        viewModel.fetchWeatherData(40.0,21.0,"477840c0a8b416725948f965ee5450ec","metric")

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

                forecastAdapter.submitList(weather.forecast)
            }
        }
    }
}

