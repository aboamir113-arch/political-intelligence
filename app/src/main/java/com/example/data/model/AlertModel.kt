package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AlertType {
    BREAKING_NEWS,
    IMPORTANT_DEVELOPMENT,
    NEW_STATEMENT,
    FILE_UPDATE,
    PERSON_UPDATE,
    CONTRADICTION,
    VERIFICATION_CHANGE,
    RECIRCULATED_NEWS,
    SOURCE_UPDATE;

    fun displayNameAr(): String = when (this) {
        BREAKING_NEWS -> "عاجل وميداني"
        IMPORTANT_DEVELOPMENT -> "تطور استراتيجي في حدث"
        NEW_STATEMENT -> "تصريح سياسي جديد"
        FILE_UPDATE -> "تحديث في ملف سياسي"
        PERSON_UPDATE -> "نشاط لشخصية مراقبة"
        CONTRADICTION -> "رصد تناقض بين المصادر"
        VERIFICATION_CHANGE -> "تغير في حالة التحقق"
        RECIRCULATED_NEWS -> "تكرار خبر دون جديد"
        SOURCE_UPDATE -> "تحديث حالة مصدر"
    }

    fun displayNameEn(): String = when (this) {
        BREAKING_NEWS -> "Breaking News"
        IMPORTANT_DEVELOPMENT -> "Important Development"
        NEW_STATEMENT -> "New Statement"
        FILE_UPDATE -> "File Update"
        PERSON_UPDATE -> "Person Update"
        CONTRADICTION -> "Contradiction Detected"
        VERIFICATION_CHANGE -> "Verification Changed"
        RECIRCULATED_NEWS -> "Recirculated News"
        SOURCE_UPDATE -> "Source Update"
    }
}

enum class AlertSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    fun displayNameAr(): String = when (this) {
        CRITICAL -> "حرج / فوري"
        HIGH -> "أولوية عالية"
        MEDIUM -> "متوسط"
        LOW -> "معلوماتي"
    }
}

enum class NotificationChannel {
    IN_APP,
    PUSH,
    EMAIL;

    fun displayNameAr(): String = when (this) {
        IN_APP -> "تنبيه داخلي بالتطبيق"
        PUSH -> "إشعار نظام الموبايل"
        EMAIL -> "بريد إلكتروني تحليلي"
    }
}

@Entity(
    tableName = "alerts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["type"]),
        Index(value = ["severity"]),
        Index(value = ["isRead"]),
        Index(value = ["createdAt"])
    ]
)
data class AlertItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val type: AlertType,
    val severity: AlertSeverity = AlertSeverity.HIGH,
    val titleAr: String,
    val messageAr: String,
    val eventId: Long? = null,
    val articleId: Long? = null,
    val politicalFileId: Long? = null,
    val personId: Long? = null,
    val claimId: Long? = null,
    val isRead: Boolean = false,
    val channel: NotificationChannel = NotificationChannel.IN_APP,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "alert_rules",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["enabled"])
    ]
)
data class AlertRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val name: String,
    val alertType: AlertType? = null,
    val topicId: Long? = null,
    val topicName: String? = null,
    val personId: Long? = null,
    val personName: String? = null,
    val organizationId: Long? = null,
    val organizationName: String? = null,
    val politicalFileId: Long? = null,
    val politicalFileTitle: String? = null,
    val sourceId: Long? = null,
    val countryCode: String? = null,
    val minSeverity: AlertSeverity = AlertSeverity.MEDIUM,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
