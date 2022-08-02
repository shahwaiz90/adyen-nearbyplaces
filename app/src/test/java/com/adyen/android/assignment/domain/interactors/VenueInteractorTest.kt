package com.adyen.android.assignment.domain.interactors

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adyen.android.assignment.MainCoroutineRule
import com.adyen.android.assignment.data.repository.VenueRepository
import com.adyen.android.assignment.data.wrapper.ResultWrapper
import com.adyen.android.assignment.domain.errorhandling.GenericException
import com.ahmadshahwaiz.network.api.ApiService
import com.ahmadshahwaiz.network.model.nearby.ResponseWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class VenueInteractorTest {

    private val apiService = mockk<ApiService>(relaxed = true)
    private val venueRepository: VenueRepository = spyk(VenueRepository(apiService))
    private lateinit var venueInteractor: VenueInteractor

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        venueInteractor = VenueInteractor(venueRepository)
    }

    @Test
    fun `when failure is returned from repository, then return failure`(){
        // Arrange
        runBlocking {
            val expectedResponse = ResultWrapper.Failure(GenericException())
            every { runBlocking { venueRepository.getNearByPlaces("") } } answers { expectedResponse }

            // Act
            val result = venueInteractor.getNearByPlaces("")

            // Assert
            Assert.assertEquals(expectedResponse, result)
        }
    }

    @Test
    fun `when success is returned from repository, then return failure`(){
        runBlocking {
            // Arrange
            val responseWrapper = mockk<ResponseWrapper>(relaxed = true)
            val expectedResponse = ResultWrapper.Success(responseWrapper)
            every { runBlocking { venueRepository.getNearByPlaces("") } } answers { expectedResponse }

            // Act
            val result = venueInteractor.getNearByPlaces("")

            // Assert
            Assert.assertEquals(expectedResponse, result)
        }
    }

}