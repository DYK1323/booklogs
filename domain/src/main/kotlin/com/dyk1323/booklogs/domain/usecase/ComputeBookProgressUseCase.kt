package com.dyk1323.booklogs.domain.usecase

/**
 * Computes progress as a 0f..1f fraction.
 *
 * Returns null when [totalPages] is unknown (the UI shows a "?" badge instead of a ring in this case) —
 * distinct from a book with zero logs yet, which has a known [totalPages] but no [currentPage], and
 * correctly computes to 0f (an empty ring, not "?").
 */
fun computeBookProgress(currentPage: Int?, totalPages: Int?): Float? {
    if (totalPages == null || totalPages <= 0) return null
    val page = currentPage ?: 0
    return (page.toFloat() / totalPages).coerceIn(0f, 1f)
}
