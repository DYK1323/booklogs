package com.dyk1323.booklogs.domain.usecase

import com.dyk1323.booklogs.domain.model.BookFormat

/**
 * Blocking validation for a book's format/totalPages combination, shared by registration and editing.
 * An EBOOK with no totalPages can never resolve a quick-log input via [resolveLoggedPage], so this must
 * be enforced before the book is saved rather than discovered later at logging time.
 */
fun validateBookForm(format: BookFormat, totalPages: Int?): String? {
    return if (format == BookFormat.EBOOK && (totalPages == null || totalPages <= 0)) {
        "전자책은 전체 페이지 수를 입력해야 나중에 진행률을 기록할 수 있어요."
    } else {
        null
    }
}
