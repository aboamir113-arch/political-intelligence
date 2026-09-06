package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "statements",
    indices = [
        Index(value = ["personId"]),
        Index(value = ["eventId"]),
        Index(value = ["articleId"]),
        Index(value = ["statementDate"])
    ]
)
data class Statement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long? = null,
    val personName: String,
    val roleTitle: String? = null,
    val quoteText: String,
    val sourceId: Long? = null,
    val sourceName: String,
    val statementDate: Long,
    val eventId: Long? = null,
    val articleId: Long? = null,
    val language: String = "ar",
    val originalUrl: String? = null,
    val context: String? = null,
    val isOfficialDeclaration: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
