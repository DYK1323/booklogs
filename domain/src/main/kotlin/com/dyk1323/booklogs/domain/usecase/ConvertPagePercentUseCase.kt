package com.dyk1323.booklogs.domain.usecase

import kotlin.math.roundToInt

/**
 * Bidirectional page <-> percent conversion for EBOOK-format books, whose progress is reported as a
 * percentage but still stored internally as a page-equivalent (see docs/PLAN.md "빠른 기록 UX").
 * Callers must resolve `totalPages` being unknown before calling this (e.g. prompt for an estimate) —
 * these are plain functions, not the place for that flow.
 */
object ConvertPagePercentUseCase {
    fun percentToPage(percent: Int, totalPages: Int): Int {
        require(totalPages > 0) { "totalPages must be > 0" }
        require(percent in 0..100) { "percent must be in 0..100" }
        return ((percent / 100.0) * totalPages).roundToInt()
    }

    fun pageToPercent(currentPage: Int, totalPages: Int): Int {
        require(totalPages > 0) { "totalPages must be > 0" }
        val percent = ((currentPage.toDouble() / totalPages) * 100).roundToInt()
        return percent.coerceIn(0, 100)
    }
}
