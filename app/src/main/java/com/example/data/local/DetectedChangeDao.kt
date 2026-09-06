package com.example.data.local

import androidx.room.*
import com.example.data.model.DetectedChangeItem
import com.example.data.model.EntityType
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectedChangeDao {

    @Query("SELECT * FROM detected_changes ORDER BY whenTimestamp DESC")
    fun getAllChanges(): Flow<List<DetectedChangeItem>>

    @Query("SELECT * FROM detected_changes ORDER BY whenTimestamp DESC LIMIT :limit")
    suspend fun getRecentChanges(limit: Int = 30): List<DetectedChangeItem>

    @Query("SELECT * FROM detected_changes WHERE entityType = :entityType AND entityId = :entityId ORDER BY whenTimestamp DESC")
    fun getChangesForEntity(entityType: EntityType, entityId: Long): Flow<List<DetectedChangeItem>>

    @Query("SELECT * FROM detected_changes WHERE entityType = :entityType AND entityId = :entityId ORDER BY whenTimestamp DESC")
    suspend fun getChangesForEntityList(entityType: EntityType, entityId: Long): List<DetectedChangeItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChange(change: DetectedChangeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChanges(changes: List<DetectedChangeItem>)

    @Delete
    suspend fun deleteChange(change: DetectedChangeItem)

    @Query("SELECT COUNT(*) FROM detected_changes")
    fun getTotalChangesCount(): Flow<Int>
}
