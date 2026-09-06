package com.example.data.local

import androidx.room.*
import com.example.data.model.AlertItem
import com.example.data.model.AlertRule
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertType
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAlertsForUser(userId: Long): Flow<List<AlertItem>>

    @Query("SELECT * FROM alerts WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadAlertsForUser(userId: Long): Flow<List<AlertItem>>

    @Query("SELECT COUNT(*) FROM alerts WHERE userId = :userId AND isRead = 0")
    fun getUnreadAlertCountFlow(userId: Long): Flow<Int>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getAlertById(id: Long): AlertItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertItem>): List<Long>

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE alerts SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Long)

    @Delete
    suspend fun deleteAlert(alert: AlertItem)

    @Query("DELETE FROM alerts WHERE userId = :userId")
    suspend fun clearAllAlerts(userId: Long)

    // Alert Rules
    @Query("SELECT * FROM alert_rules WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAlertRulesForUser(userId: Long): Flow<List<AlertRule>>

    @Query("SELECT * FROM alert_rules WHERE enabled = 1")
    suspend fun getActiveAlertRules(): List<AlertRule>

    @Query("SELECT * FROM alert_rules WHERE id = :id")
    suspend fun getAlertRuleById(id: Long): AlertRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertRule(rule: AlertRule): Long

    @Update
    suspend fun updateAlertRule(rule: AlertRule)

    @Delete
    suspend fun deleteAlertRule(rule: AlertRule)

    @Query("UPDATE alert_rules SET enabled = :enabled WHERE id = :id")
    suspend fun toggleAlertRule(id: Long, enabled: Boolean)
}
