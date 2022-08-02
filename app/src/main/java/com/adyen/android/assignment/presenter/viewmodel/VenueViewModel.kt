package com.adyen.android.assignment.presenter.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.data.di.ApplicationModule
import com.adyen.android.assignment.data.wrapper.ResultWrapper
import com.adyen.android.assignment.domain.errorhandling.BaseException
import com.adyen.android.assignment.domain.interactors.VenueInteractor
import com.ahmadshahwaiz.network.model.nearby.NearByPlacesDto
import com.ahmadshahwaiz.network.model.nearby.ResponseWrapper
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val venueInteractor: VenueInteractor,
    @ApplicationModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val errorMessage = MutableLiveData<BaseException>()
    private val nearByVenueObserver = MutableLiveData<List<NearByPlacesDto>?>()

    /**
     * Observers, notifies the objects about the near by venue data
     */
    fun nearByPlacesObserver(): MutableLiveData<List<NearByPlacesDto>?> {
        return nearByVenueObserver
    }

    /**
     * Observers, notifies the objects about the error message
     */
    fun errorMessageObserver(): MutableLiveData<BaseException> {
        return errorMessage
    }

    /**
     * Calls the interactor to get near by places
     * @param latLong lat long location params used to get near by location from this location
     */
    fun getNearByPlaces(latLong: String){
        kotlin.runCatching {
            viewModelScope.launch(ioDispatcher) {
                venueInteractor.getNearByPlaces(latLong).let { response ->
                    when (response) {
                        is ResultWrapper.Success<*> -> {
                            val result = response.data as? ResponseWrapper
                            nearByVenueObserver.postValue(result?.results)
                        }
                        is ResultWrapper.Failure -> {
                            errorMessage.postValue(response.exception)
                        }
                    }
                }
            }
        }
    }

    /**
     * We used this function to check if location services are enabled on device or not.
     * @param context we use this param to access Android ecosystem
     */
    internal fun isLocationServicesEnabled(context: Context): Boolean {
        val locationManager : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * We used this function to create scale-able bitmap to show on google maps.
     */
    internal fun bitmapFromVector(context: Context, vectorResID: Int): BitmapDescriptor {
        val vectorDrawable= ContextCompat.getDrawable(context, vectorResID)
        vectorDrawable!!.setBounds(0,0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap= Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}