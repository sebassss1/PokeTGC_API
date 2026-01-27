package com.example.poketgc_api.Data

class PokemonRepository(private val api: TcgDexApi) {
    suspend fun getAllCards(): List<PokeCard> {
        return api.getAllCard()
    }
}
