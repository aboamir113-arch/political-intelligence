package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameAr: String,
    val nameEn: String,
    val currentRoleAr: String,
    val currentRoleEn: String,
    val previousRoles: String = "",
    val organizationId: Long? = null,
    val countryCode: String,
    val bio: String = "",
    val isMonitored: Boolean = true,
    val photoEmoji: String = "👤",
    val tags: String = "Diplomat, Official",
    val stanceShiftCount: Int = 0,
    val lastStatementSummary: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
