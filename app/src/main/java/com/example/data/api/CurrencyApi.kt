package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class CurrencyResponse(
    val result: String,
    val rates: Map<String, Double>
)

interface CurrencyApi {
    @GET("v6/latest/INR")
    suspend fun getLatestRates(): CurrencyResponse
}
