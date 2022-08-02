package com.adyen.android.assignment.presenter.ui.main.callback

import com.ahmadshahwaiz.network.model.nearby.NearByPlacesDto

interface PlaceTapListener {
    fun onTap(nearByPlacesDto: NearByPlacesDto, position: Int)
}