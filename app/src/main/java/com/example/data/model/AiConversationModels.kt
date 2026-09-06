package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Strict factual classification required by anti-hallucination protocols
 */
enum class FactClassification {
    FACT,          // حقيقة موثقة بدليل قطعي أو بيان رسمي
    INFERENCE,     // استنتاج تحليلي مبني على الأدلة
    UNCERTAIN,     // معلومة غير مؤكدة أو رواية غير محسومة
    CONFLICTED,    // معلومة متناقض حولها بين المصادر
    UNKNOWN;       // معلومة مجهولة لم تتوفر في البيانات

    fun labelAr(): String = when (this) {
        FACT -> "حقيقة مؤكدة"
        INFERENCE -> "استنتاج تحليلي"
        UNCERTAIN -> "غير مؤكد / أولي"
        CONFLICTED -> "متناقض / خلافي"
        UNKNOWN -> "مجهول / غير متوفر"
    }
}

/**
 * An individual assertion with strict verification status
 */
data class FactItemVerdict(
    val statement: String,
    val classification: FactClassification,
    val rationale: String,
    val supportingSource: String? = null
)

/**
 * Structured, verifiable AI Response architecture
 */
data class StructuredAiAnswer(
    val answer: String,
    val keyFindings: List<String> = emptyList(),
    val evidence: List<String> = emptyList(),
    val sources: List<AiCitation> = emptyList(),
    val dates: List<String> = emptyList(),
    val conflicts: List<String> = emptyList(),
    val uncertainty: List<String> = emptyList(),
    val analysis: String = "",
    val factVerdicts: List<FactItemVerdict> = emptyList(),
    val isGroundingSufficient: Boolean = true,
    val language: String = "ar",
    val modelUsed: String = "gemini-3.5-flash"
)

/**
 * Persistent AI Conversation Thread
 */
@Entity(
    tableName = "ai_conversations",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isArchived"])
    ]
)
data class AiConversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val title: String,
    val modelUsed: String = "gemini-3.5-flash",
    val promptVersion: String = "v5.0-rag",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Individual message within an AI Conversation
 */
@Entity(
    tableName = "ai_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class AiMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "USER" or "ASSISTANT"
    val content: String,
    val structuredAnswerJson: String? = null,
    val citationsJson: String = "[]",
    val workspaceSnapshotJson: String? = null,
    val processingStage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
