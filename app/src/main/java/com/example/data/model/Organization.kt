package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrganizationType {
    GOVERNMENT,
    FOREIGN_MINISTRY,
    PRESIDENCY,
    MILITARY_SECURITY,
    INTERNATIONAL_BODY,
    REGIONAL_BLOC,
    POLITICAL_PARTY,
    RESEARCH_CENTER,
    ENERGY_COMPANY;

    fun displayNameAr(): String = when (this) {
        GOVERNMENT -> "حكومة / وزارة"
        FOREIGN_MINISTRY -> "وزارة خارجية"
        PRESIDENCY -> "رئاسة / ديوان ملكي"
        MILITARY_SECURITY -> "جهة عسكرية / أمنية"
        INTERNATIONAL_BODY -> "منظمة دولية"
        REGIONAL_BLOC -> "منظمة / تكتل إقليمي"
        POLITICAL_PARTY -> "حزب / تكتل سياسي"
        RESEARCH_CENTER -> "مركز دراسات وأبحاث"
        ENERGY_COMPANY -> "مؤسسة طاقة واستراتيجية"
    }
}

@Entity(tableName = "organizations")
data class Organization(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameAr: String,
    val nameEn: String,
    val type: OrganizationType,
    val countryCode: String,
    val description: String,
    val websiteUrl: String = "",
    val isMonitored: Boolean = true
)
