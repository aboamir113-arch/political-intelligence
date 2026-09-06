package com.example.data.local

import androidx.room.*
import com.example.data.model.EarlySignalItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EarlySignalDao {

    @Query("SELECT * FROM early_signals ORDER BY detectedAt DESC")
    fun getAllSignals(): Flow<List<EarlySignalItem>>

    @Query("SELECT * FROM early_signals ORDER BY detectedAt DESC")
    suspend fun getAllSignalsList(): List<EarlySignalItem>

    @Query("SELECT * FROM early_signals WHERE isAcknowledged = 0 ORDER BY detectedAt DESC")
    fun getUnacknowledgedSignals(): Flow<List<EarlySignalItem>>

    @Query("SELECT * FROM early_signals WHERE associatedFileId = :fileId ORDER BY detectedAt DESC")
    fun getSignalsForFile(fileId: Long): Flow<List<EarlySignalItem>>

    @Query("SELECT * FROM early_signals WHERE associatedPersonId = :personId ORDER BY detectedAt DESC")
    fun getSignalsForPerson(personId: Long): Flow<List<EarlySignalItem>>

    @Query("SELECT * FROM early_signals WHERE id = :id LIMIT 1")
    suspend fun getSignalById(id: Long): EarlySignalItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: EarlySignalItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignals(signals: List<EarlySignalItem>)

    @Update
    suspend fun updateSignal(signal: EarlySignalItem)

    @Query("UPDATE early_signals SET isAcknowledged = 1 WHERE id = :id")
    suspend fun acknowledgeSignal(id: Long)

    @Delete
    suspend fun deleteSignal(signal: EarlySignalItem)

    @Query("SELECT COUNT(*) FROM early_signals WHERE isAcknowledged = 0")
    fun getUnacknowledgedCount(): Flow<Int>
}
