package com.dyk1323.booklogs.ui.registration

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * EAN-13 only (ISBN-13 is EAN-13, see docs/PLAN.md "바코드 분석기"). Calls [onIsbnDetected] once with a
 * validated ISBN (13 digits, starts with 978/979) then stops firing — the caller is still expected to
 * `clearAnalyzer()` on the [ImageAnalysis] use case to fully unbind this analyzer.
 */
@ExperimentalGetImage
class BarcodeAnalyzer(
    private val onIsbnDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .build(),
    )
    private var triggered = false

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || triggered) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val isbn = barcodes.firstNotNullOfOrNull { it.rawValue?.takeIf(::isValidIsbn13) }
                if (isbn != null && !triggered) {
                    triggered = true
                    onIsbnDetected(isbn)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun isValidIsbn13(value: String): Boolean =
        value.length == 13 && value.all(Char::isDigit) && (value.startsWith("978") || value.startsWith("979"))
}
