package com.example.poketgc_api.Data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_lists")
data class UsuarioEntidadLista(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
) {
}