package com.example.data.local

import androidx.room.*
import com.example.data.model.Article
import com.example.data.model.ArticleEventCrossRef
import com.example.data.model.EventImportance
import com.example.data.model.EventStatus
import com.example.data.model.PoliticalEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PoliticalEventDao {
    @Query("SELECT * FROM political_events ORDER BY latestUpdate DESC")
    fun getAllEvents(): Flow<List<PoliticalEvent>>

    @Query("SELECT * FROM political_events ORDER BY latestUpdate DESC")
    suspend fun getAllEventsList(): List<PoliticalEvent>

    @Query("SELECT * FROM political_events WHERE id = :id")
    suspend fun getEventById(id: Long): PoliticalEvent?

    @Query("SELECT * FROM political_events WHERE id = :id")
    fun getEventByIdFlow(id: Long): Flow<PoliticalEvent?>

    @Query("SELECT * FROM political_events WHERE status = :status ORDER BY latestUpdate DESC")
    fun getEventsByStatus(status: EventStatus): Flow<List<PoliticalEvent>>

    @Query("SELECT * FROM political_events WHERE politicalFileId = :fileId ORDER BY latestUpdate DESC")
    fun getEventsForPoliticalFile(fileId: Long): Flow<List<PoliticalEvent>>

    @Query("SELECT * FROM political_events WHERE topicId = :topicId ORDER BY latestUpdate DESC")
    fun getEventsForTopic(topicId: Long): Flow<List<PoliticalEvent>>

    @Query("SELECT * FROM political_events WHERE primaryCountryCode = :countryCode OR linkedCountryCodes LIKE '%' || :countryCode || '%' ORDER BY latestUpdate DESC")
    fun getEventsForCountry(countryCode: String): Flow<List<PoliticalEvent>>

    @Query("""
        SELECT * FROM political_events 
        WHERE (:query IS NULL OR :query = '' OR titleAr LIKE '%' || :query || '%' OR summaryAr LIKE '%' || :query || '%' OR linkedPersonNames LIKE '%' || :query || '%' OR linkedOrgNames LIKE '%' || :query || '%')
        AND (:status IS NULL OR status = :status)
        AND (:importance IS NULL OR importance = :importance)
        AND (:fileId IS NULL OR politicalFileId = :fileId)
        AND (:topicId IS NULL OR topicId = :topicId)
        AND (:countryCode IS NULL OR primaryCountryCode = :countryCode OR linkedCountryCodes LIKE '%' || :countryCode || '%')
        ORDER BY latestUpdate DESC
    """)
    fun getFilteredEvents(
        query: String?,
        status: EventStatus?,
        importance: EventImportance?,
        fileId: Long?,
        topicId: Long?,
        countryCode: String?
    ): Flow<List<PoliticalEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PoliticalEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<PoliticalEvent>): List<Long>

    @Update
    suspend fun updateEvent(event: PoliticalEvent)

    @Delete
    suspend fun deleteEvent(event: PoliticalEvent)

    @Query("SELECT COUNT(*) FROM political_events")
    suspend fun getEventCount(): Int

    @Query("SELECT COUNT(*) FROM political_events WHERE status IN ('ACTIVE', 'DEVELOPING')")
    suspend fun getActiveDevelopingCount(): Int

    // Cross reference queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticleEventCrossRef(crossRef: ArticleEventCrossRef)

    @Query("DELETE FROM article_event_cross_ref WHERE articleId = :articleId AND eventId = :eventId")
    suspend fun removeArticleFromEvent(articleId: Long, eventId: Long)

    @Query("""
        SELECT a.* FROM articles a 
        INNER JOIN article_event_cross_ref x ON a.id = x.articleId 
        WHERE x.eventId = :eventId 
        ORDER BY a.publishedAt DESC
    """)
    fun getArticlesForEvent(eventId: Long): Flow<List<Article>>

    @Query("""
        SELECT e.* FROM political_events e
        INNER JOIN article_event_cross_ref x ON e.id = x.eventId
        WHERE x.articleId = :articleId
    """)
    suspend fun getEventsForArticle(articleId: Long): List<PoliticalEvent>
}
