package com.example.data.model

import java.util.*

enum class DateFilter {
    ALL_TIME,
    TODAY,
    YESTERDAY,
    LAST_3_DAYS,
    LAST_7_DAYS,
    LAST_30_DAYS,
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR,
    LAST_YEAR,
    CUSTOM_RANGE;

    fun displayNameAr(): String = when (this) {
        ALL_TIME -> "كل الأوقات"
        TODAY -> "اليوم"
        YESTERDAY -> "أمس"
        LAST_3_DAYS -> "آخر 3 أيام"
        LAST_7_DAYS -> "آخر 7 أيام"
        LAST_30_DAYS -> "آخر 30 يوماً"
        THIS_MONTH -> "هذا الشهر"
        LAST_MONTH -> "الشهر الماضي"
        LAST_3_MONTHS -> "آخر 3 أشهر"
        THIS_YEAR -> "هذا العام"
        LAST_YEAR -> "العام الماضي"
        CUSTOM_RANGE -> "نطاق مخصص"
    }

    fun displayNameEn(): String = when (this) {
        ALL_TIME -> "All Time"
        TODAY -> "Today"
        YESTERDAY -> "Yesterday"
        LAST_3_DAYS -> "Last 3 Days"
        LAST_7_DAYS -> "Last 7 Days"
        LAST_30_DAYS -> "Last 30 Days"
        THIS_MONTH -> "This Month"
        LAST_MONTH -> "Last Month"
        LAST_3_MONTHS -> "Last 3 Months"
        THIS_YEAR -> "This Year"
        LAST_YEAR -> "Last Year"
        CUSTOM_RANGE -> "Custom Range"
    }

    /**
     * Computes UTC epoch millisecond boundaries [startUtc, endUtc] for accurate query filtering.
     */
    fun getUtcRange(customFromUtc: Long? = null, customToUtc: Long? = null): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = now
        }

        return when (this) {
            ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                Pair(start, now + 86400000L)
            }
            YESTERDAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val endOfYesterday = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val startOfYesterday = calendar.timeInMillis
                Pair(startOfYesterday, endOfYesterday)
            }
            LAST_3_DAYS -> Pair(now - 3L * 86400000L, now + 86400000L)
            LAST_7_DAYS -> Pair(now - 7L * 86400000L, now + 86400000L)
            LAST_30_DAYS -> Pair(now - 30L * 86400000L, now + 86400000L)
            THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, now + 86400000L)
            }
            LAST_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val end = calendar.timeInMillis
                calendar.add(Calendar.MONTH, -1)
                val start = calendar.timeInMillis
                Pair(start, end)
            }
            LAST_3_MONTHS -> Pair(now - 90L * 86400000L, now + 86400000L)
            THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, now + 86400000L)
            }
            LAST_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val end = calendar.timeInMillis
                calendar.add(Calendar.YEAR, -1)
                val start = calendar.timeInMillis
                Pair(start, end)
            }
            CUSTOM_RANGE -> {
                val start = customFromUtc ?: 0L
                val end = customToUtc ?: Long.MAX_VALUE
                Pair(start, end)
            }
        }
    }
}
