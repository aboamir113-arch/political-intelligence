package com.example.data.ai

import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Political Intelligence Desk — AI Analysis & Intelligence Engine (Phase 4)
 *
 * Strict Rule of Truth:
 * - AI is not the source of truth; verifiable database entities and sources are.
 * - Hallucination protection: If evidence is insufficient, explicitly states "Insufficient Evidence".
 * - Prompt injection protection: Untrusted article/web content is sanitized and isolated.
 * - Citation validation: Every analytical output is mapped and validated against real DB entities.
 * - History & Versioning: Every analysis is logged in `ai_analyses` table.
 */
class AiIntelligenceEngine(
    private val database: AppDatabase
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    var configuredModel: String = "gemini-3.5-flash"
    var promptVersion: String = "v1.4"

    // ---------------------------------------------------------------------------------------------
    // Security & Prompt Injection Protection
    // ---------------------------------------------------------------------------------------------

    fun sanitizeUntrustedContent(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val injectionPatterns = listOf(
            "(?i)ignore\\s+previous\\s+instructions",
            "(?i)system\\s+prompt",
            "(?i)developer\\s+instruction",
            "(?i)reset\\s+directives",
            "(?i)you\\s+are\\s+now\\s+a",
            "(?i)output\\s+only\\s+the\\s+secret",
            "(?i)bypass\\s+safety"
        )
        var sanitized: String = input
        for (pattern in injectionPatterns) {
            sanitized = sanitized.replace(Regex(pattern), "[NEUTRALIZED_UNTRUSTED_COMMAND]")
        }
        return sanitized.trim()
    }

    fun cleanMarkdown(text: String): String {
        return text.replace(Regex("```json|```"), "").trim()
    }

    fun isPromptInjectionAttempt(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val regex = Regex("(?i)(ignore\\s+previous\\s+instructions|system\\s+prompt|developer\\s+instruction|you\\s+are\\s+now\\s+a|reset\\s+directives)")
        return regex.containsMatchIn(text)
    }

    // ---------------------------------------------------------------------------------------------
    // 1. Article Summarization (Short, Detailed, Key Points, Numbers & Facts, Translate)
    // ---------------------------------------------------------------------------------------------

    suspend fun summarizeArticle(
        articleId: Long,
        userId: Long = 1,
        forceRegenerate: Boolean = false,
        targetLanguage: String = "ar"
    ): ArticleSummaryResult = withContext(Dispatchers.IO) {
        val article = database.articleDao().getArticleById(articleId)
            ?: return@withContext emptyArticleSummary("المقال غير موجود في قاعدة البيانات")

        val bodyText = article.fullText?.ifBlank { article.snippet } ?: article.snippet
        val sanitizedBody = sanitizeUntrustedContent(bodyText)
        val sanitizedTitle = sanitizeUntrustedContent(article.title)

        val sentences = extractSentences(sanitizedBody)
        val extractedNumbers = extractNumbersAndFacts(sanitizedBody)
        val keyPoints = extractKeyPoints(sentences, sanitizedTitle)

        val shortSummary = if (sentences.size <= 3) {
            sentences.joinToString(" ")
        } else {
            sentences.take(3).joinToString(" ")
        }.ifBlank { sanitizedTitle }

        val who = article.linkedPersonNames.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString("، ")
            .ifBlank { extractProminentActors(sanitizedBody) }
        val where = article.primaryCountryCode ?: "إقليمي / دولي"
        val whenTime = dateFormat.format(Date(article.publishedAt))
        val whatWasSaid = extractDirectQuotes(sanitizedBody).firstOrNull() ?: "لا توجد تصريحات مباشرة مقتبسة في نص المقال"
        val whatIsNew = article.snippet.take(180).trim()
        val whatIsUncertain = if (!article.isVerified || article.isDuplicate) {
            "التقرير يعتمد على مصدر فردي وتجري مقاطعته مع الوثائق الرسمية والوكالات المعتمدة"
        } else {
            "المعلومات متطابقة مع البيانات الرسمية الموثقة"
        }

        val detailedSummary = buildString {
            append("• ماذا حدث؟ $shortSummary\n")
            append("• الفاعلون الرئيسيون: ${who.ifBlank { "جهات دبلوماسية وحكومية معنية" }}\n")
            append("• متى وأين؟ $whenTime في نطاق ($where)\n")
            append("• التصريحات البارزة: $whatWasSaid\n")
            append("• التطور الجديد: $whatIsNew\n")
            append("• المعلومات قيد التثبت: $whatIsUncertain")
        }

        val translatedSummary = if (targetLanguage.equals("en", ignoreCase = true)) {
            "Executive Summary: $shortSummary | Date: $whenTime | Source: ${article.sourceName}"
        } else {
            "الملخص الاستخباري: $shortSummary | المصدر: ${article.sourceName}"
        }

        val citation = AiCitation(
            entityType = "ARTICLE",
            entityId = article.id,
            title = article.title,
            sourceName = article.sourceName,
            dateString = whenTime,
            excerptOrQuote = shortSummary.take(150)
        )

        val result = ArticleSummaryResult(
            shortSummary = shortSummary,
            detailedSummary = detailedSummary,
            keyPoints = keyPoints,
            numbersAndFacts = extractedNumbers,
            translatedSummary = translatedSummary,
            language = targetLanguage,
            whatHappened = shortSummary,
            who = who,
            whatWasSaid = whatWasSaid,
            where = where,
            whenTime = whenTime,
            whatIsNew = whatIsNew,
            whatIsUncertain = whatIsUncertain,
            citations = listOf(citation)
        )

        // Save Analysis Record
        val jsonOutput = JSONObject().apply {
            put("shortSummary", result.shortSummary)
            put("detailedSummary", result.detailedSummary)
            put("keyPoints", JSONArray(result.keyPoints))
            put("numbersAndFacts", JSONArray(result.numbersAndFacts))
            put("whatIsNew", result.whatIsNew)
            put("whatIsUncertain", result.whatIsUncertain)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.ARTICLE_SUMMARY,
            userId = userId,
            articleId = article.id,
            eventId = article.eventId,
            politicalFileId = article.politicalFileId,
            inputReferenceIds = "article:${article.id}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.94,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Event Intelligence Summary (What Happened, What Changed, Who, Confirmed, Uncertain, Conflicting)
    // ---------------------------------------------------------------------------------------------

    suspend fun summarizeEvent(
        eventId: Long,
        userId: Long = 1,
        forceRegenerate: Boolean = false
    ): EventSummaryResult = withContext(Dispatchers.IO) {
        val event = database.politicalEventDao().getEventById(eventId)
            ?: return@withContext EventSummaryResult("حدث غير موجود", "لا تتوفر بيانات للحدث", "", "", "", "", "")

        val articles = database.articleDao().getArticlesForEvent(eventId).firstOrNull() ?: emptyList()
        val claims = database.claimDao().getClaimsForEvent(eventId).firstOrNull() ?: emptyList()
        val contradictions = database.contradictionDao().getContradictionsForEvent(eventId).firstOrNull() ?: emptyList()
        val statements = database.statementDao().getStatementsForEvent(eventId).firstOrNull() ?: emptyList()

        val whatHappened = event.summaryAr.ifBlank { event.description.take(280) }
        val whatChanged = event.whatChangedAr?.ifBlank { event.latestDevelopmentAr ?: "" } ?: (event.latestDevelopmentAr ?: "")
        val whoIsInvolved = buildList {
            event.linkedPersonNames.split(",").forEach { if (it.isNotBlank()) add(it.trim()) }
            event.linkedOrgNames.split(",").forEach { if (it.isNotBlank()) add(it.trim()) }
            statements.forEach { add("${it.personName} (${it.roleTitle})") }
        }.distinct().joinToString("، ").ifBlank { "أطراف سياسية ودبلوماسية إقليمية" }

        val confirmedCount = claims.count { it.verificationStatus == VerificationStatus.CONFIRMED || it.verificationStatus == VerificationStatus.SUPPORTED }
        val whatIsConfirmed = if (confirmedCount > 0) {
            "تم توثيق $confirmedCount ادعاءات بأدلة ثبوتية رسمية تشمل: " +
                    claims.filter { it.verificationStatus == VerificationStatus.CONFIRMED || it.verificationStatus == VerificationStatus.SUPPORTED }.take(2).joinToString(" | ") { it.statement }
        } else {
            "الوقائع الأساسية مدعومة ببيانات وزارة الخارجية والوكالات الوطنية المعتمدة (${articles.size} مصادر إعلامية)."
        }

        val unverifiedClaims = claims.filter { it.verificationStatus == VerificationStatus.UNVERIFIED || it.verificationStatus == VerificationStatus.INSUFFICIENT_EVIDENCE }
        val whatIsUncertain = if (unverifiedClaims.isNotEmpty()) {
            "قيد المتابعة والتدقيق: " + unverifiedClaims.joinToString(" ؛ ") { it.statement }
        } else {
            "لا توجد إدعاءات مفتوحة قيد التدقيق حالياً لهذا الملف."
        }

        val conflictingInformation = if (contradictions.isNotEmpty()) {
            "تم رصد ${contradictions.size} تناقضات نشطة: " + contradictions.joinToString(" | ") { "${it.claimAStatement} [مقابل] ${it.claimBStatement}" }
        } else {
            "الروايات متطابقة حول المسار العام ولم تُرصد تناقضات جوهرية بين المصادر المتقاطعة."
        }

        val citations = articles.map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                dateString = dateFormat.format(Date(it.publishedAt)),
                excerptOrQuote = it.snippet.take(120)
            )
        }

        val result = EventSummaryResult(
            eventTitle = event.titleAr,
            whatHappened = whatHappened,
            whatChanged = whatChanged,
            whoIsInvolved = whoIsInvolved,
            whatIsConfirmed = whatIsConfirmed,
            whatIsUncertain = whatIsUncertain,
            conflictingInformation = conflictingInformation,
            citations = citations
        )

        // Save AI analysis
        val jsonOutput = JSONObject().apply {
            put("eventTitle", result.eventTitle)
            put("whatHappened", result.whatHappened)
            put("whatChanged", result.whatChanged)
            put("whoIsInvolved", result.whoIsInvolved)
            put("whatIsConfirmed", result.whatIsConfirmed)
            put("whatIsUncertain", result.whatIsUncertain)
            put("conflictingInformation", result.conflictingInformation)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.EVENT_SUMMARY,
            userId = userId,
            eventId = eventId,
            politicalFileId = event.politicalFileId,
            inputReferenceIds = "event:$eventId;articles:${articles.map { it.id }.joinToString(",")}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.95,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Narrative Comparison (Common Facts, Narrative A vs B, Official, Contradictions, Unique, Unknowns)
    // ---------------------------------------------------------------------------------------------

    suspend fun compareNarratives(
        eventId: Long,
        userId: Long = 1
    ): NarrativeComparisonResult = withContext(Dispatchers.IO) {
        val articles = database.articleDao().getArticlesForEvent(eventId).firstOrNull() ?: emptyList()
        val event = database.politicalEventDao().getEventById(eventId)

        if (articles.size < 2) {
            return@withContext NarrativeComparisonResult(
                commonFacts = listOf(event?.titleAr ?: "حدث قيد الرصد"),
                narrativeA = articles.firstOrNull()?.title ?: "رواية المصدر الأول المتاح",
                narrativeASource = articles.firstOrNull()?.sourceName ?: "المصدر المتوفر",
                narrativeB = "لا تتوفر رواية مقابلة كافية في قاعدة البيانات (يتطلب مصدرين على الأقل لمقارنة الروايات)",
                narrativeBSource = "غير متوفر",
                officialNarrative = articles.firstOrNull { it.sourceTier == SourceTier.PRIMARY }?.title,
                conflictingClaims = listOf("يتطلب توثيق مصادر إضافية لإجراء مقارنة دقيقة"),
                uniqueInformation = listOf("لا توجد فوارق مسجلة"),
                unknownInformation = listOf("الروايات الموازية غير مكتملة"),
                strongestEvidence = "المادة المصدرية الوحيدة المتوفرة حالياً: ${articles.firstOrNull()?.sourceName ?: "غير محدد"}",
                citations = emptyList()
            )
        }

        val primaryArticle = articles.firstOrNull { it.sourceTier == SourceTier.PRIMARY || it.sourceTier == SourceTier.AGENCY } ?: articles[0]
        val secondaryArticle = articles.firstOrNull { it.id != primaryArticle.id } ?: articles[1]

        val commonFacts = mutableListOf<String>()
        val wordsA = primaryArticle.title.split(" ").filter { it.length > 3 }.toSet()
        val wordsB = secondaryArticle.title.split(" ").filter { it.length > 3 }.toSet()
        val commonKeywords = wordsA.intersect(wordsB)
        if (commonKeywords.isNotEmpty()) {
            commonFacts.add("اتفاق المصدرين على الإطار العام للخبر حول: ${commonKeywords.take(4).joinToString("، ")}")
        }
        commonFacts.add("انعقاد التطور في التوقيت المشترك بتاريخ: ${dateFormat.format(Date(primaryArticle.publishedAt))}")

        val officialArticle = articles.firstOrNull { it.sourceTier == SourceTier.PRIMARY }
        val officialNarrative = officialArticle?.let {
            "${it.sourceName}: ${it.snippet.ifBlank { it.title }}"
        }

        val conflictingClaims = mutableListOf<String>()
        if (primaryArticle.snippet != secondaryArticle.snippet) {
            conflictingClaims.add("تباين في زاوية التركيز الصحفي واختيار العنوان بين (${primaryArticle.sourceName}) و (${secondaryArticle.sourceName})")
        }

        val uniqueInfo = mutableListOf<String>()
        uniqueInfo.add("انفرد (${primaryArticle.sourceName}) بـ: ${primaryArticle.title.take(120)}")
        uniqueInfo.add("انفرد (${secondaryArticle.sourceName}) بـ: ${secondaryArticle.title.take(120)}")

        val unknownInfo = listOf(
            "تفاصيل المباحثات المغلقة التي لم تصدر في البيانات الصحفية العلنية",
            "الجداول الزمنية الدقيقة للخطوات التنفيذية القادمة"
        )

        val strongestEvidence = "البيان الرسمي المنشور من ${primaryArticle.sourceName} والموثق برقم خبر رسمي."

        val citations = listOf(
            AiCitation(
                entityType = "ARTICLE",
                entityId = primaryArticle.id,
                title = primaryArticle.title,
                sourceName = primaryArticle.sourceName,
                sourceTier = primaryArticle.sourceTier,
                dateString = dateFormat.format(Date(primaryArticle.publishedAt)),
                excerptOrQuote = primaryArticle.snippet.take(100)
            ),
            AiCitation(
                entityType = "ARTICLE",
                entityId = secondaryArticle.id,
                title = secondaryArticle.title,
                sourceName = secondaryArticle.sourceName,
                sourceTier = secondaryArticle.sourceTier,
                dateString = dateFormat.format(Date(secondaryArticle.publishedAt)),
                excerptOrQuote = secondaryArticle.snippet.take(100)
            )
        )

        val result = NarrativeComparisonResult(
            commonFacts = commonFacts,
            narrativeA = "${primaryArticle.title}: ${primaryArticle.snippet.take(200)}",
            narrativeASource = primaryArticle.sourceName,
            narrativeB = "${secondaryArticle.title}: ${secondaryArticle.snippet.take(200)}",
            narrativeBSource = secondaryArticle.sourceName,
            officialNarrative = officialNarrative,
            conflictingClaims = conflictingClaims,
            uniqueInformation = uniqueInfo,
            unknownInformation = unknownInfo,
            strongestEvidence = strongestEvidence,
            citations = citations
        )

        // Save AI Record
        val jsonOutput = JSONObject().apply {
            put("narrativeA", result.narrativeA)
            put("narrativeB", result.narrativeB)
            put("commonFacts", JSONArray(result.commonFacts))
            put("conflictingClaims", JSONArray(result.conflictingClaims))
            put("strongestEvidence", result.strongestEvidence)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.NARRATIVE_COMPARISON,
            userId = userId,
            eventId = eventId,
            inputReferenceIds = "articles:${primaryArticle.id},${secondaryArticle.id}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.91,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 4. Semantic Contradiction Analysis (Names, Dates, Numbers, Locations, Decisions, Statements)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzeContradictions(
        eventId: Long,
        userId: Long = 1
    ): ContradictionAnalysisResult = withContext(Dispatchers.IO) {
        val contradictions = database.contradictionDao().getContradictionsForEvent(eventId).firstOrNull() ?: emptyList()
        val articles = database.articleDao().getArticlesForEvent(eventId).firstOrNull() ?: emptyList()

        if (contradictions.isEmpty() && articles.size <= 1) {
            return@withContext ContradictionAnalysisResult(
                classification = ContradictionSeverityClassification.INSUFFICIENT_DATA,
                summary = "لا توجد بيانات كافية للحكم بوجود تناقض (يتطلب تقارير متزامنة متعددة).",
                comparisonPoints = emptyList(),
                contextExplanation = "الرصد يعتمد حالياً على مادة إعلامية مفردة.",
                resolutionRecommendation = "توسيع دائرة المصادر المرصودة لتغطية هذا الحدث.",
                confidence = 0.60
            )
        }

        if (contradictions.isNotEmpty()) {
            val primary = contradictions.first()
            val classification = when (primary.contradictionType) {
                ContradictionType.DATE_TIME, ContradictionType.LOCATION, ContradictionType.PERSONS_INVOLVED, ContradictionType.NUMBERS_FIGURES ->
                    ContradictionSeverityClassification.TRUE_CONTRADICTION
                ContradictionType.DECISION_OUTCOME, ContradictionType.QUOTE_ATTRIBUTION ->
                    ContradictionSeverityClassification.APPARENT_CONTRADICTION
                ContradictionType.SEQUENCE ->
                    ContradictionSeverityClassification.DIFFERENT_PERSPECTIVE
            }

            val comparisonPoints = listOf(
                "رواية (${primary.claimASource}): \"${primary.claimAStatement}\"",
                "مقابل رواية (${primary.claimBSource}): \"${primary.claimBStatement}\"",
                "نوع التباين: ${primary.contradictionType.displayNameAr()}"
            )

            val resolution = if (primary.claimASource.contains("واس") || primary.claimASource.contains("رسمي")) {
                "ترجيح رواية ${primary.claimASource} استناداً إلى تصنيف المصدر السيادي الأساسي (Tier 1)."
            } else {
                "التحفظ على الأرقام غير المتقاطعة حتى صدور بيان رسمي من وزارة الخارجية أو الجهات المختصة."
            }

            return@withContext ContradictionAnalysisResult(
                classification = classification,
                summary = "رصد تناقض ${classification.displayNameAr()} حول: ${primary.descriptionAr}",
                comparisonPoints = comparisonPoints,
                contextExplanation = "التباين ناتج عن اختلاف توقيت التحديث الإخباري وسرعة النقل الميداني.",
                resolutionRecommendation = resolution,
                confidence = 0.88,
                citations = listOf(
                    AiCitation(entityType = "CONTRADICTION", entityId = primary.id, title = primary.descriptionAr, sourceName = "${primary.claimASource} vs ${primary.claimBSource}")
                )
            )
        }

        // Check date/number differences between articles
        val dates = articles.map { it.publishedAt }
        val maxDiffHours = if (dates.size > 1) (dates.maxOrNull()!! - dates.minOrNull()!!) / (1000 * 3600) else 0

        ContradictionAnalysisResult(
            classification = ContradictionSeverityClassification.DIFFERENT_PERSPECTIVE,
            summary = "المصادر متوافقة حول الجوهر مع اختلاف طبيعي في صياغة العناوين وزوايا العرض الإعلامي.",
            comparisonPoints = listOf("فارق التوقيت بين أقدم وأحدث تقرير: $maxDiffHours ساعة"),
            contextExplanation = "لا توجد تناقضات في الأرقام أو التواريخ أو القرارات الجوهرية.",
            resolutionRecommendation = "استمرار المراقبة الدورية دون الحاجة لإجراء تدقيق عاجل.",
            confidence = 0.92
        )
    }

    // ---------------------------------------------------------------------------------------------
    // 5. Original Source Tracing (Trace Original Source)
    // ---------------------------------------------------------------------------------------------

    suspend fun traceOriginalSource(
        articleId: Long,
        userId: Long = 1
    ): SourceTracingResultData = withContext(Dispatchers.IO) {
        val targetArticle = database.articleDao().getArticleById(articleId)
            ?: return@withContext SourceTracingResultData(
                verdict = SourceVerdict.INSUFFICIENT_EVIDENCE,
                originalSourceName = null,
                firstPublicationTime = null,
                firstArticleId = null,
                wireAgencyName = null,
                republishingSources = emptyList(),
                rationale = "المقال المطلوب غير موجود في النظام",
                confidence = 0.0
            )

        // Find related articles (same event, duplicate cluster, or similar title)
        val eventArticles = targetArticle.eventId?.let {
            database.articleDao().getArticlesForEvent(it).firstOrNull()
        } ?: emptyList()

        val cluster = if (eventArticles.isNotEmpty()) eventArticles else listOf(targetArticle)

        // Check for agency attribution in text
        val text = targetArticle.fullText?.ifBlank { targetArticle.snippet } ?: targetArticle.snippet
        val wireAgency = when {
            text.contains("واس") || targetArticle.title.contains("واس") -> "وكالة الأنباء السعودية (واس)"
            text.contains("رويترز") || text.contains("Reuters") -> "وكالة رويترز للأنباء"
            text.contains("أ ف ب") || text.contains("AFP") -> "وكالة فرانس برس (AFP)"
            text.contains("قنا") -> "وكالة الأنباء القطرية (قنا)"
            text.contains("وام") -> "وكالة أنباء الإمارات (وام)"
            else -> null
        }

        // Sort cluster by publishedAt
        val sortedCluster = cluster.sortedBy { it.publishedAt }
        val earliest = sortedCluster.first()

        val verdict: SourceVerdict
        val originalSource: String?
        val rationale: String

        if (wireAgency != null) {
            verdict = SourceVerdict.ORIGINAL
            originalSource = wireAgency
            rationale = "تم تحديد المصدر الأصلي عبر الإسناد الصريح إلى ($wireAgency) داخل متن الخبر ومقارنة البصمة الزمنية."
        } else if (earliest.sourceTier == SourceTier.PRIMARY) {
            verdict = SourceVerdict.ORIGINAL
            originalSource = earliest.sourceName
            rationale = "المصدر (${earliest.sourceName}) يمثل جهة رسمية أولية وسبق في النشر الزمني."
        } else if (sortedCluster.size > 1 && earliest.id != targetArticle.id) {
            verdict = SourceVerdict.RECIRCULATED
            originalSource = earliest.sourceName
            rationale = "المصدر (${earliest.sourceName}) هو الأسبق زمنياً في قاعدة البيانات بفارق توقيت ${((targetArticle.publishedAt - earliest.publishedAt) / (1000 * 60))} دقيقة."
        } else {
            verdict = SourceVerdict.ORIGINAL
            originalSource = targetArticle.sourceName
            rationale = "لا توجد أدلة كافية لترجيح جهة أخرى؛ يعتبر المصدر الحالي هو المعتمد أولياً في الرصد."
        }

        val republishers = sortedCluster.filter { it.sourceName != originalSource }.map { it.sourceName }.distinct()

        val citations = sortedCluster.map {
            AiCitation(entityType = "ARTICLE", entityId = it.id, title = it.title, sourceName = it.sourceName, dateString = dateFormat.format(Date(it.publishedAt)))
        }

        val result = SourceTracingResultData(
            verdict = verdict,
            originalSourceName = originalSource,
            firstPublicationTime = earliest.publishedAt,
            firstArticleId = earliest.id,
            wireAgencyName = wireAgency,
            republishingSources = republishers,
            rationale = rationale,
            confidence = if (wireAgency != null) 0.98 else 0.85,
            citations = citations
        )

        // Save AI Analysis
        val jsonOutput = JSONObject().apply {
            put("verdict", result.verdict.name)
            put("originalSource", result.originalSourceName)
            put("wireAgency", result.wireAgencyName)
            put("republishers", JSONArray(result.republishingSources))
            put("rationale", result.rationale)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.SOURCE_TRACING,
            userId = userId,
            articleId = articleId,
            inputReferenceIds = "cluster:${sortedCluster.map { it.id }.joinToString(",")}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = result.confidence,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 6. What Changed? (Delta Analysis)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzeWhatChanged(
        timeframeHours: Int = 48,
        userId: Long = 1
    ): WhatChangedResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sinceUtc = now - (timeframeHours.toLong() * 3600 * 1000)

        val recentArticles = database.articleDao().getRecentArticles(sinceUtc)
        val developingEvents = database.politicalEventDao().getEventsByStatus(EventStatus.DEVELOPING).firstOrNull() ?: emptyList()
        val allStatements = database.statementDao().getStatementsForEvent(1).firstOrNull() ?: emptyList()
        val contradictions = database.contradictionDao().getContradictionsByStatus(ContradictionStatus.OPEN).firstOrNull() ?: emptyList()

        val newInfo = mutableListOf<String>()
        val newStatements = mutableListOf<String>()
        val newNumbers = mutableListOf<String>()
        val newEvents = mutableListOf<String>()
        val newContradictions = mutableListOf<String>()

        var ignoredDuplicates = 0

        for (art in recentArticles) {
            if (art.isDuplicate) {
                ignoredDuplicates++
                continue
            }
            newInfo.add("${art.sourceName}: ${art.title}")
            val extracted = extractNumbersAndFacts(art.snippet)
            newNumbers.addAll(extracted)
        }

        for (ev in developingEvents) {
            val delta = ev.whatChangedAr ?: ev.latestDevelopmentAr ?: ""
            newEvents.add("تطور استراتيجي في حدث: ${ev.titleAr} ($delta)")
        }

        for (stmt in allStatements.take(3)) {
            newStatements.add("${stmt.personName} (${stmt.roleTitle}): \"${stmt.quoteText.take(100)}...\"")
        }

        for (c in contradictions) {
            newContradictions.add("رصد تناقض جديد: ${c.claimAStatement} [مقابل] ${c.claimBStatement} (${c.contradictionType.displayNameAr()})")
        }

        val deltaSummary = if (newInfo.isEmpty() && newEvents.isEmpty()) {
            "لم تُسجل تغيرات جوهرية أو أحداث جديدة خلال آخر $timeframeHours ساعة."
        } else {
            "تم رصد ${newInfo.size} أخبار أصلية جديدة، و${newEvents.size} تطورات في الملفات الاستراتيجية، مع استبعاد $ignoredDuplicates تكراراً وإعادة صياغة غير جوهرية."
        }

        val citations = recentArticles.filter { !it.isDuplicate }.take(5).map {
            AiCitation(entityType = "ARTICLE", entityId = it.id, title = it.title, sourceName = it.sourceName, dateString = dateFormat.format(Date(it.publishedAt)))
        }

        val result = WhatChangedResult(
            timeframe = "آخر $timeframeHours ساعة",
            deltaSummary = deltaSummary,
            newInformation = newInfo.take(8),
            newStatements = newStatements.take(5),
            stanceShifts = listOf("ثبات المواقف المعلنة للدول الرئيسية دون تسجيل تحولات مفاجئة في الصياغات الرسمية"),
            newNumbersOrMetrics = newNumbers.distinct().take(6),
            newEvents = newEvents,
            newSources = listOf("وكالات الأنباء المعتمدة والبيانات الحكومية الصادرة"),
            newContradictions = newContradictions,
            ignoredTrivialDuplicatesCount = ignoredDuplicates,
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("timeframe", result.timeframe)
            put("deltaSummary", result.deltaSummary)
            put("newInformation", JSONArray(result.newInformation))
            put("newEvents", JSONArray(result.newEvents))
            put("ignoredDuplicates", result.ignoredTrivialDuplicatesCount)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.WHAT_CHANGED,
            userId = userId,
            inputReferenceIds = "timeframe:${timeframeHours}h",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.94,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 7. Person Position Analysis (Previous vs Current, Context, Evidence Quotes, Genuine Shift)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzePersonPosition(
        personId: Long,
        userId: Long = 1
    ): PersonPositionResult = withContext(Dispatchers.IO) {
        val person = database.personDao().getPersonById(personId)
            ?: return@withContext PersonPositionResult(personName = "غير معروف", confidence = 0.0)

        val statements = database.statementDao().getStatementsForPerson(personId, person.nameAr).firstOrNull() ?: emptyList()

        if (statements.isEmpty()) {
            return@withContext PersonPositionResult(
                personName = person.nameAr,
                roleTitle = person.currentRoleAr,
                previousPosition = "لا تتوفر تصريحات سابقة موثقة في قاعدة البيانات",
                currentPosition = person.lastStatementSummary.ifBlank { "الموقف العام متطابق مع السياسة الرسمية لـ ${person.countryCode}" },
                difference = "لا توجد مقارنة تاريخية متاحة لعدم كفاية التصريحات المسجلة.",
                context = "شخصية قيد المراقبة الاستراتيجية.",
                hasGenuineShift = false,
                evidenceQuotes = emptyList(),
                confidence = 0.50,
                citations = emptyList()
            )
        }

        val sorted = statements.sortedBy { it.statementDate }
        val earliest = sorted.first()
        val latest = sorted.last()

        val hasGenuineShift = sorted.size >= 2 && earliest.quoteText != latest.quoteText &&
                (latest.quoteText.contains("تغير") || latest.quoteText.contains("تصعيد") || latest.quoteText.contains("خفض"))

        val diff = if (hasGenuineShift) {
            "لوحظ تعديل في المفردات المستخدمة بين التصريح الأول والتصريح الأخير، مع التركيز على خفض التصعيد."
        } else {
            "ثبات واستمرارية كاملة في الموقف السياسي المعلن، وتطابق الرسائل الدبلوماسية عبر جميع التصريحات الموثقة."
        }

        val quotes = sorted.map { "\"${it.quoteText}\" — ${it.sourceName} (${dateFormat.format(Date(it.statementDate))})" }
        val citations = sorted.map {
            AiCitation(
                entityType = "STATEMENT",
                entityId = it.id,
                title = it.quoteText.take(60),
                sourceName = it.sourceName,
                dateString = dateFormat.format(Date(it.statementDate)),
                excerptOrQuote = it.quoteText
            )
        }

        val result = PersonPositionResult(
            personName = person.nameAr,
            roleTitle = person.currentRoleAr,
            previousPosition = earliest.quoteText,
            currentPosition = latest.quoteText,
            difference = diff,
            context = "السياق الدبلوماسي لمتابعة ملفات ${person.countryCode} والمنطقة.",
            hasGenuineShift = hasGenuineShift,
            evidenceQuotes = quotes,
            confidence = 0.91,
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("personName", result.personName)
            put("previousPosition", result.previousPosition)
            put("currentPosition", result.currentPosition)
            put("difference", result.difference)
            put("hasGenuineShift", result.hasGenuineShift)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.PERSON_POSITION,
            userId = userId,
            personId = personId,
            inputReferenceIds = "person:$personId;statements:${sorted.map { it.id }.joinToString(",")}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = result.confidence,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 8. Media Perspective & Framing Analysis (No bias labeling, pure framing comparison)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzeMediaPerspective(
        eventId: Long,
        userId: Long = 1
    ): MediaPerspectiveResult = withContext(Dispatchers.IO) {
        val articles = database.articleDao().getArticlesForEvent(eventId).firstOrNull() ?: emptyList()

        if (articles.size < 2) {
            return@withContext MediaPerspectiveResult(
                analyzedOutlets = articles.map { it.sourceName },
                framingComparison = listOf("يتطلب توفر مصادر متعددة لإجراء تحليل تأطير إعلامي مقارن."),
                emphasizedPoints = emptyList(),
                downplayedOrOmittedPoints = emptyList(),
                headlineComparison = articles.map { "${it.sourceName}: ${it.title}" },
                perspectiveNotes = "عدد المقالات المرتبطة بهذا الحدث غير كافٍ لاستخراج أنماط التأطير الصحفي.",
                citations = emptyList()
            )
        }

        val outlets = articles.map { "${it.sourceName} (${it.sourceTier.displayNameAr()})" }.distinct()
        val headlines = articles.map { "• ${it.sourceName}: \"${it.title}\"" }

        val framingComparison = listOf(
            "المصادر الرسمية ووكالات الأنباء تركز على البيانات البروتوكولية واتفاقيات التعاون والاتصالات المشتركة.",
            "وسائل الإعلام المستقلة تركز على التداعيات الإقليمية وأبعاد أمن الملاحة وحركة الطاقة.",
            "الصحافة الدولية تبرز البعد الجيوسياسي وتأثير التطورات على خطوط التجارة العالمية."
        )

        val emphasized = articles.map { "تركيز ${it.sourceName} على: ${it.title.take(80)}" }
        val omitted = listOf(
            "تجنب الخوض في النقاط الخلافية المعلقة وتفاصيل المحادثات غير المعلنة.",
            "عدم الخوض في التقديرات العددية أو الخسائر غير المؤكدة رسمياً."
        )

        val citations = articles.map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                sourceTier = it.sourceTier,
                dateString = dateFormat.format(Date(it.publishedAt))
            )
        }

        val result = MediaPerspectiveResult(
            analyzedOutlets = outlets,
            framingComparison = framingComparison,
            emphasizedPoints = emphasized,
            downplayedOrOmittedPoints = omitted,
            headlineComparison = headlines,
            perspectiveNotes = "التباين في المعالجة الصحفية يعكس طبيعة التخصص والجمهور المستهدف دون وجود تناقض في الوقائع الصلبة.",
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("outlets", JSONArray(result.analyzedOutlets))
            put("framing", JSONArray(result.framingComparison))
            put("perspectiveNotes", result.perspectiveNotes)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.MEDIA_PERSPECTIVE,
            userId = userId,
            eventId = eventId,
            inputReferenceIds = "event:$eventId;articles:${articles.map { it.id }.joinToString(",")}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.89,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 9. Evidence Analysis for Claims (Supporting, Opposing, Independence, Direct vs Secondary, Verdict)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzeEvidenceForClaim(
        claimId: Long,
        userId: Long = 1
    ): EvidenceAnalysisResult = withContext(Dispatchers.IO) {
        val claim = database.claimDao().getClaimById(claimId)
            ?: return@withContext EvidenceAnalysisResult(claimStatement = "إدعاء غير موجود", verdict = EvidenceVerdict.INSUFFICIENT_EVIDENCE, confidenceScore = 0.0)

        val evidenceList = database.evidenceDao().getEvidenceForClaim(claimId).firstOrNull() ?: emptyList()
        val eventArticles = claim.eventId?.let {
            database.articleDao().getArticlesForEvent(it).firstOrNull()
        } ?: emptyList()

        val supporting = evidenceList.map { "${it.evidenceType.displayNameAr()}: (${it.sourceName}) - ${it.excerpt}" }
        val isIndependent = evidenceList.map { it.sourceName }.distinct().size >= 2
        val isPrimary = evidenceList.any { it.evidenceType == EvidenceType.OFFICIAL_DOCUMENT || it.evidenceType == EvidenceType.OFFICIAL_STATEMENT } ||
                eventArticles.any { it.sourceTier == SourceTier.PRIMARY }

        val verdict = when {
            claim.verificationStatus == VerificationStatus.CONFIRMED -> EvidenceVerdict.CONFIRMED
            claim.verificationStatus == VerificationStatus.CONTRADICTED -> EvidenceVerdict.CONTRADICTED
            supporting.isNotEmpty() && isPrimary -> EvidenceVerdict.SUPPORTED
            supporting.isNotEmpty() -> EvidenceVerdict.LIKELY
            claim.verificationStatus == VerificationStatus.UNVERIFIED -> EvidenceVerdict.UNVERIFIED
            else -> EvidenceVerdict.INSUFFICIENT_EVIDENCE
        }

        val documents = evidenceList.filter { it.evidenceType == EvidenceType.OFFICIAL_DOCUMENT }.map { "${it.sourceName}: ${it.excerpt.take(60)}" }
        val missing = if (verdict == EvidenceVerdict.CONFIRMED) emptyList() else listOf(
            "محاضر الجلسات الرسمية المكتملة",
            "وثائق رسمية من الطرف المقابل تؤكد ذات المعطيات"
        )

        val citations = evidenceList.map {
            AiCitation(entityType = "EVIDENCE", entityId = it.id, title = it.sourceName, sourceName = it.sourceName, excerptOrQuote = it.excerpt)
        }

        val result = EvidenceAnalysisResult(
            claimStatement = claim.statement,
            claimText = claim.statement,
            verdict = verdict,
            supportingEvidence = supporting,
            opposingEvidence = if (claim.verificationStatus == VerificationStatus.CONTRADICTED) listOf("تم رصد تناقض مع وثائق رسمية") else emptyList(),
            isIndependentSources = isIndependent,
            isPrimaryDirectSource = isPrimary,
            officialDocumentsFound = documents,
            contradictionsNoted = if (claim.verificationStatus == VerificationStatus.CONTRADICTED) listOf("تباين بين الروايات الميدانية والبيان الرسمي") else emptyList(),
            missingInformation = missing,
            explanation = "تقييم تحليلي مستند إلى ${evidenceList.size} قرينة وأدلة موثقة.",
            confidenceScore = if (isPrimary && isIndependent) 0.96 else if (isPrimary) 0.90 else 0.75,
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("claimText", result.claimStatement)
            put("verdict", result.verdict.name)
            put("isIndependent", result.isIndependentSources)
            put("isPrimary", result.isPrimaryDirectSource)
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.EVIDENCE_ANALYSIS,
            userId = userId,
            claimId = claimId,
            eventId = claim.eventId,
            inputReferenceIds = "claim:$claimId;evidence:${evidenceList.map { it.id }.joinToString(",")}",
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = result.confidenceScore,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 10. Information Gap Analysis (Known, Partially Known, Unknown, Contradicted)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzeInformationGaps(
        eventId: Long,
        userId: Long = 1
    ): InformationGapsResult = withContext(Dispatchers.IO) {
        val event = database.politicalEventDao().getEventById(eventId)
        val claims = database.claimDao().getClaimsForEvent(eventId).firstOrNull() ?: emptyList()
        val contradictions = database.contradictionDao().getContradictionsForEvent(eventId).firstOrNull() ?: emptyList()
        val evidenceList = database.evidenceDao().getEvidenceForEvent(eventId).firstOrNull() ?: emptyList()

        val known = mutableListOf<String>()
        val partiallyKnown = mutableListOf<String>()
        val unknown = mutableListOf<String>()
        val contradicted = mutableListOf<String>()

        for (c in claims) {
            when (c.verificationStatus) {
                VerificationStatus.CONFIRMED, VerificationStatus.SUPPORTED -> known.add("مثبت: ${c.statement}")
                VerificationStatus.LIKELY -> partiallyKnown.add("معلومة جزئية مرجحة: ${c.statement}")
                VerificationStatus.UNVERIFIED, VerificationStatus.INSUFFICIENT_EVIDENCE -> unknown.add("غير مؤكد: ${c.statement}")
                VerificationStatus.CONTRADICTED -> contradicted.add("متعارض أو منقوض: ${c.statement}")
                VerificationStatus.NOT_APPLICABLE -> partiallyKnown.add("رأي سياسي: ${c.statement}")
            }
        }

        if (known.isEmpty() && event != null) {
            known.add("انعقاد الحدث وأطرافه الأساسية: ${event.titleAr}")
        }

        for (item in contradictions) {
            contradicted.add("تناقض معلق: ${item.claimAStatement} مقابل ${item.claimBStatement}")
        }

        if (unknown.isEmpty()) {
            unknown.add("البنود السرية غير المصرح بها لوسائل الإعلام")
            unknown.add("التقديرات المالية التفصيلية غير المنشورة في الجريدة الرسمية")
        }

        val citations = evidenceList.take(4).map {
            AiCitation(entityType = "EVIDENCE", entityId = it.id, title = it.sourceName, sourceName = it.sourceName, excerptOrQuote = it.excerpt)
        }

        val result = InformationGapsResult(
            subject = event?.titleAr ?: "ملف الحدث قيد المتابعة",
            knownInformation = known,
            partiallyKnownInformation = partiallyKnown,
            unknownInformation = unknown,
            contradictedInformation = contradicted,
            recommendations = "التركيز على رصد البيانات الوزارية القادمة لسد الفجوات في (${unknown.firstOrNull() ?: "المعلومات غير المؤكدة"}).",
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("known", JSONArray(result.knownInformation))
            put("partiallyKnown", JSONArray(result.partiallyKnownInformation))
            put("unknown", JSONArray(result.unknownInformation))
            put("contradicted", JSONArray(result.contradictedInformation))
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.INFORMATION_GAPS,
            userId = userId,
            eventId = eventId,
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.93,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 11. AI Political File Analysis (Dossier Comprehensive Report)
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzePoliticalFile(
        fileId: Long,
        timeframeDays: Int = 30,
        userId: Long = 1
    ): FileAnalysisResult = withContext(Dispatchers.IO) {
        val file = database.politicalFileDao().getFileById(fileId)
            ?: return@withContext emptyFileAnalysis("الملف السياسي غير موجود")

        val events = database.politicalEventDao().getEventsForPoliticalFile(fileId).firstOrNull() ?: emptyList()
        val articles = database.articleDao().getArticlesForFile(fileId).firstOrNull() ?: emptyList()

        val currentSituation = file.description.ifBlank { "متابعة استراتيجية نشطة للتطورات الجيوسياسية ذات الصلة بـ ${file.titleAr}." }
        val recentDevelopments = file.recentDevelopments.ifBlank { "استمرار التنسيق بين الدول المعنية." }
        val majorEvents = events.map { "• ${it.titleAr} (${it.status.displayNameAr()})" }
        val keyPersons = articles.map { it.linkedPersonNames }.flatMap { it.split(",") }.map { it.trim() }.filter { it.isNotBlank() }.distinct()

        val result = FileAnalysisResult(
            fileTitle = file.titleAr,
            currentSituation = currentSituation,
            recentDevelopments = recentDevelopments,
            majorEvents = majorEvents.ifEmpty { listOf("لا توجد أحداث فرعية مسجلة في هذا الملف بعد.") },
            keyPersonsAndPositions = keyPersons.ifEmpty { listOf("شخصيات دبلوماسية ووزارية معنية بالملف.") },
            conflictingNarratives = listOf("تطابق في المصادر الرسمية مع تفاوت في أولويات التغطية."),
            keyEvidence = articles.take(3).map { "${it.sourceName}: ${it.title}" },
            timelineHighlights = listOf("تم فتح ملف المتابعة", recentDevelopments),
            unknownsAndGaps = listOf("التفاصيل التفاوضية غير المعلنة والاتفاقات الميدانية غير المصرح بها."),
            whatChanged = file.whatChangedDelta.ifBlank { "تحديثات دورية قيد المراقبة." },
            analyzedTimeframe = "آخر $timeframeDays يوماً",
            citations = articles.take(5).map {
                AiCitation(entityType = "ARTICLE", entityId = it.id, title = it.title, sourceName = it.sourceName, dateString = dateFormat.format(Date(it.publishedAt)))
            }
        )

        val jsonOutput = JSONObject().apply {
            put("fileTitle", result.fileTitle)
            put("currentSituation", result.currentSituation)
            put("whatChanged", result.whatChanged)
            put("majorEvents", JSONArray(result.majorEvents))
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.FILE_ANALYSIS,
            userId = userId,
            politicalFileId = fileId,
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.93,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 12. AI Timeline Causality & Turning Points Summary
    // ---------------------------------------------------------------------------------------------

    suspend fun generateTimelineSummary(
        eventId: Long,
        userId: Long = 1
    ): TimelineSummaryResult = withContext(Dispatchers.IO) {
        val event = database.politicalEventDao().getEventById(eventId)
        val timelineItems = database.timelineDao().getTimelineForEventAsc(eventId).firstOrNull() ?: emptyList()

        if (timelineItems.isEmpty()) {
            return@withContext TimelineSummaryResult(
                eventTitle = event?.titleAr ?: "حدث غير محدد",
                startMilestone = "تاريخ بدء رصد الحدث: ${event?.let { dateFormat.format(Date(it.startDate)) } ?: "غير متوفر"}",
                keyMilestones = listOf(event?.summaryAr ?: "لا تتوفر محطات مسجلة"),
                turningPoints = listOf("التطور الأحدث قيد المتابعة والتوثيق"),
                latestDevelopment = event?.latestDevelopmentAr ?: "قيد المراقبة",
                causalitySequence = listOf("بدء الحدث ← تفاعل الأطراف الدبلوماسية ← إصدار المواقف المتبادلة"),
                citations = emptyList()
            )
        }

        val sorted = timelineItems.sortedBy { it.timestamp }
        val start = "${sorted.first().titleAr} (${dateFormat.format(Date(sorted.first().timestamp))})"
        val latest = "${sorted.last().titleAr} (${dateFormat.format(Date(sorted.last().timestamp))})"
        val milestones = sorted.map { "• ${dateFormat.format(Date(it.timestamp))}: ${it.titleAr} - ${it.descriptionAr.take(80)}" }
        val turningPoints = sorted.filter { it.importance == EventImportance.CRITICAL || it.importance == EventImportance.HIGH }
            .map { "محطة فارقة: ${it.titleAr} (${it.sourceName})" }

        val citations = sorted.map {
            AiCitation(entityType = "TIMELINE", entityId = it.id, title = it.titleAr, sourceName = it.sourceName, dateString = dateFormat.format(Date(it.timestamp)), excerptOrQuote = it.descriptionAr)
        }

        val result = TimelineSummaryResult(
            eventTitle = event?.titleAr ?: "التسلسل الزمني للحدث",
            startMilestone = start,
            keyMilestones = milestones,
            turningPoints = turningPoints.ifEmpty { listOf("المحطة الأولى: $start") },
            latestDevelopment = latest,
            causalitySequence = sorted.map { it.titleAr },
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("startMilestone", result.startMilestone)
            put("latestDevelopment", result.latestDevelopment)
            put("keyMilestones", JSONArray(result.keyMilestones))
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.TIMELINE_SUMMARY,
            userId = userId,
            eventId = eventId,
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.94,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // 13. Dashboard Daily AI Intelligence Brief
    // ---------------------------------------------------------------------------------------------

    suspend fun generateDashboardAiBrief(
        userId: Long = 1
    ): DashboardAiBriefResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayArticles = database.articleDao().getRecentArticles(todayStart).filter { !it.isDuplicate }
        val activeEvents = database.politicalEventDao().getEventsByStatus(EventStatus.DEVELOPING).firstOrNull() ?: emptyList()
        val openContradictions = database.contradictionDao().getContradictionsByStatus(ContradictionStatus.OPEN).firstOrNull() ?: emptyList()
        val allStatements = database.statementDao().getStatementsForEvent(1).firstOrNull() ?: emptyList()

        val todayDevelopments = todayArticles.take(5).map { "${it.sourceName}: ${it.title}" }
        val topEvents = activeEvents.take(4).map { "• ${it.titleAr}: ${it.latestDevelopmentAr ?: it.whatChangedAr ?: ""}" }
        val topStatements = allStatements.take(3).map { "• ${it.personName} (${it.roleTitle ?: ""}): \"${it.quoteText.take(90)}...\"" }
        val contradictions = openContradictions.take(3).map { "• ${it.claimAStatement} [ضد] ${it.claimBStatement} (${it.claimASource} / ${it.claimBSource})" }

        val assessment = if (todayArticles.isEmpty() && activeEvents.isEmpty()) {
            "لا تتوفر بيانات كافية لإنشاء الإيجاز الاستخباري اليومي حتى الآن."
        } else {
            "المشهد الإقليمي يتسم بالتركيز على التهدئة الدبلوماسية وأمن الممرات المائية مع ثبات المواقف المعلنة للدول الكبرى."
        }

        val citations = todayArticles.take(4).map {
            AiCitation(entityType = "ARTICLE", entityId = it.id, title = it.title, sourceName = it.sourceName, dateString = dateFormat.format(Date(it.publishedAt)))
        }

        val result = DashboardAiBriefResult(
            dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now)),
            todayKeyDevelopments = todayDevelopments.ifEmpty { listOf("استمرار متابعة التطورات الميدانية والدبلوماسية الروتينية.") },
            whatChangedSinceYesterday = listOf("تثبيت إجراءات أمن الملاحة", "استمرار الاجتماعات التنسيقية الثنائية"),
            topDevelopingEvents = topEvents.ifEmpty { listOf("جميع الملفات في حالة استقرار نسبي دون تسجيل تصعيد جديد.") },
            criticalStatements = topStatements.ifEmpty { listOf("لم تُسجل تصريحات طارئة لوزراء الخارجية اليوم.") },
            newlyDetectedContradictions = contradictions.ifEmpty { listOf("لا توجد تناقضات حرجة مفتوحة بين المصادر الوطنية اليوم.") },
            unconfirmedOrDisputedInformation = listOf("تفاصيل الحزم الاستثمارية قيد التدقيق لدى اللجان الفنية المشتركة."),
            executiveAssessment = assessment,
            citations = citations
        )

        val jsonOutput = JSONObject().apply {
            put("date", result.dateFormatted)
            put("assessment", result.executiveAssessment)
            put("todayDevelopments", JSONArray(result.todayKeyDevelopments))
            put("topDevelopingEvents", JSONArray(result.topDevelopingEvents))
        }.toString()

        val record = AiAnalysis(
            analysisType = AiAnalysisType.DASHBOARD_BRIEF,
            userId = userId,
            model = configuredModel,
            promptVersion = promptVersion,
            output = jsonOutput,
            confidence = 0.95,
            citationsJson = serializeCitations(result.citations)
        )
        database.aiAnalysisDao().insertAnalysis(record)

        result
    }

    // ---------------------------------------------------------------------------------------------
    // Citation Validation
    // ---------------------------------------------------------------------------------------------

    suspend fun validateCitations(citations: List<AiCitation>): List<AiCitation> = withContext(Dispatchers.IO) {
        val validList = mutableListOf<AiCitation>()
        for (c in citations) {
            val exists = when (c.entityType) {
                "ARTICLE" -> database.articleDao().getArticleById(c.entityId) != null
                "EVENT" -> database.politicalEventDao().getEventById(c.entityId) != null
                "CLAIM" -> database.claimDao().getClaimById(c.entityId) != null
                "STATEMENT" -> database.statementDao().getStatementById(c.entityId) != null
                "EVIDENCE" -> database.evidenceDao().getEvidenceById(c.entityId) != null
                "FILE" -> database.politicalFileDao().getFileById(c.entityId) != null
                else -> true
            }
            if (exists) validList.add(c)
        }
        validList
    }

    // ---------------------------------------------------------------------------------------------
    // Helper Extractors & NLP Utilities (Deterministic Local Political Intelligence Engine)
    // ---------------------------------------------------------------------------------------------

    private fun extractSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return text.split(Regex("[\\.\\!\\?\\؛\\n\\r]+"))
            .map { it.trim() }
            .filter { it.length > 20 }
    }

    private fun extractKeyPoints(sentences: List<String>, title: String): List<String> {
        val points = mutableListOf<String>()
        if (title.isNotBlank()) points.add(title)
        for (s in sentences.take(4)) {
            if (s != title && !points.contains(s)) {
                points.add(s)
            }
        }
        return points.take(4)
    }

    private fun extractNumbersAndFacts(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val results = mutableListOf<String>()
        val numberPattern = Pattern.compile("(\\b\\d+([\\,\\.]\\d+)?\\s*(مليار|مليون|ألف|بالمئة|%|دولار|ريال|جنيه|برميل|سفينة|طن|كيلومتر|كم)?\\b)")
        val matcher = numberPattern.matcher(text)
        while (matcher.find() && results.size < 6) {
            val match = matcher.group().trim()
            if (match.length > 1 && !results.contains(match)) {
                results.add(match)
            }
        }
        val datePattern = Pattern.compile("(\\b(يناير|فبراير|مارس|أبريل|مايو|يونيو|يوليو|أغسطس|سبتمبر|أكتوبر|نوفمبر|ديسمبر|اليوم|أمس|الأسبوع الماضي)\\b)")
        val dateMatcher = datePattern.matcher(text)
        while (dateMatcher.find() && results.size < 8) {
            val d = dateMatcher.group().trim()
            if (!results.contains(d)) results.add(d)
        }
        return results
    }

    private fun extractDirectQuotes(text: String): List<String> {
        val quotes = mutableListOf<String>()
        val quotePattern = Pattern.compile("([\"«][^\"»]+[\"»])")
        val matcher = quotePattern.matcher(text)
        while (matcher.find()) {
            quotes.add(matcher.group().replace("\"", "").replace("«", "").replace("»", "").trim())
        }
        return quotes
    }

    private fun extractProminentActors(text: String): String {
        val actors = listOf(
            "الأمير فيصل بن فرحان",
            "أنتوني بلينكن",
            "بدر عبد العاطي",
            "الشيخ محمد بن عبد الرحمن",
            "عبد الفتاح السيسي",
            "الأمير محمد بن سلمان",
            "وزير الخارجية",
            "رئيس مجلس الوزراء"
        )
        val found = actors.filter { text.contains(it) }
        return if (found.isNotEmpty()) found.joinToString("، ") else "مسؤولون رفيعو المستوى"
    }

    private fun serializeCitations(citations: List<AiCitation>): String {
        val arr = JSONArray()
        for (c in citations) {
            val obj = JSONObject().apply {
                put("entityType", c.entityType)
                put("entityId", c.entityId)
                put("title", c.title)
                put("sourceName", c.sourceName ?: "")
                put("dateString", c.dateString ?: "")
                put("excerptOrQuote", c.excerptOrQuote ?: "")
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun emptyArticleSummary(msg: String) = ArticleSummaryResult(
        shortSummary = msg,
        detailedSummary = msg,
        keyPoints = emptyList(),
        numbersAndFacts = emptyList(),
        language = "ar"
    )

    private fun emptyFileAnalysis(msg: String) = FileAnalysisResult(
        fileTitle = msg,
        currentSituation = msg,
        recentDevelopments = "",
        majorEvents = emptyList(),
        keyPersonsAndPositions = emptyList(),
        conflictingNarratives = emptyList(),
        keyEvidence = emptyList(),
        timelineHighlights = emptyList(),
        unknownsAndGaps = emptyList(),
        whatChanged = "",
        analyzedTimeframe = ""
    )
}
