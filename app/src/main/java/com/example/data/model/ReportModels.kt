package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReportType {
    DAILY_BRIEF,
    EVENT_REPORT,
    FILE_REPORT,
    MONITORING_REPORT,
    WORKSPACE_REPORT;

    fun displayNameAr(): String = when (this) {
        DAILY_BRIEF -> "التقرير السياسي اليومي"
        EVENT_REPORT -> "تقرير الحدث المتكامل"
        FILE_REPORT -> "تقرير الملف السياسي الاستراتيجي"
        MONITORING_REPORT -> "تقرير المراقبة والرصد"
        WORKSPACE_REPORT -> "تقرير مساحة البحث والتحليل"
    }

    fun displayNameEn(): String = when (this) {
        DAILY_BRIEF -> "Daily Political Brief"
        EVENT_REPORT -> "Event Comprehensive Report"
        FILE_REPORT -> "Strategic File Report"
        MONITORING_REPORT -> "Entity Monitoring Report"
        WORKSPACE_REPORT -> "Research Workspace Report"
    }
}

data class ReportSection(
    val title: String,
    val content: String,
    val bullets: List<String> = emptyList(),
    val tag: String? = null
)

data class ExecutiveReportData(
    val title: String,
    val subtitle: String,
    val reportType: ReportType,
    val timeframe: String,
    val executiveSummary: String,
    val sections: List<ReportSection> = emptyList(),
    val citations: List<AiCitation> = emptyList(),
    val keyMetrics: Map<String, String> = emptyMap(),
    val modelUsed: String = "gemini-3.5-flash",
    val promptVersion: String = "v5.0",
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "generated_reports",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["reportType"]),
        Index(value = ["targetEntityId"]),
        Index(value = ["createdAt"])
    ]
)
data class GeneratedReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val reportType: ReportType,
    val title: String,
    val subtitle: String,
    val targetEntityId: Long? = null,
    val targetEntityType: String? = null, // "EVENT", "FILE", "PERSON", "TOPIC", "DAILY"
    val executiveSummary: String,
    val contentJson: String,
    val citationsJson: String = "[]",
    val modelUsed: String = "gemini-3.5-flash",
    val promptVersion: String = "v5.0",
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
