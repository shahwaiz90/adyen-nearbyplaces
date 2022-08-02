package com.adyen.android.assignment.data.repository

import com.adyen.android.assignment.data.wrapper.ResultWrapper
import com.adyen.android.assignment.domain.errorhandling.GenericException
import com.adyen.android.assignment.domain.errorhandling.SocketTimeOutException
import com.ahmadshahwaiz.network.api.ApiService
import javax.inject.Inject

class VenueRepository @Inject constructor(private val apiService: ApiService){

    /**
     * This function returns response coming from the API
     * @param latLong lat long location params used to get near by location from this location
     * @return ResultWrapper return response in ResultWrapper either in Success or Error
     */
    suspend fun getNearByPlaces(latLong:  String): ResultWrapper<*> {
        kotlin.runCatching {
            apiService.getNearByPlaces(latLong).let {
                // to check Http header code, we can implement CustomCallAdapter and handle that logic there.
                // for now ill just check the code coming from the response instead of header.
                return if (it.results != null) {
                    ResultWrapper.Success(it)
                } else {
                    ResultWrapper.Failure(GenericException())
                }
            }
        }
        return ResultWrapper.Failure(SocketTimeOutException())
    }
}