package com.example.poketgc_api.Data

import retrofit2.http.GET
import retrofit2.http.Path

interface TcgDexApi {
    @GET("en/cards")
    suspend fun getAllCard(): List<PokeCard>

    @GET("en/cards/{id}")
    suspend fun getCardById(
        @Path("id") id: String
    ): PokeCard
}