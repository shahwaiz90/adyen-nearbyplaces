package com.adyen.android.assignment.domain.interactors

import com.adyen.android.assignment.data.repository.VenueRepository
import com.adyen.android.assignment.data.wrapper.ResultWrapper
import javax.inject.Inject

class VenueInteractor @Inject constructor(
    private val venueRepository: VenueRepository
) {
    /**
     * This functions returns the response coming from the API
     * @param latLong lat long location params used to get near by location from this location
     * @return ResultWrapper return response in ResultWrapper either in Success or Error
     */
    suspend fun getNearByPlaces(latLong: String): ResultWrapper<*> {
        return venueRepository.getNearByPlaces(latLong)
    }
}