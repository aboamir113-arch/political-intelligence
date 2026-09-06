package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Trend Analysis Engine
 * Calculates coverage velocity, actor trajectories, rising topics, and dossier activity
 * over flexible timeframes (24h, 7d, 30d, 90d, 6m, 1y).
 */
class TrendAnalysisEngine(
    private val articleDao: ArticleDao,
    private val personDao: PersonDao,
    private val organizationDao: OrganizationDao,
    private val politicalFileDao: PoliticalFileDao,
    private val topicDao: TopicDao,
    private val statementDao: StatementDao
) {

    data class TrendReport(
        val timeframe: TrendTimeframe,
        val totalArticlesCount: Int,
        val coverageDeltaPercentage: Float,
        val isCoverageRising: Boolean,
        val risingPersons: List<EntityTrendSummary>,
        val risingOrganizations: List<EntityTrendSummary>,
        val activeFiles: List<EntityTrendSummary>,
        val emergingTopics: List<EntityTrendSummary>,
        val activityAssessmentAr: String
    )

    suspend fun analyzeTrends(timeframe: TrendTimeframe, customDurationMillis: Long = 0L): TrendReport = withContext(Dispatchers.Default) {
        val now = System.currentTimeMillis()
        val duration = if (timeframe == TrendTimeframe.CUSTOM && customDurationMillis > 0L) {
            customDurationMillis
        } else {
            timeframe.durationMillis
        }

        val currentStart = now - duration
        val previousStart = currentStart - duration

        val currentArticles = articleDao.getRecentArticles(sinceUtc = currentStart)
        val previousArticles = articleDao.getArticlesInRangeList(startDateUtc = previousStart, endDateUtc = currentStart)

        val currentCount = currentArticles.size
        val previousCount = previousArticles.size

        val coverageDelta = if (previousCount > 0) {
            ((currentCount - previousCount).toFloat() / previousCount) * 100f
        } else if (currentCount > 0) {
            100f
        } else {
            0f
        }

        val allPersons = personDao.getAllPersonsList()
        val allOrgs = organizationDao.getAllOrganizationsList()
        val allFiles = politicalFileDao.getAllFilesList()
        val allTopics = topicDao.getAllTopicsList()

        // 1. Person Trends
        val personTrends = allPersons.map { person ->
            val currMentions = currentArticles.count {
                it.title.contains(person.nameAr, ignoreCase = true) ||
                        it.snippet.contains(person.nameAr, ignoreCase = true)
            }
            val prevMentions = previousArticles.count {
                it.title.contains(person.nameAr, ignoreCase = true) ||
                        it.snippet.contains(person.nameAr, ignoreCase = true)
            }
            val pct = if (prevMentions > 0) ((currMentions - prevMentions).toFloat() / prevMentions) * 100f
            else if (currMentions > 0) 100f else 0f

            EntityTrendSummary(
                entityName = person.nameAr,
                entityType = EntityType.PERSON,
                mentionCount = currMentions,
                previousPeriodCount = prevMentions,
                percentageChange = pct,
                isRising = currMentions > prevMentions,
                recentCoverageContext = person.currentRoleAr
            )
        }.sortedByDescending { it.mentionCount }

        // 2. Organization Trends
        val orgTrends = allOrgs.map { org ->
            val currMentions = currentArticles.count {
                it.title.contains(org.nameAr, ignoreCase = true) ||
                        it.snippet.contains(org.nameAr, ignoreCase = true)
            }
            val prevMentions = previousArticles.count {
                it.title.contains(org.nameAr, ignoreCase = true) ||
                        it.snippet.contains(org.nameAr, ignoreCase = true)
            }
            val pct = if (prevMentions > 0) ((currMentions - prevMentions).toFloat() / prevMentions) * 100f
            else if (currMentions > 0) 100f else 0f

            EntityTrendSummary(
                entityName = org.nameAr,
                entityType = EntityType.ORGANIZATION,
                mentionCount = currMentions,
                previousPeriodCount = prevMentions,
                percentageChange = pct,
                isRising = currMentions > prevMentions,
                recentCoverageContext = org.type.displayNameAr()
            )
        }.sortedByDescending { it.mentionCount }

        // 3. Political Files Trends
        val fileTrends = allFiles.map { file ->
            val currMentions = currentArticles.count {
                it.politicalFileId == file.id || it.title.contains(file.titleAr, ignoreCase = true)
            }
            val prevMentions = previousArticles.count {
                it.politicalFileId == file.id || it.title.contains(file.titleAr, ignoreCase = true)
            }
            val pct = if (prevMentions > 0) ((currMentions - prevMentions).toFloat() / prevMentions) * 100f
            else if (currMentions > 0) 100f else 0f

            EntityTrendSummary(
                entityName = file.titleAr,
                entityType = EntityType.POLITICAL_FILE,
                mentionCount = currMentions,
                previousPeriodCount = prevMentions,
                percentageChange = pct,
                isRising = currMentions > prevMentions,
                recentCoverageContext = file.status.displayNameAr()
            )
        }.sortedByDescending { it.mentionCount }

        // 4. Emerging Topics Trends
        val topicTrends = allTopics.map { topic ->
            val currMentions = currentArticles.count {
                it.title.contains(topic.nameAr, ignoreCase = true)
            }
            val prevMentions = previousArticles.count {
                it.title.contains(topic.nameAr, ignoreCase = true)
            }
            val pct = if (prevMentions > 0) ((currMentions - prevMentions).toFloat() / prevMentions) * 100f
            else if (currMentions > 0) 100f else 0f

            EntityTrendSummary(
                entityName = topic.nameAr,
                entityType = EntityType.TOPIC,
                mentionCount = currMentions,
                previousPeriodCount = prevMentions,
                percentageChange = pct,
                isRising = currMentions > prevMentions,
                recentCoverageContext = topic.category.displayNameAr()
            )
        }.sortedByDescending { it.mentionCount }

        val assessment = buildString {
            if (coverageDelta > 15f) {
                append("تصاعد ملحوظ في وتيرة التغطية الإخبارية والنشاط السياسي بمعدل +${coverageDelta.toInt()}%. ")
            } else if (coverageDelta < -15f) {
                append("انحسار نسبي في كثافة التغطية بمعدل ${coverageDelta.toInt()}%. ")
            } else {
                append("استقرار في وتيرة النشر والمتابعة السياسية. ")
            }

            val topPerson = personTrends.firstOrNull { it.mentionCount > 0 }
            if (topPerson != null) {
                append("تبرز شخصية (${topPerson.entityName}) كأكثر الشخصيات تداولاً (${topPerson.mentionCount} إشارة). ")
            }

            val topFile = fileTrends.firstOrNull { it.mentionCount > 0 }
            if (topFile != null) {
                append("ملف (${topFile.entityName}) يستحوذ على الأولوية في اهتمامات الرصد.")
            }
        }

        TrendReport(
            timeframe = timeframe,
            totalArticlesCount = currentCount,
            coverageDeltaPercentage = coverageDelta,
            isCoverageRising = coverageDelta >= 0,
            risingPersons = personTrends.take(6),
            risingOrganizations = orgTrends.take(6),
            activeFiles = fileTrends.take(6),
            emergingTopics = topicTrends.take(6),
            activityAssessmentAr = assessment
        )
    }
}
