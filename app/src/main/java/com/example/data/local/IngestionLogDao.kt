package com.example.data.local

import androidx.room.*
import com.example.data.model.IngestionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface IngestionLogDao {

    @Query("SELECT * FROM ingestion_logs ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentIngestionLogs(limit: Int = 60): Flow<List<IngestionLog>>

    @Query("SELECT * FROM ingestion_logs WHERE sourceId = :sourceId ORDER BY startedAt DESC LIMIT 20")
    fun getLogsForSource(sourceId: Long): Flow<List<IngestionLog>>

    @Query("SELECT SUM(errorCount) FROM ingestion_logs")
    fun getTotalErrorCount(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM ingestion_logs WHERE status = 'FAILED'")
    fun getFailedRunsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IngestionLog): Long

    @Query("DELETE FROM ingestion_logs")
    suspend fun clearLogs()
}
