package com.dyk1323.booklogs.domain.fake

import com.dyk1323.booklogs.domain.repository.TransactionRunner

/** Just runs the block — the fake repositories are already in-memory, so no real transaction is needed in tests. */
class NoopTransactionRunner : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}
