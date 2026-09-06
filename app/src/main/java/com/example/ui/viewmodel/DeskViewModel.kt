package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ingestion.TestResult
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.model.*
import com.example.data.repository.DeskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DeskTab {
    DASHBOARD,
    EVENTS,
    NEWS_FEED,
    POLITICAL_FILES,
    ENTITIES,
    RESEARCH_WORKSPACE,
    AI_ASSISTANT,
    REPORTS,
    ALERTS,
    SOURCES,
    ADMIN_DESK,
    POLITICAL_INTELLIGENCE,
    INTERVIEWS;

    fun titleAr(): String = when (this) {
        DASHBOARD -> "الرئيسية"
        EVENTS -> "الأحداث السياسية"
        NEWS_FEED -> "الأخبار والرصد"
        POLITICAL_FILES -> "الملفات"
        ENTITIES -> "الكيانات"
        RESEARCH_WORKSPACE -> "مساحة البحث"
        AI_ASSISTANT -> "المساعد الذكي"
        REPORTS -> "التقارير"
        ALERTS -> "التنبيهات الذكية"
        SOURCES -> "المصادر"
        ADMIN_DESK -> "الإدارة"
        POLITICAL_INTELLIGENCE -> "الاستخبارات والشبكات"
        INTERVIEWS -> "المقابلات والاستعداد"
    }

    fun titleEn(): String = when (this) {
        DASHBOARD -> "Dashboard"
        EVENTS -> "Events"
        NEWS_FEED -> "News Feed"
        POLITICAL_FILES -> "Dossiers"
        ENTITIES -> "Entities"
        RESEARCH_WORKSPACE -> "Workspace"
        AI_ASSISTANT -> "AI Assistant"
        REPORTS -> "Reports"
        ALERTS -> "Alerts"
        SOURCES -> "Sources"
        ADMIN_DESK -> "Admin"
        POLITICAL_INTELLIGENCE -> "Intelligence & Networks"
        INTERVIEWS -> "Interviews"
    }
}

data class DeskUiState(
    val activeTab: DeskTab = DeskTab.DASHBOARD,
    val isArabic: Boolean = true,
    val currentUser: User? = null,
    val users: List<User> = emptyList(),
    val sources: List<Source> = emptyList(),
    val activeSourcesCount: Int = 0,
    val files: List<PoliticalFile> = emptyList(),
    val activeFilesCount: Int = 0,
    val persons: List<Person> = emptyList(),
    val organizations: List<Organization> = emptyList(),
    val countries: List<Country> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val activityLogs: List<ActivityLog> = emptyList(),
    val recentStanceShifts: List<PersonPosition> = emptyList(),

    // Phase 2: Articles & Ingestion State
    val articles: List<Article> = emptyList(),
    val totalArticlesCount: Int = 0,
    val articlesTodayCount: Int = 0,
    val duplicatesCount: Int = 0,
    val ingestionLogs: List<IngestionLog> = emptyList(),
    val totalIngestionErrors: Int = 0,
    val isIngestingGlobal: Boolean = false,
    val ingestingSourceId: Long? = null,
    val syncFeedbackMessage: String? = null,

    // Phase 2: Search & Filter State
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL_TIME,
    val customFromUtc: Long? = null,
    val customToUtc: Long? = null,
    val sourceTierFilter: SourceTier? = null,
    val selectedSourceIdFilter: Long? = null,
    val selectedTopicIdFilter: Long? = null,
    val selectedCountryFilter: String? = null,
    val selectedClassificationFilter: ArticleClassification? = null,
    val selectedPoliticalFileFilter: Long? = null,
    val onlySavedFilter: Boolean = false,
    val includeDuplicatesFilter: Boolean = false,
    val filePriorityFilter: FilePriority? = null,

    // Phase 2: Modals & Detail State
    val activeArticleDetail: Article? = null,
    val showManualImportDialog: Boolean = false,
    val isManualImporting: Boolean = false,
    val manualImportError: String? = null,
    val testingSourceId: Long? = null,
    val probeStatusMessage: String? = null,

    // Phase 3: Events & Story Dossiers
    val events: List<PoliticalEvent> = emptyList(),
    val activeDevelopingEventsCount: Int = 0,
    val selectedEventStatusFilter: EventStatus? = null,
    val selectedEventImportanceFilter: EventImportance? = null,
    val activeEventDetail: PoliticalEvent? = null,
    val eventArticles: List<Article> = emptyList(),
    val eventClaims: List<Claim> = emptyList(),
    val eventStatements: List<Statement> = emptyList(),
    val eventEvidence: List<EvidenceItem> = emptyList(),
    val eventTimeline: List<TimelineItem> = emptyList(),
    val eventContradictions: List<ContradictionItem> = emptyList(),

    // Phase 3: Intelligence Verification & Contradictions
    val allClaims: List<Claim> = emptyList(),
    val allStatements: List<Statement> = emptyList(),
    val allContradictions: List<ContradictionItem> = emptyList(),
    val openContradictionsCount: Int = 0,

    // Phase 3: Smart Alerts & Notification System
    val alerts: List<AlertItem> = emptyList(),
    val unreadAlertsCount: Int = 0,
    val alertRules: List<AlertRule> = emptyList(),
    val showAlertRuleDialog: Boolean = false,
    val showCreateEventDialog: Boolean = false,
    val eventClusteringFeedback: String? = null,

    // Phase 4: AI Intelligence & Analytical Engine
    val isAiLoading: Boolean = false,
    val aiErrorMessage: String? = null,
    val currentArticleSummary: ArticleSummaryResult? = null,
    val currentEventSummary: EventSummaryResult? = null,
    val currentNarrativeComparison: NarrativeComparisonResult? = null,
    val currentContradictionAnalysis: ContradictionAnalysisResult? = null,
    val currentSourceTracing: SourceTracingResultData? = null,
    val currentWhatChanged: WhatChangedResult? = null,
    val currentPersonPosition: PersonPositionResult? = null,
    val currentMediaPerspective: MediaPerspectiveResult? = null,
    val currentEvidenceAnalysis: EvidenceAnalysisResult? = null,
    val currentInformationGaps: InformationGapsResult? = null,
    val currentFileAnalysis: FileAnalysisResult? = null,
    val currentTimelineSummary: TimelineSummaryResult? = null,
    val dashboardAiBrief: DashboardAiBriefResult? = null,
    val analysesHistory: List<AiAnalysis> = emptyList(),
    val selectedAiModel: String = "gemini-3.5-flash",
    val showAiIntelligenceDialog: Boolean = false,
    val activeAiAnalysisType: AiAnalysisType? = null,

    // Phase 5: Research Workspace State
    val workspaceState: ResearchWorkspaceState = ResearchWorkspaceState(),
    val isWorkspacePickerOpen: Boolean = false,
    val workspacePickerTarget: String? = null, // "ARTICLES", "EVENTS", "FILES", "PERSONS", "TOPICS", "SOURCES"
    val currentRagQuestion: String = "",
    val isRagLoading: Boolean = false,
    val ragErrorMessage: String? = null,
    val currentRagAnswer: StructuredAiAnswer? = null,
    val ragLanguage: String = "ar",

    // Phase 5: Saved Research Sessions
    val savedResearchList: List<SavedResearch> = emptyList(),
    val showSaveResearchDialog: Boolean = false,
    val activeSavedResearch: SavedResearch? = null,

    // Phase 5: AI Conversations & Chat Assistant
    val conversations: List<AiConversation> = emptyList(),
    val activeConversation: AiConversation? = null,
    val activeConversationMessages: List<AiMessage> = emptyList(),
    val currentAssistantMessageInput: String = "",
    val isAssistantResponding: Boolean = false,

    // Phase 5: Executive Reports & History
    val generatedReports: List<GeneratedReport> = emptyList(),
    val selectedReportDetail: GeneratedReport? = null,
    val isReportGenerating: Boolean = false,
    val reportGenerationError: String? = null,
    val showReportGenerationDialog: Boolean = false,
    val selectedReportTypeFilter: ReportType? = null,

    // Phase 6: Advanced Political Intelligence
    val relationships: List<PoliticalRelationship> = emptyList(),
    val pendingDiscoveriesCount: Int = 0,
    val earlySignals: List<EarlySignalItem> = emptyList(),
    val unacknowledgedSignalsCount: Int = 0,
    val detectedChanges: List<DetectedChangeItem> = emptyList(),
    val informationGaps: List<InformationGapItem> = emptyList(),
    val smartRules: List<SmartMonitorRule> = emptyList(),
    val graphNodes: List<GraphNode> = emptyList(),
    val graphEdges: List<GraphEdge> = emptyList(),
    val selectedGraphNode: GraphNode? = null,
    val selectedGraphEdge: GraphEdge? = null,
    val graphSelectedEntityType: EntityType? = null,
    val graphSelectedRelType: RelationshipType? = null,
    val graphMinConfidence: Float = 0.5f,
    val graphOnlyDirect: Boolean = false,
    val graphSearchQuery: String = "",
    val activeTrendReport: com.example.data.intelligence.TrendAnalysisEngine.TrendReport? = null,
    val selectedTrendTimeframe: TrendTimeframe = TrendTimeframe.DAYS_7,
    val isTrendLoading: Boolean = false,
    val selectedCrossFileIds: Set<Long> = emptySet(),
    val activeCrossFileAnalysis: CrossFileAnalysis? = null,
    val isCrossFileLoading: Boolean = false,
    val selectedCrossEventIds: Set<Long> = emptySet(),
    val activeCrossEventAnalysis: CrossEventAnalysis? = null,
    val isCrossEventLoading: Boolean = false,
    val explainabilityModalReport: com.example.data.intelligence.ExplainabilityEngine.ExplainabilityReport? = null,
    val showCreateSmartRuleDialog: Boolean = false,
    val showCreateInfoGapDialog: Boolean = false,
    val intelligenceActiveSubTab: Int = 0,
    // Interview Intelligence State
    val interviews: List<Interview> = emptyList(),
    val upcomingInterviews: List<Interview> = emptyList(),
    val completedInterviews: List<Interview> = emptyList(),
    val interviewLinks: List<InterviewLink> = emptyList(),
    val selectedInterviewDetail: Interview? = null,
    val selectedInterviewLinks: List<InterviewLink> = emptyList(),
    val selectedInterviewTranscript: InterviewTranscript? = null,
    val selectedInterviewAnalyses: List<InterviewAnalysis> = emptyList(),
    val activePreInterviewBrief: PreInterviewBriefData? = null,
    val activeOnePageBriefText: String? = null,
    val activePostInterviewReport: PostInterviewReportData? = null,
    val isInterviewAiLoading: Boolean = false,
    val interviewAiErrorMessage: String? = null,
    val interviewCalendarViewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val interviewSearchQuery: String = "",
    val selectedInterviewStatusFilter: InterviewStatus? = null,
    val selectedInterviewTypeFilter: InterviewType? = null,
    val selectedInterviewChannelFilter: String? = null,
    val showCreateInterviewDialog: Boolean = false,
    val showAddInterviewLinkDialog: Boolean = false,
    val showAddTranscriptDialog: Boolean = false,
    val showOnePageBriefDialog: Boolean = false,
    val showCompareInterviewsDialog: Boolean = false,
    val selectedInterviewIdsForComparison: Set<Long> = emptySet(),
    val inContextChatMessages: List<Pair<String, String>> = emptyList(),
    val isInContextChatResponding: Boolean = false,
    val currentInContextChatInput: String = "",
    val selectedCalendarDateUtc: Long = System.currentTimeMillis()
)

class DeskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = DeskRepository(database)

    private val _uiState = MutableStateFlow(DeskUiState())
    val uiState: StateFlow<DeskUiState> = _uiState.asStateFlow()

    private var articlesJob: Job? = null

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedIfEmpty(database)
            observeData()
            refreshFilteredArticles()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.allUsers.collect { userList ->
                _uiState.update { state ->
                    val active = userList.firstOrNull { it.isActive } ?: userList.firstOrNull()
                    state.copy(users = userList, currentUser = active)
                }
            }
        }

        viewModelScope.launch {
            repository.allSources.collect { sourceList ->
                _uiState.update { it.copy(sources = sourceList) }
            }
        }

        viewModelScope.launch {
            repository.activeSourcesCount.collect { count ->
                _uiState.update { it.copy(activeSourcesCount = count) }
            }
        }

        viewModelScope.launch {
            repository.allFiles.collect { fileList ->
                _uiState.update { it.copy(files = fileList) }
            }
        }

        viewModelScope.launch {
            repository.activeFilesCount.collect { count ->
                _uiState.update { it.copy(activeFilesCount = count) }
            }
        }

        viewModelScope.launch {
            repository.allPersons.collect { personList ->
                _uiState.update { it.copy(persons = personList) }
            }
        }

        viewModelScope.launch {
            repository.allOrganizations.collect { orgList ->
                _uiState.update { it.copy(organizations = orgList) }
            }
        }

        viewModelScope.launch {
            repository.allCountries.collect { countryList ->
                _uiState.update { it.copy(countries = countryList) }
            }
        }

        viewModelScope.launch {
            repository.allTopics.collect { topicList ->
                _uiState.update { it.copy(topics = topicList) }
            }
        }

        viewModelScope.launch {
            repository.recentLogs.collect { logs ->
                _uiState.update { it.copy(activityLogs = logs) }
            }
        }

        viewModelScope.launch {
            repository.recentStanceShifts.collect { shifts ->
                _uiState.update { it.copy(recentStanceShifts = shifts) }
            }
        }

        // Phase 2 Observables
        viewModelScope.launch {
            repository.totalArticlesCount.collect { count ->
                _uiState.update { it.copy(totalArticlesCount = count) }
            }
        }

        viewModelScope.launch {
            repository.getArticlesCountToday().collect { count ->
                _uiState.update { it.copy(articlesTodayCount = count) }
            }
        }

        viewModelScope.launch {
            repository.duplicatesCount.collect { count ->
                _uiState.update { it.copy(duplicatesCount = count) }
            }
        }

        viewModelScope.launch {
            repository.recentIngestionLogs.collect { logs ->
                _uiState.update { it.copy(ingestionLogs = logs) }
            }
        }

        viewModelScope.launch {
            repository.totalIngestionErrors.collect { errs ->
                _uiState.update { it.copy(totalIngestionErrors = errs ?: 0) }
            }
        }

        // Phase 3 Observables
        viewModelScope.launch {
            repository.allEvents.collect { eventList ->
                val activeCount = eventList.count { it.status == EventStatus.ACTIVE || it.status == EventStatus.DEVELOPING }
                _uiState.update { it.copy(events = eventList, activeDevelopingEventsCount = activeCount) }
            }
        }

        viewModelScope.launch {
            repository.allClaims.collect { claimList ->
                _uiState.update { it.copy(allClaims = claimList) }
            }
        }

        viewModelScope.launch {
            repository.allStatements.collect { statementList ->
                _uiState.update { it.copy(allStatements = statementList) }
            }
        }

        viewModelScope.launch {
            repository.allContradictions.collect { contradictionList ->
                val openCount = contradictionList.count { it.status == ContradictionStatus.OPEN }
                _uiState.update { it.copy(allContradictions = contradictionList, openContradictionsCount = openCount) }
            }
        }

        viewModelScope.launch {
            repository.getAlertsForUser(1).collect { alertList ->
                _uiState.update { it.copy(alerts = alertList) }
            }
        }

        // Interview Intelligence Observables
        viewModelScope.launch {
            repository.allInterviews.collect { interviewList ->
                _uiState.update { it.copy(interviews = interviewList) }
            }
        }

        viewModelScope.launch {
            repository.getUpcomingInterviews().collect { upcomingList ->
                _uiState.update { it.copy(upcomingInterviews = upcomingList) }
            }
        }

        viewModelScope.launch {
            repository.completedInterviews.collect { completedList ->
                _uiState.update { it.copy(completedInterviews = completedList) }
            }
        }

        viewModelScope.launch {
            repository.allInterviewLinks.collect { links ->
                _uiState.update { it.copy(interviewLinks = links) }
            }
        }

        viewModelScope.launch {
            repository.unreadAlertsCount.collect { count ->
                _uiState.update { it.copy(unreadAlertsCount = count) }
            }
        }

        viewModelScope.launch {
            repository.getAlertRulesForUser(1).collect { rules ->
                _uiState.update { it.copy(alertRules = rules) }
            }
        }

        viewModelScope.launch {
            repository.allAnalysesHistory.collect { history ->
                _uiState.update { it.copy(analysesHistory = history) }
            }
        }

        // Phase 5 Observables
        viewModelScope.launch {
            repository.getSavedResearchForUser(1).collect { list ->
                _uiState.update { it.copy(savedResearchList = list) }
            }
        }

        viewModelScope.launch {
            repository.getConversationsForUser(1).collect { convList ->
                _uiState.update { state ->
                    val active = state.activeConversation ?: convList.firstOrNull()
                    state.copy(conversations = convList, activeConversation = active)
                }
            }
        }

        viewModelScope.launch {
            repository.getReportsForUser(1).collect { reportList ->
                _uiState.update { it.copy(generatedReports = reportList) }
            }
        }

        // Phase 6 Observables
        viewModelScope.launch {
            repository.allRelationships.collect { relList ->
                _uiState.update { it.copy(relationships = relList) }
                refreshGraph()
            }
        }

        viewModelScope.launch {
            repository.pendingDiscoveriesCount.collect { count ->
                _uiState.update { it.copy(pendingDiscoveriesCount = count) }
            }
        }

        viewModelScope.launch {
            repository.allEarlySignals.collect { signals ->
                _uiState.update { it.copy(earlySignals = signals) }
            }
        }

        viewModelScope.launch {
            repository.unacknowledgedSignalsCount.collect { count ->
                _uiState.update { it.copy(unacknowledgedSignalsCount = count) }
            }
        }

        viewModelScope.launch {
            repository.allDetectedChanges.collect { changes ->
                _uiState.update { it.copy(detectedChanges = changes) }
            }
        }

        viewModelScope.launch {
            repository.allInformationGaps.collect { gaps ->
                _uiState.update { it.copy(informationGaps = gaps) }
            }
        }

        viewModelScope.launch {
            repository.allSmartRules.collect { rules ->
                _uiState.update { it.copy(smartRules = rules) }
            }
        }

        viewModelScope.launch {
            computeTrends(TrendTimeframe.DAYS_7)
        }

        viewModelScope.launch {
            refreshDashboardAiBrief()
        }
    }

    private fun refreshFilteredArticles() {
        articlesJob?.cancel()
        val s = _uiState.value
        val (startUtc, endUtc) = s.dateFilter.getUtcRange(s.customFromUtc, s.customToUtc)

        articlesJob = viewModelScope.launch {
            repository.getFilteredArticles(
                query = s.searchQuery,
                startUtc = startUtc,
                endUtc = endUtc,
                sourceId = s.selectedSourceIdFilter,
                topicId = s.selectedTopicIdFilter,
                sourceTier = s.sourceTierFilter,
                classification = s.selectedClassificationFilter,
                countryCode = s.selectedCountryFilter,
                politicalFileId = s.selectedPoliticalFileFilter,
                onlySaved = s.onlySavedFilter,
                includeDuplicates = s.includeDuplicatesFilter
            ).collect { list ->
                _uiState.update { it.copy(articles = list) }
            }
        }
    }

    fun selectTab(tab: DeskTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshFilteredArticles()
    }

    fun setDateFilter(filter: DateFilter, customFrom: Long? = null, customTo: Long? = null) {
        _uiState.update { it.copy(dateFilter = filter, customFromUtc = customFrom, customToUtc = customTo) }
        refreshFilteredArticles()
    }

    fun setSourceTierFilter(tier: SourceTier?) {
        _uiState.update { it.copy(sourceTierFilter = tier) }
        refreshFilteredArticles()
    }

    fun setSelectedSourceIdFilter(sourceId: Long?) {
        _uiState.update { it.copy(selectedSourceIdFilter = sourceId) }
        refreshFilteredArticles()
    }

    fun setSelectedTopicIdFilter(topicId: Long?) {
        _uiState.update { it.copy(selectedTopicIdFilter = topicId) }
        refreshFilteredArticles()
    }

    fun setSelectedCountryFilter(countryCode: String?) {
        _uiState.update { it.copy(selectedCountryFilter = countryCode) }
        refreshFilteredArticles()
    }

    fun setSelectedClassificationFilter(classification: ArticleClassification?) {
        _uiState.update { it.copy(selectedClassificationFilter = classification) }
        refreshFilteredArticles()
    }

    fun setSelectedPoliticalFileFilter(fileId: Long?) {
        _uiState.update { it.copy(selectedPoliticalFileFilter = fileId) }
        refreshFilteredArticles()
    }

    fun setFilePriorityFilter(priority: FilePriority?) {
        _uiState.update { it.copy(filePriorityFilter = priority) }
    }

    fun toggleOnlySavedFilter() {
        _uiState.update { it.copy(onlySavedFilter = !it.onlySavedFilter) }
        refreshFilteredArticles()
    }

    fun toggleIncludeDuplicatesFilter() {
        _uiState.update { it.copy(includeDuplicatesFilter = !it.includeDuplicatesFilter) }
        refreshFilteredArticles()
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                dateFilter = DateFilter.ALL_TIME,
                customFromUtc = null,
                customToUtc = null,
                sourceTierFilter = null,
                selectedSourceIdFilter = null,
                selectedTopicIdFilter = null,
                selectedCountryFilter = null,
                selectedClassificationFilter = null,
                selectedPoliticalFileFilter = null,
                onlySavedFilter = false,
                includeDuplicatesFilter = false
            )
        }
        refreshFilteredArticles()
    }

    fun openArticleDetails(article: Article) {
        _uiState.update { it.copy(activeArticleDetail = article) }
    }

    fun closeArticleDetails() {
        _uiState.update { it.copy(activeArticleDetail = null) }
    }

    fun toggleSaveArticle(article: Article) {
        viewModelScope.launch {
            val newSaved = !article.isSaved
            repository.setArticleSaved(article.id, newSaved)
            _uiState.update { state ->
                val updatedList = state.articles.map { if (it.id == article.id) it.copy(isSaved = newSaved) else it }
                val updatedDetail = if (state.activeArticleDetail?.id == article.id) state.activeArticleDetail.copy(isSaved = newSaved) else state.activeArticleDetail
                state.copy(articles = updatedList, activeArticleDetail = updatedDetail)
            }
        }
    }

    fun linkArticleToFile(articleId: Long, file: PoliticalFile) {
        viewModelScope.launch {
            repository.linkArticleToFile(articleId, file.id, file.titleAr)
            _uiState.update { state ->
                val updatedList = state.articles.map { if (it.id == articleId) it.copy(politicalFileId = file.id, politicalFileTitle = file.titleAr) else it }
                val updatedDetail = if (state.activeArticleDetail?.id == articleId) state.activeArticleDetail.copy(politicalFileId = file.id, politicalFileTitle = file.titleAr) else state.activeArticleDetail
                state.copy(articles = updatedList, activeArticleDetail = updatedDetail)
            }
        }
    }

    // Ingestion Actions
    fun syncAllSourcesNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isIngestingGlobal = true, syncFeedbackMessage = "جارٍ مزامنة جميع المصادر النشطة عبر محرك الرصد...") }
            val logs = repository.ingestAllActiveSources()
            val inserted = logs.sumOf { it.insertedCount }
            val dups = logs.sumOf { it.duplicateCount }
            val msg = "اكتملت المزامنة: تم إدراج $inserted مقالاً جديداً، وتحديد $dups مكرر"
            _uiState.update { it.copy(isIngestingGlobal = false, syncFeedbackMessage = msg) }
            refreshFilteredArticles()
        }
    }

    fun syncSingleSource(source: Source) {
        viewModelScope.launch {
            _uiState.update { it.copy(ingestingSourceId = source.id, probeStatusMessage = "جارٍ جلب التغذية من ${source.nameAr}...") }
            val log = repository.ingestSingleSource(source)
            val msg = if (log.status == IngestionStatus.FAILED) {
                "فشل جلب ${source.nameAr}: ${log.errorDetails}"
            } else {
                "تم جلب ${source.nameAr}: ${log.insertedCount} جديد (${log.duplicateCount} مكرر)"
            }
            _uiState.update { it.copy(ingestingSourceId = null, probeStatusMessage = msg) }
            refreshFilteredArticles()
        }
    }

    fun openManualImportDialog() {
        _uiState.update { it.copy(showManualImportDialog = true, manualImportError = null) }
    }

    fun closeManualImportDialog() {
        _uiState.update { it.copy(showManualImportDialog = false, manualImportError = null) }
    }

    fun importManualArticle(
        url: String,
        sourceId: Long,
        customTitle: String? = null,
        customSnippet: String? = null,
        customPublishedAt: Long? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isManualImporting = true, manualImportError = null) }
            val res = repository.importManualArticle(url, sourceId, customTitle, customSnippet, customPublishedAt, notes)
            if (res.isSuccess) {
                _uiState.update { it.copy(isManualImporting = false, showManualImportDialog = false) }
                refreshFilteredArticles()
            } else {
                _uiState.update { it.copy(isManualImporting = false, manualImportError = res.exceptionOrNull()?.message ?: "خطأ أثناء استخراج وحفظ الخبر") }
            }
        }
    }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            val current = _uiState.value.currentUser ?: return@launch
            val updated = current.copy(role = role)
            repository.updateUser(updated)
            repository.logActivity(
                userId = current.id,
                action = "ROLE_CHANGED",
                entityTitle = current.fullNameAr,
                details = "تم تغيير صلاحية المحلل إلى: ${role.displayNameAr()}"
            )
        }
    }

    fun switchUser(user: User) {
        viewModelScope.launch {
            val users = _uiState.value.users
            users.forEach { u ->
                repository.updateUser(u.copy(isActive = (u.id == user.id)))
            }
            _uiState.update { it.copy(currentUser = user) }
            repository.logActivity(
                userId = user.id,
                action = "USER_LOGIN",
                entityTitle = user.fullNameAr,
                details = "تسجيل دخول المحلل: ${user.fullNameAr} (${user.role.displayNameAr()})"
            )
        }
    }

    fun addUser(
        fullNameAr: String,
        fullNameEn: String,
        email: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            val initials = if (fullNameEn.isNotBlank()) {
                fullNameEn.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
            } else if (fullNameAr.isNotBlank()) {
                fullNameAr.trim().take(2)
            } else "AN"

            val username = if (email.contains("@")) {
                email.substringBefore("@").lowercase().replace(" ", ".")
            } else {
                "analyst.${System.currentTimeMillis() % 10000}"
            }

            val newUser = User(
                username = username,
                fullNameAr = fullNameAr.ifBlank { if (fullNameEn.isNotBlank()) fullNameEn else "محلل سياسي جديد" },
                fullNameEn = fullNameEn.ifBlank { if (fullNameAr.isNotBlank()) fullNameAr else "New Desk Analyst" },
                email = email.ifBlank { "$username@desk.intel" },
                role = role,
                avatarInitials = if (initials.isNotBlank()) initials else "AN",
                isActive = false
            )
            repository.insertUser(newUser)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user.id)
            if (_uiState.value.currentUser?.id == user.id) {
                val remaining = _uiState.value.users.filter { it.id != user.id }
                if (remaining.isNotEmpty()) {
                    switchUser(remaining.first())
                }
            }
        }
    }

    fun testSource(source: Source) {
        viewModelScope.launch {
            _uiState.update { it.copy(testingSourceId = source.id, probeStatusMessage = "جارٍ اختبار الاتصال بـ ${source.nameAr}...") }
            val result: TestResult = repository.testSource(source)
            _uiState.update { it.copy(testingSourceId = null, probeStatusMessage = "${result.state.displayNameAr()}: ${result.message}") }
        }
    }

    fun addSource(
        nameAr: String,
        nameEn: String,
        countryCode: String,
        tier: SourceTier,
        fetchMethod: FetchMethod,
        websiteUrl: String,
        rssUrl: String?,
        apiUrl: String?,
        apiConfigJson: String? = null,
        sourceType: String = "NEWS_AGENCY",
        notes: String,
        reliability: Int,
        isOfficial: Boolean
    ) {
        viewModelScope.launch {
            val newSource = Source(
                nameAr = nameAr,
                nameEn = nameEn.ifBlank { nameAr },
                countryCode = countryCode,
                tier = tier,
                fetchMethod = fetchMethod,
                websiteUrl = websiteUrl,
                rssUrl = rssUrl?.takeIf { it.isNotBlank() },
                apiUrl = apiUrl?.takeIf { it.isNotBlank() },
                apiConfigJson = apiConfigJson?.takeIf { it.isNotBlank() },
                sourceType = sourceType,
                reliabilityLevel = reliability,
                notes = notes,
                isOfficial = isOfficial
            )
            val id = repository.insertSource(newSource)
            // Immediately run a test probe on the new source
            testSource(newSource.copy(id = id))
        }
    }

    fun updateSource(source: Source) {
        viewModelScope.launch {
            repository.updateSource(source)
        }
    }

    fun toggleSourceStatus(source: Source) {
        val newStatus = if (source.status == SourceStatus.ACTIVE) SourceStatus.PAUSED else SourceStatus.ACTIVE
        updateSource(source.copy(status = newStatus))
    }

    fun deleteSource(source: Source) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    fun createPoliticalFile(
        titleAr: String,
        titleEn: String,
        description: String,
        priority: FilePriority,
        topicId: Long,
        primaryCountryCode: String,
        linkedCountryCodes: String,
        analystNotes: String
    ) {
        viewModelScope.launch {
            val file = PoliticalFile(
                titleAr = titleAr,
                titleEn = titleEn.ifBlank { titleAr },
                description = description,
                status = FileStatus.ACTIVE,
                priority = priority,
                topicId = topicId,
                primaryCountryCode = primaryCountryCode,
                linkedCountryCodes = linkedCountryCodes,
                ownerUserId = _uiState.value.currentUser?.id,
                analystNotes = analystNotes,
                recentDevelopments = "تم فتح ملف المتابعة الاستراتيجي.",
                whatChangedDelta = "ملف جديد قيد المراقبة اللحظية."
            )
            repository.insertFile(file)
        }
    }

    fun updateFileStatus(file: PoliticalFile, newStatus: FileStatus) {
        viewModelScope.launch {
            repository.updateFile(file.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            repository.logActivity(
                userId = _uiState.value.currentUser?.id ?: 1,
                action = "FILE_STATUS_CHANGED",
                entityTitle = file.titleAr,
                details = "تحديث حالة الملف إلى: ${newStatus.displayNameAr()}"
            )
        }
    }

    fun addPerson(
        nameAr: String,
        nameEn: String,
        currentRoleAr: String,
        currentRoleEn: String,
        countryCode: String,
        bio: String,
        tags: String
    ) {
        viewModelScope.launch {
            val person = Person(
                nameAr = nameAr,
                nameEn = nameEn.ifBlank { nameAr },
                currentRoleAr = currentRoleAr,
                currentRoleEn = currentRoleEn.ifBlank { currentRoleAr },
                countryCode = countryCode,
                bio = bio,
                tags = tags
            )
            repository.insertPerson(person)
        }
    }

    fun recordStanceShift(
        personId: Long,
        topicId: Long,
        issueTitle: String,
        previousStance: String,
        currentStance: String,
        shiftType: StanceShiftType,
        verbatimStatement: String,
        sourceName: String,
        sourceUrl: String,
        context: String,
        confidence: Int
    ) {
        viewModelScope.launch {
            val position = PersonPosition(
                personId = personId,
                topicId = topicId,
                issueTitle = issueTitle,
                previousStance = previousStance,
                currentStance = currentStance,
                shiftType = shiftType,
                verbatimStatement = verbatimStatement,
                sourceName = sourceName,
                sourceUrl = sourceUrl,
                context = context,
                evidenceConfidence = confidence
            )
            repository.insertPosition(position)
        }
    }

    fun resetAndReseedData() {
        viewModelScope.launch {
            DatabaseInitializer.seedIfEmpty(database)
            refreshFilteredArticles()
        }
    }

    // ==========================================
    // PHASE 3 METHODS: EVENTS, VERIFICATION & ALERTS
    // ==========================================

    fun selectEvent(event: PoliticalEvent?) {
        _uiState.update {
            it.copy(
                activeEventDetail = event,
                eventArticles = emptyList(),
                eventClaims = emptyList(),
                eventStatements = emptyList(),
                eventEvidence = emptyList(),
                eventTimeline = emptyList(),
                eventContradictions = emptyList()
            )
        }

        if (event != null) {
            viewModelScope.launch {
                repository.getArticlesForEvent(event.id).collect { articles ->
                    _uiState.update { it.copy(eventArticles = articles) }
                }
            }
            viewModelScope.launch {
                repository.getClaimsForEvent(event.id).collect { claims ->
                    _uiState.update { it.copy(eventClaims = claims) }
                }
            }
            viewModelScope.launch {
                repository.getStatementsForEvent(event.id).collect { statements ->
                    _uiState.update { it.copy(eventStatements = statements) }
                }
            }
            viewModelScope.launch {
                repository.getEvidenceForEvent(event.id).collect { evidence ->
                    _uiState.update { it.copy(eventEvidence = evidence) }
                }
            }
            viewModelScope.launch {
                repository.getTimelineForEvent(event.id).collect { timeline ->
                    _uiState.update { it.copy(eventTimeline = timeline) }
                }
            }
            viewModelScope.launch {
                repository.getContradictionsForEvent(event.id).collect { contradictions ->
                    _uiState.update { it.copy(eventContradictions = contradictions) }
                }
            }
        }
    }

    fun setEventStatusFilter(status: EventStatus?) {
        _uiState.update { it.copy(selectedEventStatusFilter = status) }
    }

    fun setEventImportanceFilter(importance: EventImportance?) {
        _uiState.update { it.copy(selectedEventImportanceFilter = importance) }
    }

    fun clusterArticle(article: Article) = clusterArticleIntoEvent(article)

    fun clusterArticleIntoEvent(article: Article) {
        viewModelScope.launch {
            _uiState.update { it.copy(eventClusteringFeedback = "جاري تجميع الخبر وتحليل القرائن والأدلة...") }
            val clusteredEvent = repository.clusterAndProcessArticle(article)
            _uiState.update {
                it.copy(
                    eventClusteringFeedback = "تم ربط الخبر بملف الحدث: ${clusteredEvent.titleAr} بنجاح."
                )
            }
            refreshFilteredArticles()
        }
    }

    fun linkArticleToEvent(articleId: Long, eventId: Long, eventTitle: String) {
        viewModelScope.launch {
            repository.linkArticleToEvent(articleId, eventId, eventTitle)
            refreshFilteredArticles()
        }
    }

    fun removeArticleFromEvent(articleId: Long, eventId: Long) {
        viewModelScope.launch {
            repository.removeArticleFromEvent(articleId, eventId)
            refreshFilteredArticles()
        }
    }

    fun updateClaimVerification(claimId: Long, newStatus: VerificationStatus, reason: String) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.updateClaimVerification(claimId, newStatus, reason, userId)
        }
    }

    fun updateContradictionStatus(id: Long, status: ContradictionStatus, notes: String) {
        viewModelScope.launch {
            repository.updateContradictionStatus(id, status, notes)
        }
    }

    fun createEvent(
        titleAr: String,
        titleEn: String,
        summaryAr: String,
        description: String,
        status: EventStatus,
        importance: EventImportance,
        location: String,
        primaryCountryCode: String,
        linkedCountryCodes: String,
        linkedPersonNames: String,
        linkedOrgNames: String,
        topicId: Long?,
        topicName: String?,
        politicalFileId: Long?,
        politicalFileTitle: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val event = PoliticalEvent(
                titleAr = titleAr,
                titleEn = titleEn.ifBlank { titleAr },
                summaryAr = summaryAr,
                description = description,
                status = status,
                importance = importance,
                startDate = now,
                latestUpdate = now,
                location = location,
                primaryCountryCode = primaryCountryCode,
                linkedCountryCodes = linkedCountryCodes,
                linkedPersonNames = linkedPersonNames,
                linkedOrgNames = linkedOrgNames,
                topicId = topicId,
                topicName = topicName,
                politicalFileId = politicalFileId,
                politicalFileTitle = politicalFileTitle,
                articleCount = 0,
                latestDevelopmentAr = "تم إنشاء ملف الحدث السياسي",
                whatChangedAr = "بدء تتبع مجريات وتطورات الحدث",
                independentSourceCount = 0,
                isVerified = true
            )
            repository.insertEvent(event)
            _uiState.update { it.copy(showCreateEventDialog = false) }
        }
    }

    fun updateEvent(event: PoliticalEvent) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: PoliticalEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            if (_uiState.value.activeEventDetail?.id == event.id) {
                _uiState.update { it.copy(activeEventDetail = null) }
            }
        }
    }

    fun markAlertAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAlertAsRead(id)
        }
    }

    fun markAllAlertsAsRead() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.markAllAlertsAsRead(userId)
        }
    }

    fun deleteAlert(alert: AlertItem) {
        viewModelScope.launch {
            repository.deleteAlert(alert)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.clearAllAlerts(userId)
        }
    }

    fun insertAlertRule(
        name: String,
        alertType: AlertType?,
        topicId: Long?,
        topicName: String?,
        personName: String?,
        organizationName: String?,
        politicalFileId: Long?,
        politicalFileTitle: String?,
        countryCode: String?,
        minSeverity: AlertSeverity
    ) {
        viewModelScope.launch {
            val rule = AlertRule(
                userId = _uiState.value.currentUser?.id ?: 1,
                name = name,
                alertType = alertType,
                topicId = topicId,
                topicName = topicName,
                personName = personName,
                organizationName = organizationName,
                politicalFileId = politicalFileId,
                politicalFileTitle = politicalFileTitle,
                countryCode = countryCode,
                minSeverity = minSeverity,
                enabled = true
            )
            repository.insertAlertRule(rule)
            _uiState.update { it.copy(showAlertRuleDialog = false) }
        }
    }

    fun toggleAlertRule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlertRule(id, enabled)
        }
    }

    fun deleteAlertRule(rule: AlertRule) {
        viewModelScope.launch {
            repository.deleteAlertRule(rule)
        }
    }

    fun setShowAlertRuleDialog(show: Boolean) {
        _uiState.update { it.copy(showAlertRuleDialog = show) }
    }

    fun setShowCreateEventDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateEventDialog = show) }
    }

    fun clearEventClusteringFeedback() {
        _uiState.update { it.copy(eventClusteringFeedback = null) }
    }

    // =============================================================================================
    // Phase 4: AI Intelligence & Summarization Actions
    // =============================================================================================

    fun setAiModel(modelName: String) {
        repository.aiEngine.configuredModel = modelName
        _uiState.update { it.copy(selectedAiModel = modelName) }
    }

    fun clearAiError() {
        _uiState.update { it.copy(aiErrorMessage = null) }
    }

    fun setShowAiIntelligenceDialog(show: Boolean, type: AiAnalysisType? = null) {
        _uiState.update { it.copy(showAiIntelligenceDialog = show, activeAiAnalysisType = type) }
    }

    fun generateArticleSummary(
        articleId: Long,
        forceRegenerate: Boolean = false,
        language: String = "ar"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val summary = repository.summarizeArticle(articleId, userId, forceRegenerate, language)
                _uiState.update { it.copy(currentArticleSummary = summary, isAiLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل توليد التلخيص الذكي") }
            }
        }
    }

    fun generateEventSummary(
        eventId: Long,
        forceRegenerate: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val summary = repository.summarizeEvent(eventId, userId, forceRegenerate)
                _uiState.update {
                    it.copy(
                        currentEventSummary = summary,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.EVENT_SUMMARY
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل توليد موجز الحدث") }
            }
        }
    }

    fun compareNarratives(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val comparison = repository.compareNarratives(eventId, userId)
                _uiState.update {
                    it.copy(
                        currentNarrativeComparison = comparison,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.NARRATIVE_COMPARISON
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل مقارنة الروايات") }
            }
        }
    }

    fun analyzeContradictions(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val analysis = repository.analyzeContradictions(eventId, userId)
                _uiState.update {
                    it.copy(
                        currentContradictionAnalysis = analysis,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.CONTRADICTION_ANALYSIS
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تحليل التناقضات") }
            }
        }
    }

    fun traceOriginalSource(articleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val tracing = repository.traceOriginalSource(articleId, userId)
                _uiState.update {
                    it.copy(
                        currentSourceTracing = tracing,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.SOURCE_TRACING
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تتبع المصدر الأصلي") }
            }
        }
    }

    fun analyzeWhatChanged(hours: Int = 48) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val whatChanged = repository.analyzeWhatChanged(hours, userId)
                _uiState.update {
                    it.copy(
                        currentWhatChanged = whatChanged,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.WHAT_CHANGED
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تحليل التغييرات") }
            }
        }
    }

    fun analyzePersonPosition(personId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val position = repository.analyzePersonPosition(personId, userId)
                _uiState.update {
                    it.copy(
                        currentPersonPosition = position,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.PERSON_POSITION
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تحليل موقف الشخصية") }
            }
        }
    }

    fun analyzeMediaPerspective(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val perspective = repository.analyzeMediaPerspective(eventId, userId)
                _uiState.update {
                    it.copy(
                        currentMediaPerspective = perspective,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.MEDIA_PERSPECTIVE
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تحليل التأطير الإعلامي") }
            }
        }
    }

    fun analyzeEvidenceForClaim(claimId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val evidence = repository.analyzeEvidenceForClaim(claimId, userId)
                _uiState.update {
                    it.copy(
                        currentEvidenceAnalysis = evidence,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.EVIDENCE_ANALYSIS
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تقييم الأدلة") }
            }
        }
    }

    fun analyzeInformationGaps(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val gaps = repository.analyzeInformationGaps(eventId, userId)
                _uiState.update {
                    it.copy(
                        currentInformationGaps = gaps,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.INFORMATION_GAPS
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تحليل فجوات المعلومات") }
            }
        }
    }

    fun analyzePoliticalFile(fileId: Long, days: Int = 30) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val fileAnalysis = repository.analyzePoliticalFile(fileId, days, userId)
                _uiState.update {
                    it.copy(
                        currentFileAnalysis = fileAnalysis,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.FILE_ANALYSIS
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل إعداد تقرير الملف") }
            }
        }
    }

    fun generateTimelineSummary(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val summary = repository.generateTimelineSummary(eventId, userId)
                _uiState.update {
                    it.copy(
                        currentTimelineSummary = summary,
                        isAiLoading = false,
                        showAiIntelligenceDialog = true,
                        activeAiAnalysisType = AiAnalysisType.TIMELINE_SUMMARY
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = e.message ?: "فشل تلخيص الخط الزمني") }
            }
        }
    }

    fun refreshDashboardAiBrief() {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val brief = repository.generateDashboardAiBrief(userId)
                _uiState.update { it.copy(dashboardAiBrief = brief) }
            } catch (e: Exception) {
                // Non-blocking for dashboard
            }
        }
    }

    // =============================================================================================
    // Phase 5: Research Workspace Operations
    // =============================================================================================

    fun addArticleToWorkspace(article: Article) {
        _uiState.update { state ->
            val cur = state.workspaceState.selectedArticles
            if (cur.any { it.id == article.id }) state
            else state.copy(workspaceState = state.workspaceState.copy(selectedArticles = cur + article))
        }
    }

    fun removeArticleFromWorkspace(articleId: Long) {
        _uiState.update { state ->
            state.copy(
                workspaceState = state.workspaceState.copy(
                    selectedArticles = state.workspaceState.selectedArticles.filter { it.id != articleId }
                )
            )
        }
    }

    fun addEventToWorkspace(event: PoliticalEvent) {
        _uiState.update { state ->
            val cur = state.workspaceState.selectedEvents
            if (cur.any { it.id == event.id }) state
            else state.copy(workspaceState = state.workspaceState.copy(selectedEvents = cur + event))
        }
    }

    fun removeEventFromWorkspace(eventId: Long) {
        _uiState.update { state ->
            state.copy(
                workspaceState = state.workspaceState.copy(
                    selectedEvents = state.workspaceState.selectedEvents.filter { it.id != eventId }
                )
            )
        }
    }

    fun setPoliticalFileInWorkspace(file: PoliticalFile?) {
        _uiState.update { state ->
            state.copy(workspaceState = state.workspaceState.copy(selectedPoliticalFile = file))
        }
    }

    fun addPersonToWorkspace(person: Person) {
        _uiState.update { state ->
            val cur = state.workspaceState.selectedPersons
            if (cur.any { it.id == person.id }) state
            else state.copy(workspaceState = state.workspaceState.copy(selectedPersons = cur + person))
        }
    }

    fun removePersonFromWorkspace(personId: Long) {
        _uiState.update { state ->
            state.copy(
                workspaceState = state.workspaceState.copy(
                    selectedPersons = state.workspaceState.selectedPersons.filter { it.id != personId }
                )
            )
        }
    }

    fun addSourceToWorkspace(source: Source) {
        _uiState.update { state ->
            val cur = state.workspaceState.selectedSources
            if (cur.any { it.id == source.id }) state
            else state.copy(workspaceState = state.workspaceState.copy(selectedSources = cur + source))
        }
    }

    fun removeSourceFromWorkspace(sourceId: Long) {
        _uiState.update { state ->
            state.copy(
                workspaceState = state.workspaceState.copy(
                    selectedSources = state.workspaceState.selectedSources.filter { it.id != sourceId }
                )
            )
        }
    }

    fun addTopicToWorkspace(topic: Topic) {
        _uiState.update { state ->
            val cur = state.workspaceState.selectedTopics
            if (cur.any { it.id == topic.id }) state
            else state.copy(workspaceState = state.workspaceState.copy(selectedTopics = cur + topic))
        }
    }

    fun removeTopicFromWorkspace(topicId: Long) {
        _uiState.update { state ->
            state.copy(
                workspaceState = state.workspaceState.copy(
                    selectedTopics = state.workspaceState.selectedTopics.filter { it.id != topicId }
                )
            )
        }
    }

    fun clearWorkspace() {
        _uiState.update { state ->
            state.copy(
                workspaceState = ResearchWorkspaceState(),
                currentRagAnswer = null,
                ragErrorMessage = null
            )
        }
    }

    fun setWorkspacePicker(open: Boolean, target: String? = null) {
        _uiState.update { it.copy(isWorkspacePickerOpen = open, workspacePickerTarget = target) }
    }

    // =============================================================================================
    // Phase 5: RAG Execution & Grounded Queries
    // =============================================================================================

    fun setRagQuestion(question: String) {
        _uiState.update { it.copy(currentRagQuestion = question) }
    }

    fun setRagLanguage(lang: String) {
        _uiState.update { it.copy(ragLanguage = lang) }
    }

    fun clearRagAnswer() {
        _uiState.update { it.copy(currentRagAnswer = null, ragErrorMessage = null) }
    }

    fun executeRagQuery(
        overrideQuestion: String? = null,
        overrideLanguage: String? = null
    ) {
        val q = overrideQuestion ?: _uiState.value.currentRagQuestion
        if (q.isBlank()) return
        val lang = overrideLanguage ?: _uiState.value.ragLanguage
        val userId = _uiState.value.currentUser?.id ?: 1

        viewModelScope.launch {
            _uiState.update { it.copy(isRagLoading = true, ragErrorMessage = null, currentRagQuestion = q) }
            try {
                val answer = repository.executeRagQuery(
                    question = q,
                    workspaceState = _uiState.value.workspaceState,
                    language = lang,
                    userId = userId
                )
                _uiState.update { it.copy(currentRagAnswer = answer, isRagLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRagLoading = false,
                        ragErrorMessage = e.message ?: "فشل تنفيذ الاستعلام التأصيلي RAG"
                    )
                }
            }
        }
    }

    // =============================================================================================
    // Phase 5: Saved Research Sessions
    // =============================================================================================

    fun setShowSaveResearchDialog(show: Boolean) {
        _uiState.update { it.copy(showSaveResearchDialog = show) }
    }

    fun saveCurrentResearch(title: String, mainQuestion: String) {
        viewModelScope.launch {
            val s = _uiState.value
            val userId = s.currentUser?.id ?: 1
            val research = SavedResearch(
                userId = userId,
                title = title.ifBlank { "جلسة بحث: ${mainQuestion.take(30)}" },
                mainQuestion = mainQuestion,
                articleIdsCsv = s.workspaceState.selectedArticles.joinToString(",") { it.id.toString() },
                eventIdsCsv = s.workspaceState.selectedEvents.joinToString(",") { it.id.toString() },
                politicalFileId = s.workspaceState.selectedPoliticalFile?.id,
                personIdsCsv = s.workspaceState.selectedPersons.joinToString(",") { it.id.toString() },
                sourceIdsCsv = s.workspaceState.selectedSources.joinToString(",") { it.id.toString() },
                topicIdsCsv = s.workspaceState.selectedTopics.joinToString(",") { it.id.toString() },
                resultingSummary = s.currentRagAnswer?.answer,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertSavedResearch(research)
            _uiState.update { it.copy(showSaveResearchDialog = false) }
        }
    }

    fun loadSavedResearch(research: SavedResearch) {
        viewModelScope.launch {
            val allArts = _uiState.value.articles
            val allEvs = _uiState.value.events
            val allFiles = _uiState.value.files
            val allPers = _uiState.value.persons
            val allSrcs = _uiState.value.sources
            val allTpcs = _uiState.value.topics

            val artIds = research.articleIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            val evIds = research.eventIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            val persIds = research.personIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            val srcIds = research.sourceIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            val tpcIds = research.topicIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }

            val loadedArticles = allArts.filter { artIds.contains(it.id) }
            val loadedEvents = allEvs.filter { evIds.contains(it.id) }
            val loadedFile = allFiles.firstOrNull { it.id == research.politicalFileId }
            val loadedPersons = allPers.filter { persIds.contains(it.id) }
            val loadedSources = allSrcs.filter { srcIds.contains(it.id) }
            val loadedTopics = allTpcs.filter { tpcIds.contains(it.id) }

            _uiState.update { state ->
                state.copy(
                    workspaceState = ResearchWorkspaceState(
                        selectedArticles = loadedArticles,
                        selectedEvents = loadedEvents,
                        selectedPoliticalFile = loadedFile,
                        selectedPersons = loadedPersons,
                        selectedSources = loadedSources,
                        selectedTopics = loadedTopics
                    ),
                    currentRagQuestion = research.mainQuestion,
                    activeSavedResearch = research,
                    activeTab = DeskTab.RESEARCH_WORKSPACE
                )
            }
        }
    }

    fun deleteSavedResearch(research: SavedResearch) {
        viewModelScope.launch {
            repository.deleteSavedResearch(research)
            if (_uiState.value.activeSavedResearch?.id == research.id) {
                _uiState.update { it.copy(activeSavedResearch = null) }
            }
        }
    }

    // =============================================================================================
    // Phase 5: AI Conversations & Chat Assistant
    // =============================================================================================

    private var messagesJob: Job? = null

    fun selectConversation(conversation: AiConversation) {
        _uiState.update { it.copy(activeConversation = conversation) }
        observeMessagesForConversation(conversation.id)
    }

    private fun observeMessagesForConversation(conversationId: Long) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForConversation(conversationId).collect { msgs ->
                _uiState.update { it.copy(activeConversationMessages = msgs) }
            }
        }
    }

    fun createAndSelectConversation(title: String) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            val convId = repository.createConversation(title.ifBlank { "محادثة استخبارية جديدة" }, userId)
            val newConv = AiConversation(
                id = convId,
                userId = userId,
                title = title.ifBlank { "محادثة استخبارية جديدة" }
            )
            selectConversation(newConv)
        }
    }

    fun deleteConversation(conversation: AiConversation) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.deleteConversation(conversation.id, userId)
            if (_uiState.value.activeConversation?.id == conversation.id) {
                _uiState.update {
                    it.copy(
                        activeConversation = null,
                        activeConversationMessages = emptyList()
                    )
                }
            }
        }
    }

    fun setAssistantMessageInput(text: String) {
        _uiState.update { it.copy(currentAssistantMessageInput = text) }
    }

    fun sendAssistantMessage(text: String) {
        val userQuestion = text.trim()
        if (userQuestion.isBlank()) return

        viewModelScope.launch {
            var activeConv = _uiState.value.activeConversation
            val userId = _uiState.value.currentUser?.id ?: 1
            if (activeConv == null) {
                val convId = repository.createConversation(userQuestion.take(35), userId)
                activeConv = AiConversation(id = convId, userId = userId, title = userQuestion.take(35))
                selectConversation(activeConv)
            }

            // Insert User Message
            repository.insertAiMessage(
                AiMessage(
                    conversationId = activeConv.id,
                    sender = "USER",
                    content = userQuestion,
                    timestamp = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(currentAssistantMessageInput = "", isAssistantResponding = true) }

            try {
                // Execute Grounded RAG Query
                val structuredAnswer = repository.executeRagQuery(
                    question = userQuestion,
                    workspaceState = _uiState.value.workspaceState,
                    language = _uiState.value.ragLanguage,
                    userId = userId
                )

                // Insert Assistant Message
                repository.insertAiMessage(
                    AiMessage(
                        conversationId = activeConv.id,
                        sender = "ASSISTANT",
                        content = structuredAnswer.answer,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _uiState.update { it.copy(isAssistantResponding = false) }
            } catch (e: Exception) {
                repository.insertAiMessage(
                    AiMessage(
                        conversationId = activeConv.id,
                        sender = "ASSISTANT",
                        content = "عذراً، حدث خطأ أثناء المعالجة: ${e.message}",
                        timestamp = System.currentTimeMillis()
                    )
                )
                _uiState.update { it.copy(isAssistantResponding = false) }
            }
        }
    }

    // =============================================================================================
    // Phase 5: Executive Reports & History
    // =============================================================================================

    fun selectReportDetail(report: GeneratedReport?) {
        _uiState.update { it.copy(selectedReportDetail = report) }
    }

    fun setReportTypeFilter(type: ReportType?) {
        _uiState.update { it.copy(selectedReportTypeFilter = type) }
    }

    fun setShowReportGenerationDialog(show: Boolean) {
        _uiState.update { it.copy(showReportGenerationDialog = show) }
    }

    fun deleteReport(report: GeneratedReport) {
        viewModelScope.launch {
            repository.deleteReport(report)
            if (_uiState.value.selectedReportDetail?.id == report.id) {
                _uiState.update { it.copy(selectedReportDetail = null) }
            }
        }
    }

    fun generateDailyBriefReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isReportGenerating = true, reportGenerationError = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val report = repository.generateDailyBriefReport(userId)
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        selectedReportDetail = report,
                        showReportGenerationDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        reportGenerationError = e.message ?: "فشل توليد التقرير اليومي"
                    )
                }
            }
        }
    }

    fun generateEventComprehensiveReport(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReportGenerating = true, reportGenerationError = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val report = repository.generateEventReport(eventId, userId)
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        selectedReportDetail = report,
                        showReportGenerationDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        reportGenerationError = e.message ?: "فشل توليد تقرير الحدث"
                    )
                }
            }
        }
    }

    fun generatePoliticalFileStrategicReport(fileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReportGenerating = true, reportGenerationError = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val report = repository.generatePoliticalFileReport(fileId, userId)
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        selectedReportDetail = report,
                        showReportGenerationDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        reportGenerationError = e.message ?: "فشل توليد تقرير الملف الاستراتيجي"
                    )
                }
            }
        }
    }

    fun generateMonitoringReport(targetType: String, targetId: Long, targetName: String, days: Int = 30) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReportGenerating = true, reportGenerationError = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val report = repository.generateMonitoringReport(targetType, targetId, targetName, days, userId)
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        selectedReportDetail = report,
                        showReportGenerationDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReportGenerating = false,
                        reportGenerationError = e.message ?: "فشل توليد تقرير المراقبة والرصد"
                    )
                }
            }
        }
    }

    fun deserializeReportData(contentJson: String): com.example.data.model.ExecutiveReportData? =
        repository.reportsEngine.deserializeReportData(contentJson)

    fun deserializeCitations(citationsJson: String): List<com.example.data.model.AiCitation> =
        repository.reportsEngine.deserializeCitations(citationsJson)

    // ==========================================
    // Phase 6 Methods: Advanced Political Intelligence
    // ==========================================

    fun setIntelligenceSubTab(index: Int) {
        _uiState.update { it.copy(intelligenceActiveSubTab = index) }
    }

    fun refreshGraph() {
        viewModelScope.launch {
            val s = _uiState.value
            val (nodes, edges) = repository.politicalGraphEngine.buildGraph(
                selectedEntityType = s.graphSelectedEntityType,
                selectedRelationshipType = s.graphSelectedRelType,
                minConfidence = s.graphMinConfidence,
                onlyDirectEvidence = s.graphOnlyDirect,
                searchQuery = s.graphSearchQuery
            )
            _uiState.update { it.copy(graphNodes = nodes, graphEdges = edges) }
        }
    }

    fun updateGraphFilters(
        entityType: EntityType? = null,
        relType: RelationshipType? = null,
        minConfidence: Float = 0.5f,
        onlyDirect: Boolean = false,
        searchQuery: String = ""
    ) {
        _uiState.update {
            it.copy(
                graphSelectedEntityType = entityType,
                graphSelectedRelType = relType,
                graphMinConfidence = minConfidence,
                graphOnlyDirect = onlyDirect,
                graphSearchQuery = searchQuery
            )
        }
        refreshGraph()
    }

    fun selectGraphNode(node: GraphNode?) {
        _uiState.update { it.copy(selectedGraphNode = node, selectedGraphEdge = null) }
    }

    fun selectGraphEdge(edge: GraphEdge?) {
        _uiState.update { it.copy(selectedGraphEdge = edge, selectedGraphNode = null) }
    }

    fun confirmRelationshipDiscovery(id: Long, notes: String? = null) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.confirmRelationshipDiscovery(id, notes, userId)
        }
    }

    fun rejectRelationshipDiscovery(id: Long, reason: String? = null) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.rejectRelationshipDiscovery(id, reason, userId)
        }
    }

    fun runAiRelationshipDiscovery() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.runAiRelationshipDiscovery(userId)
        }
    }

    fun acknowledgeEarlySignal(id: Long) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.acknowledgeSignal(id, userId)
        }
    }

    fun runEarlySignalsScan() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.runEarlySignalsScan(userId)
        }
    }

    fun runChangeDetectionScan() {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.runChangeDetectionScan(userId)
        }
    }

    fun computeTrends(timeframe: TrendTimeframe) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTrendTimeframe = timeframe, isTrendLoading = true) }
            try {
                val report = repository.trendAnalysisEngine.analyzeTrends(timeframe)
                _uiState.update { it.copy(activeTrendReport = report, isTrendLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isTrendLoading = false) }
            }
        }
    }

    fun toggleCrossFileSelection(fileId: Long) {
        val current = _uiState.value.selectedCrossFileIds.toMutableSet()
        if (current.contains(fileId)) current.remove(fileId) else current.add(fileId)
        _uiState.update { it.copy(selectedCrossFileIds = current) }
    }

    fun runCrossFileAnalysis() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedCrossFileIds.toList()
            if (selected.size >= 2) {
                _uiState.update { it.copy(isCrossFileLoading = true) }
                try {
                    val result = repository.crossAnalysisEngine.analyzeCrossFiles(selected)
                    _uiState.update { it.copy(activeCrossFileAnalysis = result, isCrossFileLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isCrossFileLoading = false) }
                }
            }
        }
    }

    fun toggleCrossEventSelection(eventId: Long) {
        val current = _uiState.value.selectedCrossEventIds.toMutableSet()
        if (current.contains(eventId)) current.remove(eventId) else current.add(eventId)
        _uiState.update { it.copy(selectedCrossEventIds = current) }
    }

    fun runCrossEventAnalysis() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedCrossEventIds.toList()
            if (selected.size >= 2) {
                _uiState.update { it.copy(isCrossEventLoading = true) }
                try {
                    val result = repository.crossAnalysisEngine.analyzeCrossEvents(selected)
                    _uiState.update { it.copy(activeCrossEventAnalysis = result, isCrossEventLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isCrossEventLoading = false) }
                }
            }
        }
    }

    fun requestExplainabilityForRelationship(relationship: PoliticalRelationship) {
        viewModelScope.launch {
            val report = repository.explainabilityEngine.explainRelationship(relationship)
            _uiState.update { it.copy(explainabilityModalReport = report) }
        }
    }

    fun requestExplainabilityForSignal(signal: EarlySignalItem) {
        viewModelScope.launch {
            val report = repository.explainabilityEngine.explainEarlySignal(signal)
            _uiState.update { it.copy(explainabilityModalReport = report) }
        }
    }

    fun requestExplainabilityForChange(change: DetectedChangeItem) {
        viewModelScope.launch {
            val report = repository.explainabilityEngine.explainDetectedChange(change)
            _uiState.update { it.copy(explainabilityModalReport = report) }
        }
    }

    fun dismissExplainabilityModal() {
        _uiState.update { it.copy(explainabilityModalReport = null) }
    }

    fun setShowCreateSmartRuleDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateSmartRuleDialog = show) }
    }

    fun insertSmartRule(
        nameAr: String,
        targetType: EntityType,
        targetName: String,
        keywords: String,
        sources: String,
        minImportance: FilePriority
    ) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            val rule = SmartMonitorRule(
                nameAr = nameAr,
                targetType = targetType,
                targetName = targetName,
                keywordsCommaSeparated = keywords,
                sourcesCommaSeparated = sources,
                minImportance = minImportance
            )
            repository.insertSmartRule(rule, userId)
            _uiState.update { it.copy(showCreateSmartRuleDialog = false) }
        }
    }

    fun toggleSmartRule(rule: SmartMonitorRule) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.toggleSmartRule(rule, userId)
        }
    }

    fun deleteSmartRule(rule: SmartMonitorRule) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.deleteSmartRule(rule, userId)
        }
    }

    fun setShowCreateInfoGapDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateInfoGapDialog = show) }
    }

    fun insertInformationGap(
        titleAr: String,
        descriptionAr: String,
        status: InformationGapStatus,
        entityType: EntityType? = null,
        entityId: Long? = null,
        criticality: FilePriority = FilePriority.HIGH,
        missingData: String = ""
    ) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            val gap = InformationGapItem(
                titleAr = titleAr,
                descriptionAr = descriptionAr,
                status = status,
                linkedEntityType = entityType,
                linkedEntityId = entityId,
                criticality = criticality,
                missingDataNeeded = missingData
            )
            repository.insertInformationGap(gap, userId)
            _uiState.update { it.copy(showCreateInfoGapDialog = false) }
        }
    }

    suspend fun getRelationshipsForEntity(entityType: EntityType, entityId: Long): List<PoliticalRelationship> =
        repository.getRelationshipsForEntityList(entityType, entityId)

    suspend fun getChangesForEntity(entityType: EntityType, entityId: Long): List<DetectedChangeItem> =
        repository.getChangesForEntityList(entityType, entityId)

    suspend fun getGapsForEntity(entityType: EntityType, entityId: Long): List<InformationGapItem> =
        repository.getGapsForEntityList(entityType, entityId)

    // =========================================================================
    // Interview Intelligence Methods
    // =========================================================================

    fun selectInterview(interview: Interview?) {
        _uiState.update {
            it.copy(
                selectedInterviewDetail = interview,
                activePreInterviewBrief = null,
                activeOnePageBriefText = null,
                activePostInterviewReport = null,
                inContextChatMessages = emptyList(),
                currentInContextChatInput = ""
            )
        }
        if (interview != null) {
            loadInterviewDetails(interview.id)
        }
    }

    private fun loadInterviewDetails(interviewId: Long) {
        viewModelScope.launch {
            repository.getLinksForInterview(interviewId).collect { links ->
                _uiState.update { it.copy(selectedInterviewLinks = links) }
            }
        }
        viewModelScope.launch {
            repository.getTranscriptForInterview(interviewId).collect { tr ->
                _uiState.update { it.copy(selectedInterviewTranscript = tr) }
            }
        }
        viewModelScope.launch {
            repository.getAnalysesForInterview(interviewId).collect { analyses ->
                _uiState.update { it.copy(selectedInterviewAnalyses = analyses) }
            }
        }
    }

    fun setInterviewCalendarViewMode(mode: CalendarViewMode) {
        _uiState.update { it.copy(interviewCalendarViewMode = mode) }
    }

    fun setInterviewSearchQuery(query: String) {
        _uiState.update { it.copy(interviewSearchQuery = query) }
    }

    fun setSelectedInterviewStatusFilter(status: InterviewStatus?) {
        _uiState.update { it.copy(selectedInterviewStatusFilter = status) }
    }

    fun setSelectedInterviewTypeFilter(type: InterviewType?) {
        _uiState.update { it.copy(selectedInterviewTypeFilter = type) }
    }

    fun setSelectedInterviewChannelFilter(channel: String?) {
        _uiState.update { it.copy(selectedInterviewChannelFilter = channel) }
    }

    fun setSelectedCalendarDateUtc(timeUtc: Long) {
        _uiState.update { it.copy(selectedCalendarDateUtc = timeUtc) }
    }

    fun setShowCreateInterviewDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateInterviewDialog = show) }
    }

    fun setShowAddInterviewLinkDialog(show: Boolean) {
        _uiState.update { it.copy(showAddInterviewLinkDialog = show) }
    }

    fun setShowAddTranscriptDialog(show: Boolean) {
        _uiState.update { it.copy(showAddTranscriptDialog = show) }
    }

    fun setShowOnePageBriefDialog(show: Boolean) {
        _uiState.update { it.copy(showOnePageBriefDialog = show) }
    }

    fun setShowCompareInterviewsDialog(show: Boolean) {
        _uiState.update { it.copy(showCompareInterviewsDialog = show) }
    }

    fun toggleInterviewSelectionForComparison(id: Long) {
        _uiState.update { state ->
            val set = state.selectedInterviewIdsForComparison.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            state.copy(selectedInterviewIdsForComparison = set)
        }
    }

    fun createInterview(
        channel: String,
        program: String,
        interviewer: String,
        dateUtc: Long,
        startTime: String,
        endTime: String,
        interviewType: InterviewType,
        location: String,
        subject: String,
        description: String,
        notes: String,
        status: InterviewStatus = InterviewStatus.CONFIRMED,
        isPrivate: Boolean = false,
        linkedFileIds: String = "",
        linkedEventIds: String = "",
        linkedTopicIds: String = "",
        linkedPersonIds: String = "",
        linkedCountryCodes: String = "",
        durationMinutes: Int = 45
    ) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            val interview = Interview(
                userId = userId,
                dateUtc = dateUtc,
                startTime = startTime,
                endTime = endTime,
                channel = channel,
                program = program,
                interviewer = interviewer,
                interviewType = interviewType,
                location = location,
                subject = subject,
                description = description,
                notes = notes,
                status = status,
                isPrivate = isPrivate,
                linkedFileIds = linkedFileIds,
                linkedEventIds = linkedEventIds,
                linkedTopicIds = linkedTopicIds,
                linkedPersonIds = linkedPersonIds,
                linkedCountryCodes = linkedCountryCodes,
                durationMinutes = durationMinutes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.insertInterview(interview, userId)
            _uiState.update { it.copy(showCreateInterviewDialog = false) }
            val inserted = repository.getInterviewById(newId)
            selectInterview(inserted)
        }
    }

    fun updateInterview(interview: Interview) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.updateInterview(interview, userId)
            _uiState.update { it.copy(selectedInterviewDetail = interview) }
        }
    }

    fun deleteInterview(interview: Interview) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUser?.id ?: 1
            repository.deleteInterview(interview, userId)
            _uiState.update {
                it.copy(
                    selectedInterviewDetail = null,
                    activePreInterviewBrief = null,
                    activeOnePageBriefText = null,
                    activePostInterviewReport = null
                )
            }
        }
    }

    fun addInterviewLink(
        interviewId: Long,
        url: String,
        title: String,
        source: String,
        type: InterviewLinkType,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val link = InterviewLink(
                interviewId = interviewId,
                url = url,
                title = title,
                source = source,
                type = type,
                notes = notes,
                dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            )
            repository.insertInterviewLink(link)
            _uiState.update { it.copy(showAddInterviewLinkDialog = false) }
        }
    }

    fun deleteInterviewLink(link: InterviewLink) {
        viewModelScope.launch {
            repository.deleteInterviewLink(link)
        }
    }

    fun saveInterviewTranscript(
        interviewId: Long,
        text: String,
        audioUrl: String? = null,
        videoUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterviewAiLoading = true) }
            val transcript = repository.processAndSaveTranscript(interviewId, text, audioUrl, videoUrl)
            _uiState.update {
                it.copy(
                    selectedInterviewTranscript = transcript,
                    showAddTranscriptDialog = false,
                    isInterviewAiLoading = false
                )
            }
        }
    }

    fun generatePreInterviewBrief(interviewId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterviewAiLoading = true, interviewAiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val brief = repository.generatePreInterviewBrief(interviewId, userId)
                val onePage = repository.generateOnePageBriefText(brief)
                _uiState.update {
                    it.copy(
                        activePreInterviewBrief = brief,
                        activeOnePageBriefText = onePage,
                        isInterviewAiLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInterviewAiLoading = false,
                        interviewAiErrorMessage = "تعذر إعداد الإيجاز الاستخباري: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun generateOnePageBrief(interviewId: Long) {
        val currentBrief = _uiState.value.activePreInterviewBrief
        if (currentBrief != null && currentBrief.interviewId == interviewId) {
            val text = repository.generateOnePageBriefText(currentBrief)
            _uiState.update { it.copy(activeOnePageBriefText = text, showOnePageBriefDialog = true) }
        } else {
            viewModelScope.launch {
                generatePreInterviewBrief(interviewId)
                _uiState.update { it.copy(showOnePageBriefDialog = true) }
            }
        }
    }

    fun analyzePostInterview(interviewId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterviewAiLoading = true, interviewAiErrorMessage = null) }
            try {
                val userId = _uiState.value.currentUser?.id ?: 1
                val report = repository.analyzePostInterview(interviewId, userId)
                _uiState.update {
                    it.copy(
                        activePostInterviewReport = report,
                        isInterviewAiLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInterviewAiLoading = false,
                        interviewAiErrorMessage = "تعذر إنجاز تحليل الأداء: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun updateInContextChatInput(text: String) {
        _uiState.update { it.copy(currentInContextChatInput = text) }
    }

    fun sendInContextChatMessage(interviewId: Long, question: String) {
        if (question.isBlank()) return
        val currentList = _uiState.value.inContextChatMessages.toMutableList()
        currentList.add(question to "...")
        _uiState.update {
            it.copy(
                inContextChatMessages = currentList,
                isInContextChatResponding = true,
                currentInContextChatInput = ""
            )
        }
        viewModelScope.launch {
            val response = repository.answerInContextQuestion(interviewId, question)
            _uiState.update { state ->
                val updated = state.inContextChatMessages.toMutableList()
                if (updated.isNotEmpty()) {
                    updated[updated.size - 1] = question to response
                }
                state.copy(
                    inContextChatMessages = updated,
                    isInContextChatResponding = false
                )
            }
        }
    }

    fun clearInContextChat() {
        _uiState.update { it.copy(inContextChatMessages = emptyList(), currentInContextChatInput = "") }
    }
}
