package com.example.data.local

import androidx.room.*
import com.example.data.model.FilePriority
import com.example.data.model.FileStatus
import com.example.data.model.PoliticalFile
import kotlinx.coroutines.flow.Flow

@Dao
interface PoliticalFileDao {
    @Query("SELECT * FROM political_files ORDER BY CASE priority WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, updatedAt DESC")
    fun getAllFiles(): Flow<List<PoliticalFile>>

    @Query("SELECT * FROM political_files WHERE status = 'ACTIVE'")
    suspend fun getAllFilesList(): List<PoliticalFile>

    @Query("SELECT * FROM political_files WHERE status = :status ORDER BY updatedAt DESC")
    fun getFilesByStatus(status: FileStatus): Flow<List<PoliticalFile>>

    @Query("SELECT * FROM political_files WHERE priority = :priority ORDER BY updatedAt DESC")
    fun getFilesByPriority(priority: FilePriority): Flow<List<PoliticalFile>>

    @Query("SELECT * FROM political_files WHERE status = 'ACTIVE' ORDER BY updatedAt DESC")
    fun getActiveFiles(): Flow<List<PoliticalFile>>

    @Query("SELECT COUNT(*) FROM political_files WHERE status = 'ACTIVE'")
    fun getActiveFilesCount(): Flow<Int>

    @Query("SELECT * FROM political_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): PoliticalFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: PoliticalFile): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFiles(files: List<PoliticalFile>)

    @Update
    suspend fun updateFile(file: PoliticalFile)

    @Delete
    suspend fun deleteFile(file: PoliticalFile)
}
