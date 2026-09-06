package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole =
        value?.let { runCatching { UserRole.valueOf(it) }.getOrDefault(UserRole.ANALYST) } ?: UserRole.ANALYST

    @TypeConverter
    fun fromSourceTier(tier: SourceTier?): String? = tier?.name

    @TypeConverter
    fun toSourceTier(value: String?): SourceTier =
        value?.let { runCatching { SourceTier.valueOf(it) }.getOrDefault(SourceTier.TRUSTED_MEDIA) } ?: SourceTier.TRUSTED_MEDIA

    @TypeConverter
    fun fromFetchMethod(method: FetchMethod?): String? = method?.name

    @TypeConverter
    fun toFetchMethod(value: String?): FetchMethod =
        value?.let { runCatching { FetchMethod.valueOf(it) }.getOrDefault(FetchMethod.RSS) } ?: FetchMethod.RSS

    @TypeConverter
    fun fromSourceStatus(status: SourceStatus?): String? = status?.name

    @TypeConverter
    fun toSourceStatus(value: String?): SourceStatus =
        value?.let { runCatching { SourceStatus.valueOf(it) }.getOrDefault(SourceStatus.ACTIVE) } ?: SourceStatus.ACTIVE

    @TypeConverter
    fun fromConnectionState(state: ConnectionState?): String? = state?.name

    @TypeConverter
    fun toConnectionState(value: String?): ConnectionState =
        value?.let { runCatching { ConnectionState.valueOf(it) }.getOrDefault(ConnectionState.NOT_TESTED) } ?: ConnectionState.NOT_TESTED

    @TypeConverter
    fun fromOrgType(type: OrganizationType?): String? = type?.name

    @TypeConverter
    fun toOrgType(value: String?): OrganizationType =
        value?.let { runCatching { OrganizationType.valueOf(it) }.getOrDefault(OrganizationType.GOVERNMENT) } ?: OrganizationType.GOVERNMENT

    @TypeConverter
    fun fromStanceShiftType(shift: StanceShiftType?): String? = shift?.name

    @TypeConverter
    fun toStanceShiftType(value: String?): StanceShiftType =
        value?.let { runCatching { StanceShiftType.valueOf(it) }.getOrDefault(StanceShiftType.SUBTLE_SHIFT) } ?: StanceShiftType.SUBTLE_SHIFT

    @TypeConverter
    fun fromTopicCategory(cat: TopicCategory?): String? = cat?.name

    @TypeConverter
    fun toTopicCategory(value: String?): TopicCategory =
        value?.let { runCatching { TopicCategory.valueOf(it) }.getOrDefault(TopicCategory.DIPLOMACY) } ?: TopicCategory.DIPLOMACY

    @TypeConverter
    fun fromFileStatus(status: FileStatus?): String? = status?.name

    @TypeConverter
    fun toFileStatus(value: String?): FileStatus =
        value?.let { runCatching { FileStatus.valueOf(it) }.getOrDefault(FileStatus.ACTIVE) } ?: FileStatus.ACTIVE

    @TypeConverter
    fun fromFilePriority(p: FilePriority?): String? = p?.name

    @TypeConverter
    fun toFilePriority(value: String?): FilePriority =
        value?.let { runCatching { FilePriority.valueOf(it) }.getOrDefault(FilePriority.HIGH) } ?: FilePriority.HIGH

    @TypeConverter
    fun fromArticleClassification(c: ArticleClassification?): String? = c?.name

    @TypeConverter
    fun toArticleClassification(value: String?): ArticleClassification =
        value?.let { runCatching { ArticleClassification.valueOf(it) }.getOrDefault(ArticleClassification.NEW) } ?: ArticleClassification.NEW

    @TypeConverter
    fun fromIngestionStatus(status: IngestionStatus?): String? = status?.name

    @TypeConverter
    fun toIngestionStatus(value: String?): IngestionStatus =
        value?.let { runCatching { IngestionStatus.valueOf(it) }.getOrDefault(IngestionStatus.SUCCESS) } ?: IngestionStatus.SUCCESS

    @TypeConverter
    fun fromEventStatus(status: EventStatus?): String? = status?.name

    @TypeConverter
    fun toEventStatus(value: String?): EventStatus =
        value?.let { runCatching { EventStatus.valueOf(it) }.getOrDefault(EventStatus.DEVELOPING) } ?: EventStatus.DEVELOPING

    @TypeConverter
    fun fromEventImportance(imp: EventImportance?): String? = imp?.name

    @TypeConverter
    fun toEventImportance(value: String?): EventImportance =
        value?.let { runCatching { EventImportance.valueOf(it) }.getOrDefault(EventImportance.HIGH) } ?: EventImportance.HIGH

    @TypeConverter
    fun fromVerificationStatus(status: VerificationStatus?): String? = status?.name

    @TypeConverter
    fun toVerificationStatus(value: String?): VerificationStatus =
        value?.let { runCatching { VerificationStatus.valueOf(it) }.getOrDefault(VerificationStatus.UNVERIFIED) } ?: VerificationStatus.UNVERIFIED

    @TypeConverter
    fun fromEvidenceType(type: EvidenceType?): String? = type?.name

    @TypeConverter
    fun toEvidenceType(value: String?): EvidenceType =
        value?.let { runCatching { EvidenceType.valueOf(it) }.getOrDefault(EvidenceType.NEWS_REPORT) } ?: EvidenceType.NEWS_REPORT

    @TypeConverter
    fun fromEvidenceStrength(s: EvidenceStrength?): String? = s?.name

    @TypeConverter
    fun toEvidenceStrength(value: String?): EvidenceStrength =
        value?.let { runCatching { EvidenceStrength.valueOf(it) }.getOrDefault(EvidenceStrength.STRONG) } ?: EvidenceStrength.STRONG

    @TypeConverter
    fun fromContradictionType(type: ContradictionType?): String? = type?.name

    @TypeConverter
    fun toContradictionType(value: String?): ContradictionType =
        value?.let { runCatching { ContradictionType.valueOf(it) }.getOrDefault(ContradictionType.DATE_TIME) } ?: ContradictionType.DATE_TIME

    @TypeConverter
    fun fromContradictionStatus(status: ContradictionStatus?): String? = status?.name

    @TypeConverter
    fun toContradictionStatus(value: String?): ContradictionStatus =
        value?.let { runCatching { ContradictionStatus.valueOf(it) }.getOrDefault(ContradictionStatus.OPEN) } ?: ContradictionStatus.OPEN

    @TypeConverter
    fun fromAlertType(type: AlertType?): String? = type?.name

    @TypeConverter
    fun toAlertType(value: String?): AlertType =
        value?.let { runCatching { AlertType.valueOf(it) }.getOrDefault(AlertType.BREAKING_NEWS) } ?: AlertType.BREAKING_NEWS

    @TypeConverter
    fun fromAlertSeverity(s: AlertSeverity?): String? = s?.name

    @TypeConverter
    fun toAlertSeverity(value: String?): AlertSeverity =
        value?.let { runCatching { AlertSeverity.valueOf(it) }.getOrDefault(AlertSeverity.MEDIUM) } ?: AlertSeverity.MEDIUM

    @TypeConverter
    fun fromNotificationChannel(c: NotificationChannel?): String? = c?.name

    @TypeConverter
    fun toNotificationChannel(value: String?): NotificationChannel =
        value?.let { runCatching { NotificationChannel.valueOf(it) }.getOrDefault(NotificationChannel.IN_APP) } ?: NotificationChannel.IN_APP

    @TypeConverter
    fun fromAiAnalysisType(type: AiAnalysisType?): String? = type?.name

    @TypeConverter
    fun toAiAnalysisType(value: String?): AiAnalysisType =
        value?.let { runCatching { AiAnalysisType.valueOf(it) }.getOrDefault(AiAnalysisType.ARTICLE_SUMMARY) } ?: AiAnalysisType.ARTICLE_SUMMARY

    @TypeConverter
    fun fromEvidenceVerdict(v: EvidenceVerdict?): String? = v?.name

    @TypeConverter
    fun toEvidenceVerdict(value: String?): EvidenceVerdict =
        value?.let { runCatching { EvidenceVerdict.valueOf(it) }.getOrDefault(EvidenceVerdict.UNVERIFIED) } ?: EvidenceVerdict.UNVERIFIED

    @TypeConverter
    fun fromInformationGapStatus(s: InformationGapStatus?): String? = s?.name

    @TypeConverter
    fun toInformationGapStatus(value: String?): InformationGapStatus =
        value?.let { runCatching { InformationGapStatus.valueOf(it) }.getOrDefault(InformationGapStatus.UNKNOWN) } ?: InformationGapStatus.UNKNOWN

    @TypeConverter
    fun fromSourceTracingResult(r: SourceTracingResult?): String? = r?.name

    @TypeConverter
    fun toSourceTracingResult(value: String?): SourceTracingResult =
        value?.let { runCatching { SourceTracingResult.valueOf(it) }.getOrDefault(SourceTracingResult.SOURCE_NOT_FOUND) } ?: SourceTracingResult.SOURCE_NOT_FOUND

    @TypeConverter
    fun fromContradictionSeverityClassification(c: ContradictionSeverityClassification?): String? = c?.name

    @TypeConverter
    fun toContradictionSeverityClassification(value: String?): ContradictionSeverityClassification =
        value?.let { runCatching { ContradictionSeverityClassification.valueOf(it) }.getOrDefault(ContradictionSeverityClassification.INSUFFICIENT_DATA) } ?: ContradictionSeverityClassification.INSUFFICIENT_DATA

    @TypeConverter
    fun fromReportType(type: ReportType?): String? = type?.name

    @TypeConverter
    fun toReportType(value: String?): ReportType =
        value?.let { runCatching { ReportType.valueOf(it) }.getOrDefault(ReportType.DAILY_BRIEF) } ?: ReportType.DAILY_BRIEF

    @TypeConverter
    fun fromRelationshipType(type: RelationshipType?): String? = type?.name

    @TypeConverter
    fun toRelationshipType(value: String?): RelationshipType =
        value?.let { runCatching { RelationshipType.valueOf(it) }.getOrDefault(RelationshipType.RELATED_TO) } ?: RelationshipType.RELATED_TO

    @TypeConverter
    fun fromEntityType(type: EntityType?): String? = type?.name

    @TypeConverter
    fun toEntityType(value: String?): EntityType =
        value?.let { runCatching { EntityType.valueOf(it) }.getOrDefault(EntityType.PERSON) } ?: EntityType.PERSON

    @TypeConverter
    fun fromDiscoveryStatus(status: DiscoveryStatus?): String? = status?.name

    @TypeConverter
    fun toDiscoveryStatus(value: String?): DiscoveryStatus =
        value?.let { runCatching { DiscoveryStatus.valueOf(it) }.getOrDefault(DiscoveryStatus.DISCOVERED) } ?: DiscoveryStatus.DISCOVERED

    @TypeConverter
    fun fromEvidenceNature(nature: EvidenceNature?): String? = nature?.name

    @TypeConverter
    fun toEvidenceNature(value: String?): EvidenceNature =
        value?.let { runCatching { EvidenceNature.valueOf(it) }.getOrDefault(EvidenceNature.DIRECT_EVIDENCE) } ?: EvidenceNature.DIRECT_EVIDENCE

    @TypeConverter
    fun fromInterviewType(type: InterviewType?): String? = type?.name

    @TypeConverter
    fun toInterviewType(value: String?): InterviewType =
        value?.let { runCatching { InterviewType.valueOf(it) }.getOrDefault(InterviewType.LIVE) } ?: InterviewType.LIVE

    @TypeConverter
    fun fromInterviewStatus(status: InterviewStatus?): String? = status?.name

    @TypeConverter
    fun toInterviewStatus(value: String?): InterviewStatus =
        value?.let { runCatching { InterviewStatus.valueOf(it) }.getOrDefault(InterviewStatus.CONFIRMED) } ?: InterviewStatus.CONFIRMED

    @TypeConverter
    fun fromInterviewLinkType(type: InterviewLinkType?): String? = type?.name

    @TypeConverter
    fun toInterviewLinkType(value: String?): InterviewLinkType =
        value?.let { runCatching { InterviewLinkType.valueOf(it) }.getOrDefault(InterviewLinkType.YOUTUBE) } ?: InterviewLinkType.YOUTUBE

    @TypeConverter
    fun fromInterviewAnalysisType(type: InterviewAnalysisType?): String? = type?.name

    @TypeConverter
    fun toInterviewAnalysisType(value: String?): InterviewAnalysisType =
        value?.let { runCatching { InterviewAnalysisType.valueOf(it) }.getOrDefault(InterviewAnalysisType.PRE_INTERVIEW_BRIEF) } ?: InterviewAnalysisType.PRE_INTERVIEW_BRIEF
}
