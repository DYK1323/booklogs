package com.dyk1323.booklogs.domain.repository

/**
 * Lets a use case that touches more than one repository (e.g. [com.dyk1323.booklogs.domain.usecase.ChangeBookStatusUseCase]
 * updating both a book and its round) run those calls atomically, without :domain knowing anything
 * about Room. The :app implementation wraps `RoomDatabase.withTransaction`.
 */
interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
