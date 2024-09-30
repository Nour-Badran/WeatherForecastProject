package com.example.mvvm.alert.reciever

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.media.Ringtone
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.mvvm.R
import com.example.mvvm.alert.AlertViewModel.AlertViewModel
import com.example.mvvm.alert.model.AlertRepository
import com.example.mvvm.settings.model.SettingsLocalDataSource
import com.example.mvvm.settings.model.SettingsRepository
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import com.example.mvvm.weather.model.db.WeatherDao
import com.example.mvvm.weather.model.db.WeatherDatabase
import com.example.mvvm.weather.model.localdatasource.WeatherLocalDataSource
import com.example.mvvm.weather.model.remotedatasource.WeatherRemoteDataSource
import com.example.mvvm.weather.model.utilities.isConnectedToInternet
import com.example.mvvm.weather.model.utilities.setIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var ringtone: Ringtone
    private lateinit var weatherDao: WeatherDao
    private lateinit var viewModel: AlertViewModel
    private lateinit var repository: AlertRepository
    private var delay = 60000L  // standard 1 min
    override fun onReceive(context: Context, intent: Intent) {
        val settingsLocalDataSource = SettingsLocalDataSource(context)
        val settingsRepository = SettingsRepository(settingsLocalDataSource)
        settingsViewModel = SettingsViewModel(settingsRepository)

        val (latitude, longitude) = settingsViewModel.getLatLon()

        val alertType = intent.getStringExtra("ALERT_TYPE")
        val id = intent.getIntExtra("ID",0)
        delay = intent.getLongExtra("DELAY", 60000)

        weatherDao = WeatherDatabase.getDatabase(context).weatherDao()

        repository = AlertRepository(weatherDao,context)

        viewModel= AlertViewModel(repository)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.deleteAlert(id)
        }

        if(settingsViewModel.notificationsEnabled.value==true)
        {
            if (alertType == "ALARM") {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtone = RingtoneManager.getRingtone(context, alarmSound)
                ringtone.play()

                CoroutineScope(Dispatchers.IO).launch {
                    val (triple, cityName) = getAlertWeatherStatusFromApi(context, latitude, longitude)
                    val (status, temperature, iconUrl) = triple

                    withContext(Dispatchers.Main) {
                        createAlertDialog(context, status, cityName, temperature, iconUrl)
                    }
                }
            } else if (alertType == "NOTIFICATION") {
                showNotification(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        val notificationId = 1
        val channelId = "weather_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val (latitude, longitude) = settingsViewModel.getLatLon()
            val (weatherData, cityName) = getAlertWeatherStatusFromApi(context, latitude, longitude)
            val (status, temperature, icon) = weatherData

            withContext(Dispatchers.Main) {
                val currentLanguage = Locale.getDefault().language

                var iconUrl = icon
                if(iconUrl==null)
                {
                    iconUrl = android.R.drawable.ic_dialog_alert
                }
                val notificationTitle = if (currentLanguage == "ar") {
                    "تنبيه الطقس في $cityName"
                } else {
                    "Weather Alert in $cityName"
                }

                val notificationContent = if (currentLanguage == "ar") {
                    "الحالة: $status، درجة الحرارة: $temperature"
                } else {
                    "Status: $status, Temperature: $temperature"
                }

                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(iconUrl)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationContent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.notify(notificationId, notificationBuilder.build())
            }
        }
    }


    private fun createAlertDialog(context: Context, weatherStatus: String, zoneName: String?, temperature: String, iconUrl: Int?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alarm_dialog, null)
        val weatherCity = dialogView.findViewById<TextView>(R.id.tv_alarm_zone_name)
        val weatherStatusTextView = dialogView.findViewById<TextView>(R.id.tv_alarm_status)
        val weatherTemperature = dialogView.findViewById<TextView>(R.id.tv_temperature)
        val weatherIcon = dialogView.findViewById<ImageView>(R.id.iv_weather_icon)
        val dismissButton = dialogView.findViewById<Button>(R.id.dismiss_alarm_button)

        weatherCity.text = zoneName
        weatherStatusTextView.text = weatherStatus
        weatherTemperature.text = temperature

        Glide.with(context)
            .load(iconUrl)
            .into(weatherIcon)

        val builder = AlertDialog.Builder(context, R.style.Theme_MVVM)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.setCancelable(true)

        val window = dialog.window
        window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.TOP)

        val layoutParams = window?.attributes
        layoutParams?.y = 100
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(delay)
                withContext(Dispatchers.Main) {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            } finally {
                cancel()
            }
        }

        dismissButton.setOnClickListener {
            ringtone.stop()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            ringtone.stop()
        }
    }

    private suspend fun getAlertWeatherStatusFromApi(
        context: Context,
        lat: Double,
        lon: Double
    ): Pair<Triple<String, String, Int?>, String?> {
        if (!isConnectedToInternet(context)) {
            val localDataSource = WeatherLocalDataSource(WeatherDatabase.getDatabase(context).weatherDao())
            val weatherData = localDataSource.getWeatherData().first()

            val description = weatherData.description
            val temperature = NumberFormat.getInstance(Locale.ENGLISH).format(weatherData.temperature.toInt())
            val temperatureUnit = "C"
            val cityName = weatherData.cityName
            val icon = setIcon(weatherData.icon)
            return Pair(Triple(description, "$temperature°$temperatureUnit", icon), cityName)
        }

        val selectedLanguageId = settingsViewModel.getLanguageId()
        val selectedLanguage = when (selectedLanguageId) {
            R.id.arabic_radio_button -> "ar"
            R.id.english_radio_button -> "en"
            else -> "null"
        }

        val selectedTempId = settingsViewModel.getTemperatureId()
        val selectedTemp = when (selectedTempId) {
            R.id.celsius_radio_button -> "metric"
            R.id.kelvin_radio_button -> "standard"
            R.id.fahrenheit_radio_button -> "imperial"
            else -> "null"
        }

        val data = WeatherRemoteDataSource().fetchWeatherData(
            lat, lon, "477840c0a8b416725948f965ee5450ec", selectedTemp, selectedLanguage
        )

        if (data.first() == null) {
            return Pair(Triple("No Data", "N/A", null), "Unknown Location")
        }

        val description = data.first()?.description.toString()
        val temperature = data.first()?.temperature?.toInt()
        val formattedTemperature = temperature?.let {
            NumberFormat.getInstance(if (selectedLanguage == "ar") Locale("ar") else Locale.ENGLISH).format(it)
        } ?: "N/A"

        val temperatureUnit = when (selectedTemp) {
            "metric" -> "C"
            "imperial" -> "F"
            "standard" -> "K"
            else -> "C"
        }

        val cityName = data.first()?.cityName ?: "Unknown Location"
        val temp = "$formattedTemperature°$temperatureUnit"
        val icon = data.first()?.let { setIcon(it.icon) }

        return Pair(Triple(description, temp, icon), cityName)
    }

}
