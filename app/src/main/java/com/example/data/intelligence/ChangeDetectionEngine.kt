package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Change Detection Engine
 * Discovers significant shifts: stance reversals, new diplomatic interlocutors,
 * dossier escalations, and narrative turns with explicit comparative evidence and confidence metrics.
 */
class ChangeDetectionEngine(
    private val changeDao: DetectedChangeDao,
    private val statementDao: StatementDao,
    private val personDao: PersonDao,
    private val politicalFileDao: PoliticalFileDao,
    private val politicalEventDao: PoliticalEventDao,
    private val articleDao: ArticleDao
) {

    /**
     * Scan recent corpus and detect new comparative changes against historical baselines.
     */
    suspend fun scanAndDetectChanges(): List<DetectedChangeItem> = withContext(Dispatchers.IO) {
        val statements = statementDao.getAllStatementsList()
        val persons = personDao.getAllPersonsList()
        val files = politicalFileDao.getAllFilesList()
        val events = politicalEventDao.getAllEventsList()
        val recentArticles = articleDao.getAllArticles(limit = 50)
        val existingChanges = changeDao.getRecentChanges(limit = 100)

        val detected = mutableListOf<DetectedChangeItem>()

        // 1. Detect Person Stance Shifts (from recorded positions and statements)
        val positions = personDao.getAllPositionsList()
        positions.forEach { pos ->
            val person = persons.find { it.id == pos.personId }
            val name = person?.nameAr ?: "شخصية سياسية"

            val alreadyLogged = existingChanges.any {
                it.entityType == EntityType.PERSON && it.entityId == pos.personId &&
                        it.whatChangedAr.contains(pos.issueTitle)
            }

            if (!alreadyLogged && detected.none { it.entityId == pos.personId && it.entityType == EntityType.PERSON }) {
                detected.add(
                    DetectedChangeItem(
                        entityType = EntityType.PERSON,
                        entityId = pos.personId,
                        entityName = name,
                        changeType = "STANCE_SHIFT",
                        whatChangedAr = "تحول في الموقف السياسي المعلن بشأن: ${pos.issueTitle} [${pos.shiftType.name}]",
                        whenTimestamp = pos.changeDateTimestamp,
                        comparedWithWhatAr = "الموقف السابق: \"${pos.previousStance}\"",
                        evidenceSnippet = "الموقف الحالي: \"${pos.currentStance}\" - نص التصريح: \"${pos.verbatimStatement.take(120)}\"",
                        sourceName = pos.sourceName,
                        confidence = (pos.evidenceConfidence / 100f).coerceIn(0.6f, 0.99f),
                        magnitude = if (pos.shiftType == StanceShiftType.SIGNIFICANT_REVERSAL) 0.90f else 0.70f
                    )
                )
            }
        }

        // Check statements for recent tone/quote updates
        val statementsByPerson = statements.groupBy { it.personId }
        statementsByPerson.forEach { (personId, stmtList) ->
            if (personId != null && stmtList.size >= 2) {
                val sorted = stmtList.sortedBy { it.statementDate }
                val previous = sorted.first()
                val latest = sorted.last()

                if (previous.quoteText != latest.quoteText) {
                    val person = persons.find { it.id == personId }
                    val name = person?.nameAr ?: latest.personName

                    val alreadyLogged = existingChanges.any {
                        it.entityType == EntityType.PERSON && it.entityId == personId
                    }

                    if (!alreadyLogged && detected.none { it.entityId == personId && it.entityType == EntityType.PERSON }) {
                        detected.add(
                            DetectedChangeItem(
                                entityType = EntityType.PERSON,
                                entityId = personId,
                                entityName = name,
                                changeType = "STANCE_SHIFT",
                                whatChangedAr = "تحديث في التصريحات المعلنة لـ ($name)",
                                whenTimestamp = latest.statementDate,
                                comparedWithWhatAr = "مقارنة بالتصريح السابق: \"${previous.quoteText.take(90)}\"",
                                evidenceSnippet = "التصريح الأحدث: \"${latest.quoteText.take(120)}\" (مصدر: ${latest.sourceName})",
                                sourceName = latest.sourceName,
                                confidence = 0.85f,
                                magnitude = 0.70f
                            )
                        )
                    }
                }
            }
        }

        // 2. Detect Political File Escalation or Status Transitions
        files.forEach { file ->
            val fileArticles = recentArticles.filter { it.politicalFileId == file.id }
            if (fileArticles.size >= 3) {
                val hasUrgent = file.priority == FilePriority.CRITICAL || file.priority == FilePriority.HIGH
                val alreadyLogged = existingChanges.any {
                    it.entityType == EntityType.POLITICAL_FILE && it.entityId == file.id &&
                            it.changeType == "ESCALATION"
                }

                if (hasUrgent && !alreadyLogged && detected.none { it.entityId == file.id && it.entityType == EntityType.POLITICAL_FILE }) {
                    detected.add(
                        DetectedChangeItem(
                            entityType = EntityType.POLITICAL_FILE,
                            entityId = file.id,
                            entityName = file.titleAr,
                            changeType = "ESCALATION",
                            whatChangedAr = "تصاعد وتيرة التغطية الإخبارية والاهتمام الاستراتيجي بملف (${file.titleAr})",
                            whenTimestamp = System.currentTimeMillis(),
                            comparedWithWhatAr = "مقارنة بمرحلة الرصد والمتابعة الاعتيادية السابقة للملف",
                            evidenceSnippet = "رصد ${fileArticles.size} برقيات عاجلة خلال الجولة الأخيرة تشير إلى تسارع التطورات الميدانية والدبلوماسية.",
                            sourceName = fileArticles.first().sourceName,
                            confidence = 0.85f,
                            magnitude = 0.75f
                        )
                    )
                }
            }
        }

        // 3. Detect Event Stage Transition
        events.forEach { event ->
            if (event.status == EventStatus.DEVELOPING && event.articleCount >= 5) {
                val alreadyLogged = existingChanges.any {
                    it.entityType == EntityType.EVENT && it.entityId == event.id
                }
                if (!alreadyLogged && detected.none { it.entityId == event.id && it.entityType == EntityType.EVENT }) {
                    detected.add(
                        DetectedChangeItem(
                            entityType = EntityType.EVENT,
                            entityId = event.id,
                            entityName = event.titleAr,
                            changeType = "STATUS_TRANSITION",
                            whatChangedAr = "انتقال الحدث إلى مرحلة التشعب الإقليمي وتعدد المحاور مع تجاوز عتبة 5 برقيات توثيقية",
                            whenTimestamp = event.latestUpdate,
                            comparedWithWhatAr = "مقارنة ببداية اندلاع الحدث كواقعة أولية معزولة",
                            evidenceSnippet = "تواتر متواصل من ${event.articleCount} مصادر وتحديثات في السجل الزمني.",
                            sourceName = "سجل التحليل السياسي المشترك",
                            confidence = 0.88f,
                            magnitude = 0.70f
                        )
                    )
                }
            }
        }

        if (detected.isNotEmpty()) {
            changeDao.insertChanges(detected)
        }

        detected
    }
}
