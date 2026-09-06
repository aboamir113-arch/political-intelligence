package com.example.data.repository

import com.example.data.ingestion.IngestionEngine
import com.example.data.ingestion.TestResult
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class DeskRepository(private val database: AppDatabase) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val ingestionEngine = IngestionEngine(database, httpClient)

    // Users
    val allUsers: Flow<List<User>> = database.userDao().getAllUsers()

    suspend fun getActiveUser(): User? = database.userDao().getActiveUser()

    suspend fun updateUser(user: User) = database.userDao().updateUser(user)

    suspend fun insertUser(user: User): Long {
        val id = database.userDao().insertUser(user)
        logActivity(
            userId = 1,
            action = "USER_CREATED",
            entityTitle = user.fullNameAr,
            details = "إضافة محلل جديد: ${user.fullNameAr} (${user.role.displayNameAr()})"
        )
        return id
    }

    suspend fun deleteUser(userId: Long) {
        val user = database.userDao().getUserById(userId)
        database.userDao().deleteUser(userId)
        if (user != null) {
            logActivity(
                userId = 1,
                action = "USER_DELETED",
                entityTitle = user.fullNameAr,
                details = "حذف المحلل: ${user.fullNameAr}"
            )
        }
    }

    // Sources
    val allSources: Flow<List<Source>> = database.sourceDao().getAllSources()
    val activeSourcesCount: Flow<Int> = database.sourceDao().getActiveSourcesCount()

    fun getSourcesByTier(tier: SourceTier): Flow<List<Source>> = database.sourceDao().getSourcesByTier(tier)
    fun getSourcesByStatus(status: SourceStatus): Flow<List<Source>> = database.sourceDao().getSourcesByStatus(status)

    suspend fun insertSource(source: Source): Long {
        val id = database.sourceDao().insertSource(source)
        logActivity(
            userId = 1,
            action = "SOURCE_CREATED",
            entityTitle = source.nameAr,
            details = "إضافة مصدر جديد (${source.fetchMethod.displayNameAr()}) بدرجة موثوقية ${source.reliabilityLevel}%"
        )
        return id
    }

    suspend fun updateSource(source: Source) {
        database.sourceDao().updateSource(source)
        logActivity(
            userId = 1,
            action = "SOURCE_UPDATED",
            entityTitle = source.nameAr,
            details = "تحديث بيانات المصدر (الحالة: ${source.status.displayNameAr()})"
        )
    }

    suspend fun deleteSource(source: Source) {
        database.sourceDao().deleteSource(source)
        logActivity(
            userId = 1,
            action = "SOURCE_DELETED",
            entityTitle = source.nameAr,
            details = "حذف المصدر من النظام"
        )
    }

    /**
     * Real connection & feed probe for a source.
     */
    suspend fun testSource(source: Source): TestResult = withContext(Dispatchers.IO) {
        val result = ingestionEngine.testSource(source)
        logActivity(
            userId = 1,
            action = "SOURCE_TEST",
            entityTitle = source.nameAr,
            details = "نتيجة الفحص (${result.state.displayNameAr()}): ${result.message}"
        )
        result
    }

    // Articles & News Ingestion
    val primaryArticles: Flow<List<Article>> = database.articleDao().getPrimaryArticlesPaged(limit = 120)
    val totalArticlesCount: Flow<Int> = database.articleDao().getTotalArticlesCount()
    val duplicatesCount: Flow<Int> = database.articleDao().getDuplicatesCount()

    fun getArticlesCountToday(): Flow<Int> {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return database.articleDao().getArticlesCountToday(calendar.timeInMillis)
    }

    fun getFilteredArticles(
        query: String?,
        startUtc: Long,
        endUtc: Long,
        sourceId: Long?,
        topicId: Long?,
        sourceTier: SourceTier?,
        classification: ArticleClassification?,
        countryCode: String?,
        politicalFileId: Long?,
        onlySaved: Boolean = false,
        includeDuplicates: Boolean = false
    ): Flow<List<Article>> {
        return database.articleDao().getFilteredArticles(
            query = query?.takeIf { it.isNotBlank() },
            startUtc = startUtc,
            endUtc = endUtc,
            sourceId = sourceId,
            topicId = topicId,
            sourceTier = sourceTier,
            classification = classification,
            countryCode = countryCode,
            politicalFileId = politicalFileId,
            onlySaved = if (onlySaved) 1 else 0,
            includeDuplicates = if (includeDuplicates) 1 else 0,
            limit = 120,
            offset = 0
        )
    }

    suspend fun setArticleSaved(articleId: Long, isSaved: Boolean) = withContext(Dispatchers.IO) {
        database.articleDao().setArticleSaved(articleId, isSaved)
    }

    suspend fun linkArticleToFile(articleId: Long, fileId: Long?, fileTitle: String?) = withContext(Dispatchers.IO) {
        database.articleDao().linkArticleToFile(articleId, fileId, fileTitle)
    }

    // Ingestion Sync Actions
    suspend fun ingestSingleSource(source: Source): IngestionLog = withContext(Dispatchers.IO) {
        val log = ingestionEngine.ingestSource(source)
        logActivity(
            userId = 1,
            action = "INGESTION_SYNC",
            entityTitle = source.nameAr,
            details = "جلب ${log.fetchedCount} خبراً، إدراج ${log.insertedCount}، وتحديد ${log.duplicateCount} مكرر"
        )
        log
    }

    suspend fun ingestAllActiveSources(): List<IngestionLog> = withContext(Dispatchers.IO) {
        val logs = ingestionEngine.ingestAllActiveSources()
        val totalInserted = logs.sumOf { it.insertedCount }
        val totalDuplicates = logs.sumOf { it.duplicateCount }
        logActivity(
            userId = 1,
            action = "GLOBAL_INGESTION_SYNC",
            entityTitle = "مزامنة عامة للمصادر",
            details = "اكتملت مزامنة المصادر النشطة: تم إدراج $totalInserted خبراً وتحديد $totalDuplicates مكرر"
        )
        logs
    }

    suspend fun importManualArticle(
        url: String,
        sourceId: Long,
        customTitle: String?,
        customSnippet: String?,
        customPublishedAt: Long?,
        analystNotes: String
    ): Result<Article> = withContext(Dispatchers.IO) {
        ingestionEngine.importManualArticle(url, sourceId, customTitle, customSnippet, customPublishedAt, analystNotes)
    }

    // Ingestion Logs & Health
    val recentIngestionLogs: Flow<List<IngestionLog>> = database.ingestionLogDao().getRecentIngestionLogs(limit = 50)
    val totalIngestionErrors: Flow<Int?> = database.ingestionLogDao().getTotalErrorCount()
    val failedRunsCount: Flow<Int> = database.ingestionLogDao().getFailedRunsCount()

    // Political Files (Dossiers)
    val allFiles: Flow<List<PoliticalFile>> = database.politicalFileDao().getAllFiles()
    val activeFiles: Flow<List<PoliticalFile>> = database.politicalFileDao().getActiveFiles()
    val activeFilesCount: Flow<Int> = database.politicalFileDao().getActiveFilesCount()

    suspend fun insertFile(file: PoliticalFile): Long {
        val id = database.politicalFileDao().insertFile(file)
        logActivity(
            userId = file.ownerUserId ?: 1,
            action = "FILE_CREATED",
            entityTitle = file.titleAr,
            details = "إنشاء ملف سياسي استراتيجي بأولوية ${file.priority.displayNameAr()}"
        )
        return id
    }

    suspend fun updateFile(file: PoliticalFile) = database.politicalFileDao().updateFile(file)
    suspend fun deleteFile(file: PoliticalFile) = database.politicalFileDao().deleteFile(file)

    // Countries
    val allCountries: Flow<List<Country>> = database.countryDao().getAllCountries()

    // Organizations
    val allOrganizations: Flow<List<Organization>> = database.organizationDao().getAllOrganizations()
    suspend fun insertOrganization(org: Organization) = database.organizationDao().insertOrganization(org)

    // Persons & Stance
    val allPersons: Flow<List<Person>> = database.personDao().getAllPersons()
    val monitoredPersons: Flow<List<Person>> = database.personDao().getMonitoredPersons()
    val recentStanceShifts: Flow<List<PersonPosition>> = database.personDao().getRecentStanceShifts()

    fun getPositionsForPerson(personId: Long): Flow<List<PersonPosition>> =
        database.personDao().getPositionsForPerson(personId)

    suspend fun insertPerson(person: Person): Long = database.personDao().insertPerson(person)
    suspend fun updatePerson(person: Person) = database.personDao().updatePerson(person)
    suspend fun deletePerson(person: Person) = database.personDao().deletePerson(person)

    suspend fun insertPosition(position: PersonPosition): Long {
        val id = database.personDao().insertPosition(position)
        val person = database.personDao().getPersonById(position.personId)
        if (person != null) {
            database.personDao().updatePerson(person.copy(
                stanceShiftCount = person.stanceShiftCount + 1,
                lastStatementSummary = position.verbatimStatement,
                updatedAt = System.currentTimeMillis()
            ))
        }
        return id
    }

    // Topics
    val allTopics: Flow<List<Topic>> = database.topicDao().getAllTopics()

    // Activity Logs
    val recentLogs: Flow<List<ActivityLog>> = database.activityLogDao().getRecentLogs()

    suspend fun logActivity(userId: Long, action: String, entityTitle: String, details: String) {
        database.activityLogDao().insertLog(
            ActivityLog(
                userId = userId,
                actionType = action,
                entityTitle = entityTitle,
                details = details
            )
        )
    }

    suspend fun clearLogs() = database.activityLogDao().clearLogs()

    // ==========================================
    // PHASE 3: EVENTS, CLAIMS, EVIDENCE, TIMELINES & ALERTS
    // ==========================================

    val eventClusteringEngine = com.example.data.events.EventClusteringEngine()
    val claimExtractionEngine = com.example.data.events.ClaimExtractionEngine()
    val contradictionDetectionEngine = com.example.data.events.ContradictionDetectionEngine()
    val verificationEngine = com.example.data.events.VerificationEngine()
    val smartAlertsEngine = com.example.data.events.SmartAlertsEngine()

    // Events
    val allEvents: Flow<List<PoliticalEvent>> = database.politicalEventDao().getAllEvents()

    fun getFilteredEvents(
        query: String?,
        status: EventStatus?,
        importance: EventImportance?,
        fileId: Long?,
        topicId: Long?,
        countryCode: String?
    ): Flow<List<PoliticalEvent>> = database.politicalEventDao().getFilteredEvents(
        query, status, importance, fileId, topicId, countryCode
    )

    suspend fun getEventById(id: Long): PoliticalEvent? = database.politicalEventDao().getEventById(id)
    fun getEventByIdFlow(id: Long): Flow<PoliticalEvent?> = database.politicalEventDao().getEventByIdFlow(id)
    fun getEventsForPoliticalFile(fileId: Long): Flow<List<PoliticalEvent>> = database.politicalEventDao().getEventsForPoliticalFile(fileId)

    suspend fun insertEvent(event: PoliticalEvent): Long {
        val id = database.politicalEventDao().insertEvent(event)
        logActivity(1, "EVENT_CREATED", event.titleAr, "إنشاء ملف حدث سياسي جديد [${event.status.displayNameAr()}]")
        return id
    }

    suspend fun updateEvent(event: PoliticalEvent) {
        database.politicalEventDao().updateEvent(event)
        logActivity(1, "EVENT_UPDATED", event.titleAr, "تحديث بيانات الحدث السياسي والتطورات المسجلة")
    }

    suspend fun deleteEvent(event: PoliticalEvent) {
        database.politicalEventDao().deleteEvent(event)
        logActivity(1, "EVENT_DELETED", event.titleAr, "حذف الحدث السياسي")
    }

    suspend fun linkArticleToEvent(articleId: Long, eventId: Long?, eventTitle: String?) {
        database.articleDao().linkArticleToEvent(articleId, eventId, eventTitle)
        if (eventId != null) {
            database.politicalEventDao().insertArticleEventCrossRef(
                ArticleEventCrossRef(articleId = articleId, eventId = eventId, isPrimary = true)
            )
            val event = database.politicalEventDao().getEventById(eventId)
            if (event != null) {
                database.politicalEventDao().updateEvent(
                    event.copy(
                        articleCount = event.articleCount + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun removeArticleFromEvent(articleId: Long, eventId: Long) {
        database.articleDao().linkArticleToEvent(articleId, null, null)
        database.politicalEventDao().removeArticleFromEvent(articleId, eventId)
        val event = database.politicalEventDao().getEventById(eventId)
        if (event != null && event.articleCount > 0) {
            database.politicalEventDao().updateEvent(
                event.copy(
                    articleCount = (event.articleCount - 1).coerceAtLeast(0),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun getArticlesForEvent(eventId: Long): Flow<List<Article>> = database.politicalEventDao().getArticlesForEvent(eventId)

    /**
     * Automatic Event Clustering Pipeline:
     * 1. Matches article against active events.
     * 2. Updates existing event OR creates a new Event Dossier.
     * 3. Extracts verbatim Statements and verifiable Claims.
     * 4. Synthesizes a TimelineItem development entry.
     * 5. Checks for cross-source Contradictions.
     * 6. Evaluates Smart Alert rules.
     */
    suspend fun clusterAndProcessArticle(article: Article): PoliticalEvent = withContext(Dispatchers.IO) {
        val existingEvents = database.politicalEventDao().getAllEventsList()
        val knownPersons = database.personDao().getAllPersonsList()
        val knownOrgs = database.organizationDao().getAllOrganizationsList()

        // Cluster match
        val matchResult = eventClusteringEngine.findMatchingEvent(article, existingEvents)
        val targetEvent: PoliticalEvent

        if (matchResult.matchedEvent != null) {
            // Update existing event
            val updated = eventClusteringEngine.updateEventWithNewArticle(
                matchResult.matchedEvent,
                article,
                emptyList()
            )
            database.politicalEventDao().updateEvent(updated)
            targetEvent = updated
        } else {
            // Create new event
            val newEvent = eventClusteringEngine.createEventFromArticle(article)
            val eventId = database.politicalEventDao().insertEvent(newEvent)
            targetEvent = newEvent.copy(id = eventId)
        }

        // Link article to event
        linkArticleToEvent(article.id, targetEvent.id, targetEvent.titleAr)

        // Extract Statements and Claims
        val insights = claimExtractionEngine.extractInsights(article, targetEvent.id, knownPersons, knownOrgs)
        if (insights.statements.isNotEmpty()) {
            database.statementDao().insertStatements(insights.statements)
        }
        if (insights.claims.isNotEmpty()) {
            val claimIds = database.claimDao().insertClaims(insights.claims)
            // Generate corresponding Evidence item for each claim
            val evidenceItems = insights.claims.mapIndexed { idx, claim ->
                EvidenceItem(
                    claimId = claimIds.getOrElse(idx) { 0L },
                    eventId = targetEvent.id,
                    articleId = article.id,
                    sourceId = article.sourceId,
                    sourceName = article.sourceName,
                    sourceTier = article.sourceTier,
                    evidenceType = if (article.sourceTier == SourceTier.PRIMARY) EvidenceType.OFFICIAL_STATEMENT else EvidenceType.NEWS_REPORT,
                    evidenceStrength = if (article.sourceTier == SourceTier.PRIMARY) EvidenceStrength.STRONG else EvidenceStrength.MODERATE,
                    date = article.publishedAt,
                    url = article.originalUrl,
                    excerpt = claim.statement,
                    isDirectOrigin = article.originalAgency == null,
                    originalAgency = article.originalAgency
                )
            }
            database.evidenceDao().insertAllEvidence(evidenceItems)
        }

        // Add Timeline item
        database.timelineDao().insertTimelineItem(
            TimelineItem(
                eventId = targetEvent.id,
                timestamp = article.publishedAt,
                titleAr = article.title,
                descriptionAr = article.snippet.take(240),
                sourceName = article.sourceName,
                sourceTier = article.sourceTier,
                articleId = article.id,
                importance = targetEvent.importance,
                isMilestone = article.sourceTier == SourceTier.PRIMARY,
                originalUrl = article.originalUrl
            )
        )

        // Contradiction detection
        val eventClaims = insights.claims
        if (eventClaims.size >= 2) {
            val contradictions = contradictionDetectionEngine.detectContradictions(targetEvent.id, eventClaims, listOf(article))
            if (contradictions.isNotEmpty()) {
                database.contradictionDao().insertContradictions(contradictions)
                for (c in contradictions) {
                    val alert = smartAlertsEngine.createContradictionAlert(c, targetEvent.titleAr)
                    database.alertDao().insertAlert(alert)
                }
            }
        }

        // Smart alerts evaluation
        val activeRules = database.alertDao().getActiveAlertRules()
        val articleAlerts = smartAlertsEngine.evaluateRulesForArticle(article, activeRules)
        if (articleAlerts.isNotEmpty()) {
            database.alertDao().insertAlerts(articleAlerts)
        }
        for (st in insights.statements) {
            val statementAlerts = smartAlertsEngine.evaluateRulesForStatement(st, activeRules)
            if (statementAlerts.isNotEmpty()) {
                database.alertDao().insertAlerts(statementAlerts)
            }
        }

        targetEvent
    }

    // Claims & Statements
    fun getClaimsForEvent(eventId: Long): Flow<List<Claim>> = database.claimDao().getClaimsForEvent(eventId)
    val allClaims: Flow<List<Claim>> = database.claimDao().getAllClaims()
    fun getClaimsForPerson(personId: Long?, personName: String): Flow<List<Claim>> = database.claimDao().getClaimsForPerson(personId, personName)

    suspend fun insertClaim(claim: Claim): Long = database.claimDao().insertClaim(claim)

    suspend fun updateClaimVerification(claimId: Long, newStatus: VerificationStatus, reason: String, userId: Long = 1) {
        val claim = database.claimDao().getClaimById(claimId)
        val oldStatus = claim?.verificationStatus ?: VerificationStatus.UNVERIFIED
        database.claimDao().updateVerificationStatus(claimId, newStatus, reason, System.currentTimeMillis())
        logActivity(
            userId = userId,
            action = "CLAIM_VERIFIED",
            entityTitle = claim?.statement?.take(60) ?: "ادعاء سياسي",
            details = "تحديث حالة التحقق من [${oldStatus.displayNameAr()}] إلى [${newStatus.displayNameAr()}] — السبب: $reason"
        )
        if (claim != null && oldStatus != newStatus) {
            val alert = smartAlertsEngine.createVerificationChangeAlert(claim, oldStatus, newStatus, userId)
            database.alertDao().insertAlert(alert)
        }
    }

    fun getStatementsForEvent(eventId: Long): Flow<List<Statement>> = database.statementDao().getStatementsForEvent(eventId)
    val allStatements: Flow<List<Statement>> = database.statementDao().getAllStatements()
    fun getStatementsForPerson(personId: Long?, personName: String): Flow<List<Statement>> = database.statementDao().getStatementsForPerson(personId, personName)
    fun getStatementsForArticle(articleId: Long): Flow<List<Statement>> = database.statementDao().getStatementsForArticle(articleId)

    suspend fun insertStatement(statement: Statement): Long = database.statementDao().insertStatement(statement)

    // Evidence
    fun getEvidenceForClaim(claimId: Long): Flow<List<EvidenceItem>> = database.evidenceDao().getEvidenceForClaim(claimId)
    fun getEvidenceForEvent(eventId: Long): Flow<List<EvidenceItem>> = database.evidenceDao().getEvidenceForEvent(eventId)
    val allEvidence: Flow<List<EvidenceItem>> = database.evidenceDao().getAllEvidence()

    suspend fun insertEvidence(evidence: EvidenceItem): Long {
        val id = database.evidenceDao().insertEvidence(evidence)
        logActivity(1, "EVIDENCE_ADDED", evidence.sourceName, "إضافة قرينة/دليل جديد من نوع [${evidence.evidenceType.displayNameAr()}]")
        return id
    }

    // Timelines
    fun getTimelineForEvent(eventId: Long): Flow<List<TimelineItem>> = database.timelineDao().getTimelineForEventAsc(eventId)
    suspend fun insertTimelineItem(item: TimelineItem): Long = database.timelineDao().insertTimelineItem(item)

    // Contradictions
    fun getContradictionsForEvent(eventId: Long): Flow<List<ContradictionItem>> = database.contradictionDao().getContradictionsForEvent(eventId)
    val allContradictions: Flow<List<ContradictionItem>> = database.contradictionDao().getAllContradictions()

    suspend fun updateContradictionStatus(id: Long, status: ContradictionStatus, notes: String) {
        database.contradictionDao().updateStatus(id, status, notes)
        logActivity(1, "CONTRADICTION_UPDATED", "تعارض رقم #$id", "تحديث حالة التعارض إلى [${status.displayNameAr()}]")
    }

    // Alerts & Rules
    fun getAlertsForUser(userId: Long = 1): Flow<List<AlertItem>> = database.alertDao().getAlertsForUser(userId)
    fun getUnreadAlertsForUser(userId: Long = 1): Flow<List<AlertItem>> = database.alertDao().getUnreadAlertsForUser(userId)
    val unreadAlertsCount: Flow<Int> = database.alertDao().getUnreadAlertCountFlow(1)

    suspend fun markAlertAsRead(id: Long) = database.alertDao().markAsRead(id)
    suspend fun markAllAlertsAsRead(userId: Long = 1) = database.alertDao().markAllAsRead(userId)
    suspend fun deleteAlert(alert: AlertItem) = database.alertDao().deleteAlert(alert)
    suspend fun clearAllAlerts(userId: Long = 1) = database.alertDao().clearAllAlerts(userId)

    fun getAlertRulesForUser(userId: Long = 1): Flow<List<AlertRule>> = database.alertDao().getAlertRulesForUser(userId)
    suspend fun insertAlertRule(rule: AlertRule): Long = database.alertDao().insertAlertRule(rule)
    suspend fun toggleAlertRule(id: Long, enabled: Boolean) = database.alertDao().toggleAlertRule(id, enabled)
    suspend fun deleteAlertRule(rule: AlertRule) = database.alertDao().deleteAlertRule(rule)

    // Phase 4: AI Intelligence Layer
    val aiEngine = com.example.data.ai.AiIntelligenceEngine(database)

    fun getAnalysesForArticle(articleId: Long): Flow<List<AiAnalysis>> =
        database.aiAnalysisDao().getAnalysesForArticle(articleId)

    fun getLatestAnalysisForArticle(articleId: Long, type: AiAnalysisType): Flow<AiAnalysis?> =
        database.aiAnalysisDao().getLatestAnalysisForArticle(articleId, type)

    fun getAnalysesForEvent(eventId: Long): Flow<List<AiAnalysis>> =
        database.aiAnalysisDao().getAnalysesForEvent(eventId)

    fun getLatestAnalysisForEvent(eventId: Long, type: AiAnalysisType): Flow<AiAnalysis?> =
        database.aiAnalysisDao().getLatestAnalysisForEvent(eventId, type)

    fun getAnalysesForFile(fileId: Long): Flow<List<AiAnalysis>> =
        database.aiAnalysisDao().getAnalysesForFile(fileId)

    fun getLatestDashboardBrief(): Flow<AiAnalysis?> =
        database.aiAnalysisDao().getLatestDashboardBrief()

    val allAnalysesHistory: Flow<List<AiAnalysis>> =
        database.aiAnalysisDao().getAllAnalysesPaged(100)

    suspend fun summarizeArticle(
        articleId: Long,
        userId: Long = 1,
        forceRegenerate: Boolean = false,
        targetLanguage: String = "ar"
    ): ArticleSummaryResult {
        val result = aiEngine.summarizeArticle(articleId, userId, forceRegenerate, targetLanguage)
        logActivity(userId, "AI_ARTICLE_SUMMARY", "مقال #$articleId", "توليد ملخص ذكي بنموذج (${aiEngine.configuredModel})")
        return result
    }

    suspend fun summarizeEvent(
        eventId: Long,
        userId: Long = 1,
        forceRegenerate: Boolean = false
    ): EventSummaryResult {
        val result = aiEngine.summarizeEvent(eventId, userId, forceRegenerate)
        logActivity(userId, "AI_EVENT_SUMMARY", "حدث #$eventId", "توليد موجز الحدث الاستراتيجي بنموذج (${aiEngine.configuredModel})")
        return result
    }

    suspend fun compareNarratives(eventId: Long, userId: Long = 1): NarrativeComparisonResult {
        val result = aiEngine.compareNarratives(eventId, userId)
        logActivity(userId, "AI_NARRATIVE_COMPARISON", "حدث #$eventId", "مقارنة الروايات الصحفية وتعدد المصادر")
        return result
    }

    suspend fun analyzeContradictions(eventId: Long, userId: Long = 1): ContradictionAnalysisResult {
        val result = aiEngine.analyzeContradictions(eventId, userId)
        logActivity(userId, "AI_CONTRADICTION_ANALYSIS", "حدث #$eventId", "فحص التناقضات الدلالية والتوقيتية")
        return result
    }

    suspend fun traceOriginalSource(articleId: Long, userId: Long = 1): SourceTracingResultData {
        val result = aiEngine.traceOriginalSource(articleId, userId)
        logActivity(userId, "AI_SOURCE_TRACING", "مقال #$articleId", "تتبع المصدر الأول والناقل الأصلي (${result.verdict.name})")
        return result
    }

    suspend fun analyzeWhatChanged(timeframeHours: Int = 48, userId: Long = 1): WhatChangedResult {
        val result = aiEngine.analyzeWhatChanged(timeframeHours, userId)
        logActivity(userId, "AI_WHAT_CHANGED", "آخر $timeframeHours ساعة", "تحليل الدلتا ورصد التطورات الجديدة")
        return result
    }

    suspend fun analyzePersonPosition(personId: Long, userId: Long = 1): PersonPositionResult {
        val result = aiEngine.analyzePersonPosition(personId, userId)
        logActivity(userId, "AI_PERSON_STANCE", "شخصية #$personId", "تحليل تطور الموقف السياسي والتصريحات")
        return result
    }

    suspend fun analyzeMediaPerspective(eventId: Long, userId: Long = 1): MediaPerspectiveResult {
        val result = aiEngine.analyzeMediaPerspective(eventId, userId)
        logActivity(userId, "AI_MEDIA_PERSPECTIVE", "حدث #$eventId", "تحليل التأطير وزوايا التغطية الإعلامية")
        return result
    }

    suspend fun analyzeEvidenceForClaim(claimId: Long, userId: Long = 1): EvidenceAnalysisResult {
        val result = aiEngine.analyzeEvidenceForClaim(claimId, userId)
        logActivity(userId, "AI_EVIDENCE_ANALYSIS", "إدعاء #$claimId", "تقييم قوة الحجج والوثائق الثبوتية (${result.verdict.name})")
        return result
    }

    suspend fun analyzeInformationGaps(eventId: Long, userId: Long = 1): InformationGapsResult {
        val result = aiEngine.analyzeInformationGaps(eventId, userId)
        logActivity(userId, "AI_INFORMATION_GAPS", "حدث #$eventId", "رصد فجوات المعلومات والبيانات غير المؤكدة")
        return result
    }

    suspend fun analyzePoliticalFile(fileId: Long, timeframeDays: Int = 30, userId: Long = 1): FileAnalysisResult {
        val result = aiEngine.analyzePoliticalFile(fileId, timeframeDays, userId)
        logActivity(userId, "AI_FILE_ANALYSIS", "ملف #$fileId", "إعداد التقرير الشامل للملف السياسي")
        return result
    }

    suspend fun generateTimelineSummary(eventId: Long, userId: Long = 1): TimelineSummaryResult {
        val result = aiEngine.generateTimelineSummary(eventId, userId)
        logActivity(userId, "AI_TIMELINE_SUMMARY", "حدث #$eventId", "تحليل التسلسل الزمني والسببي للمحطات")
        return result
    }

    suspend fun generateDashboardAiBrief(userId: Long = 1): DashboardAiBriefResult {
        val result = aiEngine.generateDashboardAiBrief(userId)
        logActivity(userId, "AI_DASHBOARD_BRIEF", "المكتب الرئيسي", "إعداد الإيجاز الاستخباري اليومي الذكي")
        return result
    }

    // ==========================================
    // PHASE 5: AI RESEARCH WORKSPACE, RAG & REPORTS
    // ==========================================

    val ragEngine = com.example.data.ai.AiRagEngine(database)
    val reportsEngine = com.example.data.ai.ExecutiveReportsEngine(database)

    // Saved Research
    fun getSavedResearchForUser(userId: Long): Flow<List<SavedResearch>> =
        database.savedResearchDao().getSavedResearchForUser(userId)

    suspend fun insertSavedResearch(research: SavedResearch): Long {
        val id = database.savedResearchDao().insertSavedResearch(research)
        logActivity(research.userId, "SAVE_RESEARCH", research.title, "حفظ جلسة مساحة البحث والتحليل")
        return id
    }

    suspend fun deleteSavedResearch(research: SavedResearch) {
        database.savedResearchDao().deleteSavedResearch(research)
        logActivity(research.userId, "DELETE_RESEARCH", research.title, "حذف جلسة البحث المحفوظة")
    }

    // AI Conversations & Messages
    fun getConversationsForUser(userId: Long): Flow<List<AiConversation>> =
        database.aiConversationDao().getConversationsForUser(userId)

    fun getMessagesForConversation(conversationId: Long): Flow<List<AiMessage>> =
        database.aiConversationDao().getMessagesForConversation(conversationId)

    suspend fun createConversation(title: String, userId: Long = 1): Long {
        val conversation = AiConversation(
            userId = userId,
            title = title,
            modelUsed = ragEngine.configuredModel,
            promptVersion = ragEngine.promptVersion
        )
        val id = database.aiConversationDao().insertConversation(conversation)
        logActivity(userId, "CREATE_AI_CONVERSATION", title, "بدء جلسة حوار ذكية جديدة")
        return id
    }

    suspend fun insertAiMessage(message: AiMessage): Long {
        val id = database.aiConversationDao().insertMessage(message)
        database.aiConversationDao().touchConversation(message.conversationId, message.timestamp)
        return id
    }

    suspend fun deleteConversation(conversationId: Long, userId: Long = 1) {
        database.aiConversationDao().deleteConversationById(conversationId)
        database.aiConversationDao().deleteMessagesForConversation(conversationId)
        logActivity(userId, "DELETE_AI_CONVERSATION", "محادثة #$conversationId", "حذف المحادثة وسجل رسائلها")
    }

    // Generated Reports
    fun getReportsForUser(userId: Long): Flow<List<GeneratedReport>> =
        database.generatedReportDao().getReportsForUser(userId)

    fun getReportsForUserByType(userId: Long, type: ReportType): Flow<List<GeneratedReport>> =
        database.generatedReportDao().getReportsForUserByType(userId, type)

    suspend fun getReportById(id: Long): GeneratedReport? =
        database.generatedReportDao().getReportById(id)

    suspend fun deleteReport(report: GeneratedReport) {
        database.generatedReportDao().deleteReport(report)
        logActivity(report.userId, "DELETE_REPORT", report.title, "حذف التقرير التنفيذي")
    }

    suspend fun generateDailyBriefReport(userId: Long = 1): GeneratedReport {
        val report = reportsEngine.generateDailyBrief(userId)
        logActivity(userId, "GENERATE_DAILY_BRIEF", report.title, "توليد التقرير السياسي اليومي")
        return report
    }

    suspend fun generateEventReport(eventId: Long, userId: Long = 1): GeneratedReport {
        val report = reportsEngine.generateEventReport(eventId, userId)
        logActivity(userId, "GENERATE_EVENT_REPORT", report.title, "توليد تقرير الحدث الشامل (v${report.version})")
        return report
    }

    suspend fun generatePoliticalFileReport(fileId: Long, userId: Long = 1): GeneratedReport {
        val report = reportsEngine.generatePoliticalFileReport(fileId, userId)
        logActivity(userId, "GENERATE_FILE_REPORT", report.title, "توليد تقرير الملف السياسي الاستراتيجي")
        return report
    }

    suspend fun generateMonitoringReport(targetType: String, targetId: Long, targetName: String, days: Int, userId: Long = 1): GeneratedReport {
        val report = reportsEngine.generateMonitoringReport(targetType, targetId, targetName, days, userId)
        logActivity(userId, "GENERATE_MONITORING_REPORT", report.title, "توليد تقرير المراقبة والرصد لـ $targetName")
        return report
    }

    suspend fun executeRagQuery(
        question: String,
        workspaceState: ResearchWorkspaceState,
        language: String = "ar",
        userId: Long = 1
    ): StructuredAiAnswer {
        val answer = ragEngine.executeRagQuery(question, workspaceState, language)
        logActivity(userId, "AI_RAG_QUERY", question.take(40), "استعلام RAG مؤصل مع ${answer.sources.size} مصدر موثق")
        return answer
    }

    // ==========================================
    // Phase 6: Advanced Political Intelligence
    // ==========================================
    val politicalGraphEngine = com.example.data.intelligence.PoliticalGraphEngine(
        relationshipDao = database.politicalRelationshipDao(),
        personDao = database.personDao(),
        organizationDao = database.organizationDao(),
        countryDao = database.countryDao(),
        politicalFileDao = database.politicalFileDao(),
        politicalEventDao = database.politicalEventDao(),
        articleDao = database.articleDao(),
        statementDao = database.statementDao()
    )

    val trendAnalysisEngine = com.example.data.intelligence.TrendAnalysisEngine(
        articleDao = database.articleDao(),
        personDao = database.personDao(),
        organizationDao = database.organizationDao(),
        politicalFileDao = database.politicalFileDao(),
        topicDao = database.topicDao(),
        statementDao = database.statementDao()
    )

    val changeDetectionEngine = com.example.data.intelligence.ChangeDetectionEngine(
        changeDao = database.detectedChangeDao(),
        statementDao = database.statementDao(),
        personDao = database.personDao(),
        politicalFileDao = database.politicalFileDao(),
        politicalEventDao = database.politicalEventDao(),
        articleDao = database.articleDao()
    )

    val crossAnalysisEngine = com.example.data.intelligence.CrossAnalysisEngine(
        politicalFileDao = database.politicalFileDao(),
        politicalEventDao = database.politicalEventDao(),
        personDao = database.personDao(),
        organizationDao = database.organizationDao(),
        countryDao = database.countryDao(),
        topicDao = database.topicDao(),
        statementDao = database.statementDao(),
        contradictionDao = database.contradictionDao(),
        timelineDao = database.timelineDao(),
        articleDao = database.articleDao()
    )

    val smartMonitoringEngine = com.example.data.intelligence.SmartMonitoringEngine(
        ruleDao = database.smartMonitorRuleDao(),
        alertDao = database.alertDao(),
        articleDao = database.articleDao(),
        sourceDao = database.sourceDao()
    )

    val earlySignalEngine = com.example.data.intelligence.EarlySignalEngine(
        signalDao = database.earlySignalDao(),
        articleDao = database.articleDao(),
        relationshipDao = database.politicalRelationshipDao(),
        statementDao = database.statementDao(),
        politicalFileDao = database.politicalFileDao()
    )

    val explainabilityEngine = com.example.data.intelligence.ExplainabilityEngine(
        articleDao = database.articleDao(),
        statementDao = database.statementDao(),
        politicalRelationshipDao = database.politicalRelationshipDao()
    )

    // Relationships
    val allRelationships: Flow<List<PoliticalRelationship>> = database.politicalRelationshipDao().getAllRelationships()
    val pendingDiscoveriesCount: Flow<Int> = database.politicalRelationshipDao().getPendingDiscoveriesCount()

    fun getRelationshipsForEntity(entityType: EntityType, entityId: Long): Flow<List<PoliticalRelationship>> =
        database.politicalRelationshipDao().getRelationshipsForEntity(entityType, entityId)

    suspend fun getRelationshipsForEntityList(entityType: EntityType, entityId: Long): List<PoliticalRelationship> =
        database.politicalRelationshipDao().getRelationshipsForEntityList(entityType, entityId)

    suspend fun insertRelationship(relationship: PoliticalRelationship): Long {
        val id = database.politicalRelationshipDao().insertRelationship(relationship)
        logActivity(
            userId = 1,
            action = "ADD_RELATIONSHIP",
            entityTitle = "${relationship.sourceEntityName} - ${relationship.targetEntityName}",
            details = "إضافة علاقة سياسية جديدة نوع (${relationship.relationshipType.nameAr})"
        )
        return id
    }

    suspend fun confirmRelationshipDiscovery(id: Long, analystNotes: String? = null, userId: Long = 1) {
        politicalGraphEngine.confirmDiscovery(id, analystNotes)
        logActivity(userId, "CONFIRM_RELATIONSHIP_DISCOVERY", "علاقة #$id", "اعتماد اكتشاف العلاقة من المحلل")
    }

    suspend fun rejectRelationshipDiscovery(id: Long, reason: String? = null, userId: Long = 1) {
        politicalGraphEngine.rejectDiscovery(id, reason)
        logActivity(userId, "REJECT_RELATIONSHIP_DISCOVERY", "علاقة #$id", "رفض الاكتشاف: ${reason ?: "غير دقيق"}")
    }

    suspend fun runAiRelationshipDiscovery(userId: Long = 1): List<PoliticalRelationship> {
        val discovered = politicalGraphEngine.discoverRelationshipsFromCorpus()
        logActivity(userId, "AI_RELATIONSHIP_MINING", "استخراج شبكة العلاقات", "رصد ${discovered.size} علاقة مرشحة للمراجعة")
        return discovered
    }

    // Early Signals
    val allEarlySignals: Flow<List<EarlySignalItem>> = database.earlySignalDao().getAllSignals()
    val unacknowledgedSignalsCount: Flow<Int> = database.earlySignalDao().getUnacknowledgedCount()

    fun getSignalsForFile(fileId: Long): Flow<List<EarlySignalItem>> = database.earlySignalDao().getSignalsForFile(fileId)
    fun getSignalsForPerson(personId: Long): Flow<List<EarlySignalItem>> = database.earlySignalDao().getSignalsForPerson(personId)

    suspend fun acknowledgeSignal(id: Long, userId: Long = 1) {
        database.earlySignalDao().acknowledgeSignal(id)
        logActivity(userId, "ACKNOWLEDGE_SIGNAL", "إشارة مبكرة #$id", "اطلاع المحلل وتأكيد الاستلام")
    }

    suspend fun runEarlySignalsScan(userId: Long = 1): List<EarlySignalItem> {
        val signals = earlySignalEngine.scanForEarlySignals()
        logActivity(userId, "EARLY_SIGNALS_SCAN", "فحص المؤشرات المبكرة", "اكتشاف ${signals.size} مؤشر محتمل")
        return signals
    }

    // Detected Changes
    val allDetectedChanges: Flow<List<DetectedChangeItem>> = database.detectedChangeDao().getAllChanges()

    fun getChangesForEntity(entityType: EntityType, entityId: Long): Flow<List<DetectedChangeItem>> =
        database.detectedChangeDao().getChangesForEntity(entityType, entityId)

    suspend fun getChangesForEntityList(entityType: EntityType, entityId: Long): List<DetectedChangeItem> =
        database.detectedChangeDao().getChangesForEntityList(entityType, entityId)

    suspend fun runChangeDetectionScan(userId: Long = 1): List<DetectedChangeItem> {
        val changes = changeDetectionEngine.scanAndDetectChanges()
        logActivity(userId, "CHANGE_DETECTION_SCAN", "رصد التحولات السياسية", "اكتشاف ${changes.size} تحول مقارن")
        return changes
    }

    // Information Gaps
    val allInformationGaps: Flow<List<InformationGapItem>> = database.informationGapDao().getAllGaps()

    fun getGapsForEntity(entityType: EntityType, entityId: Long): Flow<List<InformationGapItem>> =
        database.informationGapDao().getGapsForEntity(entityType, entityId)

    suspend fun getGapsForEntityList(entityType: EntityType, entityId: Long): List<InformationGapItem> =
        database.informationGapDao().getGapsForEntityList(entityType, entityId)

    suspend fun insertInformationGap(gap: InformationGapItem, userId: Long = 1): Long {
        val id = database.informationGapDao().insertGap(gap)
        logActivity(userId, "ADD_INFO_GAP", gap.titleAr, "توثيق فجوة معلوماتية بحالة (${gap.status.displayNameAr()})")
        return id
    }

    // Smart Monitor Rules
    val allSmartRules: Flow<List<SmartMonitorRule>> = database.smartMonitorRuleDao().getAllRules()

    suspend fun insertSmartRule(rule: SmartMonitorRule, userId: Long = 1): Long {
        val id = database.smartMonitorRuleDao().insertRule(rule)
        logActivity(userId, "CREATE_SMART_RULE", rule.nameAr, "إنشاء قاعدة رصد ذكية لـ ${rule.targetName}")
        return id
    }

    suspend fun toggleSmartRule(rule: SmartMonitorRule, userId: Long = 1) {
        val updated = rule.copy(isActive = !rule.isActive)
        database.smartMonitorRuleDao().updateRule(updated)
        logActivity(userId, "TOGGLE_SMART_RULE", rule.nameAr, if (updated.isActive) "تفعيل الرصد" else "إيقاف الرصد")
    }

    suspend fun deleteSmartRule(rule: SmartMonitorRule, userId: Long = 1) {
        database.smartMonitorRuleDao().deleteRule(rule)
        logActivity(userId, "DELETE_SMART_RULE", rule.nameAr, "حذف قاعدة الرصد")
    }

    // =========================================================================
    // Interview Intelligence Module
    // =========================================================================
    val interviewIntelligenceEngine = com.example.data.intelligence.InterviewIntelligenceEngine(database)

    val allInterviews: Flow<List<Interview>> = database.interviewDao().getAllInterviews()
    val completedInterviews: Flow<List<Interview>> = database.interviewDao().getCompletedInterviews()

    fun getUpcomingInterviews(sinceUtc: Long = System.currentTimeMillis() - 3600000L): Flow<List<Interview>> =
        database.interviewDao().getUpcomingInterviews(sinceUtc)

    fun getInterviewsForUser(userId: Long): Flow<List<Interview>> =
        database.interviewDao().getInterviewsForUser(userId)

    suspend fun getInterviewById(id: Long): Interview? =
        database.interviewDao().getInterviewById(id)

    fun getInterviewByIdFlow(id: Long): Flow<Interview?> =
        database.interviewDao().getInterviewByIdFlow(id)

    suspend fun getInterviewsNeedingPreparation(): List<Interview> =
        database.interviewDao().getInterviewsNeedingPreparation(System.currentTimeMillis())

    suspend fun searchInterviews(query: String): List<Interview> =
        database.interviewDao().searchInterviews(query)

    suspend fun insertInterview(interview: Interview, userId: Long = 1): Long {
        val id = database.interviewDao().insertInterview(interview)
        logActivity(
            userId = userId,
            action = "CREATE_INTERVIEW",
            entityTitle = interview.subject,
            details = "تسجيل مقابلة جديدة مع (${interview.channel} - ${interview.program}) بتاريخ ${interview.startTime}"
        )
        return id
    }

    suspend fun updateInterview(interview: Interview, userId: Long = 1) {
        database.interviewDao().updateInterview(interview.copy(updatedAt = System.currentTimeMillis()))
        logActivity(
            userId = userId,
            action = "UPDATE_INTERVIEW",
            entityTitle = interview.subject,
            details = "تحديث بيانات المقابلة (${interview.status.displayNameAr})"
        )
    }

    suspend fun deleteInterview(interview: Interview, userId: Long = 1) {
        database.interviewDao().deleteInterview(interview)
        database.interviewLinkDao().getLinksForInterviewList(interview.id).forEach {
            database.interviewLinkDao().deleteLink(it)
        }
        database.interviewTranscriptDao().deleteTranscriptByInterviewId(interview.id)
        database.interviewAnalysisDao().deleteAnalysesByInterviewId(interview.id)
        logActivity(
            userId = userId,
            action = "DELETE_INTERVIEW",
            entityTitle = interview.subject,
            details = "حذف سجل المقابلة وروابطها وتفريغها"
        )
    }

    // Interview Links
    val allInterviewLinks: Flow<List<InterviewLink>> = database.interviewLinkDao().getAllLinks()

    fun getLinksForInterview(interviewId: Long): Flow<List<InterviewLink>> =
        database.interviewLinkDao().getLinksForInterview(interviewId)

    suspend fun insertInterviewLink(link: InterviewLink): Long =
        database.interviewLinkDao().insertLink(link)

    suspend fun deleteInterviewLink(link: InterviewLink) =
        database.interviewLinkDao().deleteLink(link)

    // Interview Transcripts
    fun getTranscriptForInterview(interviewId: Long): Flow<InterviewTranscript?> =
        database.interviewTranscriptDao().getTranscriptForInterview(interviewId)

    suspend fun processAndSaveTranscript(
        interviewId: Long,
        transcriptText: String,
        audioUrl: String? = null,
        videoUrl: String? = null
    ): InterviewTranscript =
        interviewIntelligenceEngine.processTranscript(interviewId, transcriptText, audioUrl, videoUrl)

    // Interview Analysis & Briefs
    fun getAnalysesForInterview(interviewId: Long): Flow<List<InterviewAnalysis>> =
        database.interviewAnalysisDao().getAnalysesForInterview(interviewId)

    suspend fun generatePreInterviewBrief(interviewId: Long, userId: Long = 1): PreInterviewBriefData =
        interviewIntelligenceEngine.generatePreInterviewBrief(interviewId, userId)

    fun generateOnePageBriefText(brief: PreInterviewBriefData): String =
        interviewIntelligenceEngine.generateOnePageBriefText(brief)

    suspend fun analyzePostInterview(interviewId: Long, userId: Long = 1): PostInterviewReportData =
        interviewIntelligenceEngine.analyzePostInterview(interviewId, userId)

    suspend fun answerInContextQuestion(interviewId: Long, question: String): String =
        interviewIntelligenceEngine.answerInContextQuestion(interviewId, question)
}
