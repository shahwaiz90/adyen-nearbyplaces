package com.adyen.android.assignment.presenter.ui.main

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ActivityHomeBinding
import com.adyen.android.assignment.presenter.ui.baseclass.PermissionActivity
import com.adyen.android.assignment.presenter.ui.main.adapter.NearByPlacesAdapter
import com.adyen.android.assignment.presenter.ui.main.callback.PlaceTapListener
import com.adyen.android.assignment.presenter.viewmodel.VenueViewModel
import com.ahmadshahwaiz.network.model.nearby.NearByPlacesDto
import com.ahmadshahwaiz.network.model.nearby.SectionNearByPlaces
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode


@AndroidEntryPoint
class MainActivity : PermissionActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, PlaceTapListener {

    companion object {
        private const val DEFAULT_ZOOM: Float = 14f
        private const val FOCUSED_ZOOM: Float = 18f
        private const val MAP_PADDING: Int = 40
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val LOCATION_SETTING_REQUEST = 2
        private const val PERMISSION_GRANTED = -1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        private const val KEY_LIST_OF_PLACES = "list_of_places"

        // Default location
        private const val LATITUDE = 52.41
        private const val LONGITUDE = 4.96
    }

    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private var lastCameraLocation: CameraPosition? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private lateinit var binding: ActivityHomeBinding
    private val venuesViewModel : VenueViewModel by viewModels()

    private val listOfNearByPlaces = ArrayList<NearByPlacesDto>()
    private val sectionNearByPlaces = SectionNearByPlaces(listOfNearByPlaces, true)
    private val placesMarkers: HashMap<LatLng, Marker?> = HashMap()
    private val defaultLocation = LatLng(LATITUDE, LONGITUDE)

    /**
     * OnCreate function is the first function which starts when activity is started
     * @param savedInstanceState gives the information if its available
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListeners()
        setUpObservers()
        if(savedInstanceState == null) {
            checkPreConditionsToGetLocation()
            val slideDownAnimation  = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
            binding.searchNearby.startAnimation(slideDownAnimation)
        }
    }

    /**
     * Saves the state of the map when the activity is about to destroy.
     * @param outState is the param in which we can store our information before activity is destroyed
     */
    override fun onSaveInstanceState(outState: Bundle) {
        googleMap?.let { googleMap ->
            outState.putString(KEY_LIST_OF_PLACES, Gson().toJson(listOfNearByPlaces))
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Gets the state of the map when the activity is started again.
     * @param savedInstanceState before activity is resumed, this function will give us this param which we saved when activity was destroying
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
        lastCameraLocation = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        val listOfPlacesSaved = savedInstanceState.getString(KEY_LIST_OF_PLACES)

        val type = object : TypeToken<ArrayList<NearByPlacesDto>>() {}.type
        listOfNearByPlaces.clear()
        sectionNearByPlaces.isAnimationEnabled = false
        listOfNearByPlaces.addAll(Gson().fromJson(listOfPlacesSaved, type))
    }

    /**
     * This function, sets up listener and assign array to list adapter
     */
    private fun setUpListeners() {

        binding.searchNearby.setOnClickListener {
            findNearByRestaurants()
        }

        binding.venueList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity,  LinearLayoutManager.HORIZONTAL, false)
            adapter = NearByPlacesAdapter(this@MainActivity, this@MainActivity, sectionNearByPlaces, this@MainActivity.layoutInflater)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * This function sets all observers in this class
     */
    private fun setUpObservers() {
        venuesViewModel.nearByPlacesObserver().observe(this) { nearByPlacesList ->
            sectionNearByPlaces.isAnimationEnabled = true
            when {
                nearByPlacesList?.isNotEmpty() == true -> {
                    listOfNearByPlaces.apply {
                        clear()
                        addAll(nearByPlacesList)
                        removePlaceMarkers()
                        addMarkersForAllNearByLocationsOnMap(listOfNearByPlaces, true)
                        binding.venueList.adapter?.notifyItemRangeChanged(0, nearByPlacesList.size)
                    }
                }
                else -> {
                    Toast.makeText(this, getString(R.string.no_places_near_by), Toast.LENGTH_SHORT).show()
                }
            }
        }

        venuesViewModel.errorMessageObserver().observe(this) { exception ->
            if(exception != null){
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Remove all `places` markers
     */
    private fun removePlaceMarkers() {
        for (marker in placesMarkers) {
            marker.value?.remove()
        }
        placesMarkers.clear()
    }

    /**
     * Add markers on google map of near by places
     * @param listOfNearByPlaces gets list of near by places in an array list
     */
    private fun addMarkersForAllNearByLocationsOnMap(listOfNearByPlaces: ArrayList<NearByPlacesDto>, animateMarkers: Boolean) {
        val builder = LatLngBounds.Builder()
        for (i in 0 until listOfNearByPlaces.size) {
            val latLong = LatLng(listOfNearByPlaces[i].geocodes?.main?.latitude!!, listOfNearByPlaces[i].geocodes?.main?.longitude!!)
            builder.include(latLong)
            val marker =  MarkerOptions()
                .position(latLong)
                .title(listOfNearByPlaces[i].name)
                .icon(venuesViewModel.bitmapFromVector(this, R.drawable.ic_place_pin))

            val markerGroundOverlay = googleMap?.addMarker(marker)
            if(animateMarkers) {
                markerGroundOverlay?.let { setMarkerBounce(it) }
            }
            markerGroundOverlay?.tag = i
            placesMarkers[latLong] = markerGroundOverlay
        }
        val bounds = builder.build()
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
    }

    /**
     * Add bounce effect to markers
     * @param marker is param which is added in the map
     */
    private fun setMarkerBounce(marker: Marker) {
        val duration: Long = 2000
        val handler = Handler(Looper.getMainLooper())
        val startTime = SystemClock.uptimeMillis()

        val interpolator: Interpolator = BounceInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t =
                    (1 - interpolator.getInterpolation(elapsed.toFloat() / duration)).coerceAtLeast(
                        0f
                    )
                marker.setAnchor(0.5f, 1.0f + t)
                if (t > 0.0) {
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
    /**
     * @param latLong is received to load location on map
     * @param title is received to show on the marker message upon click
     * @param location is received to show on the marker message upon click, just in another format
     */

    private fun addSingleMarkerAndAnimateToMarker(latLong: LatLng?, location: Location?, title: String){
        latLong?.let { latLongValue->
            googleMap?.addMarker(MarkerOptions().position(latLongValue).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLongValue, DEFAULT_ZOOM))
        }
        location?.let { locationValue ->
            googleMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(locationValue.latitude, locationValue.longitude))
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))

        }
    }

    /**
     * Checks all pre-conditions to get the location
     */
    private fun checkPreConditionsToGetLocation(){
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || !isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, requestCode = PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }else if(venuesViewModel.isLocationServicesEnabled(this)){
            mapDeviceCurrentLocation()
        }else{
            showEnableLocationDialog()
        }
    }

    /**
     * Requests user to enable location services from his device
     */
    private fun showEnableLocationDialog() {
        this.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(this, LOCATION_SETTING_REQUEST)
                    } catch (sendEx: IntentSender.SendIntentException) { }
                }
            }
        }
    }

    /**
     * @param requestCode requestCode for the permission
     * @param permissions list of permissions received
     * @param grantResults result of those permissions from user
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPreConditionsToGetLocation()
                } else {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, requestCode = PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                    Toast.makeText(this, getString(R.string.give_location_permission), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * @param requestCode requestCode for the permission
     * @param resultCode resultCode for the permission
     * @param data result of those permissions from user
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOCATION_SETTING_REQUEST) {
            if(resultCode == PERMISSION_GRANTED) {
                mapDeviceCurrentLocation()
            }else{
                Toast.makeText(this, getString(R.string.turn_on_location_to_see), Toast.LENGTH_SHORT).show()
                showEnableLocationDialog()
            }
        }
    }

    /**
     * Add current location marker on map
     * @param lastKnownLocation is the location it gets and draw it on google maps
     */
    private fun addCurrentLocationMarker(lastKnownLocation: Location){
        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude))
                .title(getString(R.string.you_are_here))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
    }

    /**
     * This function gets the lastLocation from mFusedLocationClient class.
     * If there is no location, then camera animates to the default location
     */
    private fun mapDeviceCurrentLocation(){
        fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { location ->
            if (location.isSuccessful) {
                lastKnownLocation = location.result
                if (lastKnownLocation != null) {
                    addSingleMarkerAndAnimateToMarker(null, lastKnownLocation, getString(R.string.you_are_here))
                }else {
                    addSingleMarkerAndAnimateToMarker(defaultLocation, null, getString(R.string.you_are_here))
                }
            }
        }
    }

    /**
     * This function is called from 'OnMapReadyCallback' listener, when Map is fully loading and ready to use on the screen.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap?.uiSettings?.setAllGesturesEnabled(true)
        this.googleMap?.uiSettings?.isZoomGesturesEnabled = true
        this.googleMap?.uiSettings?.isTiltGesturesEnabled = true
        this.googleMap?.uiSettings?.isMapToolbarEnabled = false
        this.googleMap?.uiSettings?.isScrollGesturesEnabled = true
        this.googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        this.googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        this.googleMap?.setOnMarkerClickListener(this)

        if(listOfNearByPlaces.isNotEmpty()) {
            addMarkersForAllNearByLocationsOnMap(listOfNearByPlaces, false)
        }
        lastKnownLocation?.let { addCurrentLocationMarker(it) }
        lastCameraLocation?.let { cameraPosition ->
            val cameraPos: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            cameraPos.let { cameraUpdate ->
                googleMap.animateCamera(cameraUpdate)
            }
        }
    }

    /**
     * We call MapsViewModel after getting current lat and long where map just stopped
     **/
    private fun findNearByRestaurants() {
        val latLong = googleMap?.cameraPosition?.target?.latitude?.toBigDecimal()?.setScale(2, RoundingMode.UP)
            ?.toDouble()
            .toString() + "," + googleMap?.cameraPosition?.target?.longitude?.toBigDecimal()
            ?.setScale(2, RoundingMode.UP)?.toDouble().toString()

        if (googleMap?.cameraPosition?.target?.latitude != 0.0 && googleMap?.cameraPosition?.target?.longitude != 0.0) {
            if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                venuesViewModel.getNearByPlaces(latLong)
            }
        }
    }

    /**
     * This function is the implementation of the interface, when any place card is tapped
     * @param nearByPlacesDto is the object which is returned
     * @param position is the position of the item placed in the array
     */
    override fun onTap(nearByPlacesDto: NearByPlacesDto, position: Int) {
        sectionNearByPlaces.isAnimationEnabled = true
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(nearByPlacesDto.geocodes?.main?.latitude!!, nearByPlacesDto.geocodes?.main?.longitude!!), FOCUSED_ZOOM))
    }

    /**
     * This function is the implementation of the interface, when any marker is tapped
     * @param marker is the marker information received when any marker is tapped
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        sectionNearByPlaces.isAnimationEnabled = true
        val getPosition = marker.tag as? Int
        getPosition?.let { binding.venueList.smoothScrollToPosition(it) }
        return false
    }

}