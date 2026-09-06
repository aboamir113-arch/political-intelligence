package com.example.data.local

import androidx.room.*
import com.example.data.model.AiAnalysis
import com.example.data.model.AiAnalysisType
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AiAnalysis): Long

    @Query("SELECT * FROM ai_analyses WHERE articleId = :articleId ORDER BY createdAt DESC")
    fun getAnalysesForArticle(articleId: Long): Flow<List<AiAnalysis>>

    @Query("SELECT * FROM ai_analyses WHERE articleId = :articleId AND analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAnalysisForArticle(articleId: Long, type: AiAnalysisType): Flow<AiAnalysis?>

    @Query("SELECT * FROM ai_analyses WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun getAnalysesForEvent(eventId: Long): Flow<List<AiAnalysis>>

    @Query("SELECT * FROM ai_analyses WHERE eventId = :eventId AND analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAnalysisForEvent(eventId: Long, type: AiAnalysisType): Flow<AiAnalysis?>

    @Query("SELECT * FROM ai_analyses WHERE politicalFileId = :fileId ORDER BY createdAt DESC")
    fun getAnalysesForFile(fileId: Long): Flow<List<AiAnalysis>>

    @Query("SELECT * FROM ai_analyses WHERE politicalFileId = :fileId AND analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAnalysisForFile(fileId: Long, type: AiAnalysisType): Flow<AiAnalysis?>

    @Query("SELECT * FROM ai_analyses WHERE personId = :personId ORDER BY createdAt DESC")
    fun getAnalysesForPerson(personId: Long): Flow<List<AiAnalysis>>

    @Query("SELECT * FROM ai_analyses WHERE personId = :personId AND analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAnalysisForPerson(personId: Long, type: AiAnalysisType): Flow<AiAnalysis?>

    @Query("SELECT * FROM ai_analyses WHERE analysisType = 'DASHBOARD_BRIEF' ORDER BY createdAt DESC LIMIT 1")
    fun getLatestDashboardBrief(): Flow<AiAnalysis?>

    @Query("SELECT * FROM ai_analyses ORDER BY createdAt DESC LIMIT :limit")
    fun getAllAnalysesPaged(limit: Int = 100): Flow<List<AiAnalysis>>

    @Delete
    suspend fun deleteAnalysis(analysis: AiAnalysis)

    @Query("DELETE FROM ai_analyses")
    suspend fun clearAnalyses()
}
