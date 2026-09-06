package com.example.data.events

import com.example.data.model.*
import java.util.Locale

data class EventMatchResult(
    val matchedEvent: PoliticalEvent?,
    val matchScore: Double,
    val matchedReasons: List<String>,
    val isNewDevelopment: Boolean,
    val developmentDescriptionAr: String?
)

class EventClusteringEngine {

    companion object {
        const val CLUSTERING_MATCH_THRESHOLD = 0.52
        const val MAX_EVENT_LOOKBACK_MILLIS = 14L * 24 * 3600 * 1000 // 14 days active window
    }

    /**
     * Determines whether an article belongs to an existing event or requires creating a new event.
     * Uses multi-factor scoring: entity overlap, topic, dossier, temporal proximity, and content similarity.
     */
    fun findMatchingEvent(
        article: Article,
        existingEvents: List<PoliticalEvent>
    ): EventMatchResult {
        if (existingEvents.isEmpty()) {
            return EventMatchResult(null, 0.0, emptyList(), false, null)
        }

        var bestMatch: PoliticalEvent? = null
        var highestScore = 0.0
        var bestReasons = emptyList<String>()

        val articlePersons = article.linkedPersonNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val articleOrgs = article.linkedOrgNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val articleCountries = article.linkedCountryCodes.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val articleTokens = tokenize(article.normalizedTitle + " " + article.snippet)

        for (event in existingEvents) {
            val reasons = mutableListOf<String>()
            var score = 0.0

            // 1. Time proximity filter: events older than 14 days without updates receive heavy penalty
            val timeDiff = kotlin.math.abs(article.publishedAt - event.latestUpdate)
            if (timeDiff > MAX_EVENT_LOOKBACK_MILLIS && event.status == EventStatus.RESOLVED) {
                continue
            }

            // 2. Political File match (+0.25)
            if (article.politicalFileId != null && article.politicalFileId == event.politicalFileId) {
                score += 0.25
                reasons.add("تطابق الملف السياسي (${event.politicalFileTitle})")
            }

            // 3. Topic match (+0.20)
            if (article.topicId != null && article.topicId == event.topicId) {
                score += 0.20
                reasons.add("تطابق الموضوع (${event.topicName})")
            }

            // 4. Country overlap (+0.15)
            val eventCountries = event.linkedCountryCodes.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val countryOverlap = articleCountries.intersect(eventCountries.toSet())
            if (countryOverlap.isNotEmpty() || (article.primaryCountryCode != null && article.primaryCountryCode == event.primaryCountryCode)) {
                score += 0.15
                reasons.add("تطابق النطاق الجغرافي والدول (${countryOverlap.joinToString()})")
            }

            // 5. Person overlap (+0.25)
            val eventPersons = event.linkedPersonNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val personOverlap = articlePersons.intersect(eventPersons.toSet())
            if (personOverlap.isNotEmpty()) {
                score += 0.25 * (personOverlap.size.coerceAtMost(2))
                reasons.add("تطابق الشخصيات المركزية (${personOverlap.joinToString()})")
            }

            // 6. Organization overlap (+0.15)
            val eventOrgs = event.linkedOrgNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val orgOverlap = articleOrgs.intersect(eventOrgs.toSet())
            if (orgOverlap.isNotEmpty()) {
                score += 0.15
                reasons.add("تطابق المؤسسات المعنية (${orgOverlap.joinToString()})")
            }

            // 7. Content Token Jaccard Similarity (+0.30 max)
            val eventTokens = tokenize(event.titleAr + " " + event.summaryAr)
            val jaccard = computeJaccard(articleTokens, eventTokens)
            if (jaccard > 0.18) {
                score += (jaccard * 1.2).coerceAtMost(0.35)
                reasons.add("تقارب دلالي في الكلمات المفتاحية والسياق (${(jaccard * 100).toInt()}%)")
            }

            if (score > highestScore) {
                highestScore = score
                bestMatch = event
                bestReasons = reasons
            }
        }

        return if (highestScore >= CLUSTERING_MATCH_THRESHOLD && bestMatch != null) {
            val isNewDev = (article.publishedAt > bestMatch.latestUpdate)
            val devDesc = if (isNewDev) {
                "تطور وارد من ${article.sourceName}: ${article.title.take(120)}"
            } else null

            EventMatchResult(
                matchedEvent = bestMatch,
                matchScore = highestScore,
                matchedReasons = bestReasons,
                isNewDevelopment = isNewDev,
                developmentDescriptionAr = devDesc
            )
        } else {
            EventMatchResult(null, highestScore, emptyList(), false, null)
        }
    }

    /**
     * Synthesizes a new PoliticalEvent entity from an initiating article.
     */
    fun createEventFromArticle(article: Article): PoliticalEvent {
        val importance = when {
            article.importanceScore >= 80 -> EventImportance.CRITICAL
            article.importanceScore >= 60 -> EventImportance.HIGH
            article.importanceScore >= 40 -> EventImportance.MEDIUM
            else -> EventImportance.LOW
        }

        return PoliticalEvent(
            titleAr = article.title,
            titleEn = article.title,
            summaryAr = article.snippet.take(400),
            summaryEn = "",
            description = article.fullText ?: article.snippet,
            status = EventStatus.DEVELOPING,
            importance = importance,
            startDate = article.publishedAt,
            latestUpdate = article.publishedAt,
            location = article.primaryCountryCode ?: "إقليمي / دولي",
            primaryCountryCode = article.primaryCountryCode,
            linkedCountryCodes = article.linkedCountryCodes,
            linkedOrgNames = article.linkedOrgNames,
            linkedPersonNames = article.linkedPersonNames,
            topicId = article.topicId,
            topicName = article.topicName,
            politicalFileId = article.politicalFileId,
            politicalFileTitle = article.politicalFileTitle,
            articleCount = 1,
            latestDevelopmentAr = "رصد الحدث لأول مرة عبر ${article.sourceName}",
            whatChangedAr = "بدء رصد التطور السياسي وتتبع تداعياته الإقليمية",
            independentSourceCount = 1,
            isVerified = true,
            verificationSummary = if (article.sourceTier == SourceTier.PRIMARY) "مصدر أولي معتمد" else "تغطية إعلامية موثوقة"
        )
    }

    /**
     * Updates an existing event when a new development article arrives.
     * Computes "What Changed" diff and updates timestamps without duplicating.
     */
    fun updateEventWithNewArticle(
        event: PoliticalEvent,
        article: Article,
        allEventArticles: List<Article>
    ): PoliticalEvent {
        val newCount = event.articleCount + 1
        val isNewer = article.publishedAt >= event.latestUpdate
        val updatedTimestamp = if (isNewer) article.publishedAt else event.latestUpdate

        // Merge persons
        val currentPersons = event.linkedPersonNames.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
        val incomingPersons = article.linkedPersonNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val newlyAddedPersons = incomingPersons.filter { !currentPersons.contains(it) }
        currentPersons.addAll(incomingPersons)

        // Merge orgs
        val currentOrgs = event.linkedOrgNames.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
        val incomingOrgs = article.linkedOrgNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val newlyAddedOrgs = incomingOrgs.filter { !currentOrgs.contains(it) }
        currentOrgs.addAll(incomingOrgs)

        // Merge countries
        val currentCountries = event.linkedCountryCodes.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
        val incomingCountries = article.linkedCountryCodes.split(",").map { it.trim() }.filter { it.isNotBlank() }
        currentCountries.addAll(incomingCountries)

        // Count unique sources
        val allSources = (allEventArticles.map { it.sourceName } + article.sourceName).distinct()

        // Formulate "What Changed"
        val changePoints = mutableListOf<String>()
        changePoints.add("ورود تقرير جديد من ${article.sourceName}")
        if (newlyAddedPersons.isNotEmpty()) {
            changePoints.add("دخول شخصيات جديدة على خط الحدث: ${newlyAddedPersons.joinToString()}")
        }
        if (newlyAddedOrgs.isNotEmpty()) {
            changePoints.add("مشاركة هيئات/مؤسسات جديدة: ${newlyAddedOrgs.joinToString()}")
        }
        if (article.sourceTier == SourceTier.PRIMARY) {
            changePoints.add("صدور بيان رسمي / موقف حكومي مباشر")
        }

        val whatChanged = changePoints.joinToString(" • ")

        return event.copy(
            latestUpdate = updatedTimestamp,
            articleCount = newCount,
            independentSourceCount = allSources.size,
            linkedPersonNames = currentPersons.joinToString(", "),
            linkedOrgNames = currentOrgs.joinToString(", "),
            linkedCountryCodes = currentCountries.joinToString(", "),
            latestDevelopmentAr = "${article.sourceName}: ${article.title.take(120)}",
            whatChangedAr = whatChanged,
            status = if (event.status == EventStatus.RESOLVED) EventStatus.DEVELOPING else event.status,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun tokenize(text: String): Set<String> {
        val stopwords = setOf("في", "من", "على", "إلى", "عن", "مع", "هذا", "هذه", "التي", "الذي", "أن", "إن", "تم", "كان", "قد", "ما", "بين", "أو", "ثم", "the", "in", "of", "and", "to")
        return text.lowercase(Locale.ROOT)
            .split("[\\s\\p{Punct}]+".toRegex())
            .filter { it.length > 2 && !stopwords.contains(it) }
            .toSet()
    }

    private fun computeJaccard(tokensA: Set<String>, tokensB: Set<String>): Double {
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0
        val intersection = tokensA.intersect(tokensB).size
        val union = tokensA.union(tokensB).size
        return if (union == 0) 0.0 else intersection.toDouble() / union.toDouble()
    }
}
