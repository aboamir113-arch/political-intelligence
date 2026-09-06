package com.example.data.events

import com.example.data.model.*
import java.util.regex.Pattern

data class ExtractedArticleInsights(
    val claims: List<Claim>,
    val statements: List<Statement>
)

class ClaimExtractionEngine {

    // Regex for speech and quotes: «...» or "..." or “...”
    private val quotedSpeechPattern = Pattern.compile("[«“\"]([^»”\"]{10,350})[»”\"]")

    // Speech attribution indicators
    private val speechVerbPattern = Pattern.compile(
        "(أكد|صرح|قال|أعلن|شدد|أوضح|أشار|أفاد|دعا|حذر|أعرب|طالب)\\s+([\\p{L}\\s]{3,45}?)(?:أن|بأن|قائلاً|في|بشأن|حول|إلى|:)"
    )

    fun extractInsights(
        article: Article,
        eventId: Long? = null,
        knownPersons: List<Person> = emptyList(),
        knownOrgs: List<Organization> = emptyList()
    ): ExtractedArticleInsights {
        val extractedStatements = mutableListOf<Statement>()
        val extractedClaims = mutableListOf<Claim>()

        val content = "${article.title}. ${article.snippet} ${article.fullText ?: ""}"

        // 1. Direct Quotations / Statements extraction (Verbatim only)
        val quoteMatcher = quotedSpeechPattern.matcher(content)
        while (quoteMatcher.find()) {
            val quote = quoteMatcher.group(1)?.trim() ?: continue
            if (quote.length < 15 || quote.split(" ").size < 4) continue

            // Determine speaker attribution
            var speakerName = "مسؤول رسمي / مصدر دبلوماسي"
            var matchedPersonId: Long? = null
            var matchedRole: String? = null

            // Check if any known person appears in the surrounding text (150 chars prior)
            val matchStart = quoteMatcher.start()
            val windowStart = (matchStart - 160).coerceAtLeast(0)
            val precedingText = content.substring(windowStart, matchStart)

            for (person in knownPersons) {
                if (precedingText.contains(person.nameAr)) {
                    speakerName = person.nameAr
                    matchedPersonId = person.id
                    matchedRole = person.currentRoleAr
                    break
                }
            }

            if (matchedPersonId == null) {
                val verbMatcher = speechVerbPattern.matcher(precedingText)
                if (verbMatcher.find()) {
                    val candidate = verbMatcher.group(2)?.trim()
                    if (!candidate.isNullOrBlank() && candidate.length in 3..35) {
                        speakerName = candidate
                    }
                }
            }

            extractedStatements.add(
                Statement(
                    personId = matchedPersonId,
                    personName = speakerName,
                    roleTitle = matchedRole,
                    quoteText = quote,
                    sourceId = article.sourceId,
                    sourceName = article.sourceName,
                    statementDate = article.publishedAt,
                    eventId = eventId ?: article.eventId,
                    articleId = article.id,
                    language = article.language,
                    originalUrl = article.originalUrl,
                    context = article.title.take(120),
                    isOfficialDeclaration = article.sourceTier == SourceTier.PRIMARY
                )
            )
        }

        // 2. Extract Claims from title and lead sentences
        // Sentences with substantive geopolitical assertions
        val sentences = content.split("[.،؛\\n]+".toRegex()).map { it.trim() }.filter { it.length > 25 }

        for (sentence in sentences.take(4)) {
            val isSubstantive = sentence.contains("استقبل") ||
                    sentence.contains("بحث") ||
                    sentence.contains("وقع") ||
                    sentence.contains("أعلن") ||
                    sentence.contains("أكد") ||
                    sentence.contains("إطلاق") ||
                    sentence.contains("استهداف") ||
                    sentence.contains("اتفاق") ||
                    sentence.contains("مفاوضات") ||
                    sentence.contains("قرار") ||
                    sentence.contains("عقد")

            if (isSubstantive) {
                // Determine confidence and verification status based on source tier
                val (status, confidence, reason) = when (article.sourceTier) {
                    SourceTier.PRIMARY -> Triple(
                        VerificationStatus.CONFIRMED,
                        92,
                        "صادر مباشرة عن وكالة أنباء رسمية / جهة حكومية معتمدة"
                    )
                    SourceTier.AGENCY -> Triple(
                        VerificationStatus.SUPPORTED,
                        82,
                        "منقول عن وكالة أنباء ذات مصداقية عالية قيد التحقق المتقاطع"
                    )
                    SourceTier.TRUSTED_MEDIA -> Triple(
                        VerificationStatus.LIKELY,
                        70,
                        "تقرير من وسيلة إعلامية رصينة يتطلب مطابقة مع بيانات الأطراف المعنية"
                    )
                    else -> Triple(
                        VerificationStatus.UNVERIFIED,
                        45,
                        "مصدر ثانوي أو غير مستقل، يتطلب توثيقًا أوليًا إضافيًا"
                    )
                }

                // Associate person or organization if present
                var matchedPersonId: Long? = null
                var matchedPersonName: String? = null
                for (p in knownPersons) {
                    if (sentence.contains(p.nameAr)) {
                        matchedPersonId = p.id
                        matchedPersonName = p.nameAr
                        break
                    }
                }

                var matchedOrgId: Long? = null
                var matchedOrgName: String? = null
                for (o in knownOrgs) {
                    if (sentence.contains(o.nameAr)) {
                        matchedOrgId = o.id
                        matchedOrgName = o.nameAr
                        break
                    }
                }

                extractedClaims.add(
                    Claim(
                        statement = sentence,
                        articleId = article.id,
                        eventId = eventId ?: article.eventId,
                        personId = matchedPersonId,
                        personName = matchedPersonName,
                        organizationId = matchedOrgId,
                        organizationName = matchedOrgName,
                        claimDate = article.publishedAt,
                        verificationStatus = status,
                        confidenceScore = confidence,
                        confidenceReasonAr = reason
                    )
                )
            }
        }

        return ExtractedArticleInsights(extractedClaims, extractedStatements)
    }
}
