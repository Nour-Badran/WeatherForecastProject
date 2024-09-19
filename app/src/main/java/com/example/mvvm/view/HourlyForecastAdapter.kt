package com.example.mvvm.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.R
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.setIcon

class HourlyForecastAdapter :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    private var hourlyData: List<HourlyWeather> = emptyList()

    fun submitList(data: List<HourlyWeather>) {
        hourlyData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun getItemCount(): Int = hourlyData.size

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(hourlyData[position])
    }

    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(hourlyWeather: HourlyWeather) {
            val timeTextView = itemView.findViewById<TextView>(R.id.tv_hour)
            val iconImageView = itemView.findViewById<ImageView>(R.id.iv_weather_icon)
            val tempTextView = itemView.findViewById<TextView>(R.id.tv_temperature)

            // Convert timestamp to time string
            val time = java.text.SimpleDateFormat("h a", java.util.Locale.getDefault())
                .format(java.util.Date(hourlyWeather.day * 1000L))
            timeTextView.text = time

            // Set weather icon
            iconImageView.setImageResource(setIcon(hourlyWeather.icon))

            // Set temperature
            tempTextView.text = "${hourlyWeather.temperature.toInt()}°C"
        }
    }
}
