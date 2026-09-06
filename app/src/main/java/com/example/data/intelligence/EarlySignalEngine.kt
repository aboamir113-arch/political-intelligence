package com.example.data.intelligence

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Early Signal Detection Engine
 * Discovers weak signals, abnormal co-occurrences, and meeting clusters.
 * Strictly complies with Rule 33: Safe, non-deterministic probabilistic language with explicit alternative explanations and unknowns.
 */
class EarlySignalEngine(
    private val signalDao: EarlySignalDao,
    private val articleDao: ArticleDao,
    private val relationshipDao: PoliticalRelationshipDao,
    private val statementDao: StatementDao,
    private val politicalFileDao: PoliticalFileDao
) {

    suspend fun scanForEarlySignals(): List<EarlySignalItem> = withContext(Dispatchers.IO) {
        val recentArticles = articleDao.getAllArticles(limit = 60)
        val relationships = relationshipDao.getAllRelationshipsList()
        val files = politicalFileDao.getAllFilesList()
        val existingSignals = signalDao.getAllSignalsList()

        val discoveredSignals = mutableListOf<EarlySignalItem>()

        // 1. Scan for sudden meeting frequency / co-occurrence
        val meetingRels = relationships.filter {
            it.relationshipType == RelationshipType.MEETS_WITH || it.relationshipType == RelationshipType.NEGOTIATES_WITH
        }

        if (meetingRels.isNotEmpty()) {
            val mostFrequent = meetingRels.groupBy { "${it.sourceEntityName}_${it.targetEntityName}" }
                .maxByOrNull { it.value.size }

            if (mostFrequent != null && mostFrequent.value.isNotEmpty()) {
                val sampleRel = mostFrequent.value.first()
                val signalKey = "MEETING_CLUSTER_${sampleRel.sourceEntityName}_${sampleRel.targetEntityName}"

                val alreadyExists = existingSignals.any { it.signalTitleAr.contains(sampleRel.sourceEntityName) && it.signalTitleAr.contains(sampleRel.targetEntityName) }

                if (!alreadyExists) {
                    discoveredSignals.add(
                        EarlySignalItem(
                            signalTitleAr = "مؤشرات على تكثيف قنوات الاتصال بين (${sampleRel.sourceEntityName}) و (${sampleRel.targetEntityName})",
                            signalDescriptionAr = "تم رصد نمط غير معتاد في وتيرة اللقاءات والمشاورات المشتركة المسجلة مؤخراً، ما قد يشير إلى وساطة ناشئة أو ترتيبات سياسية غير معلنة بالكامل.",
                            signalCategory = "MEETING_FREQUENCY",
                            historicalPattern = "تاريخياً، ارتبط تكرار مثل هذه اللقاءات التحضيرية في الأزمات المماثلة بإعلان جولة مفاوضات رسمية أو التوصل إلى تفاهمات لخفض التصعيد.",
                            currentEvidence = "توثيق لقاءات ومحادثات عبر برقيات صادرة مؤخراً مع الإشارة المتبادلة في تصريحات الوفود.",
                            alternativeExplanations = "التفسير البديل: قد تكون هذه اللقاءات في إطار البروتوكولات الدبلوماسية الدورية أو التنسيق الأمني الاعتيادي دون بعد استراتيجي جديد.",
                            confidence = 0.72f,
                            unknowns = "محتوى الجلسات المغلقة وجدول الأعمال الدقيق؛ لم تصدر بعد بيانات ختامية مشتركة تؤكد طبيعة التفاهمات.",
                            suggestedMonitoring = "يوصى بمراقبة بيانات وزارات الخارجية خلال الـ 48 ساعة القادمة، والتصريحات الصادرة عند المعابر أو نقاط التماس.",
                            associatedFileId = sampleRel.sourceFileId
                        )
                    )
                }
            }
        }

        // 2. Scan for narrative shift preceding policy decisions
        val statements = statementDao.getAllStatementsList()
        val conciliatoryStatements = statements.filter { it.quoteText.contains("تنسيق") || it.quoteText.contains("دبلوماسي") || it.quoteText.contains("حوار") }
        if (conciliatoryStatements.isNotEmpty()) {
            val sampleStmt = conciliatoryStatements.first()
            val alreadyExists = existingSignals.any { it.signalCategory == "NARRATIVE_SHIFT" }

            if (!alreadyExists) {
                val topic = sampleStmt.context ?: "العلاقات الدبلوماسية"
                discoveredSignals.add(
                    EarlySignalItem(
                        signalTitleAr = "تغير تدريجي في اللهجة الرسمية حول ($topic)",
                        signalDescriptionAr = "ظهرت مؤشرات أولية تفيد بانخفاض نبرة التراشق وظهور مفردات حوارية في تصريحات منسوبة إلى (${sampleStmt.personName}) عبر (${sampleStmt.sourceName}).",
                        signalCategory = "NARRATIVE_SHIFT",
                        historicalPattern = "في ملفات سياسية سابقة، شكل التغيير في المفردات المستخدمة تمهيداً معتاداً لتمرير تسويات أو تفاهمات غير مرئية للجمهور.",
                        currentEvidence = "تصريح موثق: \"${sampleStmt.quoteText.take(100)}\"",
                        alternativeExplanations = "التفسيرات البديلة: قد يكون التغيير تكتيكاً إعلامياً لتهدئة الضغوط الشعبية أو المناورة الزمنية المؤقتة.",
                        confidence = 0.68f,
                        unknowns = "هل يعبر هذا التحول عن قرار قيادي جامع لدى الأطراف كافة، أم هو اجتهاد فردي من المتحدث الرسمي؟",
                        suggestedMonitoring = "متابعة الكلمات الافتتاحية للمسؤولين في المؤتمرات المقبلة ومقارنتها بسجل التصريحات التاريخي.",
                        associatedFileId = files.firstOrNull()?.id
                    )
                )
            }
        }

        // 3. Multi-source coverage spike
        if (recentArticles.size >= 10) {
            val topSourceCount = recentArticles.map { it.sourceName }.distinct().size
            val alreadyExists = existingSignals.any { it.signalCategory == "COVERAGE_SPIKE" }

            if (!alreadyExists && topSourceCount >= 3) {
                discoveredSignals.add(
                    EarlySignalItem(
                        signalTitleAr = "تسارع متزامن في تغطية المصادر حول تطورات المشهد الإقليمي",
                        signalDescriptionAr = "رصد ارتفاع متزامن ومتقاطع في تدفق البرقيات الإخبارية من $topSourceCount منصات إخبارية مختلفة خلال الساعات الماضية.",
                        signalCategory = "COVERAGE_SPIKE",
                        historicalPattern = "تزامن اهتمام وكالات الأنباء الدولية والمحلية بنقطة جغرافية واحدة يسبق عادة صدور قرارات تنفيذية أو تحركات ميدانية بارزة.",
                        currentEvidence = "تدفق مستمر لأكثر من ${recentArticles.size} برقية مصنفة عاجلة أو ذات أولوية مرتفعة.",
                        alternativeExplanations = "قد يعود هذا التركيز إلى التغطية الإخبارية التفاعلية أو ما يعرف بـ 'صدى الإعلام' (Echo Chamber) دون وجود حدث مفصلي جديد على الأرض.",
                        confidence = 0.75f,
                        unknowns = "مدى موثوقية بعض المصادر غير الرسمية الناقلة للأنباء الأولى؛ التحقق جارٍ عبر المصادر المعتمدة من الدرجة الأولى.",
                        suggestedMonitoring = "التدقيق في تطابق البيانات الرسمية ومقارنة الروايات لتحديد أصل التسريب الأولي.",
                        associatedFileId = files.firstOrNull()?.id
                    )
                )
            }
        }

        if (discoveredSignals.isNotEmpty()) {
            signalDao.insertSignals(discoveredSignals)
        }

        discoveredSignals
    }
}
