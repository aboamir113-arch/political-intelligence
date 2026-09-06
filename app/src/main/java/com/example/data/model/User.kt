package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ANALYST,
    SENIOR_ANALYST,
    ADMIN;

    fun displayNameAr(): String = when (this) {
        ANALYST -> "محلل سياسي"
        SENIOR_ANALYST -> "محلل أول / استراتيجي"
        ADMIN -> "مدير النظام والمصادر"
    }

    fun displayNameEn(): String = when (this) {
        ANALYST -> "Analyst"
        SENIOR_ANALYST -> "Senior Analyst"
        ADMIN -> "Admin"
    }
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullNameAr: String,
    val fullNameEn: String,
    val email: String,
    val role: UserRole,
    val avatarInitials: String = "AP",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
