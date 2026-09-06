package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Standard Political Relationship Types as mandated by Phase 6 Specification.
 */
enum class RelationshipType(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val isCooperative: Boolean
) {
    ALLIED_WITH("ALLIED_WITH", "حليف مع", "Allied With", true),
    OPPOSES("OPPOSES", "يعارض", "Opposes", false),
    NEGOTIATES_WITH("NEGOTIATES_WITH", "يتفاوض مع", "Negotiates With", true),
    MEETS_WITH("MEETS_WITH", "يجتمع مع", "Meets With", true),
    MEMBER_OF("MEMBER_OF", "عضو في", "Member Of", true),
    REPRESENTS("REPRESENTS", "يمثل", "Represents", true),
    RELATED_TO("RELATED_TO", "مرتبط بـ", "Related To", true),
    INVOLVED_IN("INVOLVED_IN", "منخرط في", "Involved In", true),
    MENTIONED_WITH("MENTIONED_WITH", "ورد ذكره مع", "Mentioned With", true),
    SUPPORTS("SUPPORTS", "يدعم", "Supports", true),
    CRITICIZES("CRITICIZES", "ينتقد", "Criticizes", false),
    AGREES_WITH("AGREES_WITH", "يتفق مع", "Agrees With", true),
    DISAGREES_WITH("DISAGREES_WITH", "يختلف مع", "Disagrees With", false),
    CONNECTED_TO("CONNECTED_TO", "متصل بـ", "Connected To", true);

    fun displayNameAr(): String = nameAr
    fun displayNameEn(): String = nameEn
}

enum class EntityType(val nameAr: String, val nameEn: String) {
    PERSON("شخصية", "Person"),
    ORGANIZATION("منظمة / مؤسسة", "Organization"),
    COUNTRY("دولة", "Country"),
    POLITICAL_FILE("ملف سياسي", "Political File"),
    EVENT("حدث سياسي", "Political Event"),
    ARTICLE("مقال / برقية", "Article"),
    TOPIC("موضوع", "Topic"),
    STATEMENT("تصريح", "Statement");

    fun displayNameAr(): String = nameAr
    fun displayNameEn(): String = nameEn
}

enum class DiscoveryStatus(val nameAr: String) {
    DISCOVERED("مكتشفة بالذكاء الاصطناعي"),
    CONFIRMED("معتمدة من المحلل"),
    REJECTED("مرفوضة"),
    UNDER_REVIEW("قيد التدقيق والتحقق")
}

enum class EvidenceNature(val nameAr: String, val nameEn: String) {
    DIRECT_EVIDENCE("دليل مباشر ومصدر موثق", "Direct Evidence"),
    AI_INFERENCE("استنتاج تحليلي خوارزمي", "AI Inference")
}

@Entity(
    tableName = "political_relationships",
    indices = [
        Index("sourceEntityType", "sourceEntityId"),
        Index("targetEntityType", "targetEntityId"),
        Index("relationshipType"),
        Index("discoveryStatus")
    ]
)
data class PoliticalRelationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceEntityType: EntityType,
    val sourceEntityId: Long,
    val sourceEntityName: String,
    val targetEntityType: EntityType,
    val targetEntityId: Long,
    val targetEntityName: String,
    val relationshipType: RelationshipType,
    val confidence: Float = 0.85f,
    val evidenceNature: EvidenceNature = EvidenceNature.DIRECT_EVIDENCE,
    val sourceArticleId: Long? = null,
    val sourceEventId: Long? = null,
    val sourceFileId: Long? = null,
    val sourceName: String? = null,
    val evidenceSnippet: String = "",
    val discoveryMethod: String = "DOCUMENTED_SOURCE",
    val discoveryStatus: DiscoveryStatus = DiscoveryStatus.CONFIRMED,
    val discoveredAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = System.currentTimeMillis(),
    val analystNotes: String? = null,
    val validFromUtc: Long = System.currentTimeMillis(),
    val validToUtc: Long? = null
)

/**
 * Information Gaps Entity mandated by Phase 6
 */
@Entity(
    tableName = "information_gaps",
    indices = [Index("status"), Index("linkedEntityType", "linkedEntityId")]
)
data class InformationGapItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleAr: String,
    val descriptionAr: String,
    val status: InformationGapStatus,
    val linkedEntityType: EntityType? = null,
    val linkedEntityId: Long? = null,
    val criticality: FilePriority = FilePriority.HIGH,
    val missingDataNeeded: String = "",
    val identifiedAt: Long = System.currentTimeMillis()
)

/**
 * Early Signals with strict probabilistic safeguards (non-deterministic phrasing)
 */
@Entity(
    tableName = "early_signals",
    indices = [Index("detectedAt"), Index("associatedFileId"), Index("isAcknowledged")]
)
data class EarlySignalItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val signalTitleAr: String,
    val signalDescriptionAr: String,
    val signalCategory: String, // e.g. "ACTOR_CO_OCCURRENCE", "COVERAGE_SPIKE", "NARRATIVE_SHIFT"
    val historicalPattern: String,
    val currentEvidence: String,
    val alternativeExplanations: String,
    val confidence: Float = 0.70f,
    val unknowns: String = "",
    val suggestedMonitoring: String = "",
    val detectedAt: Long = System.currentTimeMillis(),
    val associatedFileId: Long? = null,
    val associatedEventId: Long? = null,
    val associatedPersonId: Long? = null,
    val isAcknowledged: Boolean = false
)

/**
 * Change Detection tracking ("What Changed?", When, Compared With What, Evidence, Confidence)
 */
@Entity(
    tableName = "detected_changes",
    indices = [Index("whenTimestamp"), Index("entityType", "entityId")]
)
data class DetectedChangeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: EntityType,
    val entityId: Long,
    val entityName: String,
    val changeType: String, // "STANCE_SHIFT", "NEW_ACTOR", "ALLIANCE_SHIFT", "ESCALATION", "NARRATIVE_CHANGE"
    val whatChangedAr: String,
    val whenTimestamp: Long = System.currentTimeMillis(),
    val comparedWithWhatAr: String,
    val evidenceSnippet: String,
    val sourceName: String,
    val confidence: Float = 0.85f,
    val magnitude: Float = 0.75f // 0.0 to 1.0 scale
)

/**
 * Advanced Smart Monitoring Rule
 */
@Entity(
    tableName = "smart_monitor_rules",
    indices = [Index("targetType"), Index("isActive")]
)
data class SmartMonitorRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameAr: String,
    val targetType: EntityType,
    val targetId: Long? = null,
    val targetName: String,
    val keywordsCommaSeparated: String = "",
    val sourcesCommaSeparated: String = "",
    val minImportance: FilePriority = FilePriority.MEDIUM,
    val timeframeDays: Int = 30,
    val isActive: Boolean = true,
    val lastTriggeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Smart Alert Categories for Phase 6
 */
enum class SmartAlertCategory(val nameAr: String, val nameEn: String) {
    SIGNIFICANT_CHANGE("تغير مهم", "Significant Change"),
    NEW_ACTOR("ظهور طرف جديد", "New Actor"),
    ESCALATION("تصاعد النشاط", "Escalation"),
    CONTRADICTION("تعارض في الروايات", "Contradiction"),
    NEW_RELATIONSHIP("علاقة جديدة", "New Relationship"),
    NARRATIVE_SHIFT("تحول في الخطاب", "Narrative Shift"),
    UNUSUAL_ACTIVITY("نشاط غير معتاد", "Unusual Activity");

    fun displayNameAr(): String = nameAr
}

/**
 * Trend Analysis Timeframe Intervals
 */
enum class TrendTimeframe(val nameAr: String, val nameEn: String, val durationMillis: Long) {
    HOURS_24("24 ساعة", "24 Hours", 24L * 60 * 60 * 1000),
    DAYS_7("7 أيام", "7 Days", 7L * 24 * 60 * 60 * 1000),
    DAYS_30("30 يوماً", "30 Days", 30L * 24 * 60 * 60 * 1000),
    DAYS_90("90 يوماً", "90 Days", 90L * 24 * 60 * 60 * 1000),
    MONTHS_6("6 أشهر", "6 Months", 180L * 24 * 60 * 60 * 1000),
    YEAR_1("سنة كاملة", "1 Year", 365L * 24 * 60 * 60 * 1000),
    CUSTOM("فترة مخصصة", "Custom Range", 0L);

    fun displayNameAr(): String = nameAr
}

/**
 * Data holder for Trend Metrics
 */
data class EntityTrendSummary(
    val entityName: String,
    val entityType: EntityType,
    val mentionCount: Int,
    val previousPeriodCount: Int,
    val percentageChange: Float,
    val isRising: Boolean,
    val recentCoverageContext: String
)

/**
 * Cross-File Analysis Output
 */
data class CrossFileAnalysis(
    val selectedFiles: List<PoliticalFile>,
    val sharedPersons: List<Person>,
    val sharedOrganizations: List<Organization>,
    val sharedCountries: List<Country>,
    val sharedEvents: List<PoliticalEvent>,
    val sharedTopics: List<Topic>,
    val crossFileStatements: List<Statement>,
    val crossFileContradictions: List<ContradictionItem>,
    val synthesisTextAr: String,
    val confidence: Float,
    val evidenceReferences: List<String>
)

/**
 * Cross-Event Analysis Output
 */
data class CrossEventAnalysis(
    val selectedEvents: List<PoliticalEvent>,
    val commonActors: List<Person>,
    val commonOrganizations: List<Organization>,
    val commonLocations: List<String>,
    val comparativeTimeline: List<TimelineItem>,
    val keyDivergences: List<String>,
    val potentialRamifications: List<String>,
    val unverifiedPoints: List<String>,
    val synthesisAr: String,
    val confidence: Float
)

/**
 * Graph Node & Edge Models for the Interactive Compose Relationship Canvas
 */
data class GraphNode(
    val id: String, // e.g. "PERSON_1", "FILE_2"
    val entityType: EntityType,
    val entityId: Long,
    val label: String,
    val roleOrCategory: String = "",
    val importanceScore: Float = 1.0f,
    val x: Float = 0f,
    val y: Float = 0f
)

data class GraphEdge(
    val id: Long,
    val sourceNodeId: String,
    val targetNodeId: String,
    val relationshipType: RelationshipType,
    val label: String,
    val isDirect: Boolean,
    val confidence: Float,
    val relationship: PoliticalRelationship
)
