package com.mfa.facedetector

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.camerax.BaseFaceAnalyzer
import com.mfa.camerax.GraphicOverlay
import com.mfa.camerax.RectangleOverlay
import com.mfa.view.activity.HomeActivity.Companion.TAG

class MlKitEkspresiAnalyzer(private val onExpressionDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces.first()
                    val smilingProbability = face.smilingProbability ?: 0f
                    val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f
                    val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f
                    val yawAngle = face.headEulerAngleY

                    // Menentukan ekspresi berdasarkan probabilitas
                    val expression = when {
                        yawAngle > 15 -> "hadap kiri"
                        yawAngle < -15 -> "hadap kanan"
                        smilingProbability > 0.5 -> "senyum"
                        leftEyeOpenProbability < 0.5 && rightEyeOpenProbability < 0.5 -> "kedip"
                        leftEyeOpenProbability < 0.5 -> "tutup mata kanan"
                        rightEyeOpenProbability < 0.5 -> "tutup mata kiri"
                        else -> "neutral"
                    }
                    onExpressionDetected(expression)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
            .addOnCompleteListener {
                image.close()
            }
    }
}
