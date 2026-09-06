package com.example.data.local

import androidx.room.*
import com.example.data.model.EvidenceItem
import com.example.data.model.EvidenceType
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence ORDER BY date DESC")
    fun getAllEvidence(): Flow<List<EvidenceItem>>

    @Query("SELECT * FROM evidence ORDER BY date DESC")
    suspend fun getAllEvidenceList(): List<EvidenceItem>

    @Query("SELECT * FROM evidence WHERE claimId = :claimId ORDER BY date DESC")
    fun getEvidenceForClaim(claimId: Long): Flow<List<EvidenceItem>>

    @Query("SELECT * FROM evidence WHERE eventId = :eventId ORDER BY date DESC")
    fun getEvidenceForEvent(eventId: Long): Flow<List<EvidenceItem>>

    @Query("SELECT * FROM evidence WHERE evidenceType = :type ORDER BY date DESC")
    fun getEvidenceByType(type: EvidenceType): Flow<List<EvidenceItem>>

    @Query("SELECT * FROM evidence WHERE id = :id")
    suspend fun getEvidenceById(id: Long): EvidenceItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(item: EvidenceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvidence(items: List<EvidenceItem>): List<Long>

    @Update
    suspend fun updateEvidence(item: EvidenceItem)

    @Delete
    suspend fun deleteEvidence(item: EvidenceItem)

    @Query("SELECT COUNT(*) FROM evidence")
    suspend fun getEvidenceCount(): Int
}
