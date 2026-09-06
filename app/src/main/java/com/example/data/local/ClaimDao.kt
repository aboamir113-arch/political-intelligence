package com.example.data.local

import androidx.room.*
import com.example.data.model.Claim
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims ORDER BY claimDate DESC")
    fun getAllClaims(): Flow<List<Claim>>

    @Query("SELECT * FROM claims ORDER BY claimDate DESC")
    suspend fun getAllClaimsList(): List<Claim>

    @Query("SELECT * FROM claims WHERE eventId = :eventId ORDER BY claimDate DESC")
    fun getClaimsForEvent(eventId: Long): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE articleId = :articleId ORDER BY claimDate DESC")
    fun getClaimsForArticle(articleId: Long): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE personId = :personId OR personName LIKE '%' || :personName || '%' ORDER BY claimDate DESC")
    fun getClaimsForPerson(personId: Long?, personName: String): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE verificationStatus = :status ORDER BY claimDate DESC")
    fun getClaimsByStatus(status: VerificationStatus): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE id = :id")
    suspend fun getClaimById(id: Long): Claim?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: Claim): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<Claim>): List<Long>

    @Update
    suspend fun updateClaim(claim: Claim)

    @Delete
    suspend fun deleteClaim(claim: Claim)

    @Query("UPDATE claims SET verificationStatus = :status, confidenceReasonAr = :reason, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateVerificationStatus(id: Long, status: VerificationStatus, reason: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM claims")
    suspend fun getClaimCount(): Int

    @Query("SELECT COUNT(*) FROM claims WHERE verificationStatus = 'CONFIRMED'")
    suspend fun getConfirmedClaimCount(): Int
}
