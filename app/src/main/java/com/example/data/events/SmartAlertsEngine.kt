package com.example.data.events

import com.example.data.model.*

class SmartAlertsEngine {

    /**
     * Evaluates whether an incoming event or article matches an analyst's active AlertRules.
     */
    fun evaluateRulesForArticle(
        article: Article,
        rules: List<AlertRule>
    ): List<AlertItem> {
        val triggered = mutableListOf<AlertItem>()

        for (rule in rules) {
            if (!rule.enabled) continue

            var isMatch = false
            var matchReason = ""

            // Political file check
            if (rule.politicalFileId != null && rule.politicalFileId == article.politicalFileId) {
                isMatch = true
                matchReason = "تطور جديد متعلق بالملف السياسي المتابع (${rule.politicalFileTitle ?: article.politicalFileTitle})"
            }

            // Person check
            if (!isMatch && rule.personName != null && article.linkedPersonNames.contains(rule.personName)) {
                isMatch = true
                matchReason = "ورود نشاط أو تصريح يخص الشخصية المتابعة (${rule.personName})"
            }

            // Topic check
            if (!isMatch && rule.topicId != null && rule.topicId == article.topicId) {
                isMatch = true
                matchReason = "خبر جديد يندرج ضمن الموضوع المتابع (${rule.topicName ?: article.topicName})"
            }

            // Organization check
            if (!isMatch && rule.organizationName != null && article.linkedOrgNames.contains(rule.organizationName)) {
                isMatch = true
                matchReason = "بيان أو تحرك يخص المؤسسة المتابعة (${rule.organizationName})"
            }

            // Country check
            if (!isMatch && rule.countryCode != null && (article.primaryCountryCode == rule.countryCode || article.linkedCountryCodes.contains(rule.countryCode))) {
                isMatch = true
                matchReason = "تطور جيوسياسي يخص الدولة المراقبة (${rule.countryCode})"
            }

            // Breaking news check
            if (!isMatch && rule.alertType == AlertType.BREAKING_NEWS && article.importanceScore >= 80) {
                isMatch = true
                matchReason = "خبر عاجل ذو أولوية استراتيجية قصوى"
            }

            if (isMatch) {
                val severity = when {
                    article.importanceScore >= 80 -> AlertSeverity.CRITICAL
                    article.importanceScore >= 60 -> AlertSeverity.HIGH
                    else -> AlertSeverity.MEDIUM
                }

                if (severity >= rule.minSeverity) {
                    triggered.add(
                        AlertItem(
                            userId = rule.userId,
                            type = rule.alertType ?: AlertType.IMPORTANT_DEVELOPMENT,
                            severity = severity,
                            titleAr = "تنبيه ذكي: ${rule.name}",
                            messageAr = "$matchReason: ${article.title.take(110)}",
                            articleId = article.id,
                            politicalFileId = article.politicalFileId,
                            channel = NotificationChannel.IN_APP
                        )
                    )
                }
            }
        }

        return triggered
    }

    /**
     * Triggers alert when a direct statement from a monitored person is detected.
     */
    fun evaluateRulesForStatement(
        statement: Statement,
        rules: List<AlertRule>
    ): List<AlertItem> {
        val triggered = mutableListOf<AlertItem>()
        for (rule in rules) {
            if (!rule.enabled) continue
            if (rule.personName != null && statement.personName.contains(rule.personName)) {
                triggered.add(
                    AlertItem(
                        userId = rule.userId,
                        type = AlertType.NEW_STATEMENT,
                        severity = AlertSeverity.HIGH,
                        titleAr = "تصريح جديد: ${statement.personName}",
                        messageAr = "صدر تصريح رسمي عبر ${statement.sourceName}: \"${statement.quoteText.take(100)}\"",
                        articleId = statement.articleId,
                        personId = statement.personId,
                        eventId = statement.eventId,
                        channel = NotificationChannel.IN_APP
                    )
                )
            }
        }
        return triggered
    }

    /**
     * Triggers alert when a contradiction between sources is detected.
     */
    fun createContradictionAlert(
        contradiction: ContradictionItem,
        eventTitle: String,
        userId: Long = 1
    ): AlertItem {
        return AlertItem(
            userId = userId,
            type = AlertType.CONTRADICTION,
            severity = AlertSeverity.HIGH,
            titleAr = "رصد تناقض بين المصادر",
            messageAr = "تعارض في حدث '$eventTitle': ${contradiction.descriptionAr}",
            eventId = contradiction.eventId,
            channel = NotificationChannel.IN_APP
        )
    }

    /**
     * Triggers alert when a claim's verification status shifts.
     */
    fun createVerificationChangeAlert(
        claim: Claim,
        oldStatus: VerificationStatus,
        newStatus: VerificationStatus,
        userId: Long = 1
    ): AlertItem {
        val severity = if (newStatus == VerificationStatus.CONFIRMED || newStatus == VerificationStatus.CONTRADICTED) {
            AlertSeverity.CRITICAL
        } else {
            AlertSeverity.MEDIUM
        }

        return AlertItem(
            userId = userId,
            type = AlertType.VERIFICATION_CHANGE,
            severity = severity,
            titleAr = "تغير حالة التحقق: ${newStatus.displayNameAr()}",
            messageAr = "تم تحديث تصنيف الادعاء '${claim.statement.take(80)}' من ${oldStatus.displayNameAr()} إلى ${newStatus.displayNameAr()}.",
            claimId = claim.id,
            eventId = claim.eventId,
            articleId = claim.articleId,
            channel = NotificationChannel.IN_APP
        )
    }
}
