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
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.setIcon

class WeatherForecastAdapter(private val dailyWeather: List<DailyWeather>) :
    ListAdapter<DailyWeather, WeatherForecastAdapter.WeatherViewHolder>(ForecastDiffCallback()) {

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
            val date = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(forecast.day * 1000L)) // Multiply by 1000 to convert to milliseconds
            itemView.findViewById<TextView>(R.id.tv_forecast_date).text = date
            itemView.findViewById<TextView>(R.id.tv_forecast_temp).text = "${forecast.maxTemp}°C"
            itemView.findViewById<ImageView>(R.id.imageView).setImageResource(setIcon(forecast.icon))
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
