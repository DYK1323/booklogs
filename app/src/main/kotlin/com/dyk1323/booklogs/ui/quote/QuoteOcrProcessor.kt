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

class QuoteOcrProcessor {
    private val koreanRecognizer: TextRecognizer =
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    private val latinRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(bitmap: Bitmap, cropRect: Rect): String {
        val cropped = Bitmap.createBitmap(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
        )
        val image = InputImage.fromBitmap(cropped, 0)
        val (koreanText, latinText) = coroutineScope {
            val korean = async { koreanRecognizer.processText(image).toQuoteText() }
            val latin = async { latinRecognizer.processText(image).toQuoteText() }
            korean.await() to latin.await()
        }
        return pickBetterQuoteText(koreanText, latinText)
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
