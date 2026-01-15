package com.example.poketgc_api.Data
import retrofit2.http.GET
import retrofit2.http.Path

interface TcgDexApi {
    @GET("en/cards")
    suspend fun getAllCard(): ApiResponse<List<PokemonCard>>

    @GET("en/card/swsw2")
    suspend fun getCardById(
    @Path("id") id: String
    ): ApiResponse<PokemonCard>
}