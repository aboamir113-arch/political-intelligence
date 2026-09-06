package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class IngestionStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    NO_NEW_ITEMS;

    fun displayNameAr(): String = when (this) {
        SUCCESS -> "ناجح بالكامل"
        PARTIAL -> "ناجح جزئياً (مع تنبيهات)"
        FAILED -> "فشل الجلب"
        NO_NEW_ITEMS -> "متصل (لا عناصر جديدة)"
    }

    fun displayNameEn(): String = when (this) {
        SUCCESS -> "Success"
        PARTIAL -> "Partial"
        FAILED -> "Failed"
        NO_NEW_ITEMS -> "No New Items"
    }
}

@Entity(tableName = "ingestion_logs")
data class IngestionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: Long,
    val sourceName: String,
    val ingestionMethod: FetchMethod,
    val startedAt: Long,
    val completedAt: Long,
    val status: IngestionStatus,
    val fetchedCount: Int = 0,
    val insertedCount: Int = 0,
    val duplicateCount: Int = 0,
    val errorCount: Int = 0,
    val errorDetails: String? = null
)
