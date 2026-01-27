package com.example.poketgc_api.Data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "list_cards",
    primaryKeys = ["listId", "cardId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntidadLista::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class ListCardEntity(
    val listId: Long,
    val cardId: String,
    val localId: String?,
    val nombre: String?,
    val imagen: String?,
    val rarity: String?,
    val types: String?
)

