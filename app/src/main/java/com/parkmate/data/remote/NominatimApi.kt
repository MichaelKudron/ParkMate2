package com.parkmate.data.remote

import com.parkmate.data.remote.models.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "jsonv2",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): GeocodingResponse
}
