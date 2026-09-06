package com.example.data.ingestion

import com.example.data.model.FetchMethod

data class RawArticle(
    val title: String,
    val originalUrl: String,
    val snippet: String,
    val fullText: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val publishedAt: Long,
    val sourceId: Long,
    val sourceName: String,
    val method: FetchMethod,
    val language: String = "ar",
    val rawMetadataJson: String? = null
)
