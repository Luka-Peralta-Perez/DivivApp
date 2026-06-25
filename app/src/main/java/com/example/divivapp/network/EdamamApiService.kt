package com.example.divivapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface EdamamApiService {

    @GET("food-database/v2/parser")
    suspend fun buscarAlimento(
        @Query("app_id")         appId: String,
        @Query("app_key")        appKey: String,
        @Query("ingr")           ingrediente: String,
        @Query("nutrition-type") nutritionType: String = "cooking"
    ): EdamamResponse
}
