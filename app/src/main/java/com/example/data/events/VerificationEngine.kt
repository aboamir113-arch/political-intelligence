package com.example.data.events

import com.example.data.model.*

data class VerificationEvaluation(
    val updatedStatus: VerificationStatus,
    val confidenceScore: Int,
    val explanationAr: String,
    val independentSourceCount: Int,
    val hasPrimaryEvidence: Boolean,
    val hasContradiction: Boolean
)

class VerificationEngine {

    /**
     * Evaluates a Claim based on its attached EvidenceItems and known contradictions.
     */
    fun evaluateClaim(
        claim: Claim,
        evidenceList: List<EvidenceItem>,
        contradictions: List<ContradictionItem>
    ): VerificationEvaluation {
        if (evidenceList.isEmpty()) {
            return VerificationEvaluation(
                updatedStatus = VerificationStatus.INSUFFICIENT_EVIDENCE,
                confidenceScore = 20,
                explanationAr = "لا توجد أدلة موثقة مسجلة تدعم هذا الادعاء حتى الآن.",
                independentSourceCount = 0,
                hasPrimaryEvidence = false,
                hasContradiction = false
            )
        }

        // Check active contradictions
        val activeContradictions = contradictions.filter { it.claimId == claim.id && it.status == ContradictionStatus.OPEN }
        if (activeContradictions.isNotEmpty()) {
            return VerificationEvaluation(
                updatedStatus = VerificationStatus.CONTRADICTED,
                confidenceScore = 40,
                explanationAr = "توجد روايات أو بيانات متناقضة مباشرة مع هذا الادعاء بحاجة إلى مراجعة وتدقيق.",
                independentSourceCount = evidenceList.map { it.sourceName }.distinct().size,
                hasPrimaryEvidence = evidenceList.any { it.sourceTier == SourceTier.PRIMARY },
                hasContradiction = true
            )
        }

        // Calculate Independent Sources (Rule 11: Ignore duplicate wire syndications)
        val independentOrigins = mutableSetOf<String>()
        for (item in evidenceList) {
            if (item.isDirectOrigin) {
                independentOrigins.add(item.sourceName)
            } else if (!item.originalAgency.isNullOrBlank()) {
                independentOrigins.add(item.originalAgency)
            } else {
                independentOrigins.add(item.sourceName)
            }
        }
        val independentCount = independentOrigins.size

        val hasOfficialDoc = evidenceList.any { it.evidenceType == EvidenceType.OFFICIAL_DOCUMENT || it.evidenceType == EvidenceType.OFFICIAL_STATEMENT }
        val hasDirectQuote = evidenceList.any { it.evidenceType == EvidenceType.DIRECT_QUOTE }
        val hasPrimaryTier = evidenceList.any { it.sourceTier == SourceTier.PRIMARY }
        val hasStrongEvidence = evidenceList.any { it.evidenceStrength == EvidenceStrength.STRONG }

        return when {
            hasOfficialDoc && (hasPrimaryTier || independentCount >= 1) -> {
                VerificationEvaluation(
                    updatedStatus = VerificationStatus.CONFIRMED,
                    confidenceScore = 95,
                    explanationAr = "مؤكد بوثيقة رسمية أو بيان وزاري مباشر صادر عن الجهات المعنية.",
                    independentSourceCount = independentCount,
                    hasPrimaryEvidence = true,
                    hasContradiction = false
                )
            }
            independentCount >= 2 && hasStrongEvidence -> {
                VerificationEvaluation(
                    updatedStatus = VerificationStatus.SUPPORTED,
                    confidenceScore = 85,
                    explanationAr = "مدعوم بروايات متطابقة من عدة مصادر إخبارية مستقلة وموثوقة دون تناقضات مسجلة.",
                    independentSourceCount = independentCount,
                    hasPrimaryEvidence = hasPrimaryTier,
                    hasContradiction = false
                )
            }
            hasDirectQuote || (independentCount >= 1 && hasPrimaryTier) -> {
                VerificationEvaluation(
                    updatedStatus = VerificationStatus.LIKELY,
                    confidenceScore = 75,
                    explanationAr = "مرجح بدلالة اقتباسات مباشرة وتغطية من وكالة أنباء رئيسية، قيد اكتمال القرائن.",
                    independentSourceCount = independentCount,
                    hasPrimaryEvidence = hasPrimaryTier,
                    hasContradiction = false
                )
            }
            independentCount == 1 -> {
                val origin = evidenceList.first().sourceName
                VerificationEvaluation(
                    updatedStatus = VerificationStatus.UNVERIFIED,
                    confidenceScore = 50,
                    explanationAr = "يستند إلى مصدر وحيد ($origin) دون تأكيد موازٍ من جهات مستقلة أخرى.",
                    independentSourceCount = 1,
                    hasPrimaryEvidence = false,
                    hasContradiction = false
                )
            }
            else -> {
                VerificationEvaluation(
                    updatedStatus = VerificationStatus.INSUFFICIENT_EVIDENCE,
                    confidenceScore = 30,
                    explanationAr = "أدلة أولية ضعيفة تتطلب تدقيقًا إضافيًا ومطابقة استخباراتية.",
                    independentSourceCount = independentCount,
                    hasPrimaryEvidence = false,
                    hasContradiction = false
                )
            }
        }
    }
}
