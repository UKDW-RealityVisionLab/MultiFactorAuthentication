package com.mfa.facedetector

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class EkspresiRecognizer @Throws(IOException::class) constructor(
    private val onExpressionDetected: (String) -> Unit // Callback untuk auto foto
) {
    private val faceDetector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(options)
    }

    fun detectExpression(croppedBitmap: Bitmap, boundingBox: Rect) {
        // Crop gambar sesuai bounding box wajah
        if (boundingBox.width() > 0 && boundingBox.height() > 0) {
            val image = InputImage.fromBitmap(croppedBitmap, 0)

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
