package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SourceTier {
    PRIMARY,        // Official gazettes, direct government ministries, presidential statements
    AGENCY,         // National & international wire services (SPA, Reuters, AFP, WAM)
    TRUSTED_MEDIA,  // Established international & regional news outlets
    SECONDARY,      // Aggregators, secondary reporting, analytical columns
    UNVERIFIED;     // Social media, unconfirmed feeds, independent leaks

    fun displayNameAr(): String = when (this) {
        PRIMARY -> "مصدر أولي / رسمي"
        AGENCY -> "وكالة أنباء"
        TRUSTED_MEDIA -> "وسيلة إعلام موثوقة"
        SECONDARY -> "مصدر ثانوي / ناقل"
        UNVERIFIED -> "غير مؤكد / مفتوح"
    }

    fun displayNameEn(): String = when (this) {
        PRIMARY -> "Primary / Official"
        AGENCY -> "News Agency"
        TRUSTED_MEDIA -> "Trusted Media"
        SECONDARY -> "Secondary Media"
        UNVERIFIED -> "Unverified"
    }
}

enum class FetchMethod {
    RSS,
    API,
    OFFICIAL_SITE,
    MANUAL;

    fun displayNameAr(): String = when (this) {
        RSS -> "تغذية RSS"
        API -> "واجهة برمجية API"
        OFFICIAL_SITE -> "موقع رسمي (ويب)"
        MANUAL -> "إدخال يدوي"
    }
}

enum class SourceStatus {
    ACTIVE,
    PAUSED,
    ERROR;

    fun displayNameAr(): String = when (this) {
        ACTIVE -> "نشط"
        PAUSED -> "متوقف مؤقتاً"
        ERROR -> "تعذر الاتصال"
    }
}

enum class ConnectionState {
    CONNECTED,
    SLOW,
    FAILED,
    INVALID_FEED,
    AUTHENTICATION_ERROR,
    TIMEOUT,
    BLOCKED,
    NO_NEW_ITEMS,
    NOT_TESTED;

    fun displayNameAr(): String = when (this) {
        CONNECTED -> "متصل بنجاح"
        SLOW -> "استجابة بطيئة"
        FAILED -> "فشل الاتصال"
        INVALID_FEED -> "تغذية غير صالحة"
        AUTHENTICATION_ERROR -> "خطأ مصادقة API"
        TIMEOUT -> "انتهاء وقت الاستجابة"
        BLOCKED -> "محظور تقنياً / حماية"
        NO_NEW_ITEMS -> "متصل (لا توجد عناصر جديدة)"
        NOT_TESTED -> "لم يُختبر"
    }

    fun displayNameEn(): String = when (this) {
        CONNECTED -> "Connected"
        SLOW -> "Slow Response"
        FAILED -> "Connection Failed"
        INVALID_FEED -> "Invalid Feed"
        AUTHENTICATION_ERROR -> "Auth Error"
        TIMEOUT -> "Timeout"
        BLOCKED -> "Blocked / Protected"
        NO_NEW_ITEMS -> "No New Items"
        NOT_TESTED -> "Not Tested"
    }
}

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameAr: String,
    val nameEn: String,
    val countryCode: String,
    val language: String = "ar", // "ar" or "en"
    val tier: SourceTier,
    val fetchMethod: FetchMethod,
    val websiteUrl: String,
    val rssUrl: String? = null,
    val apiUrl: String? = null,
    val apiConfigJson: String? = null,
    val sourceType: String = "NEWS_AGENCY", // OFFICIAL, NEWS_AGENCY, NEWSPAPER, MAGAZINE, NEWS_WEBSITE, RESEARCH_CENTER, GOVERNMENT, etc.
    val status: SourceStatus = SourceStatus.ACTIVE,
    val updateFrequencyMinutes: Int = 15,
    val lastSyncAt: Long? = null,
    val connectionState: ConnectionState = ConnectionState.NOT_TESTED,
    val lastTestMessage: String? = null,
    val reliabilityLevel: Int = 85, // 1 to 100
    val notes: String = "",
    val isOfficial: Boolean = false,
    val totalArticlesIngested: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
