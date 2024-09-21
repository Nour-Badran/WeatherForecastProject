package com.example.mvvm.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm.R
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.utilities.setIcon
import org.intellij.lang.annotations.Language
import java.text.NumberFormat

import java.text.SimpleDateFormat
import java.util.*

class HourlyForecastAdapter(private val selectedLanguage: String) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    private var hourlyData: List<HourlyWeather> = emptyList()

    fun submitList(data: List<HourlyWeather>) {
        hourlyData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view, selectedLanguage)
    }

    override fun getItemCount(): Int = hourlyData.size

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(hourlyData[position])
    }

    inner class HourlyViewHolder(itemView: View, private val language: String) :
        RecyclerView.ViewHolder(itemView) {

        private val timeTextView = itemView.findViewById<TextView>(R.id.tv_hour)
        private val iconImageView = itemView.findViewById<ImageView>(R.id.iv_weather_icon)
        private val tempTextView = itemView.findViewById<TextView>(R.id.tv_temperature)
        private val labelTextView = itemView.findViewById<TextView>(R.id.tv_today_tomorrow)

        fun bind(hourlyWeather: HourlyWeather) {
            // Set Locale based on selected language for hour formatting
            val locale = if (language == "ar") Locale("ar") else Locale.ENGLISH

            // Use the appropriate time format for the selected language
            val timeFormat = SimpleDateFormat("h a", locale)

            // Adjust the AM/PM symbols based on the language (if needed)
            if (language == "ar") {
                timeFormat.dateFormatSymbols.amPmStrings = arrayOf("ص", "م") // Arabic AM/PM symbols
            }

            // Convert timestamp to time string using the correct format
            val time = timeFormat.format(Date(hourlyWeather.day * 1000L))
            timeTextView.text = time

            // Set weather icon
            iconImageView.setImageResource(setIcon(hourlyWeather.icon))

            // Format the temperature based on the selected language
            val numberFormat = NumberFormat.getInstance(locale)
            val formattedTemperature = numberFormat.format(hourlyWeather.temperature.toInt())

            // Set the localized degree symbol
            val degreeSymbol = if (language == "ar") "°م" else "°C"

            // Set the temperature text
            tempTextView.text = "$formattedTemperature$degreeSymbol"

            // Determine "Today" or "Tomorrow"
            val currentDate = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
            val forecastDate = java.time.Instant.ofEpochSecond(hourlyWeather.day)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()

            // Set localized label for "Today" or "Tomorrow"
            val label = when (forecastDate) {
                currentDate -> itemView.context.getString(R.string.today_label)
                currentDate.plusDays(1) -> itemView.context.getString(R.string.tomorrow_label)
                else -> ""
            }
            labelTextView.text = label
        }



    }
}


