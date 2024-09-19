package com.example.mvvm.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mvvm.model.FiveDayResponse

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherEntity: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastData(forecastEntities: List<ForecastEntity>)

    @Query("SELECT * FROM weather LIMIT 1")
    suspend fun getWeatherData(): WeatherEntity?

    @Query("SELECT * FROM forecast")
    suspend fun getForecastData(): List<ForecastEntity>

    @Query("DELETE FROM weather")
    suspend fun deleteAllWeatherData()

    @Query("DELETE FROM forecast")
    suspend fun deleteAllForecastData()
}
