package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EventStatus {
    ACTIVE,
    DEVELOPING,
    RESOLVED,
    ARCHIVED;

    fun displayNameAr(): String = when (this) {
        ACTIVE -> "نشط ومستمر"
        DEVELOPING -> "حدث متطور"
        RESOLVED -> "مكتمل / مسوّى"
        ARCHIVED -> "مؤرشف"
    }

    fun displayNameEn(): String = when (this) {
        ACTIVE -> "Active"
        DEVELOPING -> "Developing"
        RESOLVED -> "Resolved"
        ARCHIVED -> "Archived"
    }
}

enum class EventImportance {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    fun displayNameAr(): String = when (this) {
        CRITICAL -> "حرج وحساس للغاية"
        HIGH -> "أهمية عالية"
        MEDIUM -> "أهمية متوسطة"
        LOW -> "أهمية اعتيادية"
    }

    fun displayNameEn(): String = when (this) {
        CRITICAL -> "Critical"
        HIGH -> "High"
        MEDIUM -> "Medium"
        LOW -> "Low"
    }
}

@Entity(
    tableName = "political_events",
    indices = [
        Index(value = ["status"]),
        Index(value = ["importance"]),
        Index(value = ["latestUpdate"]),
        Index(value = ["topicId"]),
        Index(value = ["politicalFileId"]),
        Index(value = ["primaryCountryCode"])
    ]
)
data class PoliticalEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleAr: String,
    val titleEn: String,
    val summaryAr: String,
    val summaryEn: String = "",
    val description: String = "",
    val status: EventStatus = EventStatus.DEVELOPING,
    val importance: EventImportance = EventImportance.HIGH,
    val startDate: Long,
    val latestUpdate: Long,
    val location: String = "",
    val primaryCountryCode: String? = null,
    val linkedCountryCodes: String = "", // comma-separated e.g. "SA,EG,YE"
    val linkedOrgNames: String = "",     // comma-separated
    val linkedPersonNames: String = "",   // comma-separated
    val topicId: Long? = null,
    val topicName: String? = null,
    val politicalFileId: Long? = null,
    val politicalFileTitle: String? = null,
    val articleCount: Int = 1,
    val latestDevelopmentAr: String? = null,
    val whatChangedAr: String? = null,
    val independentSourceCount: Int = 1,
    val isVerified: Boolean = true,
    val verificationSummary: String = "مدعوم بمصادر متعددة",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "article_event_cross_ref",
    primaryKeys = ["articleId", "eventId"],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["articleId"])
    ]
)
data class ArticleEventCrossRef(
    val articleId: Long,
    val eventId: Long,
    val isPrimary: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
