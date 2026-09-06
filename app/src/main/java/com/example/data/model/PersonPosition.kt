package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StanceShiftType {
    NO_CHANGE,           // Syntactic rephrasing without semantic shift
    SUBTLE_SHIFT,        // Nuanced calibration
    SIGNIFICANT_REVERSAL // Meaningful structural policy shift
}

@Entity(tableName = "person_positions")
data class PersonPosition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val topicId: Long,
    val fileId: Long? = null,
    val issueTitle: String,
    val previousStance: String,
    val currentStance: String,
    val shiftType: StanceShiftType = StanceShiftType.SUBTLE_SHIFT,
    val changeDateTimestamp: Long = System.currentTimeMillis(),
    val verbatimStatement: String,
    val sourceName: String,
    val sourceUrl: String,
    val context: String,
    val evidenceConfidence: Int = 85 // 0-100%
)
