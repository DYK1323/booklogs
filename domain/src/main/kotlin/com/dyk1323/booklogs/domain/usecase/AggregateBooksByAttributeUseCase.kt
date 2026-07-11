package com.dyk1323.booklogs.domain.usecase

data class AttributeCount(
    val label: String,
    val count: Int,
)

/**
 * Groups items by a key extracted with [keySelector] (genre/author/publisher/country for the stats
 * screen — the same function serves all four, only the selector differs), counts each group, sorts
 * descending, and folds anything past [topN] into a single "기타" bucket so the bar list stays readable.
 * A null or blank key groups under "미상".
 */
fun <T> aggregateBooksByAttribute(
    items: List<T>,
    keySelector: (T) -> String?,
    topN: Int = 7,
    unknownLabel: String = "미상",
    otherLabel: String = "기타",
): List<AttributeCount> {
    val counts = items
        .groupingBy { item -> keySelector(item)?.trim()?.takeIf { it.isNotEmpty() } ?: unknownLabel }
        .eachCount()

    val sorted = counts.entries
        .sortedByDescending { it.value }
        .map { AttributeCount(it.key, it.value) }

    if (sorted.size <= topN) return sorted

    val top = sorted.take(topN)
    val otherCount = sorted.drop(topN).sumOf { it.count }
    return top + AttributeCount(otherLabel, otherCount)
}
