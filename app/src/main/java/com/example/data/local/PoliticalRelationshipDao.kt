package com.example.data.local

import androidx.room.*
import com.example.data.model.DiscoveryStatus
import com.example.data.model.EntityType
import com.example.data.model.PoliticalRelationship
import com.example.data.model.RelationshipType
import kotlinx.coroutines.flow.Flow

@Dao
interface PoliticalRelationshipDao {

    @Query("SELECT * FROM political_relationships ORDER BY discoveredAt DESC")
    fun getAllRelationships(): Flow<List<PoliticalRelationship>>

    @Query("SELECT * FROM political_relationships ORDER BY discoveredAt DESC")
    suspend fun getAllRelationshipsList(): List<PoliticalRelationship>

    @Query("SELECT * FROM political_relationships WHERE discoveryStatus = :status ORDER BY discoveredAt DESC")
    fun getRelationshipsByStatus(status: DiscoveryStatus): Flow<List<PoliticalRelationship>>

    @Query("SELECT * FROM political_relationships WHERE (sourceEntityType = :entityType AND sourceEntityId = :entityId) OR (targetEntityType = :entityType AND targetEntityId = :entityId) ORDER BY discoveredAt DESC")
    fun getRelationshipsForEntity(entityType: EntityType, entityId: Long): Flow<List<PoliticalRelationship>>

    @Query("SELECT * FROM political_relationships WHERE (sourceEntityType = :entityType AND sourceEntityId = :entityId) OR (targetEntityType = :entityType AND targetEntityId = :entityId) ORDER BY discoveredAt DESC")
    suspend fun getRelationshipsForEntityList(entityType: EntityType, entityId: Long): List<PoliticalRelationship>

    @Query("SELECT * FROM political_relationships WHERE relationshipType = :type ORDER BY discoveredAt DESC")
    fun getRelationshipsByType(type: RelationshipType): Flow<List<PoliticalRelationship>>

    @Query("SELECT * FROM political_relationships WHERE id = :id LIMIT 1")
    suspend fun getRelationshipById(id: Long): PoliticalRelationship?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: PoliticalRelationship): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<PoliticalRelationship>)

    @Update
    suspend fun updateRelationship(relationship: PoliticalRelationship)

    @Delete
    suspend fun deleteRelationship(relationship: PoliticalRelationship)

    @Query("UPDATE political_relationships SET discoveryStatus = :newStatus, confirmedAt = :confirmedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: DiscoveryStatus, confirmedAt: Long? = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM political_relationships WHERE discoveryStatus = 'DISCOVERED'")
    fun getPendingDiscoveriesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM political_relationships")
    fun getTotalRelationshipsCount(): Flow<Int>
}
