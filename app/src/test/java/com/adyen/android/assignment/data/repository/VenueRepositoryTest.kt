package com.adyen.android.assignment.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adyen.android.assignment.data.wrapper.ResultWrapper
import com.adyen.android.assignment.domain.errorhandling.GenericException
import com.ahmadshahwaiz.network.api.ApiService
import com.ahmadshahwaiz.network.model.nearby.ResponseWrapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class VenueRepositoryTest {
    private val apiService = mockk<ApiService>(relaxed = true)
    private lateinit var venueRepository: VenueRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        venueRepository = VenueRepository(apiService)
    }

    @Test
    fun `when failure is returned from repository, then return failure`(){
        // Arrange
        runBlocking {
            val inputData = ResponseWrapper(null)
            val expectedResponse = ResultWrapper.Failure(GenericException())
            every { runBlocking { apiService.getNearByPlaces("") } } answers { inputData }

            // Act
            val result = venueRepository.getNearByPlaces("") as? ResultWrapper.Failure

            // Assert
            Assert.assertEquals(expectedResponse.exception.message, result?.exception?.message)
        }
    }

    @Test
    fun `when success is returned from repository, then return success`(){
        // Arrange
        runBlocking {
            val inputData = ResponseWrapper(arrayListOf())
            val expectedResponse = ResultWrapper.Success(inputData)
            every { runBlocking { apiService.getNearByPlaces("") } } answers { inputData }

            // Act
            val result = venueRepository.getNearByPlaces("") as? ResultWrapper.Success

            // Assert
            Assert.assertEquals(expectedResponse.data, result?.data)
        }
    }


}