package com.example.poketgc_api.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Query("SELECT * FROM user_lists")
    fun getAllLists(): Flow<List<UserListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: UserListEntity): Long

    @Delete
    suspend fun deleteList(list: UserListEntity)

    @Query("SELECT * FROM list_cards WHERE listId = :listId")
    fun getCardsForList(listId: Long): Flow<List<ListCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: ListCardEntity)

    @Query("DELETE FROM list_cards WHERE listId = :listId AND cardId = :cardId")
    suspend fun deleteCardFromList(listId: Long, cardId: String)
}
