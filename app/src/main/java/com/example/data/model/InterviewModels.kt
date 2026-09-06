package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Type of interview format.
 */
enum class InterviewType(val displayNameAr: String, val displayNameEn: String) {
    LIVE("مباشر على الهواء", "Live"),
    RECORDED("مسجلة مسبقاً", "Recorded"),
    PHONE("مداخلة هاتفية", "Phone"),
    VIDEO_CALL("اتصال مرئي / سكايب / زوم", "Video Call"),
    RADIO("مقابلة إذاعية", "Radio"),
    TV("استوديو تلفزيوني", "TV Studio"),
    OTHER("منصة رقمية / بودكاست / أخرى", "Other")
}

/**
 * Operational lifecycle status of an interview.
 */
enum class InterviewStatus(val displayNameAr: String, val displayNameEn: String) {
    PLANNED("مخطط لها", "Planned"),
    CONFIRMED("مؤكدة", "Confirmed"),
    COMPLETED("مكتملة ومنتهية", "Completed"),
    CANCELLED("ملغاة", "Cancelled"),
    POSTPONED("مؤجلة", "Postponed")
}

/**
 * Type of link or media attachment for an interview.
 */
enum class InterviewLinkType(val displayNameAr: String, val displayNameEn: String) {
    VIDEO("فيديو المقابلة", "Video"),
    YOUTUBE("يوتيوب", "YouTube"),
    CHANNEL_SITE("موقع المحطة / المنصة", "Channel Website"),
    ARTICLE("تقرير أو مقال صحفي", "Article"),
    RECORDING("تسجيل صوتي", "Audio Recording"),
    TRANSCRIPT("تفريغ نصي", "Transcript"),
    OTHER("مصدر آخر", "Other Source")
}

/**
 * Type of AI analysis performed on an interview.
 */
enum class InterviewAnalysisType(val displayNameAr: String, val displayNameEn: String) {
    PRE_INTERVIEW_BRIEF("إيجاز التحضير الذكي الشامل", "Pre-Interview Brief"),
    ONE_PAGE_BRIEF("إيجاز الاستوديو - صفحة واحدة", "One-Page Studio Brief"),
    POST_INTERVIEW_REPORT("تقرير أداء ما بعد المقابلة", "Post-Interview Report"),
    INTERVIEW_CHAT("استشارة حية داخل المقابلة", "Interview In-Context Chat")
}

/**
 * Reminder intervals for upcoming interviews.
 */
enum class InterviewReminderType(val displayNameAr: String, val offsetMillis: Long) {
    ONE_DAY_BEFORE("قبل 24 ساعة", 86400000L),
    THREE_HOURS_BEFORE("قبل 3 ساعات", 10800000L),
    ONE_HOUR_BEFORE("قبل ساعة واحدة", 3600000L)
}

/**
 * Calendar display modes for the Interview Calendar.
 */
enum class CalendarViewMode(val displayNameAr: String, val displayNameEn: String) {
    MONTH("عرض شهري", "Month"),
    WEEK("عرض أسبوعي", "Week"),
    DAY("عرض يومي", "Day"),
    LIST("قائمة المقابلات", "List")
}

/**
 * Main Interview Entity in Room Database.
 */
@Entity(
    tableName = "interviews",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["dateUtc"]),
        Index(value = ["status"])
    ]
)
data class Interview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val dateUtc: Long,
    val startTime: String, // e.g. "20:00"
    val endTime: String,   // e.g. "21:00"
    val channel: String,
    val program: String,
    val interviewer: String,
    val interviewType: InterviewType = InterviewType.LIVE,
    val location: String = "",
    val subject: String,
    val description: String = "",
    val notes: String = "",
    val status: InterviewStatus = InterviewStatus.CONFIRMED,
    val isPrivate: Boolean = false,
    // Comma-separated relational IDs
    val linkedFileIds: String = "",
    val linkedEventIds: String = "",
    val linkedTopicIds: String = "",
    val linkedPersonIds: String = "",
    val linkedOrgIds: String = "",
    val linkedCountryCodes: String = "",
    val durationMinutes: Int = 30,
    val thumbnailUri: String? = null,
    val remindersEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Links / media attachments associated with an interview.
 */
@Entity(
    tableName = "interview_links",
    indices = [Index(value = ["interviewId"])]
)
data class InterviewLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val interviewId: Long,
    val url: String,
    val title: String,
    val source: String = "",
    val type: InterviewLinkType = InterviewLinkType.YOUTUBE,
    val dateStr: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Transcripts or verbatim records for an interview.
 */
@Entity(
    tableName = "interview_transcripts",
    indices = [Index(value = ["interviewId"])]
)
data class InterviewTranscript(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val interviewId: Long,
    val transcriptText: String,
    val language: String = "ar",
    val audioUrlOrPath: String? = null,
    val videoUrlOrPath: String? = null,
    val extractedQuestionsJson: String = "[]",
    val extractedAnswersJson: String = "[]",
    val extractedKeyPointsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * AI Pre-Interview or Post-Interview Analysis records.
 */
@Entity(
    tableName = "interview_analysis",
    indices = [Index(value = ["interviewId"])]
)
data class InterviewAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val interviewId: Long,
    val analysisType: InterviewAnalysisType,
    val outputJson: String,
    val model: String = "gemini-3.5-flash",
    val promptVersion: String = "v1.0",
    val sourceIdsUsed: String = "",
    val analysisVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

// =========================================================================
// Structured Analytical Models for Pre-Interview Brief & Post-Analysis
// =========================================================================

data class WhatChangedItem(
    val title: String,
    val dateStr: String,
    val source: String,
    val evidence: String,
    val sourceUrl: String = "",
    val impactAnalysis: String = ""
)

data class KeyFactItem(
    val fact: String,
    val importanceScore: Int = 85,
    val source: String,
    val dateStr: String,
    val category: String = "استراتيجي"
)

data class LikelyQuestionItem(
    val question: String,
    val whyItMayBeAsked: String,
    val suggestedTalkingPoints: List<String>,
    val evidence: String,
    val historicalContext: String = ""
)

data class TalkingPointItem(
    val mainIdea: String,
    val supportingEvidence: String,
    val relevantEvent: String,
    val historicalExample: String,
    val source: String,
    val suggestedPhrasing: String
)

data class HistoricalHookItem(
    val eventName: String,
    val dateStr: String,
    val whatHappened: String,
    val similarity: String,
    val difference: String,
    val whyUsefulInInterview: String,
    val source: String
)

data class PreviousStatementItem(
    val topicOrQuestion: String,
    val previousPosition: String,
    val dateStr: String,
    val source: String,
    val currentPosition: String,
    val whatChanged: String,
    val evidence: String,
    val confidence: Float = 0.85f
)

data class ChallengePointItem(
    val topic: String,
    val potentialChallenge: String,
    val pastClaimOrStance: String,
    val recommendedResponse: String,
    val riskLevel: String = "متوسط"
)

data class StrongestEvidenceItem(
    val claim: String,
    val evidence: String,
    val source: String,
    val dateStr: String,
    val evidenceType: String,
    val verificationStatus: String,
    val confidence: Float
)

data class MediaPerspectiveItem(
    val mediaGroup: String, // e.g. "الإعلام اللبناني", "الإعلام العربي", "الإعلام الدولي", "المصادر الرسمية"
    val framing: String,
    val emphasis: String,
    val importantOmissions: String,
    val dominantNarrative: String
)

data class InterviewStrategyItem(
    val coreMessages: List<String> = emptyList(),       // 3 Core Messages
    val supportingFacts: List<String> = emptyList(),     // 3 Supporting Facts
    val historicalExamples: List<String> = emptyList(),  // 3 Historical Examples
    val difficultQuestions: List<String> = emptyList(),   // 3 Difficult Questions
    val thingsToClarify: List<String> = emptyList(),     // 3 Things To Clarify
    val thingsToAvoid: List<String> = emptyList(),       // 3 Things To Avoid Saying Without Evidence
    val recentDevelopments: List<String> = emptyList()   // 3 Recent Developments
)

data class PreInterviewBriefData(
    val interviewId: Long,
    val subject: String,
    val channelAndProgram: String,
    val interviewer: String,
    val generatedAtUtc: Long = System.currentTimeMillis(),
    val whatChanged: List<WhatChangedItem> = emptyList(),
    val keyFacts: List<KeyFactItem> = emptyList(),
    val likelyQuestions: List<LikelyQuestionItem> = emptyList(),
    val talkingPoints: List<TalkingPointItem> = emptyList(),
    val historicalHooks: List<HistoricalHookItem> = emptyList(),
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val previousStatements: List<PreviousStatementItem> = emptyList(),
    val challengePoints: List<ChallengePointItem> = emptyList(),
    val strongestEvidence: List<StrongestEvidenceItem> = emptyList(),
    val mediaPerspectives: List<MediaPerspectiveItem> = emptyList(),
    val strategy: InterviewStrategyItem = InterviewStrategyItem()
)

data class PostInterviewReportData(
    val interviewId: Long,
    val summary: String,
    val mainArguments: List<String> = emptyList(),
    val questionsAsked: List<String> = emptyList(),
    val answersGiven: List<String> = emptyList(),
    val strongPoints: List<String> = emptyList(),
    val weakPoints: List<String> = emptyList(),
    val evidenceUsed: List<String> = emptyList(),
    val unsupportedClaims: List<String> = emptyList(),
    val contradictionsIdentified: List<String> = emptyList(),
    val previousPositionComparison: List<String> = emptyList(),
    val bestAnswers: List<String> = emptyList(),
    val answersCouldBeImproved: List<String> = emptyList(),
    val importantQuotes: List<String> = emptyList(),
    val followUpTopics: List<String> = emptyList(),
    val analyzedAtUtc: Long = System.currentTimeMillis()
)
