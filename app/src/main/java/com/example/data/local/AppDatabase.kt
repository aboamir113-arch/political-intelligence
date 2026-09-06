package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Source::class,
        Country::class,
        Organization::class,
        Person::class,
        PersonPosition::class,
        Topic::class,
        PoliticalFile::class,
        ActivityLog::class,
        Article::class,
        IngestionLog::class,
        PoliticalEvent::class,
        ArticleEventCrossRef::class,
        Claim::class,
        Statement::class,
        EvidenceItem::class,
        TimelineItem::class,
        ContradictionItem::class,
        AlertItem::class,
        AlertRule::class,
        AiAnalysis::class,
        SavedResearch::class,
        AiConversation::class,
        AiMessage::class,
        GeneratedReport::class,
        PoliticalRelationship::class,
        EarlySignalItem::class,
        DetectedChangeItem::class,
        InformationGapItem::class,
        SmartMonitorRule::class,
        Interview::class,
        InterviewLink::class,
        InterviewTranscript::class,
        InterviewAnalysis::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sourceDao(): SourceDao
    abstract fun countryDao(): CountryDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun personDao(): PersonDao
    abstract fun topicDao(): TopicDao
    abstract fun politicalFileDao(): PoliticalFileDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun articleDao(): ArticleDao
    abstract fun ingestionLogDao(): IngestionLogDao
    abstract fun politicalEventDao(): PoliticalEventDao
    abstract fun claimDao(): ClaimDao
    abstract fun statementDao(): StatementDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun timelineDao(): TimelineDao
    abstract fun contradictionDao(): ContradictionDao
    abstract fun alertDao(): AlertDao
    abstract fun aiAnalysisDao(): AiAnalysisDao
    abstract fun savedResearchDao(): SavedResearchDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun generatedReportDao(): GeneratedReportDao
    abstract fun politicalRelationshipDao(): PoliticalRelationshipDao
    abstract fun earlySignalDao(): EarlySignalDao
    abstract fun detectedChangeDao(): DetectedChangeDao
    abstract fun informationGapDao(): InformationGapDao
    abstract fun smartMonitorRuleDao(): SmartMonitorRuleDao
    abstract fun interviewDao(): InterviewDao
    abstract fun interviewLinkDao(): InterviewLinkDao
    abstract fun interviewTranscriptDao(): InterviewTranscriptDao
    abstract fun interviewAnalysisDao(): InterviewAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "political_desk_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
