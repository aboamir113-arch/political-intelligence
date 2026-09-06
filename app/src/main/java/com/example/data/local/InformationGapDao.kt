package com.example.data.local

import androidx.room.*
import com.example.data.model.EntityType
import com.example.data.model.InformationGapItem
import com.example.data.model.InformationGapStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InformationGapDao {

    @Query("SELECT * FROM information_gaps ORDER BY identifiedAt DESC")
    fun getAllGaps(): Flow<List<InformationGapItem>>

    @Query("SELECT * FROM information_gaps ORDER BY identifiedAt DESC")
    suspend fun getAllGapsList(): List<InformationGapItem>

    @Query("SELECT * FROM information_gaps WHERE status = :status ORDER BY identifiedAt DESC")
    fun getGapsByStatus(status: InformationGapStatus): Flow<List<InformationGapItem>>

    @Query("SELECT * FROM information_gaps WHERE linkedEntityType = :entityType AND linkedEntityId = :entityId ORDER BY identifiedAt DESC")
    fun getGapsForEntity(entityType: EntityType, entityId: Long): Flow<List<InformationGapItem>>

    @Query("SELECT * FROM information_gaps WHERE linkedEntityType = :entityType AND linkedEntityId = :entityId ORDER BY identifiedAt DESC")
    suspend fun getGapsForEntityList(entityType: EntityType, entityId: Long): List<InformationGapItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGap(gap: InformationGapItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGaps(gaps: List<InformationGapItem>)

    @Update
    suspend fun updateGap(gap: InformationGapItem)

    @Delete
    suspend fun deleteGap(gap: InformationGapItem)

    @Query("SELECT COUNT(*) FROM information_gaps WHERE status = 'UNKNOWN' OR status = 'UNVERIFIED'")
    fun getCriticalGapsCount(): Flow<Int>
}
