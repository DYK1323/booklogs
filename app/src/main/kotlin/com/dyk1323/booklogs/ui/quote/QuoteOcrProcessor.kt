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
        val koreanText = koreanRecognizer.processText(image).toQuoteText()
        if (koreanText.isNotBlank()) return koreanText

        return latinRecognizer.processText(image).toQuoteText()
    }

    suspend fun detectPageNumber(bitmap: Bitmap): Int? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val koreanNumber = koreanRecognizer.processText(image).findCornerPageNumber(bitmap.width, bitmap.height)
        if (koreanNumber != null) return koreanNumber

        return latinRecognizer.processText(image).findCornerPageNumber(bitmap.width, bitmap.height)
    }

    fun close() {
        koreanRecognizer.close()
        latinRecognizer.close()
    }
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

private fun Text.findCornerPageNumber(imageWidth: Int, imageHeight: Int): Int? =
    textBlocks
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
