package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ContradictionType {
    DATE_TIME,
    LOCATION,
    PERSONS_INVOLVED,
    NUMBERS_FIGURES,
    DECISION_OUTCOME,
    QUOTE_ATTRIBUTION,
    SEQUENCE;

    fun displayNameAr(): String = when (this) {
        DATE_TIME -> "تعارض في التاريخ والوقت"
        LOCATION -> "تعارض في مكان الحدث"
        PERSONS_INVOLVED -> "تعارض في هويات/حضور الشخصيات"
        NUMBERS_FIGURES -> "تعارض في الأرقام والإحصاءات"
        DECISION_OUTCOME -> "تعارض في نتائج المحادثات والقرارات"
        QUOTE_ATTRIBUTION -> "تعارض في نسبة التصريحات والاقتباسات"
        SEQUENCE -> "تعارض في التسلسل الزمني للأحداث"
    }

    fun displayNameEn(): String = when (this) {
        DATE_TIME -> "Date / Time Conflict"
        LOCATION -> "Location Conflict"
        PERSONS_INVOLVED -> "Persons Conflict"
        NUMBERS_FIGURES -> "Numbers / Figures Conflict"
        DECISION_OUTCOME -> "Outcome / Decision Conflict"
        QUOTE_ATTRIBUTION -> "Attribution Conflict"
        SEQUENCE -> "Sequence Conflict"
    }
}

enum class ContradictionStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED;

    fun displayNameAr(): String = when (this) {
        OPEN -> "قيد الرصد والتعارض قائم"
        UNDER_REVIEW -> "قيد تدقيق المحلل"
        RESOLVED -> "تم التوضيح بمصدر أولي"
        DISMISSED -> "تعارض ظاهري غير جوهري"
    }
}

@Entity(
    tableName = "contradictions",
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["claimId"]),
        Index(value = ["contradictionType"]),
        Index(value = ["status"])
    ]
)
data class ContradictionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val claimId: Long? = null,
    val contradictionType: ContradictionType,
    val claimAStatement: String,
    val claimASource: String,
    val claimADate: Long,
    val claimBStatement: String,
    val claimBSource: String,
    val claimBDate: Long,
    val descriptionAr: String,
    val status: ContradictionStatus = ContradictionStatus.OPEN,
    val analystNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
