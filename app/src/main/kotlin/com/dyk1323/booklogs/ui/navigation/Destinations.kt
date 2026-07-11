package com.dyk1323.booklogs.ui.navigation

object Destinations {
    const val DASHBOARD = "dashboard"
    const val BOOK_DETAIL = "book/{bookId}"
    const val REGISTRATION_ENTRY = "registration"
    const val REGISTRATION_SCAN = "registration/scan"
    const val REGISTRATION_SEARCH = "registration/search"
    const val REGISTRATION_CONFIRM = "registration/confirm"

    fun bookDetail(bookId: Long): String = "book/$bookId"
}
