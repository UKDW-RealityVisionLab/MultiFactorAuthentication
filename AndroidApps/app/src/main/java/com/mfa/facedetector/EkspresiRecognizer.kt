package com.mfa.facedetector

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.preprocessor.PreprocessingUtils
import java.io.IOException

class EkspresiRecognizer @Throws(IOException::class) constructor(
    private val onExpressionDetected: (String) -> Unit // Callback untuk auto foto
) {
    private val faceDetector: FaceDetector
    private var lastExpression: String = ""
    private var lastDetectionTime: Long = 0L
    private val EXPRESSION_COOLDOWN = 1000L

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(options)
    }

//    fun processExpression(expression: String) {
//        val currentTime = System.currentTimeMillis()
//
//        // Cek cooldown dan perubahan ekspresi
//        if (expression != lastExpression || currentTime - lastDetectionTime > EXPRESSION_COOLDOWN) {
//            lastExpression = expression
//            lastDetectionTime = currentTime
//            onExpressionDetected(expression)
//        }
//    }

    fun resetState() {
        // Reset internal state recognizer jika diperlukan
        lastExpression = ""
        lastDetectionTime = 0L
    }

    fun detectExpression(croppedBitmap: Bitmap, boundingBox: Rect) {
        // Crop gambar sesuai bounding box wajah
        if (boundingBox.width() > 0 && boundingBox.height() > 0) {
            val p = PreprocessingUtils()

            if (p.isBlurry(p.convertRawGreyImg(croppedBitmap))) {
                Log.e("BLURRY", "Gambar Blurry")
                return
            }

            var greyPixels = p.convertGreyImg(croppedBitmap);
            greyPixels = p.convolve(greyPixels, p.generateGaussianKernel(3, 3f/6f), 3)

            val processedPixels = p.convertArrayToBitmap(greyPixels);
            val image = InputImage.fromBitmap(processedPixels, 0)

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces.first()
                        val smilingProb = face.smilingProbability ?: 0.0f

                        val expression = if (smilingProb > 0.5) {
                            Log.d("FaceRecognizer", "Ekspresi tersenyum terdeteksi")
                            "smile"
                        } else {
                            Log.d("FaceRecognizer", "Ekspresi netral terdeteksi")
                            "neutral"
                        }

                        // Panggil callback dan kirimkan ekspresi terdeteksi
                        onExpressionDetected(expression)
                    } else {
                        Log.d("FaceRecognizer", "Tidak ada wajah terdeteksi")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FaceRecognizer", "Error: ${e.message}")
                }
        } else {
            Log.e("EkspresiRecognizer", "BoundingBox tidak valid")
        }
    }
}
