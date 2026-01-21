package com.parkmate.data.remote.models

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("display_name")
    val displayName: String?,
    val lat: String?,
    val lon: String?
)
