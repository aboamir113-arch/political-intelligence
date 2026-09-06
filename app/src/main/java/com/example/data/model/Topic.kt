package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TopicCategory {
    DIPLOMACY,
    SECURITY_DEFENSE,
    REGIONAL_CONFLICT,
    ENERGY_ECONOMY,
    ELECTIONS_POLITICS,
    MARITIME_CHOKEPOINTS;

    fun displayNameAr(): String = when (this) {
        DIPLOMACY -> "دبلوماسية ومعاهدات"
        SECURITY_DEFENSE -> "أمن ودفاع"
        REGIONAL_CONFLICT -> "نزاعات إقليمية"
        ENERGY_ECONOMY -> "طاقة واقتصاد جيوسياسي"
        ELECTIONS_POLITICS -> "انتخابات وحكومات"
        MARITIME_CHOKEPOINTS -> "ممرات مائية واستراتيجية"
    }
}

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameAr: String,
    val nameEn: String,
    val category: TopicCategory,
    val description: String,
    val colorHex: String = "#2563EB",
    val isFollowed: Boolean = true,
    val trackedArticlesCount: Int = 0
)
