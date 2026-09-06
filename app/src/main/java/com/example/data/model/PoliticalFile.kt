package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FileStatus {
    ACTIVE,
    MONITORING,
    ARCHIVED;

    fun displayNameAr(): String = when (this) {
        ACTIVE -> "نشط وقيد المتابعة"
        MONITORING -> "مراقبة دورية"
        ARCHIVED -> "مؤرشف"
    }
}

enum class FilePriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    fun displayNameAr(): String = when (this) {
        CRITICAL -> "حرج / طارئ"
        HIGH -> "أولوية عالية"
        MEDIUM -> "متوسط"
        LOW -> "عادي"
    }

    fun colorHex(): String = when (this) {
        CRITICAL -> "#EF4444"
        HIGH -> "#F59E0B"
        MEDIUM -> "#3B82F6"
        LOW -> "#64748B"
    }
}

@Entity(tableName = "political_files")
data class PoliticalFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleAr: String,
    val titleEn: String,
    val description: String,
    val status: FileStatus = FileStatus.ACTIVE,
    val priority: FilePriority = FilePriority.HIGH,
    val topicId: Long,
    val primaryCountryCode: String,
    val linkedCountryCodes: String = "", // comma-separated country codes e.g. "SA,EG,YE,US"
    val linkedPersonNames: String = "",  // comma-separated names for fast preview
    val linkedOrgNames: String = "",
    val isPrivate: Boolean = false,
    val ownerUserId: Long? = null,
    val analystNotes: String = "",
    val recentDevelopments: String = "",
    val whatChangedDelta: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
