package com.adyen.android.assignment.presenter.ui.main.adapter

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.ViewholderNearByPlacesBinding
import com.adyen.android.assignment.presenter.ui.main.callback.PlaceTapListener
import com.ahmadshahwaiz.network.model.nearby.SectionNearByPlaces
import com.bumptech.glide.Glide


class NearbyPlacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var viewholderNearByPlacesBinding: ViewholderNearByPlacesBinding = ViewholderNearByPlacesBinding.bind(view)

    /**
     * @param placeTapListener a listener attached here, so that we can notify it after any particular action
     * @param context Glide needs this context to render image on the view
     * @param position Position for the object placed in an array to track the places
     * @param sectionNearByPlaces is the object which we show on screen with attribute isAnimationEnabled
     */
    fun bindPlaces(placeTapListener: PlaceTapListener, context: Context, position: Int, sectionNearByPlaces: SectionNearByPlaces){
        if(sectionNearByPlaces.isAnimationEnabled) {
            val slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
            viewholderNearByPlacesBinding.placeContainer.startAnimation(slideDownAnimation)
        }

        viewholderNearByPlacesBinding.apply {
            placeContainer.setOnClickListener {
                placeTapListener.onTap(sectionNearByPlaces.nearByPlacesDto[position], position)
            }
            placeName.text = sectionNearByPlaces.nearByPlacesDto[position].name
            locationDescription.text = sectionNearByPlaces.nearByPlacesDto[position].location?.address
            distance.text = context.getString(R.string.distance_n_meters, sectionNearByPlaces.nearByPlacesDto[position].distance.toString())
            if(sectionNearByPlaces.nearByPlacesDto[position].categories?.isNotEmpty() == true){
                Glide.with(context)
                    .load(sectionNearByPlaces.nearByPlacesDto[position].categories?.get(0)?.icon?.prefix+IMAGE_SIZE_64+sectionNearByPlaces.nearByPlacesDto[position].categories?.get(0)?.icon?.suffix)
                    .centerCrop()
                    .placeholder(R.drawable.ic_place)
                    .into(locationIcon)
            }else{
                Glide.with(context)
                    .load(R.drawable.ic_place)
                    .centerCrop()
                    .placeholder(R.drawable.ic_place)
                    .into(locationIcon)
            }
        }
    }

    companion object {
        const val IMAGE_SIZE_64 = "64"
    }
}
