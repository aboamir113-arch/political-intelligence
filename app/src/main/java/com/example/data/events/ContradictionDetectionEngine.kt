package com.example.data.events

import com.example.data.model.*
import java.util.regex.Pattern

class ContradictionDetectionEngine {

    // Day names in Arabic
    private val daysOfWeek = listOf("السبت", "الأحد", "الاثنين", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    // Outcome antonym pairs
    private val opposingTerms = listOf(
        Pair("نفت", "أكدت"),
        Pair("نفى", "أكد"),
        Pair("رفضت", "وافقت"),
        Pair("رفض", "وافق"),
        Pair("فشل", "نجاح"),
        Pair("تعثرت", "أحرزت تقدم"),
        Pair("أغلقت", "فتحت"),
        Pair("انسحاب", "بقاء"),
        Pair("وقف إطلاق النار", "استمرار العمليات")
    )

    // Number extraction pattern (digits followed by units or words)
    private val numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(مليون|مليار|ألف|قتيل|جريح|صاروخ|طائرة|سفينة|شخص)?")

    fun detectContradictions(
        eventId: Long,
        claims: List<Claim>,
        articles: List<Article>
    ): List<ContradictionItem> {
        val detected = mutableListOf<ContradictionItem>()

        // 1. Cross-check claims for direct contradictory outcomes
        for (i in 0 until claims.size) {
            for (j in i + 1 until claims.size) {
                val claimA = claims[i]
                val claimB = claims[j]

                // Skip if same source or identical
                if (claimA.statement == claimB.statement) continue

                // Check Opposing Polarities (e.g. deny vs confirm)
                for ((negTerm, posTerm) in opposingTerms) {
                    val aHasNeg = claimA.statement.contains(negTerm)
                    val bHasPos = claimB.statement.contains(posTerm)
                    val aHasPos = claimA.statement.contains(posTerm)
                    val bHasNeg = claimB.statement.contains(negTerm)

                    if ((aHasNeg && bHasPos) || (aHasPos && bHasNeg)) {
                        detected.add(
                            ContradictionItem(
                                eventId = eventId,
                                claimId = claimA.id,
                                contradictionType = ContradictionType.DECISION_OUTCOME,
                                claimAStatement = claimA.statement,
                                claimASource = getSourceForClaim(claimA, articles),
                                claimADate = claimA.claimDate,
                                claimBStatement = claimB.statement,
                                claimBSource = getSourceForClaim(claimB, articles),
                                claimBDate = claimB.claimDate,
                                descriptionAr = "تعارض في الموقف أو النتيجة: أحدهما يشير إلى '$negTerm' بينما الآخر يفيد بـ '$posTerm'.",
                                status = ContradictionStatus.OPEN
                            )
                        )
                        break
                    }
                }

                // Check Day of Week Discrepancy
                val dayA = daysOfWeek.firstOrNull { claimA.statement.contains(it) }
                val dayB = daysOfWeek.firstOrNull { claimB.statement.contains(it) }
                if (dayA != null && dayB != null && dayA != dayB && !dayA.contains(dayB) && !dayB.contains(dayA)) {
                    detected.add(
                        ContradictionItem(
                            eventId = eventId,
                            claimId = claimA.id,
                            contradictionType = ContradictionType.DATE_TIME,
                            claimAStatement = claimA.statement,
                            claimASource = getSourceForClaim(claimA, articles),
                            claimADate = claimA.claimDate,
                            claimBStatement = claimB.statement,
                            claimBSource = getSourceForClaim(claimB, articles),
                            claimBDate = claimB.claimDate,
                            descriptionAr = "تعارض في توقيت الحدث: مصدر يحدد يوم '$dayA' ومصدر آخر يشير إلى '$dayB'.",
                            status = ContradictionStatus.OPEN
                        )
                    )
                }

                // Check Figure / Number Discrepancies if sharing similar subject
                val matcherA = numberPattern.matcher(claimA.statement)
                val matcherB = numberPattern.matcher(claimB.statement)
                if (matcherA.find() && matcherB.find()) {
                    val numA = matcherA.group(1)
                    val unitA = matcherA.group(2) ?: ""
                    val numB = matcherB.group(1)
                    val unitB = matcherB.group(2) ?: ""

                    if (unitA.isNotBlank() && unitA == unitB && numA != numB) {
                        detected.add(
                            ContradictionItem(
                                eventId = eventId,
                                claimId = claimA.id,
                                contradictionType = ContradictionType.NUMBERS_FIGURES,
                                claimAStatement = claimA.statement,
                                claimASource = getSourceForClaim(claimA, articles),
                                claimADate = claimA.claimDate,
                                claimBStatement = claimB.statement,
                                claimBSource = getSourceForClaim(claimB, articles),
                                claimBDate = claimB.claimDate,
                                descriptionAr = "تعارض في الأرقام والإحصائيات: المصدر الأول يذكر '$numA $unitA' والآخر يذكر '$numB $unitB'.",
                                status = ContradictionStatus.OPEN
                            )
                        )
                    }
                }
            }
        }

        return detected
    }

    private fun getSourceForClaim(claim: Claim, articles: List<Article>): String {
        return articles.find { it.id == claim.articleId }?.sourceName ?: "مصدر إخباري مجهول"
    }
}
