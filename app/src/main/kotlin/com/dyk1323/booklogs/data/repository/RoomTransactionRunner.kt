package com.dyk1323.booklogs.data.repository

import androidx.room.withTransaction
import com.dyk1323.booklogs.data.local.BooklogsDatabase
import com.dyk1323.booklogs.domain.repository.TransactionRunner

class RoomTransactionRunner(
    private val database: BooklogsDatabase,
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = database.withTransaction(block)
}
