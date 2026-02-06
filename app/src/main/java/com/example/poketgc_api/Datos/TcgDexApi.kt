package com.example.poketgc_api.Datos

import com.example.poketgc_api.Datos.Data.PokeCardData
import retrofit2.http.GET
import retrofit2.http.Path

interface TcgDexApi {
    @GET("en/cards")
    suspend fun getAllCard(): List<PokeCardData>

    @GET("en/cards/{id}")
    suspend fun getCardById(
        @Path("id") id: String
    ): PokeCardData
}