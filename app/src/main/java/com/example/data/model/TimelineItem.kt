package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timeline_items",
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["timestamp"]),
        Index(value = ["articleId"]),
        Index(value = ["statementId"])
    ]
)
data class TimelineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val timestamp: Long,
    val titleAr: String,
    val titleEn: String = "",
    val descriptionAr: String,
    val sourceName: String,
    val sourceTier: SourceTier = SourceTier.PRIMARY,
    val articleId: Long? = null,
    val statementId: Long? = null,
    val importance: EventImportance = EventImportance.HIGH,
    val isMilestone: Boolean = false,
    val originalUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
