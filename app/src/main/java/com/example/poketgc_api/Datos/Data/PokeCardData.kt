package com.example.poketgc_api.Datos.Data

import com.google.gson.annotations.SerializedName

data class PokeCardData(
    val id: String,
    val localId: String?,
    @SerializedName("name")
    val nombre: String?,
    @SerializedName("image")
    val imagen: String?,
    val rarity: String?,
    val types: List<String>?
)