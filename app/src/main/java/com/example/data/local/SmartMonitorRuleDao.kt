package com.example.data.local

import androidx.room.*
import com.example.data.model.SmartMonitorRule
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartMonitorRuleDao {

    @Query("SELECT * FROM smart_monitor_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<SmartMonitorRule>>

    @Query("SELECT * FROM smart_monitor_rules WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRules(): Flow<List<SmartMonitorRule>>

    @Query("SELECT * FROM smart_monitor_rules WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveRulesList(): List<SmartMonitorRule>

    @Query("SELECT * FROM smart_monitor_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): SmartMonitorRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: SmartMonitorRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<SmartMonitorRule>)

    @Update
    suspend fun updateRule(rule: SmartMonitorRule)

    @Delete
    suspend fun deleteRule(rule: SmartMonitorRule)

    @Query("UPDATE smart_monitor_rules SET lastTriggeredAt = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long = System.currentTimeMillis())
}
