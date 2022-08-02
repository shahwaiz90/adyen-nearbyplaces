package com.ahmadshahwaiz.network.model.nearby

data class SectionNearByPlaces(
    val nearByPlacesDto : List<NearByPlacesDto>,
    var isAnimationEnabled : Boolean,
)