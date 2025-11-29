package com.madtracking.app.domain.model

/**
 * İlaç uyum oranı hesaplama sonucu.
 * 
 * @param planned Planlanan intake sayısı
 * @param taken Alınan intake sayısı
 * @param missed Kaçırılan intake sayısı
 * @param adherenceRatio Uyum oranı (0.0 - 1.0 arası), null ise hesaplanamadı
 */
data class AdherenceResult(
    val planned: Int,
    val taken: Int,
    val missed: Int,
    val adherenceRatio: Double?
)

