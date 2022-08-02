package com.ahmadshahwaiz.network.api

import com.ahmadshahwaiz.network.BuildConfig
import com.ahmadshahwaiz.network.api.constants.NetworkConstants
import com.ahmadshahwaiz.network.model.nearby.ResponseWrapper
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {

    @Headers("Authorization: ${BuildConfig.FOURSQUARE_SECRET_KEY}")
    @GET("/v3/places/nearby")
    suspend fun getNearByPlaces(@Query(NetworkConstants.Params.LATITUDE_LONGITUDE) latLong: String): ResponseWrapper

}