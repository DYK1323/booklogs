package com.dyk1323.booklogs.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ComputeBookProgressUseCaseTest {

    @Test
    fun `unknown totalPages returns null (the caller shows a question-mark badge)`() {
        assertNull(computeBookProgress(currentPage = 50, totalPages = null))
    }

    @Test
    fun `zero or negative totalPages is also treated as unknown`() {
        assertNull(computeBookProgress(currentPage = 50, totalPages = 0))
        assertNull(computeBookProgress(currentPage = 50, totalPages = -10))
    }

    @Test
    fun `zero logs yet (currentPage null) with a known totalPages computes to exactly 0f, not null`() {
        assertEquals(0f, computeBookProgress(currentPage = null, totalPages = 300))
    }

    @Test
    fun `normal progress is currentPage over totalPages`() {
        assertEquals(0.5f, computeBookProgress(currentPage = 150, totalPages = 300))
    }

    @Test
    fun `progress is clamped to 1f even if currentPage exceeds totalPages`() {
        assertEquals(1f, computeBookProgress(currentPage = 350, totalPages = 300))
    }
}
