package com.example.data.local

import androidx.room.*
import com.example.data.model.Article
import com.example.data.model.ArticleClassification
import com.example.data.model.SourceTier
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC LIMIT :limit OFFSET :offset")
    fun getAllArticlesPaged(limit: Int, offset: Int): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isDuplicate = 0 ORDER BY publishedAt DESC LIMIT :limit OFFSET :offset")
    fun getPrimaryArticlesPaged(limit: Int = 100, offset: Int = 0): Flow<List<Article>>

    @Query("""
        SELECT * FROM articles 
        WHERE (:includeDuplicates = 1 OR isDuplicate = 0)
          AND publishedAt >= :startUtc AND publishedAt <= :endUtc
          AND (:sourceId IS NULL OR sourceId = :sourceId)
          AND (:topicId IS NULL OR topicId = :topicId)
          AND (:sourceTier IS NULL OR sourceTier = :sourceTier)
          AND (:classification IS NULL OR classification = :classification)
          AND (:countryCode IS NULL OR primaryCountryCode = :countryCode OR linkedCountryCodes LIKE '%' || :countryCode || '%')
          AND (:politicalFileId IS NULL OR politicalFileId = :politicalFileId)
          AND (:onlySaved = 0 OR isSaved = 1)
          AND (
               :query IS NULL OR :query = '' 
               OR title LIKE '%' || :query || '%' 
               OR snippet LIKE '%' || :query || '%'
               OR sourceName LIKE '%' || :query || '%'
               OR linkedPersonNames LIKE '%' || :query || '%'
               OR linkedOrgNames LIKE '%' || :query || '%'
               OR topicName LIKE '%' || :query || '%'
               OR politicalFileTitle LIKE '%' || :query || '%'
          )
        ORDER BY publishedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getFilteredArticles(
        query: String?,
        startUtc: Long,
        endUtc: Long,
        sourceId: Long?,
        topicId: Long?,
        sourceTier: SourceTier?,
        classification: ArticleClassification?,
        countryCode: String?,
        politicalFileId: Long?,
        onlySaved: Int = 0,
        includeDuplicates: Int = 0,
        limit: Int = 100,
        offset: Int = 0
    ): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Long): Article?

    @Query("SELECT * FROM articles WHERE originalUrl = :url OR canonicalUrl = :url LIMIT 1")
    suspend fun findByOriginalUrl(url: String): Article?

    @Query("SELECT * FROM articles WHERE contentHash = :contentHash LIMIT 1")
    suspend fun findByContentHash(contentHash: String): Article?

    @Query("SELECT * FROM articles WHERE publishedAt >= :sinceUtc ORDER BY publishedAt DESC LIMIT 250")
    suspend fun getRecentArticles(sinceUtc: Long): List<Article>

    @Query("SELECT * FROM articles WHERE publishedAt >= :startDateUtc AND publishedAt <= :endDateUtc ORDER BY publishedAt DESC")
    suspend fun getArticlesInRangeList(startDateUtc: Long, endDateUtc: Long): List<Article>

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC LIMIT :limit")
    suspend fun getAllArticles(limit: Int = 250): List<Article>

    @Query("SELECT COUNT(*) FROM articles")
    fun getTotalArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE publishedAt >= :todayStartUtc")
    fun getArticlesCountToday(todayStartUtc: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE isDuplicate = 1")
    fun getDuplicatesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<Article>): List<Long>

    @Update
    suspend fun updateArticle(article: Article)

    @Query("UPDATE articles SET isSaved = :isSaved WHERE id = :id")
    suspend fun setArticleSaved(id: Long, isSaved: Boolean)

    @Query("UPDATE articles SET politicalFileId = :fileId, politicalFileTitle = :fileTitle WHERE id = :articleId")
    suspend fun linkArticleToFile(articleId: Long, fileId: Long?, fileTitle: String?)

    @Query("UPDATE articles SET eventId = :eventId, eventTitle = :eventTitle WHERE id = :articleId")
    suspend fun linkArticleToEvent(articleId: Long, eventId: Long?, eventTitle: String?)

    @Query("SELECT * FROM articles WHERE eventId = :eventId ORDER BY publishedAt DESC")
    fun getArticlesForEvent(eventId: Long): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE politicalFileId = :fileId ORDER BY publishedAt DESC")
    fun getArticlesForFile(fileId: Long): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE linkedPersonNames LIKE '%' || :personName || '%' ORDER BY publishedAt DESC LIMIT 50")
    fun getArticlesForPerson(personName: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE linkedOrgNames LIKE '%' || :orgName || '%' ORDER BY publishedAt DESC LIMIT 50")
    fun getArticlesForOrg(orgName: String): Flow<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("DELETE FROM articles")
    suspend fun clearAllArticles()
}
