package com.example.mvvm.alert.model

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.example.mvvm.weather.model.db.WeatherDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AlertRepository(private val alertDao: WeatherDao, private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun insertAlert(alert: AlertEntity) {
        alertDao.insertAlert(alert)
    }

    suspend fun deleteAlert(id: Int) {
        alertDao.deleteAlertById(id)
    }

    fun getAllAlerts(): Flow<List<AlertEntity>> {
        return alertDao.getAllAlerts()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlert(alert: AlertEntity, pendingIntent: PendingIntent) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, alert.year)
            set(Calendar.MONTH, alert.month)
            set(Calendar.DAY_OF_MONTH, alert.day)
            set(Calendar.HOUR_OF_DAY, alert.hour)
            set(Calendar.MINUTE, alert.minute)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun cancelAlert(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
    }
}
