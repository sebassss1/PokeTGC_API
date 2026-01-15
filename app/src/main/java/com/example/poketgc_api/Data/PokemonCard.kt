package com.example.poketgc_api.Data

import com.google.gson.annotations.SerializedName

data class PokemonCard(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("image")
    val image: String?
)
