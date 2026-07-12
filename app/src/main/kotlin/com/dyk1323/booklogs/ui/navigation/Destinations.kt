package com.dyk1323.booklogs.ui.navigation

object Destinations {
    const val DASHBOARD = "dashboard"
    const val LIBRARY = "library"
    const val BOOK_DETAIL = "book/{bookId}"
    const val BOOK_EDIT = "book/{bookId}/edit"
    const val QUOTE_CAPTURE = "book/{bookId}/quote-capture"
    const val QUOTE_LIST = "book/{bookId}/quotes"
    const val REVIEW_LIST = "book/{bookId}/reviews"

    // reviewId defaults to -1 (Navigation-Compose has no nullable Long arg type) meaning "새 독후감".
    const val REVIEW_EDITOR = "book/{bookId}/review?reviewId={reviewId}"
    const val NO_REVIEW_ID = -1L
    const val SETTINGS = "settings"
    const val REGISTRATION_ENTRY = "registration"
    const val REGISTRATION_SCAN = "registration/scan"
    const val REGISTRATION_SEARCH = "registration/search"
    const val REGISTRATION_CONFIRM = "registration/confirm"

    fun bookDetail(bookId: Long): String = "book/$bookId"
    fun bookEdit(bookId: Long): String = "book/$bookId/edit"
    fun quoteCapture(bookId: Long): String = "book/$bookId/quote-capture"
    fun quoteList(bookId: Long): String = "book/$bookId/quotes"
    fun reviewList(bookId: Long): String = "book/$bookId/reviews"
    fun reviewEditor(bookId: Long, reviewId: Long? = null): String =
        "book/$bookId/review?reviewId=${reviewId ?: NO_REVIEW_ID}"
}
