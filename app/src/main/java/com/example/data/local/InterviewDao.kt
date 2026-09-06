package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {

    @Query("SELECT * FROM interviews ORDER BY dateUtc DESC")
    fun getAllInterviews(): Flow<List<Interview>>

    @Query("SELECT * FROM interviews ORDER BY dateUtc DESC")
    suspend fun getAllInterviewsList(): List<Interview>

    @Query("SELECT * FROM interviews WHERE (userId = :userId OR isPrivate = 0) ORDER BY dateUtc DESC")
    fun getInterviewsForUser(userId: Long): Flow<List<Interview>>

    @Query("SELECT * FROM interviews WHERE (userId = :userId OR isPrivate = 0) ORDER BY dateUtc DESC")
    suspend fun getInterviewsForUserList(userId: Long): List<Interview>

    @Query("SELECT * FROM interviews WHERE id = :id")
    suspend fun getInterviewById(id: Long): Interview?

    @Query("SELECT * FROM interviews WHERE id = :id")
    fun getInterviewByIdFlow(id: Long): Flow<Interview?>

    @Query("SELECT * FROM interviews WHERE dateUtc >= :sinceUtc AND status NOT IN ('CANCELLED') ORDER BY dateUtc ASC")
    fun getUpcomingInterviews(sinceUtc: Long): Flow<List<Interview>>

    @Query("SELECT * FROM interviews WHERE dateUtc >= :sinceUtc AND status NOT IN ('CANCELLED') ORDER BY dateUtc ASC")
    suspend fun getUpcomingInterviewsList(sinceUtc: Long): List<Interview>

    @Query("SELECT * FROM interviews WHERE dateUtc >= :startOfDayUtc AND dateUtc <= :endOfDayUtc ORDER BY startTime ASC")
    suspend fun getInterviewsForDateRange(startOfDayUtc: Long, endOfDayUtc: Long): List<Interview>

    @Query("SELECT * FROM interviews WHERE status = 'COMPLETED' ORDER BY dateUtc DESC")
    fun getCompletedInterviews(): Flow<List<Interview>>

    @Query("SELECT * FROM interviews WHERE status = 'COMPLETED' ORDER BY dateUtc DESC")
    suspend fun getCompletedInterviewsList(): List<Interview>

    @Query("SELECT * FROM interviews WHERE status IN ('PLANNED', 'CONFIRMED') AND dateUtc >= :nowUtc ORDER BY dateUtc ASC")
    suspend fun getInterviewsNeedingPreparation(nowUtc: Long): List<Interview>

    @Query("""
        SELECT * FROM interviews 
        WHERE (subject LIKE '%' || :query || '%' 
               OR channel LIKE '%' || :query || '%' 
               OR program LIKE '%' || :query || '%' 
               OR interviewer LIKE '%' || :query || '%' 
               OR notes LIKE '%' || :query || '%'
               OR description LIKE '%' || :query || '%')
        ORDER BY dateUtc DESC
    """)
    suspend fun searchInterviews(query: String): List<Interview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(interview: Interview): Long

    @Update
    suspend fun updateInterview(interview: Interview)

    @Delete
    suspend fun deleteInterview(interview: Interview)

    @Query("DELETE FROM interviews WHERE id = :id")
    suspend fun deleteInterviewById(id: Long)
}

@Dao
interface InterviewLinkDao {

    @Query("SELECT * FROM interview_links WHERE interviewId = :interviewId ORDER BY createdAt DESC")
    fun getLinksForInterview(interviewId: Long): Flow<List<InterviewLink>>

    @Query("SELECT * FROM interview_links WHERE interviewId = :interviewId ORDER BY createdAt DESC")
    suspend fun getLinksForInterviewList(interviewId: Long): List<InterviewLink>

    @Query("SELECT * FROM interview_links ORDER BY createdAt DESC")
    fun getAllLinks(): Flow<List<InterviewLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: InterviewLink): Long

    @Delete
    suspend fun deleteLink(link: InterviewLink)

    @Query("DELETE FROM interview_links WHERE id = :id")
    suspend fun deleteLinkById(id: Long)
}

@Dao
interface InterviewTranscriptDao {

    @Query("SELECT * FROM interview_transcripts WHERE interviewId = :interviewId ORDER BY createdAt DESC LIMIT 1")
    fun getTranscriptForInterview(interviewId: Long): Flow<InterviewTranscript?>

    @Query("SELECT * FROM interview_transcripts WHERE interviewId = :interviewId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getTranscriptForInterviewList(interviewId: Long): InterviewTranscript?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: InterviewTranscript): Long

    @Update
    suspend fun updateTranscript(transcript: InterviewTranscript)

    @Query("DELETE FROM interview_transcripts WHERE interviewId = :interviewId")
    suspend fun deleteTranscriptByInterviewId(interviewId: Long)
}

@Dao
interface InterviewAnalysisDao {

    @Query("SELECT * FROM interview_analysis WHERE interviewId = :interviewId ORDER BY createdAt DESC")
    fun getAnalysesForInterview(interviewId: Long): Flow<List<InterviewAnalysis>>

    @Query("SELECT * FROM interview_analysis WHERE interviewId = :interviewId AND analysisType = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAnalysis(interviewId: Long, type: InterviewAnalysisType): InterviewAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: InterviewAnalysis): Long

    @Delete
    suspend fun deleteAnalysis(analysis: InterviewAnalysis)

    @Query("DELETE FROM interview_analysis WHERE interviewId = :interviewId")
    suspend fun deleteAnalysesByInterviewId(interviewId: Long)
}
