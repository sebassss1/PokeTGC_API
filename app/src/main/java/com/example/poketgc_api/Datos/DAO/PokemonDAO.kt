package com.example.poketgc_api.Datos.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.poketgc_api.Datos.Data.ListCardEntity
import com.example.poketgc_api.Datos.Data.UsuarioEntidadLista
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDAO {
    //Listas
    @Query("SELECT * FROM user_lists ORDER BY id DESC")
    fun getAllLists(): Flow<List<UsuarioEntidadLista>>

    @Insert
    suspend fun insertList(list: UsuarioEntidadLista): Long

    @Delete
    suspend fun deleteList(list: UsuarioEntidadLista)

    //CARTAS POR LISTA
    @Query("SELECT * FROM list_cards WHERE listId = :listId")
    fun getCardsForList(listId: Long): Flow<List<ListCardEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertCard(card: ListCardEntity)

    @Query("DELETE FROM list_cards WHERE listId = :listId AND cardId = :cardId")
    suspend fun deleteCardFromList(listId: Long, cardId: String)
}