package com.example.data.local

import androidx.room.*
import com.example.data.model.GeneratedReport
import com.example.data.model.ReportType
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: GeneratedReport): Long

    @Update
    suspend fun updateReport(report: GeneratedReport)

    @Delete
    suspend fun deleteReport(report: GeneratedReport)

    @Query("SELECT * FROM generated_reports WHERE userId = :userId ORDER BY createdAt DESC")
    fun getReportsForUser(userId: Long): Flow<List<GeneratedReport>>

    @Query("SELECT * FROM generated_reports WHERE userId = :userId AND reportType = :type ORDER BY createdAt DESC")
    fun getReportsForUserByType(userId: Long, type: ReportType): Flow<List<GeneratedReport>>

    @Query("SELECT * FROM generated_reports WHERE targetEntityId = :entityId AND targetEntityType = :entityType ORDER BY version DESC")
    fun getReportVersionsForEntity(entityId: Long, entityType: String): Flow<List<GeneratedReport>>

    @Query("SELECT * FROM generated_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): GeneratedReport?

    @Query("SELECT COUNT(*) FROM generated_reports WHERE targetEntityId = :entityId AND targetEntityType = :entityType")
    suspend fun getVersionCountForEntity(entityId: Long, entityType: String): Int

    @Query("DELETE FROM generated_reports WHERE userId = :userId")
    suspend fun clearUserReports(userId: Long)
}
