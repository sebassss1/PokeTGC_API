package com.example.poketgc_api.Data

import com.google.gson.annotations.SerializedName

data class PokemonCard(
    val id: String,
    @SerializedName("name")
    @SerializedName("image")
)
