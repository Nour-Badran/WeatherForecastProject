package com.example.mvvm.weather.model.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.pojos.ForecastEntity
import com.example.mvvm.weather.model.pojos.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherEntity: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastData(forecastEntities: List<ForecastEntity>)

    @Query("SELECT * FROM weather LIMIT 1")
    fun getWeatherData(): Flow<WeatherEntity>

    @Query("SELECT * FROM forecast")
    fun getForecastData(): Flow<List<ForecastEntity>>

    @Query("DELETE FROM weather")
    suspend fun deleteAllWeatherData()

    @Query("DELETE FROM forecast")
    suspend fun deleteAllForecastData()

    @Query("SELECT * FROM favs")
    fun getAll(): Flow<List<FavoritePlaces>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoritePlaces)

    @Delete
    suspend fun delete(favorite: FavoritePlaces)

}
