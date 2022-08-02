package com.adyen.android.assignment.presenter.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.presenter.ui.main.callback.PlaceTapListener
import com.ahmadshahwaiz.network.model.nearby.NearByPlacesDto
import com.ahmadshahwaiz.network.model.nearby.SectionNearByPlaces

class NearByPlacesAdapter(
    private val context: Context,
    private val placeTapListener: PlaceTapListener,
    private val dataSource: SectionNearByPlaces,
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<NearbyPlacesViewHolder>() {

    /**
     * When list is created this function is called on start
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyPlacesViewHolder {
        val view = layoutInflater.inflate(R.layout.viewholder_near_by_places, parent, false)
        return NearbyPlacesViewHolder(view)
    }

    /**
     * This function binds the view in the list and show on screen
     */
    override fun onBindViewHolder(holder: NearbyPlacesViewHolder, position: Int) {
        holder.bindPlaces(placeTapListener, context, position, dataSource)
    }

    /**
     * This function returns the total count of places in the data source.
     */
    override fun getItemCount(): Int {
        return dataSource.nearByPlacesDto.size
    }
}