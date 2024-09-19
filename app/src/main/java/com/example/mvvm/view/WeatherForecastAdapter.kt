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
import com.example.mvvm.capitalizeFirstLetter
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.setIcon

class WeatherForecastAdapter : ListAdapter<DailyWeather, WeatherForecastAdapter.WeatherViewHolder>(ForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(forecast: DailyWeather) {
            // Convert Unix timestamp to date
            val dateText = if (position == 0) {
                "Tomorrow"
            } else {
                // Convert Unix timestamp to date for other days
                java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(forecast.day * 1000L))
            }

            val weatherDescription = forecast.weatherStatus
            val capitalizedDescription = capitalizeFirstLetter(weatherDescription)
            // Set values to views
            itemView.findViewById<TextView>(R.id.tv_forecast_date).text = dateText
            itemView.findViewById<TextView>(R.id.tv_weather_status).text = capitalizedDescription
            itemView.findViewById<TextView>(R.id.tv_max_temp).text = "Max ${forecast.maxTemp}°C"
            itemView.findViewById<TextView>(R.id.tv_min_temp).text = "Min ${forecast.minTemp}°C"

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
