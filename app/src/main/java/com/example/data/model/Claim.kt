package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VerificationStatus {
    CONFIRMED,             // Supported by primary official documents or multiple direct confirmations
    SUPPORTED,             // Corroborated by multiple credible independent wire sources
    LIKELY,                // Plausible and consistent with established reporting and context
    UNVERIFIED,            // Single secondary source, anonymous attribution, or uncorroborated
    CONTRADICTED,          // Conflicting with official records, direct statements, or verified timelines
    INSUFFICIENT_EVIDENCE, // Too little empirical evidence to corroborate or refute
    NOT_APPLICABLE;        // Subjective stance, editorial viewpoint, or rhetorical statement

    fun displayNameAr(): String = when (this) {
        CONFIRMED -> "مؤكد بوثائق/تصريحات رسمية"
        SUPPORTED -> "مدعوم بأدلة مستقلة متطابقة"
        LIKELY -> "مرجح وفق القرائن والسياق"
        UNVERIFIED -> "قيد التحقق / غير مؤكد بعد"
        CONTRADICTED -> "متناقض مع روايات أخرى"
        INSUFFICIENT_EVIDENCE -> "أدلة غير كافية للحكم"
        NOT_APPLICABLE -> "رأي سياسي / لا ينطبق"
    }

    fun displayNameEn(): String = when (this) {
        CONFIRMED -> "Confirmed"
        SUPPORTED -> "Supported"
        LIKELY -> "Likely"
        UNVERIFIED -> "Unverified"
        CONTRADICTED -> "Contradicted"
        INSUFFICIENT_EVIDENCE -> "Insufficient Evidence"
        NOT_APPLICABLE -> "N/A / Opinion"
    }
}

@Entity(
    tableName = "claims",
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["articleId"]),
        Index(value = ["verificationStatus"]),
        Index(value = ["personId"]),
        Index(value = ["organizationId"])
    ]
)
data class Claim(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val statement: String,
    val articleId: Long? = null,
    val eventId: Long? = null,
    val personId: Long? = null,
    val personName: String? = null,
    val organizationId: Long? = null,
    val organizationName: String? = null,
    val claimDate: Long,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val confidenceScore: Int = 60, // 1 to 100
    val confidenceReasonAr: String = "يستند إلى مصدر إخباري أولي قيد المقارنة مع التقارير الموازية",
    val verifiedByUserId: Long? = null,
    val evidenceCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
