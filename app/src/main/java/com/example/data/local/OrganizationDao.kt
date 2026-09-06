package com.example.data.local

import androidx.room.*
import com.example.data.model.Organization
import com.example.data.model.OrganizationType
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations ORDER BY isMonitored DESC, nameAr ASC")
    fun getAllOrganizations(): Flow<List<Organization>>

    @Query("SELECT * FROM organizations")
    suspend fun getAllOrganizationsList(): List<Organization>

    @Query("SELECT * FROM organizations WHERE countryCode = :countryCode")
    fun getOrganizationsByCountry(countryCode: String): Flow<List<Organization>>

    @Query("SELECT * FROM organizations WHERE type = :type")
    fun getOrganizationsByType(type: OrganizationType): Flow<List<Organization>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationById(id: Long): Organization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: Organization): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrganizations(orgs: List<Organization>)

    @Update
    suspend fun updateOrganization(org: Organization)

    @Delete
    suspend fun deleteOrganization(org: Organization)
}
