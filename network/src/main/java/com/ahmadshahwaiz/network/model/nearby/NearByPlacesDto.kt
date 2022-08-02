package com.ahmadshahwaiz.network.model.nearby

data class NearByPlacesDto(
    val name: String,
    val categories: List<Category>?,
    val distance: Int,
    val geocodes: GeoCode?,
    val location: Location?,
    val timezone: String="",
)

