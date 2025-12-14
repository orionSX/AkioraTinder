package com.example.mobile_final.database

import androidx.room.*
import com.example.mobile_final.dto.PlayerProfile

@Dao
interface PlayerDao {
    @Query("SELECT * FROM playerprofiles")
    suspend fun getAllPlayers(): List<PlayerProfile>

    @Query("SELECT * FROM playerprofiles WHERE id = :id")
    suspend fun getPlayerById(id: Int): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerProfile)

    @Update
    suspend fun updatePlayer(player: PlayerProfile)

    @Delete
    suspend fun deletePlayer(player: PlayerProfile)
}