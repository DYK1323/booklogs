package com.dyk1323.booklogs.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class AggregateBooksByAttributeUseCaseTest {

    @Test
    fun `counts and sorts groups descending`() {
        val items = listOf("fiction", "fiction", "essay", "fiction", "essay")

        val result = aggregateBooksByAttribute(items, keySelector = { it })

        assertEquals(listOf("fiction" to 3, "essay" to 2), result.map { it.label to it.count })
    }

    @Test
    fun `null or blank keys are grouped under the unknown label`() {
        val items = listOf("fiction", null, "  ", "fiction")

        val result = aggregateBooksByAttribute(items, keySelector = { it })

        assertEquals(2, result.first { it.label == "fiction" }.count)
        assertEquals(2, result.first { it.label == "미상" }.count)
    }

    @Test
    fun `groups beyond topN fold into a single other bucket`() {
        val items = listOf("a", "a", "a", "b", "b", "c", "d", "e", "f")

        val result = aggregateBooksByAttribute(items, keySelector = { it }, topN = 3)

        assertEquals(4, result.size) // top 3 + "기타"
        assertEquals(listOf("a", "b", "c"), result.take(3).map { it.label })
        val other = result.last()
        assertEquals("기타", other.label)
        assertEquals(3, other.count) // d, e, f: 1 each
    }

    @Test
    fun `fewer groups than topN produces no other bucket`() {
        val items = listOf("a", "b")

        val result = aggregateBooksByAttribute(items, keySelector = { it }, topN = 7)

        assertEquals(2, result.size)
    }

    @Test
    fun `empty input produces an empty result`() {
        assertEquals(emptyList<AttributeCount>(), aggregateBooksByAttribute(emptyList<String>(), keySelector = { it }))
    }
}
