package com.medtracking.app.domain.model

/**
 * İlaç kullanım talimatı - yemek ilişkisi.
 */
enum class MealRelation {
    BEFORE_MEAL,   // Yemekten önce
    AFTER_MEAL,    // Yemekten sonra
    WITH_MEAL,     // Yemekle birlikte
    EMPTY_STOMACH, // Aç karnına
    IRRELEVANT;    // Fark etmez / ilişkili değil

    /**
     * Kullanıcı dostu metin döndürür.
     */
    fun toDisplayText(): String = when (this) {
        BEFORE_MEAL -> "Yemekten önce al"
        AFTER_MEAL -> "Yemekten sonra al"
        WITH_MEAL -> "Yemekle birlikte al"
        EMPTY_STOMACH -> "Aç karnına al"
        IRRELEVANT -> ""
    }

    /**
     * Kısa gösterim metni (seçim dropdown için).
     */
    fun toShortDisplayText(): String = when (this) {
        BEFORE_MEAL -> "Yemekten önce"
        AFTER_MEAL -> "Yemekten sonra"
        WITH_MEAL -> "Yemekle birlikte"
        EMPTY_STOMACH -> "Aç karnına"
        IRRELEVANT -> "Fark etmez"
    }

    companion object {
        /**
         * String'den MealRelation'a güvenli dönüşüm.
         * Hata durumunda IRRELEVANT döner.
         */
        fun fromString(value: String?): MealRelation {
            return try {
                value?.let { valueOf(it) } ?: IRRELEVANT
            } catch (e: Exception) {
                IRRELEVANT
            }
        }
    }
}

