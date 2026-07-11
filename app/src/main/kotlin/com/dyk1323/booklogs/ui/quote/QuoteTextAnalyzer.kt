package com.dyk1323.booklogs.ui.quote

import android.graphics.Rect
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable

data class RecognizedQuoteLine(
    val text: String,
    val boundingBox: Rect?,
)

@ExperimentalGetImage
class QuoteTextAnalyzer(
    private val onLinesRecognized: (List<RecognizedQuoteLine>, Int?) -> Unit,
) : ImageAnalysis.Analyzer,
    Closeable {

    private val koreanRecognizer: TextRecognizer =
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    private val latinRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var processing = false
    private var triggered = false

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || processing || triggered) {
            imageProxy.close()
            return
        }

        processing = true
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        koreanRecognizer.process(image)
            .continueWithTask { koreanTask ->
                val koreanLines = koreanTask.result?.textBlocks.orEmpty()
                    .flatMap { block -> block.lines }
                    .mapNotNull { line -> line.text.trim().takeIf { it.isNotEmpty() }?.let { RecognizedQuoteLine(it, line.boundingBox) } }
                latinRecognizer.process(image).continueWith { latinTask ->
                    val latinLines = latinTask.result?.textBlocks.orEmpty()
                        .flatMap { block -> block.lines }
                        .mapNotNull { line -> line.text.trim().takeIf { it.isNotEmpty() }?.let { RecognizedQuoteLine(it, line.boundingBox) } }
                    mergeLines(koreanLines + latinLines)
                }
            }
            .addOnSuccessListener { lines ->
                if (lines.isNotEmpty() && !triggered) {
                    triggered = true
                    onLinesRecognized(lines, extractPageNumber(lines))
                }
            }
            .addOnCompleteListener {
                processing = false
                imageProxy.close()
            }
    }

    override fun close() {
        koreanRecognizer.close()
        latinRecognizer.close()
    }
}

private fun mergeLines(lines: List<RecognizedQuoteLine>): List<RecognizedQuoteLine> =
    lines
        .distinctBy { it.text.normalizedTextKey() }
        .sortedWith(compareBy<RecognizedQuoteLine> { it.boundingBox?.top ?: Int.MAX_VALUE }.thenBy { it.boundingBox?.left ?: 0 })

private fun extractPageNumber(lines: List<RecognizedQuoteLine>): Int? =
    lines
        .mapNotNull { line ->
            val value = line.text.trim().takeIf { it.length in 1..4 && it.all(Char::isDigit) }?.toIntOrNull()
            value?.let { page -> page to cornerScore(line.boundingBox) }
        }
        .minByOrNull { it.second }
        ?.first

private fun cornerScore(rect: Rect?): Int {
    if (rect == null) return Int.MAX_VALUE
    val verticalEdge = minOf(rect.top, -rect.bottom)
    val horizontalEdge = minOf(rect.left, -rect.right)
    return verticalEdge + horizontalEdge
}

private fun String.normalizedTextKey(): String =
    lowercase().replace("\\s+".toRegex(), " ")
