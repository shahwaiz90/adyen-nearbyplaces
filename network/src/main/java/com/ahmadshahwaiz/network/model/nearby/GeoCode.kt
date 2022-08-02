package com.ahmadshahwaiz.network.model.nearby

data class GeoCode(
    val main: Main
)

data class Main(
    val latitude: Double,
    val longitude: Double,
)