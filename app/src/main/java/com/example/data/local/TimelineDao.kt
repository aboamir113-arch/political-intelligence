package com.example.data.local

import androidx.room.*
import com.example.data.model.TimelineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_items WHERE eventId = :eventId ORDER BY timestamp ASC")
    fun getTimelineForEventAsc(eventId: Long): Flow<List<TimelineItem>>

    @Query("SELECT * FROM timeline_items WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getTimelineForEventDesc(eventId: Long): Flow<List<TimelineItem>>

    @Query("SELECT * FROM timeline_items ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTimelineItems(limit: Int): Flow<List<TimelineItem>>

    @Query("SELECT * FROM timeline_items ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllTimelineItems(limit: Int = 250): List<TimelineItem>

    @Query("SELECT * FROM timeline_items WHERE eventId = :eventId ORDER BY timestamp ASC")
    suspend fun getTimelineForEvent(eventId: Long): List<TimelineItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItem(item: TimelineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItems(items: List<TimelineItem>): List<Long>

    @Update
    suspend fun updateTimelineItem(item: TimelineItem)

    @Delete
    suspend fun deleteTimelineItem(item: TimelineItem)

    @Query("SELECT COUNT(*) FROM timeline_items WHERE eventId = :eventId")
    suspend fun getTimelineItemCount(eventId: Long): Int
}
