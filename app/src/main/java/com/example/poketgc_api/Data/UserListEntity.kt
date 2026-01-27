package com.example.poketgc_api.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_lists")
data class UserListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "list_cards")
data class ListCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val cardId: String,
    val localId: String,
    val nombre: String?,
    val imagen: String?,
    val rarity: String?,
    val types: String? // Almacenado como String separado por comas
)
