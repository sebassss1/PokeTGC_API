package com.example.poketgc_api.Data

import java.net.URL

data class PokemonData(
    val imagen: URL?,
    val id: Int,
    val nombre: String?,
    val localId: String?,
    val rarity: String?
)