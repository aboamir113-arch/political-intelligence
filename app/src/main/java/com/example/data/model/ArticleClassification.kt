package com.example.data.model

enum class ArticleClassification {
    NEW,
    DEVELOPMENT,
    UPDATE,
    CONFIRMATION,
    RECIRCULATED,
    DUPLICATE;

    fun displayNameAr(): String = when (this) {
        NEW -> "خبر جديد"
        DEVELOPMENT -> "تطور لاحق"
        UPDATE -> "تحديث"
        CONFIRMATION -> "تأكيد رسمي"
        RECIRCULATED -> "معاد التدوير"
        DUPLICATE -> "مكرر"
    }

    fun displayNameEn(): String = when (this) {
        NEW -> "New"
        DEVELOPMENT -> "Development"
        UPDATE -> "Update"
        CONFIRMATION -> "Confirmation"
        RECIRCULATED -> "Recirculated"
        DUPLICATE -> "Duplicate"
    }
}
