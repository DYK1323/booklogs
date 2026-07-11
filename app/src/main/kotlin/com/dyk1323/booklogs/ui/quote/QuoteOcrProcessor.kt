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
