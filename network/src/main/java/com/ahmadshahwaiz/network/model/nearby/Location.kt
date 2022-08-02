package com.ahmadshahwaiz.network.model.nearby

data class Location(
    val address: String,
    val country: String,
    val formatted_address: String,
    val locality: String,
    val neighbourhood: List<String>,
    val postcode: String,
    val region: String,
)
