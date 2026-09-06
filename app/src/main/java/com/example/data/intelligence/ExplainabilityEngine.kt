package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Explainability Engine
 * Provides transparent, audit-ready breakdowns answering:
 * "لماذا ظهر هذا الاستنتاج؟" (Why did this insight appear?)
 */
class ExplainabilityEngine(
    private val articleDao: ArticleDao,
    private val statementDao: StatementDao,
    private val politicalRelationshipDao: PoliticalRelationshipDao
) {

    data class ExplainabilityReport(
        val insightTitle: String,
        val methodologyAr: String,
        val computationalMethod: String,
        val evaluatedDataPointsCount: Int,
        val confidenceScore: Float,
        val evidenceSnippets: List<String>,
        val primarySourcesInvolved: List<String>,
        val limitationsAndUncertainty: String
    )

    suspend fun explainRelationship(relationship: PoliticalRelationship): ExplainabilityReport = withContext(Dispatchers.IO) {
        val article = relationship.sourceArticleId?.let { articleDao.getArticleById(it) }

        ExplainabilityReport(
            insightTitle = "${relationship.sourceEntityName} ← [${relationship.relationshipType.displayNameAr()}] → ${relationship.targetEntityName}",
            methodologyAr = if (relationship.evidenceNature == EvidenceNature.DIRECT_EVIDENCE)
                "استنتاج مباشر مبني على وثيقة أو تصريح رسمي موثق ومطابق لمعايير الدرجة الأولى."
            else
                "استنتاج خوارزمي مستند إلى تقاطعات التغطية الإخبارية والذكر المشترك المتكرر في سياق سياسي واحد.",
            computationalMethod = relationship.discoveryMethod,
            evaluatedDataPointsCount = if (article != null) 3 else 1,
            confidenceScore = relationship.confidence,
            evidenceSnippets = listOfNotNull(
                relationship.evidenceSnippet.ifBlank { null },
                article?.let { "عنوان البرقية: ${it.title} (${it.sourceName})" }
            ),
            primarySourcesInvolved = listOfNotNull(relationship.sourceName, article?.sourceName).distinct(),
            limitationsAndUncertainty = if (relationship.evidenceNature == EvidenceNature.AI_INFERENCE)
                "استنتاج قيد التدقيق التحليلي؛ ينصح بمراجعة النص الأصلي واعتماده من المحلل البشري."
            else
                "علاقة موثقة بشكل مباشر عبر المصدر المعلن."
        )
    }

    suspend fun explainEarlySignal(signal: EarlySignalItem): ExplainabilityReport = withContext(Dispatchers.IO) {
        ExplainabilityReport(
            insightTitle = signal.signalTitleAr,
            methodologyAr = "رصد إشارة مبكرة عبر خوارزمية قياس وتيرة الاجتماعات والتغطية ومقارنة الأنماط التاريخية مع استبعاد التنبؤ الجزمي.",
            computationalMethod = "BAYESIAN_PATTERN_HEURISTIC",
            evaluatedDataPointsCount = 7,
            confidenceScore = signal.confidence,
            evidenceSnippets = listOf(
                "الدليل الحالي: ${signal.currentEvidence}",
                "النمط التاريخي: ${signal.historicalPattern}"
            ),
            primarySourcesInvolved = listOf("سجل البرقيات الدبلوماسية", "شبكة التغطية الإخبارية المعتمدة"),
            limitationsAndUncertainty = "المعلومات الناقصة: ${signal.unknowns}. التفسير البديل: ${signal.alternativeExplanations}"
        )
    }

    suspend fun explainDetectedChange(change: DetectedChangeItem): ExplainabilityReport = withContext(Dispatchers.IO) {
        ExplainabilityReport(
            insightTitle = change.whatChangedAr,
            methodologyAr = "مقارنة زمنية ونبرية بين التصريحات ومستويات التغطية السابقة واللاحقة للكيان.",
            computationalMethod = "DELTA_STANCE_COMPARATOR",
            evaluatedDataPointsCount = 4,
            confidenceScore = change.confidence,
            evidenceSnippets = listOf(
                "المعطى المقارن: ${change.comparedWithWhatAr}",
                "الدليل الأحدث: ${change.evidenceSnippet}"
            ),
            primarySourcesInvolved = listOf(change.sourceName),
            limitationsAndUncertainty = "مستوى التغير المقاس: ${(change.magnitude * 100).toInt()}%. ينبغي مراعاة السياق السياسي الإقليمي الضاغط على المتحدث."
        )
    }
}
