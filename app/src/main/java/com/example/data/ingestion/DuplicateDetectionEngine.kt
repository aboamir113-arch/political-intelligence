package com.example.data.ingestion

import com.example.data.local.ArticleDao
import com.example.data.model.Article
import com.example.data.model.ArticleClassification
import org.json.JSONArray
import java.util.Locale
import kotlin.math.max

sealed class DuplicateResolution {
    data class Unique(val classification: ArticleClassification) : DuplicateResolution()
    data class Duplicate(val primaryArticle: Article, val updatedRepublishingJson: String) : DuplicateResolution()
    data class Recirculated(val originalArticle: Article) : DuplicateResolution()
}

class DuplicateDetectionEngine(private val articleDao: ArticleDao) {

    private val updateKeywords = listOf(
        "تطورات", "تحديث", "مستجدات", "لاحقا", "لاحقاً", "عقب", "بعد ساعات",
        "تأكيد", "تأكيدا", "تأكيداً", "بيان توضيحي", "رد رسمي", "مؤتمر صحفي",
        "updated", "developments", "breaking", "confirmed"
    )

    suspend fun evaluate(payload: NormalizedArticlePayload): DuplicateResolution {
        // Level 1: Exact URL Match
        val existingByUrl = articleDao.findByOriginalUrl(payload.originalUrl) 
            ?: articleDao.findByOriginalUrl(payload.canonicalUrl)
        if (existingByUrl != null) {
            val updatedJson = appendRepublishingSource(existingByUrl.republishingSourcesJson, payload.sourceName)
            return DuplicateResolution.Duplicate(existingByUrl, updatedJson)
        }

        // Level 2: Content Hash Match
        val existingByHash = articleDao.findByContentHash(payload.contentHash)
        if (existingByHash != null) {
            val updatedJson = appendRepublishingSource(existingByHash.republishingSourcesJson, payload.sourceName)
            return DuplicateResolution.Duplicate(existingByHash, updatedJson)
        }

        // Level 3: Normalized Title Similarity against recent articles (last 7 days)
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000)
        val candidates = articleDao.getRecentArticles(sevenDaysAgo)

        val targetTokens = tokenize(payload.normalizedTitle)

        for (candidate in candidates) {
            val candTokens = tokenize(candidate.normalizedTitle)
            val similarity = jaccardSimilarity(targetTokens, candTokens)

            if (similarity >= 0.82) { // Strong title match
                val timeDiffHours = (payload.publishedAt - candidate.publishedAt) / (1000 * 3600)

                // Check for recirculated vs development vs duplicate
                val hasUpdateKeyword = updateKeywords.any { 
                    payload.title.contains(it, ignoreCase = true) || payload.snippet.contains(it, ignoreCase = true) 
                }

                return when {
                    hasUpdateKeyword && timeDiffHours > 3 -> {
                        DuplicateResolution.Unique(ArticleClassification.DEVELOPMENT)
                    }
                    timeDiffHours > 48 -> {
                        // Old news published again without substantive delta -> Recirculated
                        DuplicateResolution.Recirculated(candidate)
                    }
                    else -> {
                        // Close timeframe and same title -> Republishing duplicate
                        val updatedJson = appendRepublishingSource(candidate.republishingSourcesJson, payload.sourceName)
                        DuplicateResolution.Duplicate(candidate, updatedJson)
                    }
                }
            } else if (similarity >= 0.60) {
                // Moderate similarity: check if it's an update on the same topic/entities
                if (payload.topicId != null && payload.topicId == candidate.topicId) {
                    val hasUpdateKeyword = updateKeywords.any { 
                        payload.title.contains(it, ignoreCase = true) || payload.snippet.contains(it, ignoreCase = true) 
                    }
                    if (hasUpdateKeyword) {
                        return DuplicateResolution.Unique(ArticleClassification.UPDATE)
                    }
                }
            }
        }

        // Level 4: Check if it's an official confirmation
        val isConfirmation = payload.title.contains("تأكيد") || 
                             payload.title.contains("بيان رسمي") ||
                             payload.sourceTier.name == "PRIMARY"

        val finalClassification = if (isConfirmation && candidates.any { tokenize(it.normalizedTitle).intersect(targetTokens).size >= 3 }) {
            ArticleClassification.CONFIRMATION
        } else {
            ArticleClassification.NEW
        }

        return DuplicateResolution.Unique(finalClassification)
    }

    private fun appendRepublishingSource(existingJson: String, newSourceName: String): String {
        return try {
            val array = if (existingJson.isNotBlank()) JSONArray(existingJson) else JSONArray()
            var exists = false
            for (i in 0 until array.length()) {
                if (array.getString(i).equals(newSourceName, ignoreCase = true)) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                array.put(newSourceName)
            }
            array.toString()
        } catch (_: Exception) {
            "[\"$newSourceName\"]"
        }
    }

    private fun tokenize(text: String): Set<String> {
        return text.split("\\s+".toRegex())
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.length > 2 }
            .toSet()
    }

    private fun jaccardSimilarity(s1: Set<String>, s2: Set<String>): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        val intersection = s1.intersect(s2).size
        val union = s1.union(s2).size
        return intersection.toDouble() / max(union, 1).toDouble()
    }
}
