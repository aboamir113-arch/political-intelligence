package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AiAnalysisType {
    ARTICLE_SUMMARY,
    EVENT_SUMMARY,
    NARRATIVE_COMPARISON,
    CONTRADICTION_ANALYSIS,
    SOURCE_TRACING,
    WHAT_CHANGED,
    PERSON_POSITION,
    MEDIA_PERSPECTIVE,
    EVIDENCE_ANALYSIS,
    INFORMATION_GAPS,
    FILE_ANALYSIS,
    TIMELINE_SUMMARY,
    DASHBOARD_BRIEF;

    fun displayNameAr(): String = when (this) {
        ARTICLE_SUMMARY -> "تلخيص مقال تحليلي"
        EVENT_SUMMARY -> "موجز الحدث الاستراتيجي"
        NARRATIVE_COMPARISON -> "مقارنة الروايات وتعدد المصادر"
        CONTRADICTION_ANALYSIS -> "تحليل التناقضات الدلالية"
        SOURCE_TRACING -> "تتبع المصدر الأصلي والناقل"
        WHAT_CHANGED -> "تحليل ماذا تغير؟ (الدلتا)"
        PERSON_POSITION -> "تحليل مواقف الشخصيات"
        MEDIA_PERSPECTIVE -> "تحليل التغطية والتأطير الإعلامي"
        EVIDENCE_ANALYSIS -> "فحص الأدلة وقوة الحجج"
        INFORMATION_GAPS -> "رصد فجوات المعلومات الناقصة"
        FILE_ANALYSIS -> "التقرير الشامل للملف السياسي"
        TIMELINE_SUMMARY -> "موجز التسلسل الزمني والسببي"
        DASHBOARD_BRIEF -> "الإيجاز الاستخباري اليومي"
    }

    fun displayNameEn(): String = when (this) {
        ARTICLE_SUMMARY -> "Article Summary"
        EVENT_SUMMARY -> "Event Dossier Summary"
        NARRATIVE_COMPARISON -> "Narrative Comparison"
        CONTRADICTION_ANALYSIS -> "Contradiction Analysis"
        SOURCE_TRACING -> "Source Tracing"
        WHAT_CHANGED -> "What Changed (Delta)"
        PERSON_POSITION -> "Person Position Analysis"
        MEDIA_PERSPECTIVE -> "Media Perspective & Framing"
        EVIDENCE_ANALYSIS -> "Evidence & Claim Analysis"
        INFORMATION_GAPS -> "Information Gaps Analysis"
        FILE_ANALYSIS -> "Political File Report"
        TIMELINE_SUMMARY -> "Timeline Causality Summary"
        DASHBOARD_BRIEF -> "Daily Intelligence Brief"
    }
}

enum class EvidenceVerdict {
    CONFIRMED,
    SUPPORTED,
    LIKELY,
    UNVERIFIED,
    DISPUTED,
    REFUTED,
    CONTRADICTED,
    INSUFFICIENT_EVIDENCE;

    fun displayNameAr(): String = when (this) {
        CONFIRMED -> "مؤكد بأدلة قاطعة"
        SUPPORTED -> "مدعوم بشواهد موثوقة"
        LIKELY -> "مرجح وقيد التثبت"
        UNVERIFIED -> "غير مؤكد / إدعاء أولي"
        DISPUTED -> "محل خلاف ونزاع"
        REFUTED -> "مفند ومدحوض"
        CONTRADICTED -> "متناقض مع وثائق أخرى"
        INSUFFICIENT_EVIDENCE -> "أدلة غير كافية للحسم"
    }
}

enum class InformationGapStatus(val nameAr: String) {
    KNOWN("معلومة مثبتة"),
    PARTIALLY_KNOWN("معلومة جزئية ناقصة"),
    UNKNOWN("مجهول / لا تتوفر بيانات"),
    CONTRADICTED("معلومات متعارضة"),
    UNVERIFIED("غير متحقق منها بما يكفي");

    fun displayNameAr(): String = nameAr
}

enum class SourceVerdict {
    ORIGINAL,
    RECIRCULATED,
    DISPUTED,
    INSUFFICIENT_EVIDENCE;

    fun displayNameAr(): String = when (this) {
        ORIGINAL -> "المصدر الأصلي للخبر"
        RECIRCULATED -> "مصدر ناقل معاد تدويره"
        DISPUTED -> "مصدر متنازع عليه"
        INSUFFICIENT_EVIDENCE -> "أدلة غير كافية للحكم"
    }

    fun displayNameEn(): String = when (this) {
        ORIGINAL -> "Original Source"
        RECIRCULATED -> "Recirculated"
        DISPUTED -> "Disputed"
        INSUFFICIENT_EVIDENCE -> "Insufficient Evidence"
    }
}

enum class SourceTracingResult {
    ORIGINAL_SOURCE_FOUND,
    LIKELY_ORIGINAL_SOURCE,
    MULTIPLE_POSSIBLE_SOURCES,
    SOURCE_NOT_FOUND;

    fun displayNameAr(): String = when (this) {
        ORIGINAL_SOURCE_FOUND -> "تم تحديد المصدر الأصلي بدقة"
        LIKELY_ORIGINAL_SOURCE -> "المصدر المرجح لنشوء الخبر"
        MULTIPLE_POSSIBLE_SOURCES -> "مصادر متعددة محتملة النشوء"
        SOURCE_NOT_FOUND -> "تعذر تحديد المصدر الأول"
    }
}

enum class ContradictionSeverityClassification {
    TRUE_CONTRADICTION,
    APPARENT_CONTRADICTION,
    DIFFERENT_PERSPECTIVE,
    INSUFFICIENT_DATA;

    fun displayNameAr(): String = when (this) {
        TRUE_CONTRADICTION -> "تناقض فعلي صريح"
        APPARENT_CONTRADICTION -> "تناقض ظاهري يفسره السياق"
        DIFFERENT_PERSPECTIVE -> "اختلاف في التغطية وزاوية الرصد"
        INSUFFICIENT_DATA -> "بيانات غير كافية للحكم"
    }
}

data class AiCitation(
    val entityType: String = "ARTICLE", // ARTICLE, EVENT, SOURCE, STATEMENT, CLAIM, EVIDENCE
    val entityId: Long = 0,
    val title: String = "",
    val sourceName: String = "",
    val sourceTier: SourceTier = SourceTier.TRUSTED_MEDIA,
    val articleTitle: String = title,
    val excerpt: String = "",
    val dateString: String? = null,
    val excerptOrQuote: String? = excerpt
)

@Entity(
    tableName = "ai_analyses",
    indices = [
        Index(value = ["analysisType"]),
        Index(value = ["articleId"]),
        Index(value = ["eventId"]),
        Index(value = ["politicalFileId"]),
        Index(value = ["personId"]),
        Index(value = ["claimId"]),
        Index(value = ["createdAt"])
    ]
)
data class AiAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val analysisType: AiAnalysisType,
    val userId: Long = 1,
    val articleId: Long? = null,
    val eventId: Long? = null,
    val claimId: Long? = null,
    val politicalFileId: Long? = null,
    val personId: Long? = null,
    val inputReferenceIds: String = "",
    val model: String = "gemini-3.5-flash",
    val promptVersion: String = "v1.0",
    val output: String,
    val confidence: Double = 0.92,
    val citationsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Typed Data Models for Analytical Consumption
data class ArticleSummaryResult(
    val shortSummary: String = "",
    val detailedSummary: String = "",
    val keyPoints: List<String> = emptyList(),
    val numbersAndFacts: List<String> = emptyList(),
    val translatedSummary: String? = null,
    val language: String = "ar",
    val whatHappened: String = "",
    val who: String = "",
    val whatWasSaid: String = "",
    val where: String = "",
    val whenTime: String = "",
    val whatIsNew: String = "",
    val whatIsUncertain: String = "",
    val modelUsed: String = "gemini-3.5-flash",
    val confidenceScore: Double = 0.94,
    val citations: List<AiCitation> = emptyList()
) {
    val keyBulletPoints: List<String> get() = keyPoints
}

data class EventSummaryResult(
    val eventTitle: String = "",
    val executiveSummary: String = "",
    val whatHappened: String = "",
    val whatChanged: String = "",
    val keyActorsAndStances: String = "",
    val whoIsInvolved: String = "",
    val whatIsConfirmed: String = "",
    val confirmedFacts: List<String> = emptyList(),
    val whatIsUncertain: String = "",
    val uncertainInfo: List<String> = emptyList(),
    val conflictingInformation: String = "",
    val conflictingInfo: List<String> = emptyList(),
    val citations: List<AiCitation> = emptyList()
)

data class SourcePerspectiveItem(
    val sourceName: String,
    val tier: SourceTier = SourceTier.TRUSTED_MEDIA,
    val framing: String = "",
    val narrativeContent: String = ""
)

data class NarrativeComparisonResult(
    val synthesisOverview: String = "",
    val commonFacts: List<String> = emptyList(),
    val consensusPoints: List<String> = emptyList(),
    val disputedPoints: List<String> = emptyList(),
    val sourcePerspectives: List<SourcePerspectiveItem> = emptyList(),
    val narrativeA: String = "",
    val narrativeASource: String = "",
    val narrativeB: String = "",
    val narrativeBSource: String = "",
    val officialNarrative: String? = null,
    val conflictingClaims: List<String> = emptyList(),
    val uniqueInformation: List<String> = emptyList(),
    val unknownInformation: List<String> = emptyList(),
    val strongestEvidence: String = "",
    val citations: List<AiCitation> = emptyList()
)

data class ContradictionAnalysisItem(
    val disputedTopic: String,
    val severity: ContradictionSeverityClassification = ContradictionSeverityClassification.TRUE_CONTRADICTION,
    val partyA: String,
    val statementA: String,
    val partyB: String,
    val statementB: String,
    val explanation: String
)

data class ContradictionAnalysisResult(
    val overallVerdict: String = "",
    val classification: ContradictionSeverityClassification = ContradictionSeverityClassification.TRUE_CONTRADICTION,
    val summary: String = "",
    val items: List<ContradictionAnalysisItem> = emptyList(),
    val comparisonPoints: List<String> = emptyList(),
    val contextExplanation: String = "",
    val resolutionRecommendation: String = "",
    val confidence: Double = 0.9,
    val citations: List<AiCitation> = emptyList()
)

data class SourceTracingResultData(
    val verdict: SourceVerdict = SourceVerdict.ORIGINAL,
    val originalSourceName: String? = null,
    val firstPublicationTime: Long? = null,
    val firstArticleId: Long? = null,
    val wireAgencyName: String? = null,
    val republishingSources: List<String> = emptyList(),
    val rationale: String = "",
    val explanation: String = rationale,
    val attributionChain: List<String> = emptyList(),
    val confidence: Double = 0.92,
    val citations: List<AiCitation> = emptyList()
)

data class WhatChangedResult(
    val timeframe: String = "48h",
    val deltaSummary: String = "",
    val evolvingEvents: List<String> = emptyList(),
    val newInformation: List<String> = emptyList(),
    val newStatements: List<String> = emptyList(),
    val stanceShifts: List<String> = emptyList(),
    val newNumbersOrMetrics: List<String> = emptyList(),
    val newEvents: List<String> = emptyList(),
    val newSources: List<String> = emptyList(),
    val newContradictions: List<String> = emptyList(),
    val ignoredTrivialDuplicatesCount: Int = 0,
    val citations: List<AiCitation> = emptyList()
)

data class PersonPositionResult(
    val personName: String = "",
    val roleTitle: String = "",
    val currentStance: String = "",
    val previousPosition: String = "",
    val currentPosition: String = "",
    val evolutionOverTime: List<String> = emptyList(),
    val notedContradictions: List<String> = emptyList(),
    val difference: String = "",
    val context: String = "",
    val hasGenuineShift: Boolean = false,
    val evidenceQuotes: List<String> = emptyList(),
    val confidence: Double = 0.88,
    val citations: List<AiCitation> = emptyList()
)

data class MediaCoverageAngleItem(
    val category: String,
    val focusPoints: List<String> = emptyList(),
    val loadedLanguageUsed: List<String> = emptyList()
)

data class MediaPerspectiveResult(
    val overallFramingSummary: String = "",
    val coverageAngles: List<MediaCoverageAngleItem> = emptyList(),
    val analyzedOutlets: List<String> = emptyList(),
    val framingComparison: List<String> = emptyList(),
    val emphasizedPoints: List<String> = emptyList(),
    val downplayedOrOmittedPoints: List<String> = emptyList(),
    val headlineComparison: List<String> = emptyList(),
    val perspectiveNotes: String = "",
    val citations: List<AiCitation> = emptyList()
)

data class EvidenceAnalysisResult(
    val claimStatement: String = "",
    val claimText: String = claimStatement,
    val verdict: EvidenceVerdict = EvidenceVerdict.SUPPORTED,
    val supportingEvidence: List<String> = emptyList(),
    val opposingEvidence: List<String> = emptyList(),
    val isIndependentSources: Boolean = true,
    val isPrimaryDirectSource: Boolean = false,
    val officialDocumentsFound: List<String> = emptyList(),
    val contradictionsNoted: List<String> = emptyList(),
    val missingInformation: List<String> = emptyList(),
    val explanation: String = "",
    val confidenceScore: Double = 0.9,
    val citations: List<AiCitation> = emptyList()
)

data class InformationGapsResult(
    val subject: String = "",
    val overview: String = "",
    val knownInformation: List<String> = emptyList(),
    val partiallyKnownInformation: List<String> = emptyList(),
    val unknownInformation: List<String> = emptyList(),
    val unansweredQuestions: List<String> = emptyList(),
    val contradictedInformation: List<String> = emptyList(),
    val recommendedVerifications: List<String> = emptyList(),
    val recommendations: String = "",
    val citations: List<AiCitation> = emptyList()
)

data class FileAnalysisResult(
    val fileTitle: String = "",
    val strategicOverview: String = "",
    val currentSituation: String = "",
    val recentDevelopments: String = "",
    val majorEvents: List<String> = emptyList(),
    val mainTrajectories: List<String> = emptyList(),
    val forwardLookingOutlook: List<String> = emptyList(),
    val keyPersonsAndPositions: List<String> = emptyList(),
    val conflictingNarratives: List<String> = emptyList(),
    val keyEvidence: List<String> = emptyList(),
    val timelineHighlights: List<String> = emptyList(),
    val unknownsAndGaps: List<String> = emptyList(),
    val whatChanged: String = "",
    val analyzedTimeframe: String = "30d",
    val citations: List<AiCitation> = emptyList()
)

data class TimelineSummaryResult(
    val eventTitle: String = "",
    val narrativeArc: String = "",
    val startMilestone: String = "",
    val keyMilestones: List<String> = emptyList(),
    val turningPoints: List<String> = emptyList(),
    val latestDevelopment: String = "",
    val causalitySequence: List<String> = emptyList(),
    val citations: List<AiCitation> = emptyList()
)

data class DashboardAiBriefResult(
    val dateFormatted: String = "",
    val overallSituationBrief: String = "",
    val executiveAssessment: String = overallSituationBrief,
    val todayKeyDevelopments: List<String> = emptyList(),
    val whatChangedSinceYesterday: List<String> = emptyList(),
    val topDevelopingEvents: List<String> = emptyList(),
    val criticalStatements: List<String> = emptyList(),
    val newlyDetectedContradictions: List<String> = emptyList(),
    val unconfirmedOrDisputedInformation: List<String> = emptyList(),
    val citations: List<AiCitation> = emptyList()
)
