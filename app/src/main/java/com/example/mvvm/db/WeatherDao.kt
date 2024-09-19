package com.example.mvvm.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather LIMIT 1")
    suspend fun getWeatherData(): WeatherEntity?

    @Query("DELETE FROM weather")
    suspend fun deleteAllWeatherData()
}