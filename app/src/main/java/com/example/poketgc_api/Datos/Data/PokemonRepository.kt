package com.example.poketgc_api.Datos.Data

import com.example.poketgc_api.Datos.TcgDexApi

class PokemonRepository(private val api: TcgDexApi) {
    suspend fun getAllCards(): List<PokeCardData> {
        return api.getAllCard()
    }
}
