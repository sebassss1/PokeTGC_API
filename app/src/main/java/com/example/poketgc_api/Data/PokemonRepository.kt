package com.example.poketgc_api.Data

class PokemonRepository(private val api: TcgDexApi) {
    suspend fun getAllCards(): List<PokemonCard> {
        return api.getAllCards()
    }
}
