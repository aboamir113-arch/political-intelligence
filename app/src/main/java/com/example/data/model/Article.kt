package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["originalUrl"], unique = false),
        Index(value = ["contentHash"], unique = false),
        Index(value = ["publishedAt"]),
        Index(value = ["sourceId"]),
        Index(value = ["classification"]),
        Index(value = ["isDuplicate"]),
        Index(value = ["eventId"])
    ]
)
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val normalizedTitle: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val contentHash: String, // SHA-256 hash for level 2 duplicate detection
    val snippet: String,
    val fullText: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val sourceId: Long,
    val sourceName: String,
    val sourceTier: SourceTier = SourceTier.TRUSTED_MEDIA,
    val language: String = "ar",
    val publishedAt: Long, // UTC epoch timestamp
    val fetchedAt: Long = System.currentTimeMillis(), // UTC epoch timestamp
    val ingestionMethod: FetchMethod = FetchMethod.RSS,
    val isDuplicate: Boolean = false,
    val primaryArticleId: Long? = null, // if duplicate, references original article
    val duplicateCount: Int = 0,
    val republishingSourcesJson: String = "", // JSON array of source names that also republished this
    val classification: ArticleClassification = ArticleClassification.NEW,
    val isSaved: Boolean = false,
    val analystNotes: String = "",
    val topicId: Long? = null,
    val topicName: String? = null,
    val primaryCountryCode: String? = null,
    val linkedCountryCodes: String = "", // comma-separated e.g. "SA,EG"
    val linkedPersonNames: String = "", // comma-separated
    val linkedOrgNames: String = "", // comma-separated
    val politicalFileId: Long? = null,
    val politicalFileTitle: String? = null,
    val eventId: Long? = null,
    val eventTitle: String? = null,
    val importanceScore: Int = 50, // 1 to 100
    val isVerified: Boolean = true,
    val originalAgency: String? = null, // Source independence: e.g. "واس", "رويترز" if syndicated
    val rawMetadataJson: String? = null
)
