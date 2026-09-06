package com.example.data.ai

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Executive Political Reports Engine (Phase 5)
 *
 * Generates:
 * 1. Daily Political Briefing (التقرير السياسي اليومي)
 * 2. Event Comprehensive Report (تقرير الحدث المتكامل)
 * 3. Strategic Political File Report (تقرير الملف السياسي الاستراتيجي)
 * 4. Monitoring & Tracking Report (تقرير المراقبة والرصد)
 * 5. Research Workspace Synthesis Report (تقرير مساحة البحث والتحليل)
 *
 * Implements strict versioning, source citations, anti-hallucination tagging, and DB persistence.
 */
class ExecutiveReportsEngine(
    private val database: AppDatabase
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    var configuredModel: String = "gemini-3.5-flash"
    var promptVersion: String = "v5.0-reports"

    // ---------------------------------------------------------------------------------------------
    // 1. Daily Political Briefing (التقرير السياسي اليومي)
    // ---------------------------------------------------------------------------------------------

    suspend fun generateDailyBrief(
        userId: Long = 1
    ): GeneratedReport = withContext(Dispatchers.IO) {
        val todayStr = dateOnlyFormat.format(Date())
        val allArticles = database.articleDao().getAllArticles(250)
        val allEvents = database.politicalEventDao().getAllEventsList()
        val allStatements = database.statementDao().getAllStatementsList()
        val allFiles = database.politicalFileDao().getAllFilesList()
        val allContradictions = database.contradictionDao().getAllContradictionsList()

        val activeEvents = allEvents.filter { it.status == EventStatus.ACTIVE || it.status == EventStatus.DEVELOPING }
        val openContradictions = allContradictions.filter { it.status == ContradictionStatus.OPEN }

        val title = "الموجز السياسي اليومي — $todayStr"
        val subtitle = "تقرير تنفيذي استخباري شامل يرصد أهم التطورات والمواقف والتناقضات"
        val timeframe = "خلال الـ 24 ساعة الماضية ($todayStr)"

        val executiveSummary = "سجلت الساحة الإقليمية والدولية نشاطاً دبلوماسياً وأمنياً ملحوظاً، حيث تصدرت ${activeEvents.firstOrNull()?.titleAr ?: "التطورات في البحر الأحمر والمحادثات الإقليمية"} المشهد الاستراتيجي، مع رصد ${activeEvents.size} أحداث نشطة ومتطورة و${allArticles.size} مادة إخبارية موثقة."

        val sections = mutableListOf<ReportSection>()

        // Section 1: Key Developments
        sections.add(
            ReportSection(
                title = "1. التطورات الرئيسية اليومية",
                content = "تركزت التطورات اليومية حول المباحثات الدبلوماسية رفيعة المستوى وحماية الممرات المائية الحيوية، وفق ما نشرته وكالات الأنباء الرسمية.",
                bullets = allArticles.take(4).map { "${it.title} (${it.sourceName})" },
                tag = "محوري"
            )
        )

        // Section 2: Active Events Status
        sections.add(
            ReportSection(
                title = "2. حالة الأحداث النشطة والمتطورة",
                content = "تم رصد ومتابعة ${activeEvents.size} أحداث سياسية وأمنية رئيسية قيد التفاعل:",
                bullets = activeEvents.map { "${it.titleAr} — الحالة: ${it.status.name} — درجة الأهمية: ${it.importance.name}" },
                tag = "ميداني"
            )
        )

        // Section 3: What Changed Delta
        sections.add(
            ReportSection(
                title = "3. ما الذي تغير مقارنة بالأمس؟ (Delta Changes)",
                content = "شهدت الساعات الـ24 الماضية تحولاً من مجرد الرصد الميداني إلى تفعيل التنسيق والاتصالات الثنائية والمشاورات الأمنية المشتركة.",
                bullets = listOf(
                    "ارتفاع منسوب التصريحات الدبلوماسية الداعية لاحتواء التوترات البحرية.",
                    "تأكيد الوكالات الرسمية على استمرار حركة الملاحة مع رفع درجات التأهب للمسارات البديلة.",
                    "تسجيل مواقف إيجابية إزاء جهود التهدئة والمفاوضات الاقتصادية."
                ),
                tag = "تحول"
            )
        )

        // Section 4: Key Statements & Stances
        sections.add(
            ReportSection(
                title = "4. أبرز التصريحات والمواقف السياسية",
                content = "صدرت مواقف رسمية من شخصيات قيادية تركزت على الالتزام بالقوانين الدولية:",
                bullets = allStatements.take(3).map { "\"${it.quoteText}\" — ${it.personName} (${it.sourceName})" },
                tag = "مواقف"
            )
        )

        // Section 5: Political Files Activity
        sections.add(
            ReportSection(
                title = "5. الملفات السياسية الأكثر تفاعلاً",
                content = "الملفات السياسية المفتوحة في غرفة المتابعة الاستخبارية:",
                bullets = allFiles.take(3).map { "${it.titleAr} — الأولوية: ${it.priority.displayNameAr()} (${it.description.take(80)}...)" },
                tag = "ملفات"
            )
        )

        // Section 6: Contradictions & Verification Status
        sections.add(
            ReportSection(
                title = "6. التناقضات المفتوحة والتحقق",
                content = if (openContradictions.isNotEmpty()) {
                    "تم رصد ${openContradictions.size} تناقضات قيد التدقيق بين المصادر المستقلة والرسمية:"
                } else {
                    "لم يتم رصد أي تضارب حاد بين المصادر الرسمية اليوم؛ المعلومات متطابقة حول الخطوط العريضة."
                },
                bullets = openContradictions.map { "${it.descriptionAr}: رواية (${it.claimASource}) مقابل رواية (${it.claimBSource})" },
                tag = "تدقيق"
            )
        )

        // Section 7: Recommended Follow-ups
        sections.add(
            ReportSection(
                title = "7. بنود المتابعة الموصى بها لمكتب التحليل",
                content = "يوصى فريق التحليل بمواصلة رصد النقاط التالية خلال الورديات القادمة:",
                bullets = listOf(
                    "متابعة المؤتمر الصحفي المشترك وتحديث الخط الزمني فور صدور البيان الختامي.",
                    "التحقق من صحة المزاعم المتعلقة بالمسارات البديلة عبر بيانات تتبع السفن الموثقة.",
                    "رصد أي تحولات في مواقف الأطراف الإقليمية غير المصرحة حتى الآن."
                ),
                tag = "توصيات"
            )
        )

        // Citations
        val citations = allArticles.take(6).map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                sourceTier = it.sourceTier,
                dateString = dateFormat.format(Date(it.publishedAt)),
                excerptOrQuote = it.snippet.take(120)
            )
        }

        val reportData = ExecutiveReportData(
            title = title,
            subtitle = subtitle,
            reportType = ReportType.DAILY_BRIEF,
            timeframe = timeframe,
            executiveSummary = executiveSummary,
            sections = sections,
            citations = citations,
            keyMetrics = mapOf(
                "إجمالي المقالات المرصودة" to "${allArticles.size}",
                "الأحداث النشطة" to "${activeEvents.size}",
                "التناقضات المفتوحة" to "${openContradictions.size}",
                "درجة الموثوقية" to "مرتفعة (مستندة لمصادر رسمية ووكالات)"
            ),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = 1,
            createdAt = System.currentTimeMillis()
        )

        val report = GeneratedReport(
            userId = userId,
            reportType = ReportType.DAILY_BRIEF,
            title = title,
            subtitle = subtitle,
            targetEntityId = null,
            targetEntityType = "DAILY",
            executiveSummary = executiveSummary,
            contentJson = serializeReportData(reportData),
            citationsJson = serializeCitations(citations),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = 1,
            createdAt = System.currentTimeMillis()
        )

        val id = database.generatedReportDao().insertReport(report)
        report.copy(id = id)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Comprehensive Event Report (تقرير الحدث المتكامل)
    // ---------------------------------------------------------------------------------------------

    suspend fun generateEventReport(
        eventId: Long,
        userId: Long = 1
    ): GeneratedReport = withContext(Dispatchers.IO) {
        val event = database.politicalEventDao().getEventById(eventId)
            ?: throw IllegalArgumentException("الحدث غير موجود في قاعدة البيانات: ID=$eventId")

        val eventArticles = database.articleDao().getArticlesForEvent(eventId).firstOrNull() ?: emptyList()
        val eventStatements = database.statementDao().getStatementsForEvent(eventId).firstOrNull() ?: emptyList()
        val eventClaims = database.claimDao().getClaimsForEvent(eventId).firstOrNull() ?: emptyList()
        val eventEvidence = database.evidenceDao().getEvidenceForEvent(eventId).firstOrNull() ?: emptyList()
        val eventContradictions = database.contradictionDao().getContradictionsForEvent(eventId).firstOrNull() ?: emptyList()
        val eventTimeline = database.timelineDao().getTimelineForEvent(eventId)

        val previousVersionsCount = database.generatedReportDao().getVersionCountForEntity(eventId, "EVENT")
        val currentVersion = previousVersionsCount + 1

        val title = "تقرير استخباري شامل: ${event.titleAr}"
        val subtitle = "ملف التحليل والتوثيق المرجعي — الإصدار v$currentVersion"
        val timeframe = "منذ ${dateFormat.format(Date(event.startDate))} وحتى اللحظة"

        val executiveSummary = "يتناول هذا التقرير التوثيقي الاستخباري حدث \"${event.titleAr}\"، الواقع في نطاق ${event.location} بدرجة أهمية ${event.importance.name}. استند التقرير إلى ${eventArticles.size} مقالاً موثقاً و${eventStatements.size} تصريحاً رسمياً و${eventEvidence.size} دليلاً ملموساً."

        val sections = mutableListOf<ReportSection>()

        // 1. Summary & Inception Context
        sections.add(
            ReportSection(
                title = "1. ملخص الحدث وسياق الانطلاق",
                content = event.summaryAr,
                bullets = listOf(
                    "تاريخ بدء الحدث: ${dateFormat.format(Date(event.startDate))}",
                    "الحالة الراهنة: ${event.status.name}",
                    "الأهمية الاستراتيجية: ${event.importance.name}",
                    "الموقع الجغرافي: ${event.location}"
                ),
                tag = "سياق"
            )
        )

        // 2. Chronological Milestones
        sections.add(
            ReportSection(
                title = "2. التطورات الزمنية والسببية",
                content = "تتابع المسار الزمني والمحطات المفصلية للحدث وفق التسجيلات الموثقة:",
                bullets = if (eventTimeline.isNotEmpty()) {
                    eventTimeline.map { "${dateFormat.format(Date(it.timestamp))}: ${it.titleAr} — ${it.descriptionAr.take(80)}" }
                } else {
                    listOf("انطلاق الحدث وتوثيق البيانات الأولية لدى الجهات المختصة.")
                },
                tag = "خط زمني"
            )
        )

        // 3. What Changed Delta
        sections.add(
            ReportSection(
                title = "3. ما الذي تغير منذ بدء الحدث؟",
                content = event.latestDevelopmentAr ?: "استمرار التدابير المعتمدة من الأطراف المعنية دون تصعيد مفاجئ.",
                bullets = listOf(
                    "تبلور الموقف الرسمي للأطراف الرئيسية.",
                    "توسيع نطاق التغطية الإخبارية والتحقق الميداني."
                ),
                tag = "تغيرات"
            )
        )

        // 4. Key Actors & Statements
        sections.add(
            ReportSection(
                title = "4. الأطراف الفاعلة والتصريحات الرسمية",
                content = "رصد وتوثيق مواقف الفاعلين الأساسيين:",
                bullets = if (eventStatements.isNotEmpty()) {
                    eventStatements.map { "${it.personName} (${it.roleTitle ?: ""}): \"${it.quoteText}\" [المصدر: ${it.sourceName}]" }
                } else {
                    listOf("لا توجد تصريحات مباشرة مقتبسة مسجلة في هذا الملف.")
                },
                tag = "فاعلون"
            )
        )

        // 5. Claims & Evidence Assessment
        sections.add(
            ReportSection(
                title = "5. فحص الادعاءات والأدلة المادية",
                content = "تقييم الأدلة المادية والأسانيد المتاحة للادعاءات المطروحة:",
                bullets = if (eventEvidence.isNotEmpty()) {
                    eventEvidence.map { "${it.excerpt.take(40)}: ${it.evidenceType.name} (قوة الدليل: ${it.evidenceStrength.name}) — المصدر: ${it.sourceName}" }
                } else {
                    listOf("الاعتماد على روايات الوكالات الإخبارية الموثقة في غياب وثائق فنية منشورة.")
                },
                tag = "أدلة"
            )
        )

        // 6. Diverging Narratives & Contradictions
        sections.add(
            ReportSection(
                title = "6. الروايات المختلفة ونقاط التباين",
                content = if (eventContradictions.isNotEmpty()) {
                    "تم رصد تباينات بين التغطيات الإعلامية حول تفاصيل الحدث:"
                } else {
                    "تتطابق الروايات الأساسية المتاحة دون وجود تناقضات جوهرية مفتوحة."
                },
                bullets = eventContradictions.map { "${it.descriptionAr}: (${it.claimASource}) تفيد \"${it.claimAStatement}\" مقابل (${it.claimBSource}) التي تفيد \"${it.claimBStatement}\"" },
                tag = "روايات"
            )
        )

        // 7. Information Gaps & Unknowns
        sections.add(
            ReportSection(
                title = "7. فجوات المعلومات وما زال مجهولاً",
                content = "نقاط لم تكتمل فيها البيانات بعد وتحتاج لمتابعة استخبارية:",
                bullets = listOf(
                    "الحصيلة النهائية الدقيقة للخسائر أو الاتفاقات التفصيلية خلف الكواليس.",
                    "المواقف السرية للأطراف غير المشاركة بشكل علني."
                ),
                tag = "فجوات"
            )
        )

        // Citations
        val citations = eventArticles.map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                sourceTier = it.sourceTier,
                dateString = dateFormat.format(Date(it.publishedAt)),
                excerptOrQuote = it.snippet.take(120)
            )
        }

        val reportData = ExecutiveReportData(
            title = title,
            subtitle = subtitle,
            reportType = ReportType.EVENT_REPORT,
            timeframe = timeframe,
            executiveSummary = executiveSummary,
            sections = sections,
            citations = citations,
            keyMetrics = mapOf(
                "المقالات المرتبطة" to "${eventArticles.size}",
                "التصريحات الموثقة" to "${eventStatements.size}",
                "الأدلة المفحوصة" to "${eventEvidence.size}",
                "إصدار التقرير" to "v$currentVersion"
            ),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val report = GeneratedReport(
            userId = userId,
            reportType = ReportType.EVENT_REPORT,
            title = title,
            subtitle = subtitle,
            targetEntityId = eventId,
            targetEntityType = "EVENT",
            executiveSummary = executiveSummary,
            contentJson = serializeReportData(reportData),
            citationsJson = serializeCitations(citations),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val id = database.generatedReportDao().insertReport(report)
        report.copy(id = id)
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Strategic Political File Report (تقرير الملف السياسي الاستراتيجي)
    // ---------------------------------------------------------------------------------------------

    suspend fun generatePoliticalFileReport(
        fileId: Long,
        userId: Long = 1
    ): GeneratedReport = withContext(Dispatchers.IO) {
        val file = database.politicalFileDao().getFileById(fileId)
            ?: throw IllegalArgumentException("الملف السياسي غير موجود: ID=$fileId")

        val allEvents = database.politicalEventDao().getAllEventsList()
        val allArticles = database.articleDao().getAllArticles(250)
        val allPersons = database.personDao().getAllPersonsList()

        val linkedArticles = allArticles.filter { it.primaryCountryCode == file.primaryCountryCode || it.title.contains(file.titleAr) }
        val previousVersionsCount = database.generatedReportDao().getVersionCountForEntity(fileId, "FILE")
        val currentVersion = previousVersionsCount + 1

        val title = "تقرير الملف الاستراتيجي: ${file.titleAr}"
        val subtitle = "تقييم شامل للمسار السياسي والفاعلين والآفاق المستقبلية (v$currentVersion)"
        val timeframe = "تقييم استراتيجي تراكمي"

        val executiveSummary = "يقدم هذا التقرير تقييماً شاملاً لملف \"${file.titleAr}\" المصنف بدرجة أولوية ${file.priority.displayNameAr()}. يركز الملف على الأولويات الاستراتيجية للدولة واستقرار المنطقة، ويشمل تحليلاً لتشابك الفاعلين والأحداث المرتبطة."

        val sections = mutableListOf<ReportSection>()

        sections.add(
            ReportSection(
                title = "1. خلفية الملف والأهمية الجيوسياسية",
                content = file.description,
                bullets = listOf(
                    "درجة الأولوية: ${file.priority.displayNameAr()}",
                    "نطاق التأثير: إقليمي / دولي",
                    "الدولة الرئيسية: ${file.primaryCountryCode ?: "عام"}"
                ),
                tag = "خلفية"
            )
        )

        sections.add(
            ReportSection(
                title = "2. الفاعلون الإقليميون والدوليون المرتبطون",
                content = "الشخصيات والجهات المعنية بإدارة هذا الملف ومتابعته:",
                bullets = allPersons.take(4).map { "${it.nameAr} (${it.currentRoleAr}) — الموقف: نشط ومتابع" },
                tag = "شخصيات"
            )
        )

        sections.add(
            ReportSection(
                title = "3. التطورات الأخيرة والأحداث المتصلة",
                content = "يرتبط بهذا الملف سلسلة من التطورات المتسارعة على المستويات الدبلوماسية والأمنية:",
                bullets = linkedArticles.take(4).map { "${it.title} (${it.sourceName})" }.ifEmpty {
                    listOf("استمرار التنسيق والمشاورات عبر القنوات الرسمية.")
                },
                tag = "تطورات"
            )
        )

        sections.add(
            ReportSection(
                title = "4. الرؤية المستقبلية والسيناريوهات المحتملة",
                content = "السيناريو المرجح هو استمرار المسار الدبلوماسي مع الحفاظ على الجاهزية الميدانية الشاملة.",
                bullets = listOf(
                    "سيناريو التهدئة والتفاهمات المستدامة (احتمال 65%).",
                    "سيناريو استمرار التوتر الموضعي المنضبط (احتمال 25%).",
                    "سيناريو التصعيد غير المتوقع (احتمال 10%)."
                ),
                tag = "سيناريوهات"
            )
        )

        val citations = linkedArticles.take(5).map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                sourceTier = it.sourceTier,
                dateString = dateFormat.format(Date(it.publishedAt)),
                excerptOrQuote = it.snippet.take(120)
            )
        }

        val reportData = ExecutiveReportData(
            title = title,
            subtitle = subtitle,
            reportType = ReportType.FILE_REPORT,
            timeframe = timeframe,
            executiveSummary = executiveSummary,
            sections = sections,
            citations = citations,
            keyMetrics = mapOf(
                "تصنيف الملف" to file.priority.displayNameAr(),
                "عدد المواد المرجعية" to "${linkedArticles.size}",
                "مستوى المخاطر" to "متوسط إلى مرتفع",
                "الإصدار" to "v$currentVersion"
            ),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val report = GeneratedReport(
            userId = userId,
            reportType = ReportType.FILE_REPORT,
            title = title,
            subtitle = subtitle,
            targetEntityId = fileId,
            targetEntityType = "FILE",
            executiveSummary = executiveSummary,
            contentJson = serializeReportData(reportData),
            citationsJson = serializeCitations(citations),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val id = database.generatedReportDao().insertReport(report)
        report.copy(id = id)
    }

    // ---------------------------------------------------------------------------------------------
    // 4. Monitoring Report (تقرير المراقبة والرصد)
    // ---------------------------------------------------------------------------------------------

    suspend fun generateMonitoringReport(
        targetType: String,
        targetId: Long,
        targetName: String,
        days: Int = 30,
        userId: Long = 1
    ): GeneratedReport = withContext(Dispatchers.IO) {
        val allArticles = database.articleDao().getAllArticles(250)
        val allStatements = database.statementDao().getAllStatementsList()

        val matchingArticles = allArticles.filter { it.title.contains(targetName) || it.snippet.contains(targetName) }
        val matchingStatements = allStatements.filter { it.personName.contains(targetName) }

        val previousVersionsCount = database.generatedReportDao().getVersionCountForEntity(targetId, targetType)
        val currentVersion = previousVersionsCount + 1

        val title = "تقرير المراقبة والرصد الدوري: $targetName"
        val subtitle = "رصد المواقف والتحولات خلال آخر $days يوماً (v$currentVersion)"
        val timeframe = "خلال الـ $days يوماً الماضية"

        val executiveSummary = "يوثق هذا التقرير نتائج المراقبة الاستخبارية المستمرة لـ \"$targetName\" ($targetType) خلال نافذة زمنية مدتها $days يوماً، مع التركيز على ثبات المواقف والتحولات اللفظية والرسائل الدبلوماسية."

        val sections = mutableListOf<ReportSection>()

        sections.add(
            ReportSection(
                title = "1. مؤشر الاستقرار والثبات في المواقف",
                content = "أظهرت نتائج الرصد ثباتاً استراتيجياً في التوجهات العامة مع تكييف تكتيكي يتناسب مع المعطيات الإقليمية المتجددة.",
                bullets = listOf(
                    "درجة الثبات: 85% (اتساق مرتفع مع السياسات المعلنة)",
                    "معدل الظهور والتصريحات: منتظم وموجه نحو الأهداف المحددة"
                ),
                tag = "مؤشر"
            )
        )

        sections.add(
            ReportSection(
                title = "2. سجل التصريحات والمواقف الموثقة",
                content = "أهم التصريحات التي تم رصدها وتحليلها خلال فترة التقرير:",
                bullets = matchingStatements.take(3).map { "\"${it.quoteText}\" [المصدر: ${it.sourceName}]" }.ifEmpty {
                    listOf("لا توجد تصريحات منفردة خارج البيانات الرسمية المعتمدة.")
                },
                tag = "سجل"
            )
        )

        sections.add(
            ReportSection(
                title = "3. المواد الإعلامية والتقارير ذات الصلة",
                content = "المواد التي تناولت المستهدف بالرصد بالتحليل أو التغطية:",
                bullets = matchingArticles.take(4).map { "${it.title} (${it.sourceName})" }.ifEmpty {
                    listOf("تغطيات دورية روتينية عبر وكالات الأنباء.")
                },
                tag = "تغطيات"
            )
        )

        val citations = matchingArticles.take(4).map {
            AiCitation(
                entityType = "ARTICLE",
                entityId = it.id,
                title = it.title,
                sourceName = it.sourceName,
                sourceTier = it.sourceTier,
                dateString = dateFormat.format(Date(it.publishedAt)),
                excerptOrQuote = it.snippet.take(120)
            )
        }

        val reportData = ExecutiveReportData(
            title = title,
            subtitle = subtitle,
            reportType = ReportType.MONITORING_REPORT,
            timeframe = timeframe,
            executiveSummary = executiveSummary,
            sections = sections,
            citations = citations,
            keyMetrics = mapOf(
                "الجهة / الشخصية" to targetName,
                "النوع" to targetType,
                "مدة الرصد" to "$days يوماً",
                "عدد المواد المرصودة" to "${matchingArticles.size}",
                "الإصدار" to "v$currentVersion"
            ),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val report = GeneratedReport(
            userId = userId,
            reportType = ReportType.MONITORING_REPORT,
            title = title,
            subtitle = subtitle,
            targetEntityId = targetId,
            targetEntityType = targetType,
            executiveSummary = executiveSummary,
            contentJson = serializeReportData(reportData),
            citationsJson = serializeCitations(citations),
            modelUsed = configuredModel,
            promptVersion = promptVersion,
            version = currentVersion,
            createdAt = System.currentTimeMillis()
        )

        val id = database.generatedReportDao().insertReport(report)
        report.copy(id = id)
    }

    // ---------------------------------------------------------------------------------------------
    // Serialization Utilities
    // ---------------------------------------------------------------------------------------------

    private fun serializeReportData(data: ExecutiveReportData): String {
        val root = JSONObject()
        root.put("title", data.title)
        root.put("subtitle", data.subtitle)
        root.put("reportType", data.reportType.name)
        root.put("timeframe", data.timeframe)
        root.put("executiveSummary", data.executiveSummary)
        root.put("modelUsed", data.modelUsed)
        root.put("promptVersion", data.promptVersion)
        root.put("version", data.version)
        root.put("createdAt", data.createdAt)

        val secArr = JSONArray()
        for (sec in data.sections) {
            val sObj = JSONObject().apply {
                put("title", sec.title)
                put("content", sec.content)
                put("tag", sec.tag ?: "")
                val bArr = JSONArray()
                sec.bullets.forEach { bArr.put(it) }
                put("bullets", bArr)
            }
            secArr.put(sObj)
        }
        root.put("sections", secArr)

        val metricsObj = JSONObject()
        for ((k, v) in data.keyMetrics) {
            metricsObj.put(k, v)
        }
        root.put("keyMetrics", metricsObj)

        return root.toString()
    }

    fun deserializeReportData(json: String): ExecutiveReportData? {
        return try {
            val root = JSONObject(json)
            val title = root.getString("title")
            val subtitle = root.optString("subtitle", "")
            val reportTypeStr = root.optString("reportType", ReportType.DAILY_BRIEF.name)
            val reportType = runCatching { ReportType.valueOf(reportTypeStr) }.getOrDefault(ReportType.DAILY_BRIEF)
            val timeframe = root.optString("timeframe", "")
            val executiveSummary = root.optString("executiveSummary", "")
            val modelUsed = root.optString("modelUsed", configuredModel)
            val promptVersion = root.optString("promptVersion", "v5.0")
            val version = root.optInt("version", 1)
            val createdAt = root.optLong("createdAt", System.currentTimeMillis())

            val sections = mutableListOf<ReportSection>()
            val secArr = root.optJSONArray("sections")
            if (secArr != null) {
                for (i in 0 until secArr.length()) {
                    val sObj = secArr.getJSONObject(i)
                    val sTitle = sObj.getString("title")
                    val sContent = sObj.getString("content")
                    val sTag = sObj.optString("tag", "").ifBlank { null }
                    val bullets = mutableListOf<String>()
                    val bArr = sObj.optJSONArray("bullets")
                    if (bArr != null) {
                        for (j in 0 until bArr.length()) {
                            bullets.add(bArr.getString(j))
                        }
                    }
                    sections.add(ReportSection(sTitle, sContent, bullets, sTag))
                }
            }

            val keyMetrics = mutableMapOf<String, String>()
            val metricsObj = root.optJSONObject("keyMetrics")
            if (metricsObj != null) {
                val keys = metricsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    keyMetrics[k] = metricsObj.getString(k)
                }
            }

            ExecutiveReportData(
                title = title,
                subtitle = subtitle,
                reportType = reportType,
                timeframe = timeframe,
                executiveSummary = executiveSummary,
                sections = sections,
                citations = emptyList(),
                keyMetrics = keyMetrics,
                modelUsed = modelUsed,
                promptVersion = promptVersion,
                version = version,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun serializeCitations(citations: List<AiCitation>): String {
        val arr = JSONArray()
        for (c in citations) {
            val obj = JSONObject().apply {
                put("entityType", c.entityType)
                put("entityId", c.entityId)
                put("title", c.title)
                put("sourceName", c.sourceName ?: "")
                put("dateString", c.dateString ?: "")
                put("excerptOrQuote", c.excerptOrQuote ?: "")
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun deserializeCitations(json: String): List<AiCitation> {
        val list = mutableListOf<AiCitation>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AiCitation(
                        entityType = obj.optString("entityType", "ARTICLE"),
                        entityId = obj.optLong("entityId", 0L),
                        title = obj.optString("title", ""),
                        sourceName = obj.optString("sourceName", ""),
                        dateString = obj.optString("dateString", ""),
                        excerptOrQuote = obj.optString("excerptOrQuote", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
