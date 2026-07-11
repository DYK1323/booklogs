package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookFormat

/**
 * Format-aware validation + conversion for a raw quick-log input value into the page number that
 * actually gets stored — shared by the dashboard's quick-log sheet and book detail's log editor so
 * both surfaces enforce identical EBOOK-percent/PHYSICAL-page rules (see docs/PLAN.md "빠른 기록 UX").
 */
sealed interface ResolveLoggedPageResult {
    data class Success(val currentPage: Int) : ResolveLoggedPageResult
    data class Error(val message: String) : ResolveLoggedPageResult
}

fun resolveLoggedPage(format: BookFormat, totalPages: Int?, inputValue: Int): ResolveLoggedPageResult {
    return if (format == BookFormat.EBOOK) {
        if (totalPages == null || totalPages <= 0) {
            ResolveLoggedPageResult.Error("전자책은 전체 페이지 수가 필요해요.")
        } else if (inputValue !in 0..100) {
            ResolveLoggedPageResult.Error("진행률은 0부터 100까지 입력해주세요.")
        } else {
            ResolveLoggedPageResult.Success(ConvertPagePercentUseCase.percentToPage(inputValue, totalPages))
        }
    } else {
        if (totalPages != null && inputValue > totalPages) {
            ResolveLoggedPageResult.Error("전체 페이지보다 큰 값이에요.")
        } else {
            ResolveLoggedPageResult.Success(inputValue)
        }
    }
}
