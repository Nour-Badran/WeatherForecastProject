package com.example.mvvm

import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.repo.IWeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var fakeWeatherRepository: IWeatherRepository

    @Before
    fun setUp() {
        fakeWeatherRepository = FakeWeatherRepository()
    }

    @Test
    fun addPlace_InsertNewPlace_returnLocalWithNewPlace() = runTest {
        val place = FavoritePlaces("Test City", 0.0,0.0)

        fakeWeatherRepository.insertFavoritePlace(place)

        val result = fakeWeatherRepository.getFavPlaces().first()
        assertEquals(1, result.size)
        assertEquals(place, result[0])
    }

    @Test
    fun removePlace_RemoveExistingPlace_returnLocalWithoutPlace() = runTest {
        val place = FavoritePlaces("Test City",0.0,0.0 )
        fakeWeatherRepository.insertFavoritePlace(place)

        fakeWeatherRepository.deleteFavoritePlace(place)

        val result = fakeWeatherRepository.getFavPlaces().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getFavoritePlaces_returnLocal() = runTest {
        val result = fakeWeatherRepository.getFavPlaces().first()

        val fav1 = FavoritePlaces("Test City",0.0,0.0 )
        val fav2 = FavoritePlaces("Test City",0.0,0.0 )

        val favList = listOf(fav1,fav2)

        fakeWeatherRepository.insertFavoritePlace(fav1)
        fakeWeatherRepository.insertFavoritePlace(fav2)

        val result2 = fakeWeatherRepository.getFavPlaces().first()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(result2,favList)
    }
}
