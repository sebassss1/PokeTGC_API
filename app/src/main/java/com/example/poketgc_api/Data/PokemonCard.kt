package com.example.poketgc_api.Data

import com.google.gson.annotations.SerializedName

data class PokemonCard(
    val id: String,
    val localId: String,
    @SerializedName("name")
    val nombre: String?,
    @SerializedName("image")
    val imagen: String?,
    val rarity: String?
)
