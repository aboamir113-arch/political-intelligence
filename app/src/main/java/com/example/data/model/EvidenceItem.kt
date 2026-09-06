package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EvidenceType {
    OFFICIAL_DOCUMENT,
    OFFICIAL_STATEMENT,
    DIRECT_QUOTE,
    NEWS_REPORT,
    INDEPENDENT_REPORT,
    SECONDARY_SOURCE,
    HISTORICAL_RECORD,
    OTHER;

    fun displayNameAr(): String = when (this) {
        OFFICIAL_DOCUMENT -> "وثيقة / بيان وزاري رسمي"
        OFFICIAL_STATEMENT -> "تصريح سياسي رسمي"
        DIRECT_QUOTE -> "اقتباس مباشر مسجل"
        NEWS_REPORT -> "تقرير إخباري موثق"
        INDEPENDENT_REPORT -> "تقرير بحثي / استقصائي مستقل"
        SECONDARY_SOURCE -> "مصدر ناقل / ثانوي"
        HISTORICAL_RECORD -> "سجل أرشيفي / تاريخي"
        OTHER -> "قرينة أخرى"
    }

    fun displayNameEn(): String = when (this) {
        OFFICIAL_DOCUMENT -> "Official Document"
        OFFICIAL_STATEMENT -> "Official Statement"
        DIRECT_QUOTE -> "Direct Quote"
        NEWS_REPORT -> "News Report"
        INDEPENDENT_REPORT -> "Independent Report"
        SECONDARY_SOURCE -> "Secondary Source"
        HISTORICAL_RECORD -> "Historical Record"
        OTHER -> "Other"
    }
}

enum class EvidenceStrength {
    STRONG,
    MODERATE,
    WEAK,
    DUBIOUS;

    fun displayNameAr(): String = when (this) {
        STRONG -> "قوي وحاسم"
        MODERATE -> "متوسط الدلالة"
        WEAK -> "ضعيف / قرينة أولية"
        DUBIOUS -> "مشكوك فيه / متناقض"
    }

    fun displayNameEn(): String = when (this) {
        STRONG -> "Strong"
        MODERATE -> "Moderate"
        WEAK -> "Weak"
        DUBIOUS -> "Dubious"
    }
}

@Entity(
    tableName = "evidence",
    indices = [
        Index(value = ["claimId"]),
        Index(value = ["eventId"]),
        Index(value = ["articleId"]),
        Index(value = ["evidenceType"]),
        Index(value = ["evidenceStrength"])
    ]
)
data class EvidenceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val claimId: Long,
    val eventId: Long? = null,
    val articleId: Long? = null,
    val sourceId: Long? = null,
    val sourceName: String,
    val sourceTier: SourceTier = SourceTier.TRUSTED_MEDIA,
    val evidenceType: EvidenceType = EvidenceType.NEWS_REPORT,
    val evidenceStrength: EvidenceStrength = EvidenceStrength.STRONG,
    val date: Long,
    val url: String? = null,
    val excerpt: String,
    val notes: String = "",
    val isDirectOrigin: Boolean = true,
    val originalAgency: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
