package com.dyk1323.booklogs.ui.quote

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

/** A single OCR-recognized word on the captured page, in reading order. */
data class RecognizedWord(val text: String, val boundingBox: Rect, val lineId: Int)

/** Android-free counterpart of [RecognizedWord] so [joinWords] stays plain-Kotlin unit testable. */
data class WordToken(val text: String, val lineId: Int)

class QuoteOcrProcessor {
    private val koreanRecognizer: TextRecognizer =
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    private val latinRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeWords(bitmap: Bitmap): List<RecognizedWord> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val (korean, latin) = coroutineScope {
            val k = async { koreanRecognizer.processText(image) }
            val l = async { latinRecognizer.processText(image) }
            k.await() to l.await()
        }
        return pickBetterText(korean, latin).toRecognizedWords()
    }

    suspend fun detectPageNumber(bitmap: Bitmap): Int? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val (koreanResult, latinResult) = coroutineScope {
            val korean = async { koreanRecognizer.processText(image) }
            val latin = async { latinRecognizer.processText(image) }
            korean.await() to latin.await()
        }
        return listOf(koreanResult, latinResult).findCornerPageNumber(bitmap.width, bitmap.height)
    }

    fun close() {
        koreanRecognizer.close()
        latinRecognizer.close()
    }
}

/** Both recognizers run in parallel; keep whichever produced the more complete transcription (ties favor Korean). */
internal fun pickBetterQuoteText(koreanText: String, latinText: String): String = when {
    koreanText.isBlank() -> latinText
    latinText.isBlank() -> koreanText
    koreanText.length >= latinText.length -> koreanText
    else -> latinText
}

/**
 * Word-level bounding boxes from the two recognizers can't be merged (different coordinate-worthy
 * results), so unlike [pickBetterQuoteText] this must settle on a single [Text] object to draw words
 * from. Reuses the same length-based heuristic, applied to each result's derived transcription.
 */
private fun pickBetterText(korean: Text, latin: Text): Text {
    val koreanText = korean.toQuoteText()
    val latinText = latin.toQuoteText()
    return if (pickBetterQuoteText(koreanText, latinText) == koreanText) korean else latin
}

/** Flattens block/line/word in ML Kit's reading order; words sharing a line share [RecognizedWord.lineId]. */
private fun Text.toRecognizedWords(): List<RecognizedWord> {
    var lineId = 0
    return textBlocks.flatMap { it.lines }.flatMap { line ->
        val words = line.elements.mapNotNull { element ->
            element.boundingBox?.let { RecognizedWord(element.text, it, lineId) }
        }
        lineId++
        words
    }
}

/**
 * Joins a selected word range into the final quote text, replacing line breaks with spaces by default.
 * [mergedLineBreakGaps] holds gap indices (the position between word i and i+1) the user has toggled to
 * "이어붙이기" — Korean words are sometimes split mid-word across a line wrap, and OCR alone can't tell
 * whether a line-break gap is a real word boundary, so the user decides per gap. Same-line gaps are
 * always a plain space.
 */
fun joinWords(
    words: List<WordToken>,
    startIndex: Int,
    endIndex: Int,
    mergedLineBreakGaps: Set<Int> = emptySet(),
): String {
    if (words.isEmpty()) return ""
    val from = minOf(startIndex, endIndex).coerceIn(words.indices)
    val to = maxOf(startIndex, endIndex).coerceIn(words.indices)
    val builder = StringBuilder(words[from].text)
    for (i in from until to) {
        val sameLine = words[i].lineId == words[i + 1].lineId
        val separator = if (sameLine || i !in mergedLineBreakGaps) " " else ""
        builder.append(separator).append(words[i + 1].text)
    }
    return builder.toString()
}

private suspend fun TextRecognizer.processText(image: InputImage): Text =
    suspendCancellableCoroutine { continuation ->
        process(image)
            .addOnSuccessListener { text ->
                if (continuation.isActive) continuation.resume(text)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

private fun Text.toQuoteText(): String =
    textBlocks
        .flatMap { block -> block.lines }
        .map { line -> line.text.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .joinToString("\n")
        .ifBlank { text.trim() }

private fun List<Text>.findCornerPageNumber(imageWidth: Int, imageHeight: Int): Int? =
    flatMap { it.textBlocks }
        .flatMap { block -> block.lines }
        .mapNotNull { line ->
            val value = line.text.trim()
                .takeIf { it.length in 1..4 && it.all(Char::isDigit) }
                ?.toIntOrNull()
            val box = line.boundingBox
            if (value == null || box == null) null else value to box.cornerScore(imageWidth, imageHeight)
        }
        .minByOrNull { it.second }
        ?.first

private fun Rect.cornerScore(imageWidth: Int, imageHeight: Int): Int {
    val topLeft = left + top
    val topRight = (imageWidth - right) + top
    val bottomLeft = left + (imageHeight - bottom)
    val bottomRight = (imageWidth - right) + (imageHeight - bottom)
    return minOf(topLeft, topRight, bottomLeft, bottomRight)
}
