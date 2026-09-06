package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Advanced Smart Monitoring Engine
 * Executes multi-layered intelligence gating (Article -> Relevance -> Importance -> Novelty -> Change -> Alert)
 * and enforces Source Independence detection.
 */
class SmartMonitoringEngine(
    private val ruleDao: SmartMonitorRuleDao,
    private val alertDao: AlertDao,
    private val articleDao: ArticleDao,
    private val sourceDao: SourceDao
) {

    data class MonitoringEvaluationResult(
        val articleId: Long,
        val matchedRules: List<SmartMonitorRule>,
        val importanceScore: Float,
        val isNovel: Boolean,
        val representsChange: Boolean,
        val smartAlertCreated: Boolean,
        val smartCategory: SmartAlertCategory?,
        val sourceIndependenceDisclosure: String
    )

    /**
     * Process an ingested article through the 6-Layer Smart Monitoring Pipeline.
     */
    suspend fun evaluateArticle(article: Article): MonitoringEvaluationResult = withContext(Dispatchers.IO) {
        val activeRules = ruleDao.getActiveRulesList()
        val text = "${article.title} ${article.snippet}"

        // Layer 2: Relevance Detection
        val matchedRules = activeRules.filter { rule ->
            val matchesEntity = rule.targetName.isNotBlank() && text.contains(rule.targetName, ignoreCase = true)
            val matchesKeywords = if (rule.keywordsCommaSeparated.isNotBlank()) {
                rule.keywordsCommaSeparated.split(",").map { it.trim() }
                    .any { kw -> kw.isNotBlank() && text.contains(kw, ignoreCase = true) }
            } else false
            val matchesSource = if (rule.sourcesCommaSeparated.isNotBlank()) {
                rule.sourcesCommaSeparated.split(",").map { it.trim() }
                    .any { src -> src.isNotBlank() && article.sourceName.contains(src, ignoreCase = true) }
            } else true

            (matchesEntity || matchesKeywords) && matchesSource
        }

        // Layer 3: Importance Scoring (0.0 to 1.0)
        val source = sourceDao.getSourceById(article.sourceId)
        val sourceWeight = when (source?.tier) {
            SourceTier.PRIMARY -> 0.40f
            SourceTier.AGENCY -> 0.35f
            SourceTier.TRUSTED_MEDIA -> 0.30f
            SourceTier.SECONDARY -> 0.20f
            SourceTier.UNVERIFIED -> 0.10f
            null -> 0.25f
        }
        val priorityWeight = when {
            article.importanceScore >= 80 -> 0.40f
            article.importanceScore >= 60 -> 0.30f
            article.importanceScore >= 40 -> 0.20f
            else -> 0.10f
        }
        val relevanceWeight = if (matchedRules.isNotEmpty()) 0.20f else 0.05f
        val importanceScore = (sourceWeight + priorityWeight + relevanceWeight).coerceIn(0f, 1f)

        // Layer 4: Novelty Detection & Source Independence
        val recentArticles = articleDao.getAllArticles(limit = 40)
        val similarArticles = recentArticles.filter { it.id != article.id && it.canonicalUrl != article.canonicalUrl }
            .filter {
                it.title.contains(article.title.take(20), ignoreCase = true) ||
                        it.snippet.contains(article.snippet.take(30), ignoreCase = true)
            }

        val isNovel = similarArticles.isEmpty() || article.isDuplicate == false

        val sourceIndependenceDisclosure = if (similarArticles.size >= 2) {
            val syndicationPrimary = similarArticles.firstOrNull { it.sourceTier == SourceTier.PRIMARY || it.sourceTier == SourceTier.AGENCY }
            if (syndicationPrimary != null) {
                "تم رصد الخبر في عدة مصادر (${similarArticles.size + 1})، لكن معظمها يعتمد على وكالة أو مصدر واحد: ${syndicationPrimary.sourceName}."
            } else {
                "تغطية متعددة متزامنة عبر ${similarArticles.size + 1} منصات إخبارية."
            }
        } else {
            "مصدر مستقل ومباشر."
        }

        // Layer 5: Change Detection
        val representsChange = text.contains("تراجع") || text.contains("تحول") || text.contains("أعلن رسمياً") ||
                text.contains("اتفاق مفاجئ") || text.contains("تصعيد") || text.contains("خرق") || text.contains("طرف جديد")

        // Layer 6: Alert Decision
        var smartAlertCreated = false
        var alertCategory: SmartAlertCategory? = null

        if (matchedRules.isNotEmpty() && importanceScore >= 0.65f && (isNovel || representsChange)) {
            smartAlertCreated = true
            alertCategory = when {
                representsChange && text.contains("تحول") -> SmartAlertCategory.SIGNIFICANT_CHANGE
                text.contains("طرف جديد") || text.contains("انضمام") -> SmartAlertCategory.NEW_ACTOR
                text.contains("تصعيد") || article.importanceScore >= 80 -> SmartAlertCategory.ESCALATION
                text.contains("تعارض") || text.contains("نفى") -> SmartAlertCategory.CONTRADICTION
                text.contains("لقاء") || text.contains("محادثات") -> SmartAlertCategory.NEW_RELATIONSHIP
                text.contains("تغير الخطاب") || text.contains("لهجة جديدة") -> SmartAlertCategory.NARRATIVE_SHIFT
                else -> SmartAlertCategory.UNUSUAL_ACTIVITY
            }

            // Create Alert Item
            val alert = AlertItem(
                titleAr = "${alertCategory.displayNameAr()}: ${article.title.take(70)}",
                messageAr = "${article.snippet.take(150)}\n($sourceIndependenceDisclosure)",
                type = AlertType.IMPORTANT_DEVELOPMENT,
                severity = if (article.importanceScore >= 80) AlertSeverity.CRITICAL else AlertSeverity.HIGH,
                articleId = article.id,
                eventId = article.eventId,
                politicalFileId = article.politicalFileId
            )
            alertDao.insertAlert(alert)

            // Update rule trigger
            matchedRules.forEach { r ->
                ruleDao.updateLastTriggered(r.id)
            }
        }

        MonitoringEvaluationResult(
            articleId = article.id,
            matchedRules = matchedRules,
            importanceScore = importanceScore,
            isNovel = isNovel,
            representsChange = representsChange,
            smartAlertCreated = smartAlertCreated,
            smartCategory = alertCategory,
            sourceIndependenceDisclosure = sourceIndependenceDisclosure
        )
    }
}
