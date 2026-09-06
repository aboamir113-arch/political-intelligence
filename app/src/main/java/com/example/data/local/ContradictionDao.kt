package com.example.data.local

import androidx.room.*
import com.example.data.model.ContradictionItem
import com.example.data.model.ContradictionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ContradictionDao {
    @Query("SELECT * FROM contradictions ORDER BY createdAt DESC")
    fun getAllContradictions(): Flow<List<ContradictionItem>>

    @Query("SELECT * FROM contradictions ORDER BY createdAt DESC")
    suspend fun getAllContradictionsList(): List<ContradictionItem>

    @Query("SELECT * FROM contradictions WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun getContradictionsForEvent(eventId: Long): Flow<List<ContradictionItem>>

    @Query("SELECT * FROM contradictions WHERE status = :status ORDER BY createdAt DESC")
    fun getContradictionsByStatus(status: ContradictionStatus): Flow<List<ContradictionItem>>

    @Query("SELECT * FROM contradictions WHERE id = :id")
    suspend fun getContradictionById(id: Long): ContradictionItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContradiction(item: ContradictionItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContradictions(items: List<ContradictionItem>): List<Long>

    @Update
    suspend fun updateContradiction(item: ContradictionItem)

    @Delete
    suspend fun deleteContradiction(item: ContradictionItem)

    @Query("UPDATE contradictions SET status = :status, analystNotes = :notes WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ContradictionStatus, notes: String)

    @Query("SELECT COUNT(*) FROM contradictions WHERE status = 'OPEN'")
    suspend fun getOpenContradictionCount(): Int
}
