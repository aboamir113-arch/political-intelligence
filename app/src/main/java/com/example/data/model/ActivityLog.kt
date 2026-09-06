package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val actionType: String, // "SOURCE_TEST", "FILE_CREATED", "SOURCE_ADDED", "ROLE_SWITCH", "NOTE_ADDED"
    val entityTitle: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
