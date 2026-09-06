package com.example.data.ingestion

import com.example.data.model.*
import java.security.MessageDigest
import java.util.Locale

data class NormalizedArticlePayload(
    val title: String,
    val normalizedTitle: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val contentHash: String,
    val snippet: String,
    val fullText: String?,
    val author: String?,
    val imageUrl: String?,
    val publishedAt: Long,
    val sourceId: Long,
    val sourceName: String,
    val sourceTier: SourceTier,
    val language: String,
    val originalAgency: String?, // Wire source if syndicated
    val primaryCountryCode: String?,
    val linkedCountryCodes: String,
    val linkedPersonNames: String,
    val linkedOrgNames: String,
    val topicId: Long?,
    val topicName: String?,
    val politicalFileId: Long?,
    val politicalFileTitle: String?,
    val importanceScore: Int,
    val rawMetadataJson: String?
)

class ArticleNormalizer {

    private val trackingParams = listOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "fbclid", "gclid", "ref", "source", "feature", "share"
    )

    private val wireAgencies = listOf(
        Pair(listOf("واس", "وكالة الأنباء السعودية", "spa"), "واس (وكالة الأنباء السعودية)"),
        Pair(listOf("رويترز", "reuters"), "رويترز (Reuters)"),
        Pair(listOf("أ ف ب", "وكالة الصحافة الفرنسية", "فرانس برس", "afp"), "أ ف ب (AFP)"),
        Pair(listOf("وام", "وكالة أنباء الإمارات", "wam"), "وام (وكالة أنباء الإمارات)"),
        Pair(listOf("الأناضول", "anadolu"), "الأناضول (Anadolu)"),
        Pair(listOf("أسوشيتد برس", "اسوشيتد برس", "ap"), "أسوشيتد برس (AP)"),
        Pair(listOf("قنا", "وكالة الأنباء القطرية", "qna"), "قنا (وكالة الأنباء القطرية)"),
        Pair(listOf("إيرنا", "وكالة إرنا", "irna"), "إيرنا (IRNA)"),
        Pair(listOf("تاس", "tass"), "تاس (TASS)"),
        Pair(listOf("شينخوا", "xinhua"), "شينخوا (Xinhua)")
    )

    fun normalizeArabic(text: String): String {
        return text
            .replace("[إأآا]".toRegex(), "ا")
            .replace("ى".toRegex(), "ي")
            .replace("ة".toRegex(), "ه")
            .replace("[ًٌٍَُِّْـ]".toRegex(), "") // Tashkeel & Tatweel
            .replace("[^\\p{L}\\p{Nd}\\s]".toRegex(), " ") // Keep only letters & numbers
            .replace("\\s+".toRegex(), " ")
            .lowercase(Locale.ROOT)
            .trim()
    }

    fun cleanCanonicalUrl(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val query = uri.query
            if (query.isNullOrBlank()) {
                "${uri.scheme}://${uri.host}${uri.path ?: ""}"
            } else {
                val cleanedQuery = query.split("&")
                    .filterNot { param ->
                        val key = param.substringBefore("=").lowercase(Locale.ROOT)
                        trackingParams.contains(key)
                    }
                    .joinToString("&")
                val qStr = if (cleanedQuery.isNotBlank()) "?$cleanedQuery" else ""
                "${uri.scheme}://${uri.host}${uri.path ?: ""}$qStr"
            }
        } catch (_: Exception) {
            url.substringBefore("?utm_").substringBefore("&utm_")
        }
    }

    fun computeContentHash(normalizedTitle: String, snippet: String): String {
        val sample = "$normalizedTitle|${normalizeArabic(snippet.take(160))}"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(sample.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun detectOriginalAgency(text: String, title: String): String? {
        val combined = "$title $text"
        val lower = combined.lowercase(Locale.ROOT)

        for ((keywords, agencyName) in wireAgencies) {
            for (kw in keywords) {
                val pattern = "(نقلا عن|وفق|ذكرت|افادت|اعلنت|بحسب|اورزت|صرحت)\\s*.*$kw".toRegex(RegexOption.IGNORE_CASE)
                if (pattern.containsMatchIn(lower) || lower.contains("نقلاً عن $kw") || lower.contains("نقلا عن $kw") || lower.contains("بحسب $kw")) {
                    return agencyName
                }
            }
        }
        return null
    }

    fun normalize(
        raw: RawArticle,
        sourceTier: SourceTier,
        knownCountries: List<Country>,
        knownPersons: List<Person>,
        knownOrgs: List<Organization>,
        knownTopics: List<Topic>,
        knownFiles: List<PoliticalFile>
    ): NormalizedArticlePayload {
        val cleanTitle = raw.title.trim()
        val normTitle = normalizeArabic(cleanTitle)
        val canonical = cleanCanonicalUrl(raw.originalUrl)
        val hash = computeContentHash(normTitle, raw.snippet)
        val originalAgency = detectOriginalAgency(raw.snippet + " " + (raw.fullText ?: ""), cleanTitle)

        val combinedContent = "$cleanTitle ${raw.snippet} ${raw.fullText ?: ""}"
        val normContent = normalizeArabic(combinedContent)

        // Entity Matcher: Countries
        var primaryCountry: String? = null
        val matchedCountries = mutableListOf<String>()
        for (c in knownCountries) {
            val nameNorm = normalizeArabic(c.nameAr)
            if (normContent.contains(nameNorm) || normContent.contains(normalizeArabic(c.nameEn))) {
                matchedCountries.add(c.code)
                if (primaryCountry == null) primaryCountry = c.code
            }
        }

        // Entity Matcher: Persons
        val matchedPersons = mutableListOf<String>()
        var personInferredCountry: String? = null
        for (p in knownPersons) {
            val nameNorm = normalizeArabic(p.nameAr)
            if (normContent.contains(nameNorm)) {
                matchedPersons.add(p.nameAr)
                if (personInferredCountry == null && p.countryCode.isNotBlank()) {
                    personInferredCountry = p.countryCode
                }
            }
        }

        // Entity Matcher: Organizations
        val matchedOrgs = mutableListOf<String>()
        var orgInferredCountry: String? = null
        for (o in knownOrgs) {
            val nameNorm = normalizeArabic(o.nameAr)
            if (normContent.contains(nameNorm)) {
                matchedOrgs.add(o.nameAr)
                if (orgInferredCountry == null && o.countryCode.isNotBlank()) {
                    orgInferredCountry = o.countryCode
                }
            }
        }

        if (primaryCountry == null) {
            primaryCountry = personInferredCountry ?: orgInferredCountry
            if (primaryCountry != null && !matchedCountries.contains(primaryCountry)) {
                matchedCountries.add(primaryCountry)
            }
        }

        // Topic Matcher
        var matchedTopicId: Long? = null
        var matchedTopicName: String? = null
        for (t in knownTopics) {
            val topicNorm = normalizeArabic(t.nameAr)
            if (normContent.contains(topicNorm) || normContent.contains(t.nameEn.lowercase(Locale.ROOT))) {
                matchedTopicId = t.id
                matchedTopicName = t.nameAr
                break
            }
        }

        // Political File Matcher
        var matchedFileId: Long? = null
        var matchedFileTitle: String? = null
        for (f in knownFiles) {
            val fileNorm = normalizeArabic(f.titleAr)
            if (normContent.contains(fileNorm)) {
                matchedFileId = f.id
                matchedFileTitle = f.titleAr
                break
            }
        }

        // Calculate Importance score (1-100)
        var score = when (sourceTier) {
            SourceTier.PRIMARY -> 85
            SourceTier.AGENCY -> 75
            SourceTier.TRUSTED_MEDIA -> 65
            SourceTier.SECONDARY -> 45
            SourceTier.UNVERIFIED -> 25
        }
        if (matchedPersons.isNotEmpty()) score += 10
        if (matchedFileId != null) score += 15
        if (cleanTitle.contains("عاجل") || cleanTitle.contains("بيان مشترك") || cleanTitle.contains("قرار رئاسي")) score += 10
        val finalScore = score.coerceIn(10, 100)

        return NormalizedArticlePayload(
            title = cleanTitle,
            normalizedTitle = normTitle,
            originalUrl = raw.originalUrl.trim(),
            canonicalUrl = canonical,
            contentHash = hash,
            snippet = raw.snippet.trim(),
            fullText = raw.fullText?.trim(),
            author = raw.author,
            imageUrl = raw.imageUrl,
            publishedAt = raw.publishedAt,
            sourceId = raw.sourceId,
            sourceName = raw.sourceName,
            sourceTier = sourceTier,
            language = raw.language,
            originalAgency = originalAgency,
            primaryCountryCode = primaryCountry,
            linkedCountryCodes = matchedCountries.distinct().joinToString(","),
            linkedPersonNames = matchedPersons.distinct().joinToString(", "),
            linkedOrgNames = matchedOrgs.distinct().joinToString(", "),
            topicId = matchedTopicId,
            topicName = matchedTopicName,
            politicalFileId = matchedFileId,
            politicalFileTitle = matchedFileTitle,
            importanceScore = finalScore,
            rawMetadataJson = raw.rawMetadataJson
        )
    }
}
