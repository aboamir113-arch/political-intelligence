package com.example.data.local

import androidx.room.*
import com.example.data.model.SavedResearch
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedResearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedResearch(research: SavedResearch): Long

    @Update
    suspend fun updateSavedResearch(research: SavedResearch)

    @Delete
    suspend fun deleteSavedResearch(research: SavedResearch)

    @Query("SELECT * FROM saved_research WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getSavedResearchForUser(userId: Long): Flow<List<SavedResearch>>

    @Query("SELECT * FROM saved_research WHERE id = :id LIMIT 1")
    suspend fun getSavedResearchById(id: Long): SavedResearch?

    @Query("SELECT COUNT(*) FROM saved_research WHERE userId = :userId")
    fun getSavedResearchCount(userId: Long): Flow<Int>

    @Query("DELETE FROM saved_research WHERE userId = :userId")
    suspend fun clearUserSavedResearch(userId: Long)
}
