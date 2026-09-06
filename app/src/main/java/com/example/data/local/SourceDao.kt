package com.example.data.local

import androidx.room.*
import com.example.data.model.ConnectionState
import com.example.data.model.Source
import com.example.data.model.SourceStatus
import com.example.data.model.SourceTier
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY reliabilityLevel DESC, nameAr ASC")
    fun getAllSources(): Flow<List<Source>>

    @Query("SELECT * FROM sources ORDER BY reliabilityLevel DESC")
    suspend fun getAllSourcesList(): List<Source>

    @Query("SELECT * FROM sources WHERE status = 'ACTIVE' ORDER BY reliabilityLevel DESC")
    suspend fun getActiveSourcesList(): List<Source>

    @Query("SELECT * FROM sources WHERE status = :status ORDER BY reliabilityLevel DESC")
    fun getSourcesByStatus(status: SourceStatus): Flow<List<Source>>

    @Query("SELECT * FROM sources WHERE tier = :tier ORDER BY reliabilityLevel DESC")
    fun getSourcesByTier(tier: SourceTier): Flow<List<Source>>

    @Query("SELECT * FROM sources WHERE id = :id LIMIT 1")
    suspend fun getSourceById(id: Long): Source?

    @Query("SELECT COUNT(*) FROM sources WHERE status = 'ACTIVE'")
    fun getActiveSourcesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: Source): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSources(sources: List<Source>)

    @Update
    suspend fun updateSource(source: Source)

    @Query("UPDATE sources SET connectionState = :state, lastTestMessage = :message, lastSyncAt = :testedAt WHERE id = :sourceId")
    suspend fun updateSourceConnection(sourceId: Long, state: ConnectionState, message: String, testedAt: Long)

    @Query("UPDATE sources SET connectionState = :state, lastTestMessage = :message, lastSyncAt = :testedAt WHERE id = :sourceId")
    suspend fun updateConnectionStatus(sourceId: Long, state: ConnectionState, message: String, testedAt: Long)

    @Delete
    suspend fun deleteSource(source: Source)

    @Query("DELETE FROM sources WHERE id = :sourceId")
    suspend fun deleteSourceById(sourceId: Long)
}
