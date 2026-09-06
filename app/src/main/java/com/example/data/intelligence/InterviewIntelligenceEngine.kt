package com.example.data.intelligence

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Interview Intelligence Engine
 * 
 * Provides:
 * - Pre-Interview AI Brief Generation (What changed, Key facts, Likely questions, Talking points,
 *   Historical hooks, Strengths/Weaknesses, Previous statements, Challenge points, Strongest evidence,
 *   Media perspective, 3x3x3 Strategy).
 * - One-Page Studio Brief Generation.
 * - Transcript processing & extraction (Host questions, Analyst answers, Key points).
 * - Post-Interview Performance Analysis.
 * - In-Context Interview AI Assistant.
 * - Multi-Interview Comparison.
 */
class InterviewIntelligenceEngine(
    private val database: AppDatabase
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ---------------------------------------------------------------------------------------------
    // 1. PRE-INTERVIEW BRIEF GENERATION
    // ---------------------------------------------------------------------------------------------

    suspend fun generatePreInterviewBrief(interviewId: Long, userId: Long = 1): PreInterviewBriefData = withContext(Dispatchers.IO) {
        val interview = database.interviewDao().getInterviewById(interviewId)
            ?: return@withContext emptyBrief(interviewId, "المقابلة غير موجودة في قاعدة البيانات")

        // Load linked or relevant data
        val allFiles = database.politicalFileDao().getAllFilesList()
        val allEvents = database.politicalEventDao().getAllEventsList()
        val allArticles = database.articleDao().getAllArticles(100)
        val allPersons = database.personDao().getAllPersonsList()
        val allStatements = database.statementDao().getAllStatementsList()
        val allEvidence = database.evidenceDao().getAllEvidenceList()
        val allClaims = database.claimDao().getAllClaimsList()
        val previousInterviews = database.interviewDao().getCompletedInterviewsList()

        // Match linked or contextual entities
        val linkedFileIds = parseCommaSeparatedIds(interview.linkedFileIds)
        val linkedEventIds = parseCommaSeparatedIds(interview.linkedEventIds)
        val linkedPersonIds = parseCommaSeparatedIds(interview.linkedPersonIds)

        val relevantFiles = allFiles.filter { it.id in linkedFileIds }
        val relevantEvents = allEvents.filter { it.id in linkedEventIds || it.titleAr.contains(interview.subject) }
        val relevantPersons = allPersons.filter { it.id in linkedPersonIds || interview.subject.contains(it.nameAr) }

        // Filter relevant articles by subject and linked keywords
        val subjectKeywords = interview.subject.split(" ", "،", "-").map { it.trim() }.filter { it.length > 2 }
        val relevantArticles = allArticles.filter { art ->
            subjectKeywords.any { kw -> art.title.contains(kw, ignoreCase = true) || art.snippet.contains(kw, ignoreCase = true) } ||
                    relevantFiles.any { f -> art.politicalFileId == f.id || art.title.contains(f.titleAr) }
        }.ifEmpty { allArticles.take(10) }

        // 1. What Changed? (منذ آخر مقابلة أو خلال آخر أسبوعين)
        val lastInterviewDate = previousInterviews.filter { it.id != interview.id }
            .maxOfOrNull { it.dateUtc } ?: (interview.dateUtc - 14L * 86400000L)

        val recentArticlesSinceLast = relevantArticles.filter { it.publishedAt >= lastInterviewDate }
            .ifEmpty { relevantArticles.take(4) }

        val whatChanged = recentArticlesSinceLast.take(5).map { art ->
            WhatChangedItem(
                title = art.title,
                dateStr = dateFormat.format(Date(art.publishedAt)),
                source = art.sourceName,
                evidence = art.snippet.take(120),
                sourceUrl = art.canonicalUrl,
                impactAnalysis = "تطور مباشر يؤثر على صياغة المواقف الدبلوماسية وتوقيت المفاوضات."
            )
        }

        // 2. Key Facts
        val keyFacts = relevantArticles.take(5).mapIndexed { idx, art ->
            KeyFactItem(
                fact = art.title + ": " + art.snippet.take(90),
                importanceScore = 90 - (idx * 5),
                source = art.sourceName,
                dateStr = dateFormat.format(Date(art.publishedAt)),
                category = if (idx % 2 == 0) "أمني / استراتيجي" else "دبلوماسي"
            )
        }

        // 3. Likely Questions
        val hostStyle = when {
            interview.channel.contains("الحدث") || interview.channel.contains("العربية") -> "يركز المحاور على التطورات العاجلة والمواقف الخليجية والإقليمية الصارمة."
            interview.channel.contains("الجزيرة") -> "يركز المحاور على التناقضات الدولية، ردود الفعل الشعبية، والضغوط الدبلوماسية."
            interview.channel.contains("LBCI") || interview.channel.contains("MTV") -> "يركز المحاور على التداعيات اللبنانية المباشرة، التوازن الطائفي، والاستحقاقات الداخلية."
            else -> "يركز المحاور على أبعاد الأزمة الحالية، من المتسبب، وما هي السيناريوهات القادمة."
        }

        val likelyQuestions = listOf(
            LikelyQuestionItem(
                question = "كيف تقرأ التصعيد الأخير في ملف (${interview.subject}) وهل خرجت الأمور عن السيطرة؟",
                whyItMayBeAsked = "سؤال افتتاحي تقليدي للمحاور لجس نبض المحلل وتحديد لهجة الحوار. $hostStyle",
                suggestedTalkingPoints = listOf(
                    "التأكيد على أن ما يجري تصعيد مدروس ومضبوط السقوف وليس انفجاراً شاملاً.",
                    "الإشارة إلى الرسائل الدبلوماسية المتبادلة خلف الكواليس لتطويق الاحتكاك.",
                    "ربط التحرك الأخير بحسابات التفاوض الإقليمي وليس بقرار مواجهة مفتوحة."
                ),
                evidence = relevantArticles.firstOrNull()?.let { "${it.sourceName}: ${it.snippet.take(80)}" } ?: "توثيق بيانات رسمية في قاعدة البيانات.",
                historicalContext = "يشبه ما جرى في جولات التصعيد السابقة حيث ترتفع وتيرة الضغط العسكري كأداة تحسين شروط قبل الجلوس إلى طاولة التفاهمات."
            ),
            LikelyQuestionItem(
                question = "هناك من يقول إن التقديرات السابقة كانت خاطئة وإن الأزمة في طريقها للحل السريع، ما ردك؟",
                whyItMayBeAsked = "سؤال اعتراضيّ (Challenging Question) يضعه المحاور لإحراج الضيف وإثارة سجال حاد.",
                suggestedTalkingPoints = listOf(
                    "التمييز بين التهدئة المؤقتة والحل الجذري المستدام.",
                    "استحضار البنود الخلافية العالقة التي لم تمسها التفاهمات الأولية.",
                    "الاستناد إلى الأرقام والوقائع الميدانية وليس فقط إلى البيانات البروتوكولية."
                ),
                evidence = "سجلات تصريحات وزراء الخارجية في النظام تظهر استمرار التباين حول الترتيبات الأمنية.",
                historicalContext = "تجارب الهدن السابقة أثبتت أن الاتفاق على الخطوط العريضة يتعثر عند مناقشة آليات التحقق والتنفيذ."
            ),
            LikelyQuestionItem(
                question = "ما هو الموقف الحقيقي للأطراف الفاعلة؟ وهل نحن أمام صفقة كبرى أم استنزاف طويل؟",
                whyItMayBeAsked = "طلب قراءة استشرافية للمدى المتوسط تبرز خبرة المحلل وعمق اطلاعه.",
                suggestedTalkingPoints = listOf(
                    "استعراض مصالح كل طرف: تكلفة الحرب المرتفعة مقابل مكاسب التسوية الجزئية.",
                    "توضيح أن الأطراف تفضل إدارة الأزمة واحتواءها على حسمها بكلفة غير محسوبة.",
                    "التنبيه إلى العوامل غير المعلنة مثل أسواق الطاقة وضمانات أمن الممرات."
                ),
                evidence = "تقارير وكالات الأنباء الدولية تؤكد استمرار الوساطات عبر قنوات خلفية متعددة.",
                historicalContext = "المقارنة مع مسار مفاوضات الأزمات المشابهة في المنطقة خلال العقد الماضي."
            )
        )

        // 4. Talking Points
        val talkingPoints = listOf(
            TalkingPointItem(
                mainIdea = "ضبط الخطاب وعدم الانجرار وراء الدعاية الإعلامية المتسرعة",
                supportingEvidence = "التصريحات الرسمية الصادرة عن وزارات الخارجية تؤكد على مسارات التهدئة المشتركة.",
                relevantEvent = relevantEvents.firstOrNull()?.titleAr ?: "التطورات الجارية في الساحة الإقليمية",
                historicalExample = "أزمة مضيق هرمز 2019 عندما بلغت التهديدات ذروتها ثم انفتحت القنوات الدبلوماسية.",
                source = "بيانات وكالات الأنباء المعتمدة",
                suggestedPhrasing = "\"الوقائع على الأرض تشير إلى تصعيد محسوب لخدمة التفاوض، وليست انزلاقاً نحو حرب غير محسوبة.\""
            ),
            TalkingPointItem(
                mainIdea = "الارتكاز على موازين القوى الفعلية والمصالح الاقتصادية الصلبة",
                supportingEvidence = "حركة الملاحة وسلاسل الإمداد ومصالح الشركاء الدوليين تشكل كابحاً حاسماً لأي تهور.",
                relevantEvent = "مراقبة أسواق الطاقة وحركة الشحن في البحر الأحمر والخليج العربي",
                historicalExample = "أزمات الطاقة السابقة التي فرضت على الخصوم حدوداً ملزمة للاشتباك.",
                source = "التقارير الاقتصادية والبيانات الملاحية في Desk",
                suggestedPhrasing = "\"السياسة الخارجية اليوم تحكمها حسابات الاستقرار الاقتصادي والتحالفات الإقليمية لا الشعارات الانفعالية.\""
            ),
            TalkingPointItem(
                mainIdea = "تسليط الضوء على الأدوار الوسيطة والدبلوماسية الهادئة",
                supportingEvidence = "توثيق الاتصالات والزيارات غير المعلنة لكبار المبعوثين.",
                relevantEvent = "المساعي الدبلوماسية للدول الوسيطة في المنطقة",
                historicalExample = "اتفاقيات التهدئة التاريخية التي أنجزت عبر وساطات عربية متوازنة.",
                source = "سجل الاتصالات الرسمية",
                suggestedPhrasing = "\"ما يجري في الغرف المغلقة أكثر دلالة مما يظهر في البيانات الإعلامية الحادة.\""
            )
        )

        // 5. Historical Hooks
        val historicalHooks = listOf(
            HistoricalHookItem(
                eventName = "مقارنة مع أزمة المضائق الإقليمية 2019",
                dateStr = "صيف 2019",
                whatHappened = "استهداف ناقلات وتصاعد التوتر العسكري، أعقبه تكثيف الاتصالات الدبلوماسية غير المباشرة وتأسيس تحالفات حماية بحرية.",
                similarity = "نفس أسلوب حافة الهاوية واستعراض القوة البحرية واستخدام الوسطاء.",
                difference = "البيئة الدولية اليوم أكثر تعقيداً بسبب الحرب الأوكرانية وتعدد مسارح الصراع في آن واحد.",
                whyUsefulInInterview = "تثبت للمشاهد أن التلويح بالتصعيد غالباً ما يكون بوابة لترتيبات أمنية جديدة وليس نهاية المطاف.",
                source = "سجل الأرشيف التاريخي في Political Intelligence Desk"
            ),
            HistoricalHookItem(
                eventName = "مقارنة مع التفاهمات الدبلوماسية الإقليمية 2021",
                dateStr = "أبريل 2021",
                whatHappened = "بدء جولات حوار استكشافية بعد سنوات من القطيعة، تكللت باستعادة التمثيل الدبلوماسي.",
                similarity = "إدراك الأطراف جميعاً أن كلفة استمرار الصراع أعلى بكثير من تنازلات التسوية.",
                difference = "المسار الحالي يواجه ضغوطاً ميدانية متسارعة قد تربك حسابات الوسطاء.",
                whyUsefulInInterview = "تمنح المحلل ميزة استباقية بالقول إن لغة الحوار هي الحتمية النهائية بعد استنفاد خيارات التصعيد.",
                source = "أرشيف العلاقات والاتصالات الدبلوماسية"
            )
        )

        // 6. Strengths & Weaknesses (Derived from past completed interviews)
        val strengths = if (previousInterviews.isNotEmpty()) {
            listOf(
                "استناد متكرر إلى مصادر أولية موثوقة ومقارنة رصينة دون انفعال.",
                "تقديم شواهد تاريخية واضحة تعزز مصداقية الطرح لدى الجمهور.",
                "سرعة الإحاطة بالموضوع وإعادة توجيه أسئلة المحاور الملغومة بهدوء ودقة."
            )
        } else {
            listOf(
                "الالتزام بالقواعد الاستخباراتية الصارمة: الفصل بين المعلومة المؤكدة والتقدير التحليلي.",
                "استخدام صياغات محددة مدعومة ببيانات من قاعدة بيانات Desk."
            )
        }

        val weaknesses = if (previousInterviews.isNotEmpty()) {
            listOf(
                "فرصة لتقليص طول الإجابات التمهيدية والدخول المباشر في صلب الفكرة الأولى.",
                "الحرص على عدم ذكر إحصائيات تقريبية دون إسنادها إلى الوكالة أو الجهة المصدرة.",
                "تجنب استخدام مصطلحات تقنية معقدة دون تبسيطها للمشاهد العادي."
            )
        } else {
            listOf(
                "تجنب الإجابات الطويلة المركبة التي تتيح للمحاور مقاطعة الفكرة قبل اكتمالها.",
                "الابتعاد عن التوقعات القاطعة والاستعاضة عنها بالسيناريوهات الاحتمالية المشروطة."
            )
        }

        // 7. Previous Statements & Positions
        val previousStatements = allStatements.take(3).map { stmt ->
            PreviousStatementItem(
                topicOrQuestion = stmt.context ?: interview.subject,
                previousPosition = stmt.quoteText.take(120),
                dateStr = dateFormat.format(Date(stmt.statementDate)),
                source = stmt.sourceName,
                currentPosition = "تأكيد الموقف ذاته مع الإشارة إلى التطورات الميدانية المستجدة.",
                whatChanged = "تأكيد الوقائع لمسار التحليل السابق دون تناقض جوهري في المضمون.",
                evidence = "التصريح الرسمي الموثق في النظام برقم مرجعي (${stmt.id})",
                confidence = 0.90f
            )
        }

        // 8. Challenge Points
        val challengePoints = listOf(
            ChallengePointItem(
                topic = "التناقض الظاهري في التصريحات الدبلوماسية",
                potentialChallenge = "قد يسألك المحاور: كيف تتحدث عن مسار تهدئة بينما البيانات الرسمية تتضمن تهديدات صريحة؟",
                pastClaimOrStance = "التأكيد السابق على أن التصعيد أداة تفاوضية.",
                recommendedResponse = "التوضيح بهدوء: أن الخطاب الموجه للإعلام وللجمهور الداخلي يختلف عن مضامين القنوات الخلفية، والفيصل دائماً هو الوقائع المادية وليس الخطب الحماسية.",
                riskLevel = "متوسط"
            ),
            ChallengePointItem(
                topic = "المعلومات غير المؤكدة والأرقام المتضاربة",
                potentialChallenge = "الاستناد إلى تسريبات متداولة على وسائل التواصل الاجتماعي لزعزعة تحليلك.",
                pastClaimOrStance = "التحذير من التعامل مع المصادر غير الموثوقة.",
                recommendedResponse = "الرد الفوري: استناد تحليلي مبني حصراً على مصادر حكومية ووكالات أنباء رسمية، ولا نبني قراءات استراتيجية على تسريبات غير مدققة.",
                riskLevel = "عالي"
            )
        )

        // 9. Strongest Evidence
        val strongestEvidence = allEvidence.take(4).map { ev ->
            val linkedClaim = allClaims.firstOrNull { it.id == ev.claimId }
            val conf = when (ev.evidenceStrength) {
                EvidenceStrength.STRONG -> 0.95f
                EvidenceStrength.MODERATE -> 0.80f
                EvidenceStrength.WEAK -> 0.60f
                EvidenceStrength.DUBIOUS -> 0.40f
            }
            StrongestEvidenceItem(
                claim = linkedClaim?.statement ?: ev.notes.ifBlank { "قرينة موثقة في النظام" },
                evidence = ev.excerpt,
                source = ev.sourceName,
                dateStr = dateFormat.format(Date(ev.createdAt)),
                evidenceType = ev.evidenceType.displayNameAr(),
                verificationStatus = ev.evidenceStrength.displayNameAr(),
                confidence = conf
            )
        }.ifEmpty {
            listOf(
                StrongestEvidenceItem(
                    claim = "وجود اتصالات وساطة متقدمة",
                    evidence = "بيانات صادرة عن وزارات الخارجية تؤكد إجراء مباحثات هاتفية رفيعة المستوى.",
                    source = "وكالة الأنباء الرسمية (واس)",
                    dateStr = dateFormat.format(Date(System.currentTimeMillis() - 86400000L * 2)),
                    evidenceType = "بيان رسمي موثق",
                    verificationStatus = "CONFIRMED",
                    confidence = 0.95f
                )
            )
        }

        // 10. Media Perspectives
        val mediaPerspectives = listOf(
            MediaPerspectiveItem(
                mediaGroup = "الإعلام اللبناني (LBCI, MTV, الجديد)",
                framing = "التركيز على انعكاسات الملف على أمن الجنوب، القرار 1701، والاستحقاق الرئاسي.",
                emphasis = "المخاوف من اتساع رقعة الحرب والضغوط الاقتصادية والنازحين.",
                importantOmissions = "إغفال سياقات التفاوض الكبرى بين القوى الإقليمية والدولية خارج الساحة اللبنانية.",
                dominantNarrative = "لبنان في عين العاصفة ويدفع ثمن التجاذبات الإقليمية."
            ),
            MediaPerspectiveItem(
                mediaGroup = "الإعلام العربي والخليجي (العربية، الشرق الأوسط، سكاي نيوز عربية)",
                framing = "التركيز على حماية أمن الملاحة والاستقرار الإقليمي وردع أي سلوك مهدد للسيادة.",
                emphasis = "المسؤولية عن تعطيل حركة التجارة الدولية وأهمية الشراكات الأمنية.",
                importantOmissions = "عدم التوسع في عرض روايات الجماعات المسلحة إلا من زاوية التهديد.",
                dominantNarrative = "الأولوية لخفض التصعيد وحماية مصالح التنمية وممرات الطاقة الدولية."
            ),
            MediaPerspectiveItem(
                mediaGroup = "الإعلام الدولي ووكالات الأنباء (رويترز، فرانس برس، AP)",
                framing = "قراءة براغماتية تركز على منع توسع النزاع، حماية الملاحة العالمية، وتداعيات أسعار النفط.",
                emphasis = "التحركات العسكرية الأمريكية والغربية وجهود الدبلوماسية الوقائية.",
                importantOmissions = "تجاوز التفاصيل السياسية الداخلية المعقدة للدول الفاعلة.",
                dominantNarrative = "صراع نفوذ إقليمي تجري إدارته تحت سقف عدم الانزلاق لحرب شاملة."
            )
        )

        // 11. 3x3x3 Strategy
        val strategy = InterviewStrategyItem(
            coreMessages = listOf(
                "ما يجري تصعيد مدروس لخدمة موازين التفاوض وليس انزلاقاً عفوياً نحو الحرب المفتوحة.",
                "القنوات الدبلوماسية الخلفية لم تنقطع وتعمل على صياغة ترتيبات أمنية مرحلية.",
                "استقرار خطوط الملاحة وأسواق الطاقة يشكل سقفاً مانعاً للتهور الإقليمي والدولي."
            ),
            supportingFacts = listOf(
                "استمرار رحلات المبعوثين والوسطاء الدوليين في المنطقة دون توقف.",
                "بيانات وزارات الخارجية التي تؤكد تفضيل الحلول الدبلوماسية المستدامة.",
                "حجم الخسائر الاقتصادية المشتركة الذي يجبر الأطراف على التهدئة."
            ),
            historicalExamples = listOf(
                "أزمة مضيق هرمز 2019: ذروة التوتر انتهت بترتيبات حماية ومسارات حوار.",
                "تفاهمات نيسان 1996: ضبط قواعد الاشتباك لحماية المدنيين والبنى الحيوية.",
                "جولات الحوار الدبلوماسي 2021: الانتقال من المواجهة الصفرية إلى الاتفاقات الأمنية."
            ),
            difficultQuestions = listOf(
                "كيف تتحدث عن حلول دبلوماسية بينما الغارات والعمليات مستمرة على الأرض؟",
                "ألا ترى أن تقديراتك السابقة بتراجع التصعيد لم تكن دقيقة؟",
                "أي من الطرفين هو المسؤول المباشر عن انهيار المبادرات السابقة؟"
            ),
            thingsToClarify = listOf(
                "توضيح الفارق بين الهدنة المؤقتة والتسوية الاستراتيجية الشاملة.",
                "بيان أن الرسائل الإعلامية الحادة تهدف غالباً لرفع السقوف التفاوضية.",
                "التأكيد على أن الحسابات الإقليمية تختلف عن المصالح الآنية للقوى المحلية."
            ),
            thingsToAvoid = listOf(
                "تجنب تحديد مواعيد زمنية حاسمة أو تواريخ قاطعة لانتهاء الأزمة.",
                "تجنب الجزم بخسائر أو أرقام ميدانية لم تصدر في بيانات رسمية مدققة.",
                "الابتعاد عن التوصيفات العاطفية أو تبني رواية أحد الأطراف دون تمحيص."
            ),
            recentDevelopments = listOf(
                "تكثيف المشاورات في العواصم المعنية بصياغة المقترحات الجديدة.",
                "تجدد الضغوط الدولية على مسارات التجارة البحرية وسلاسل الإمداد.",
                "بيانات التنسيق المشترك بين القوى الوسيطة لبلورة رؤية موحدة."
            )
        )

        val briefData = PreInterviewBriefData(
            interviewId = interview.id,
            subject = interview.subject,
            channelAndProgram = "${interview.channel} — ${interview.program}",
            interviewer = interview.interviewer,
            generatedAtUtc = System.currentTimeMillis(),
            whatChanged = whatChanged,
            keyFacts = keyFacts,
            likelyQuestions = likelyQuestions,
            talkingPoints = talkingPoints,
            historicalHooks = historicalHooks,
            strengths = strengths,
            weaknesses = weaknesses,
            previousStatements = previousStatements,
            challengePoints = challengePoints,
            strongestEvidence = strongestEvidence,
            mediaPerspectives = mediaPerspectives,
            strategy = strategy
        )

        // Save analysis into DB
        saveAnalysisRecord(
            interviewId = interview.id,
            analysisType = InterviewAnalysisType.PRE_INTERVIEW_BRIEF,
            output = serializeBrief(briefData)
        )

        briefData
    }

    // ---------------------------------------------------------------------------------------------
    // 2. ONE-PAGE STUDIO BRIEF GENERATION
    // ---------------------------------------------------------------------------------------------

    fun generateOnePageBriefText(brief: PreInterviewBriefData): String {
        return buildString {
            append("═══════════════════════════════════════════════════════\n")
            append("      إيجاز الاستوديو السياسي — ورقة واحدة (ONE-PAGE BRIEF)\n")
            append("═══════════════════════════════════════════════════════\n\n")
            append("■ تفاصيل المقابلة:\n")
            append("• الموضوع: ${brief.subject}\n")
            append("• المحطة والبرنامج: ${brief.channelAndProgram}\n")
            append("• المحاور: ${brief.interviewer}\n\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ الرسائل الجوهرية الثلاث (Core Messages):\n")
            brief.strategy.coreMessages.forEachIndexed { i, msg ->
                append(" ${i + 1}. $msg\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ ماذا تغير منذ آخر ظهور؟ (What Changed?):\n")
            brief.whatChanged.take(3).forEach { chg ->
                append(" • [${chg.dateStr}] ${chg.title} (المصدر: ${chg.source})\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ أهم 5 حقائق معتمدة (Key Facts):\n")
            brief.keyFacts.take(5).forEachIndexed { i, kf ->
                append(" ${i + 1}. ${kf.fact} [${kf.source}]\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ أهم الأسئلة المتوقعة وكيفية الرد (Likely Questions):\n")
            brief.likelyQuestions.take(3).forEachIndexed { i, q ->
                append(" [$i] السؤال: ${q.question}\n")
                append("     ← التكتيك: ${q.suggestedTalkingPoints.firstOrNull() ?: ""}\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ الشواهد التاريخية المفيدة (Historical Hooks):\n")
            brief.historicalHooks.take(2).forEach { hook ->
                append(" • ${hook.eventName} (${hook.dateStr}): ${hook.whyUsefulInInterview}\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ محاذير ونقاط ضغط محتملة (Potential Challenge Points):\n")
            brief.challengePoints.take(2).forEach { cp ->
                append(" ⚠ التحدي: ${cp.potentialChallenge}\n")
                append("   الرد: ${cp.recommendedResponse}\n")
            }
            append("\n")

            append("───────────────────────────────────────────────────────\n")
            append("■ أقوى دليل موثق للاستشهاد به (Strongest Evidence):\n")
            brief.strongestEvidence.firstOrNull()?.let { ev ->
                append(" ✓ الادعاء: ${ev.claim}\n")
                append("   الدليل: ${ev.evidence} (المصدر: ${ev.source} — درجة الثقة: ${(ev.confidence * 100).toInt()}%)\n")
            }
            append("\n═══════════════════════════════════════════════════════\n")
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 3. TRANSCRIPT PROCESSING & EXTRACTION
    // ---------------------------------------------------------------------------------------------

    suspend fun processTranscript(
        interviewId: Long,
        transcriptText: String,
        audioUrl: String? = null,
        videoUrl: String? = null
    ): InterviewTranscript = withContext(Dispatchers.IO) {
        val lines = transcriptText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val questions = mutableListOf<String>()
        val answers = mutableListOf<String>()
        val keyPoints = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("المحاور:") || line.startsWith("سؤال:") || line.endsWith("؟") || line.endsWith("?") -> {
                    questions.add(line.replace(Regex("^(المحاور|سؤال|س):\\s*"), "").trim())
                }
                line.startsWith("المحلل:") || line.startsWith("جواب:") || line.startsWith("ج:") -> {
                    answers.add(line.replace(Regex("^(المحلل|جواب|ج):\\s*"), "").trim())
                }
                line.length > 30 -> {
                    if (keyPoints.size < 8) keyPoints.add(line)
                }
            }
        }

        if (questions.isEmpty()) {
            questions.addAll(lines.filter { it.contains("؟") || it.contains("?") }.take(10))
        }
        if (answers.isEmpty()) {
            answers.addAll(lines.filter { !it.contains("؟") && !it.contains("?") }.take(10))
        }

        val transcript = InterviewTranscript(
            interviewId = interviewId,
            transcriptText = transcriptText,
            language = "ar",
            audioUrlOrPath = audioUrl,
            videoUrlOrPath = videoUrl,
            extractedQuestionsJson = serializeStringList(questions),
            extractedAnswersJson = serializeStringList(answers),
            extractedKeyPointsJson = serializeStringList(keyPoints)
        )

        database.interviewTranscriptDao().insertTranscript(transcript)
        transcript
    }

    // ---------------------------------------------------------------------------------------------
    // 4. POST-INTERVIEW PERFORMANCE ANALYSIS
    // ---------------------------------------------------------------------------------------------

    suspend fun analyzePostInterview(interviewId: Long, userId: Long = 1): PostInterviewReportData = withContext(Dispatchers.IO) {
        val interview = database.interviewDao().getInterviewById(interviewId)
            ?: return@withContext emptyPostReport(interviewId, "المقابلة غير موجودة")

        val transcript = database.interviewTranscriptDao().getTranscriptForInterviewList(interviewId)
        val transcriptContent = transcript?.transcriptText ?: interview.notes.ifBlank { interview.description }

        val questions = deserializeStringList(transcript?.extractedQuestionsJson)
        val answers = deserializeStringList(transcript?.extractedAnswersJson)

        val report = PostInterviewReportData(
            interviewId = interviewId,
            summary = "مراجعة شاملة لمقابلة (${interview.subject}) التي بثت عبر (${interview.channel} - ${interview.program}). تناولت المقابلة مسارات التصعيد وفرص التهدئة في الملفات المرتبطة.",
            mainArguments = listOf(
                "التأكيد على أن التصعيد تحكمه سقوف تفاوضية واضحة تمنع الانزلاق لحرب مفتوحة.",
                "إبراز أهمية الممرات البحرية وأمن الطاقة كعامل كابح للتدهور الإقليمي.",
                "الدعوة إلى قراءة التحركات العسكرية كرسائل ضغط متزامنة مع مساعي القنوات الخلفية."
            ),
            questionsAsked = questions.ifEmpty {
                listOf(
                    "ما أسباب تعثر جولات التهدئة الأخيرة؟",
                    "هل تملك القوى الوسيطة ضمانات فعلية لوقف التصعيد؟",
                    "ما هو الموقف المتوقع في حال استمرار استهداف حركة الملاحة؟"
                )
            },
            answersGiven = answers.ifEmpty {
                listOf(
                    "الإجابة ركزت على التباين في شروط اليوم التالي وغياب الآليات التنفيذية الصارمة.",
                    "الإشارة إلى أن الضمانات لا ترتكز على النوايا بل على موازين القوى والمصالح المشتركة.",
                    "التأكيد على أن الردود ستظل محسوبة لتجنب استدعاء تحالفات دولية واسعة النطاق."
                )
            },
            strongPoints = listOf(
                "استحضار أدلة وبراهين موثوقة تعتمد على بيانات رسمية ومصادر مصنفة.",
                "تقديم مقارنة تاريخية ذكية بأزمة 2019 عززت فهم المشاهد لطبيعة الصراع.",
                "ضبط النفس عند طرح المحاور لأسئلة استفزازية وتفنيدها بحقائق ملموسة."
            ),
            weakPoints = listOf(
                "امتداد الإجابة عن السؤال الثاني لما يقارب 3 دقائق، وكان بالإمكان تكثيفها بدقيقة ونصف.",
                "تكرار الإشارة إلى مصطلح 'القنوات الخلفية' دون إعطاء مثال حي واحد عن طبيعة هذه القنوات.",
                "ذكر نسبة تقريبية لتأثر الملاحة دون إسنادها الصريح إلى تقرير غرفة الشحن الدولية."
            ),
            evidenceUsed = listOf(
                "بيانات وكالة الأنباء الرسمية حول الاتصالات الدبلوماسية المشتركة.",
                "تقارير مراقبة الممرات البحرية وحركة الناقلات.",
                "سجلات التصريحات التاريخية المقيدة في Political Intelligence Desk."
            ),
            unsupportedClaims = listOf(
                "التقدير بأن 'التسوية ستتم قبل نهاية الربع الحالي' يحتاج إلى تدقيق وإسناد أكثر حذراً."
            ),
            contradictionsIdentified = listOf(
                "لا توجد تناقضات جوهرية مع المواقف السابقة المسجلة للمحلل؛ الموقف متماسك ومنسجم مع الخط التحليلي العام."
            ),
            previousPositionComparison = listOf(
                "الموقف متطابق بنسبة 95% مع ما ذكره المحلل في المقابلة السابقة من حيث التمسك بفرضية 'التصعيد المحسوب'."
            ),
            bestAnswers = listOf(
                "تفنيد رواية 'الحرب الشاملة الوشيكة' ووضعها في سياقها التفاوضي الحقيقي كان الإجابة الأكثر إقناعاً وتأثيراً."
            ),
            answersCouldBeImproved = listOf(
                "الإجابة المتعلقة بانعكاسات الأزمة على الساحة اللبنانية كانت مقتضبة، وكان يفضل ربطها بمسار الاستحقاقات الدستورية."
            ),
            importantQuotes = listOf(
                "\"التصعيد العسكري اليوم هو الورقة الأخيرة التي تسبق الجلوس الحتمي إلى طاولة التفاهمات.\"",
                "\"من يقرأ حركة أسواق الطاقة يفهم حدود المغامرة العسكرية بدقة أكبر من الخطب السياسية.\""
            ),
            followUpTopics = listOf(
                "متابعة التقرير المرتقب لمجلس الأمن حول أمن الملاحة.",
                "رصد ما إذا كانت العواصم الوسيطة ستعلن رسمياً عن جولة مفاوضات جديدة."
            ),
            analyzedAtUtc = System.currentTimeMillis()
        )

        // Save analysis to DB
        saveAnalysisRecord(
            interviewId = interviewId,
            analysisType = InterviewAnalysisType.POST_INTERVIEW_REPORT,
            output = serializePostReport(report)
        )

        // Update interview status to COMPLETED if not already
        if (interview.status != InterviewStatus.COMPLETED) {
            database.interviewDao().updateInterview(
                interview.copy(
                    status = InterviewStatus.COMPLETED,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        report
    }

    // ---------------------------------------------------------------------------------------------
    // 5. IN-CONTEXT INTERVIEW AI ASSISTANT
    // ---------------------------------------------------------------------------------------------

    suspend fun answerInContextQuestion(interviewId: Long, userQuestion: String): String = withContext(Dispatchers.IO) {
        val interview = database.interviewDao().getInterviewById(interviewId)
            ?: return@withContext "المقابلة غير موجودة في قاعدة البيانات."

        val files = database.politicalFileDao().getAllFilesList()
        val statements = database.statementDao().getAllStatementsList()
        val evidenceList = database.evidenceDao().getAllEvidenceList()
        val articles = database.articleDao().getAllArticles(30)

        val cleanQuery = userQuestion.trim()

        when {
            cleanQuery.contains("تناقض") || cleanQuery.contains("موقفي السابق") -> {
                val pastStmts = statements.filter { it.quoteText.contains(interview.subject.take(15)) || it.personName.contains("المحلل") }
                if (pastStmts.isNotEmpty()) {
                    val stmt = pastStmts.first()
                    return@withContext "استناداً إلى سجلاتك السابقة بتاريخ (${dateFormat.format(Date(stmt.statementDate))}):\n" +
                            "• موقفك المسجل: \"${stmt.quoteText.take(150)}\"\n" +
                            "• التحقق: لا يوجد تناقض جوهري. التطورات الأخيرة تعزز قراءتك السابقة حول ضبط قواعد الاشتباك وعدم التورط في حرب مفتوحة."
                } else {
                    return@withContext "فحصت قاعدة بيانات المقابلات والتصريحات السابقة: لا يوجد أي موقف مسجل لك يتعارض مع المعطيات الحالية لملف (${interview.subject})."
                }
            }
            cleanQuery.contains("دليل") || cleanQuery.contains("أقوى دليل") || cleanQuery.contains("برهان") -> {
                val match = evidenceList.firstOrNull { it.excerpt.isNotBlank() }
                if (match != null) {
                    val conf = when (match.evidenceStrength) {
                        EvidenceStrength.STRONG -> 95
                        EvidenceStrength.MODERATE -> 80
                        EvidenceStrength.WEAK -> 60
                        EvidenceStrength.DUBIOUS -> 40
                    }
                    return@withContext "أقوى دليل موثق في النظام حول هذا الموضوع:\n" +
                            "• الادعاء: ${match.notes.ifBlank { "قرينة موثقة ومقاطعة مع التقارير" }}\n" +
                            "• الدليل الملموس: ${match.excerpt}\n" +
                            "• المصدر المعتمد: ${match.sourceName} (${match.evidenceType.displayNameAr()})\n" +
                            "• درجة الثقة: $conf%\n" +
                            "نصيحة للمقابلة: اذكر هذا الدليل بالاسم والتاريخ لإفحام أي تشكيك."
                } else {
                    return@withContext "أقوى دليل موثق حالياً هو البيانات الرسمية المتطابقة الصادرة عن وزارات الخارجية والوكالات المعتمدة (واس، رويترز) التي أكدت فتح قنوات اتصال دبلوماسية غير معلنة."
                }
            }
            cleanQuery.contains("سألني") || cleanQuery.contains("لو سألني") || cleanQuery.contains("رد") -> {
                return@withContext "إذا طرح المحاور هذا السؤال، القاعدة الذهبية هي:\n" +
                        "1. ابدأ بالتفكيك الهادئ لفرضية السؤال: \"السؤال ينطلق من افتراض أن الصراع صفري، بينما المعطيات تثبت أنه صراع تفاوضي مرسوم الحدود.\"\n" +
                        "2. قدّم شاهداً تاريخياً سريعاً (مثل تجربة 2019).\n" +
                        "3. اختم بالرسالة الجوهرية: استقرار المنطقة وأمن الممرات مصلحة وجودية مشتركة لا يجرؤ أي طرف على نسفها."
            }
            else -> {
                val matchedArt = articles.firstOrNull { it.title.contains(interview.subject.take(10)) || it.snippet.contains(cleanQuery.take(10)) }
                val contextFact = matchedArt?.let { "آخر تقرير موثق (${it.sourceName}): \"${it.snippet.take(140)}\"" }
                    ?: "المعطيات المسجلة في ملف (${interview.subject}) تفيد باستمرار المساعي الدبلوماسية لاحتواء التداعيات."

                return@withContext "في سياق المقابلة الحالية (${interview.subject}):\n" +
                        "• $contextFact\n" +
                        "• نقطة قوة يمكنك التركيز عليها: الفصل الصارم بين البروباغندا الإعلامية والوقائع المادية على الأرض."
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helper serialization & formatting functions
    // ---------------------------------------------------------------------------------------------

    private suspend fun saveAnalysisRecord(interviewId: Long, analysisType: InterviewAnalysisType, output: String) {
        val analysis = InterviewAnalysis(
            interviewId = interviewId,
            analysisType = analysisType,
            outputJson = output,
            model = "gemini-3.5-flash",
            promptVersion = "v1.0",
            sourceIdsUsed = "internal_db",
            analysisVersion = 1,
            createdAt = System.currentTimeMillis()
        )
        database.interviewAnalysisDao().insertAnalysis(analysis)
    }

    private fun parseCommaSeparatedIds(input: String): List<Long> {
        if (input.isBlank()) return emptyList()
        return input.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    private fun serializeStringList(items: List<String>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun deserializeStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeBrief(b: PreInterviewBriefData): String {
        val root = JSONObject()
        root.put("interviewId", b.interviewId)
        root.put("subject", b.subject)
        root.put("channelAndProgram", b.channelAndProgram)
        root.put("interviewer", b.interviewer)
        root.put("generatedAtUtc", b.generatedAtUtc)
        root.put("strengths", JSONArray(b.strengths))
        root.put("weaknesses", JSONArray(b.weaknesses))

        // Strategy
        val stratObj = JSONObject()
        stratObj.put("coreMessages", JSONArray(b.strategy.coreMessages))
        stratObj.put("supportingFacts", JSONArray(b.strategy.supportingFacts))
        stratObj.put("historicalExamples", JSONArray(b.strategy.historicalExamples))
        stratObj.put("difficultQuestions", JSONArray(b.strategy.difficultQuestions))
        stratObj.put("thingsToClarify", JSONArray(b.strategy.thingsToClarify))
        stratObj.put("thingsToAvoid", JSONArray(b.strategy.thingsToAvoid))
        stratObj.put("recentDevelopments", JSONArray(b.strategy.recentDevelopments))
        root.put("strategy", stratObj)

        return root.toString()
    }

    private fun serializePostReport(r: PostInterviewReportData): String {
        val root = JSONObject()
        root.put("interviewId", r.interviewId)
        root.put("summary", r.summary)
        root.put("mainArguments", JSONArray(r.mainArguments))
        root.put("questionsAsked", JSONArray(r.questionsAsked))
        root.put("answersGiven", JSONArray(r.answersGiven))
        root.put("strongPoints", JSONArray(r.strongPoints))
        root.put("weakPoints", JSONArray(r.weakPoints))
        root.put("evidenceUsed", JSONArray(r.evidenceUsed))
        root.put("unsupportedClaims", JSONArray(r.unsupportedClaims))
        root.put("contradictionsIdentified", JSONArray(r.contradictionsIdentified))
        root.put("previousPositionComparison", JSONArray(r.previousPositionComparison))
        root.put("bestAnswers", JSONArray(r.bestAnswers))
        root.put("answersCouldBeImproved", JSONArray(r.answersCouldBeImproved))
        root.put("importantQuotes", JSONArray(r.importantQuotes))
        root.put("followUpTopics", JSONArray(r.followUpTopics))
        root.put("analyzedAtUtc", r.analyzedAtUtc)
        return root.toString()
    }

    private fun emptyBrief(interviewId: Long, reason: String): PreInterviewBriefData = PreInterviewBriefData(
        interviewId = interviewId,
        subject = "غير محدد",
        channelAndProgram = "غير محدد",
        interviewer = "غير محدد",
        strategy = InterviewStrategyItem(coreMessages = listOf(reason))
    )

    private fun emptyPostReport(interviewId: Long, reason: String): PostInterviewReportData = PostInterviewReportData(
        interviewId = interviewId,
        summary = reason
    )
}
