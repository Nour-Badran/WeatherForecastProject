package com.example.mvvm.repo

import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.repo.IWeatherRepository
import com.example.mvvm.weather.model.repo.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var repo: IWeatherRepository

    lateinit var fakeRemoteDataSource: FakeRemoteDataSource
    lateinit var fakeLocalDataSource: FakeLocalDataSource

    @Before
    fun setUp() {
        fakeRemoteDataSource = FakeRemoteDataSource()
        fakeLocalDataSource = FakeLocalDataSource()
        repo = WeatherRepository(fakeLocalDataSource,fakeRemoteDataSource)
    }

    @Test
    fun addPlace_InsertNewPlace_returnLocalWithNewPlace() = runTest {
        val place = FavoritePlaces("Test City", 0.0,0.0)

        repo.insertFavoritePlace(place)

        val result = repo.getFavPlaces().first()
        assertEquals(1, result.size)
        assertEquals(place, result[0])
    }

    @Test
    fun removePlace_RemoveExistingPlace_returnLocalWithoutPlace() = runTest {
        val place = FavoritePlaces("Test City",0.0,0.0 )
        repo.insertFavoritePlace(place)

        repo.deleteFavoritePlace(place)

        val result = repo.getFavPlaces().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getFavoritePlaces_returnLocal() = runTest {
        val result = repo.getFavPlaces().first()
        assertTrue(result.isEmpty())

        val fav1 = FavoritePlaces("Test City",0.0,0.0 )
        val fav2 = FavoritePlaces("Test City",0.0,0.0 )

        val favList = listOf(fav1,fav2)

        repo.insertFavoritePlace(fav1)
        repo.insertFavoritePlace(fav2)

        val result2 = repo.getFavPlaces().first()

        // Assert
        assertEquals(result2,favList)
    }
}
