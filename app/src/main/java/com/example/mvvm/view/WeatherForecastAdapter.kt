package com.example.mvvm.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.R
import com.example.mvvm.utilities.capitalizeFirstLetter
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.utilities.setIcon
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class WeatherForecastAdapter(private val selectedLanguage: String,private val selectedTemp: String) :
    ListAdapter<DailyWeather, WeatherForecastAdapter.WeatherViewHolder>(ForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(getItem(position), selectedLanguage,selectedTemp)
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(forecast: DailyWeather, selectedLanguage: String,tempUnit: String) {
            // Convert Unix timestamp to date
            val dateText = if (adapterPosition == 0) {
                if (selectedLanguage == "ar") "غداً" else "Tomorrow"
            } else {
                val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", if (selectedLanguage == "ar") Locale("ar") else Locale.ENGLISH)
                dateFormat.format(java.util.Date(forecast.day * 1000L))
            }

            val weatherDescription = forecast.weatherStatus
            val capitalizedDescription = capitalizeFirstLetter(weatherDescription)

            itemView.findViewById<TextView>(R.id.tv_forecast_date).text = dateText
            itemView.findViewById<TextView>(R.id.tv_weather_status).text = capitalizedDescription

            // Format max and min temperatures
            val numberFormat = NumberFormat.getInstance(if (selectedLanguage == "ar") Locale("ar") else Locale.ENGLISH)
            val maxTemp = numberFormat.format(forecast.maxTemp.toInt())
            val minTemp = numberFormat.format(forecast.minTemp.toInt())

            val temperatureUnit = when (tempUnit) {
                "metric" -> "C"       // Celsius
                "imperial" -> "F"     // Fahrenheit
                "standard" -> "K"     // Kelvin
                else -> "C"
            }

            itemView.findViewById<TextView>(R.id.tv_max_temp).text = "${if (selectedLanguage == "ar") "الحد الأقصى" else "Max"} $maxTemp°$temperatureUnit"
            itemView.findViewById<TextView>(R.id.tv_min_temp).text = "${if (selectedLanguage == "ar") "الحد الأدنى" else "Min"} $minTemp°$temperatureUnit"

            // Set weather icon
            itemView.findViewById<ImageView>(R.id.iv_weather_icon).setImageResource(setIcon(forecast.icon))
        }
    }
}

class ForecastDiffCallback : DiffUtil.ItemCallback<DailyWeather>() {
    override fun areItemsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
        return oldItem.day == newItem.day
    }

    override fun areContentsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
        return oldItem == newItem
    }
}
