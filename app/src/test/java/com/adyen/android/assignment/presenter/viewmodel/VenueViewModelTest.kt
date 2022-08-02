package com.adyen.android.assignment.presenter.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.adyen.android.assignment.MainCoroutineRule
import com.adyen.android.assignment.data.repository.VenueRepository
import com.adyen.android.assignment.data.wrapper.ResultWrapper
import com.adyen.android.assignment.domain.errorhandling.BaseException
import com.adyen.android.assignment.domain.errorhandling.GenericException
import com.adyen.android.assignment.domain.interactors.VenueInteractor
import com.ahmadshahwaiz.network.model.nearby.NearByPlacesDto
import com.ahmadshahwaiz.network.model.nearby.ResponseWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
internal class VenueViewModelTest {

    private lateinit var venueViewModel: VenueViewModel
    private val venueRepository = mockk<VenueRepository>(relaxed = true)
    private val venueInteractor: VenueInteractor = spyk(VenueInteractor(venueRepository))
    private lateinit var errorObserver: Observer<BaseException>
    private lateinit var nearByPlacesObserver: Observer<List<NearByPlacesDto>?>

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @ExperimentalCoroutinesApi
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        venueViewModel = VenueViewModel(venueInteractor, testDispatcher)

        // init observers
        errorObserver = spyk()
        nearByPlacesObserver = spyk()

        venueViewModel.nearByPlacesObserver().observeForever(nearByPlacesObserver)
        venueViewModel.errorMessageObserver().observeForever(errorObserver)
    }

    @Test
    fun `when exception is thrown from venue interactor, then errorObserver should be called`() {
        // Arrange
        val exceptionResponse = ResultWrapper.Failure(GenericException())
        every { runBlocking { venueInteractor.getNearByPlaces("") } } answers { exceptionResponse }

        // Act
        venueViewModel.getNearByPlaces("")

        // Assert
        verify(exactly = 1) { errorObserver.onChanged(exceptionResponse.exception) }
    }

    @Test
    fun `when success is returned from venue interactor, then nearByPlacesObserver should be called`() {
        // Arrange
        val nearByPlacesDto = spyk<ArrayList<NearByPlacesDto>>()
        val successResponse = ResultWrapper.Success(nearByPlacesDto)
        every { runBlocking { venueInteractor.getNearByPlaces("") } } answers { successResponse }

        // Act
        venueViewModel.getNearByPlaces("")

        // Assert
        verify(exactly = 1) { nearByPlacesObserver.onChanged(any()) }
    }

    @Test
    fun `when correct dto is returned from venue interactor, then nearByPlacesObserver should be called`() {
        // Arrange
        val nearByPlacesDto = spyk<List<NearByPlacesDto>>()
        val responseWrapper = ResponseWrapper(nearByPlacesDto)
        val successResponse = ResultWrapper.Success(responseWrapper)
        val expectedResult = successResponse.data
        every { runBlocking { venueInteractor.getNearByPlaces("") } } answers { successResponse }

        // Act
        venueViewModel.getNearByPlaces("")

        // Assert
        verify(exactly = 1) { nearByPlacesObserver.onChanged(expectedResult?.results) }
    }
}