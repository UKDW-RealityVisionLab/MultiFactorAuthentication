package com.mfa.facedetector

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.camerax.BaseFaceAnalyzer
import com.mfa.camerax.GraphicOverlay
import com.mfa.camerax.RectangleOverlay
import com.mfa.view.activity.HomeActivity.Companion.TAG
import com.google.mlkit.vision.face.FaceLandmark


class MlKitEkspresiAnalyzer(private val onExpressionDetected: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    private var lastBlinkTime = 0L
    private var blinkCount = 0
    private var lastContourPositions: List<Pair<Float, Float>>? = null
    private var stableFrameCount = 0
    private val stabilityThreshold = 15     // Jumlah frame kontur tidak bergerak
    private val movementThreshold = 5.0f     // Total movement minimum agar dianggap bergerak

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return image.close()
        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces.first()

                    // Ambil contour wajah
                    val currentContour = face.getContour(FaceContour.FACE)?.points?.map { it.x to it.y } ?: emptyList()

                    if (lastContourPositions != null && currentContour.size == lastContourPositions!!.size) {
                        val totalMovement =
                            currentContour.zip(lastContourPositions!!).sumOf { (curr, last) ->
                                val dx = curr.first - last.first
                                val dy = curr.second - last.second
                                Math.sqrt((dx * dx + dy * dy).toDouble())
                            }.toFloat()
                    }

                    // ===== Ekspresi =====
                    val smilingProbability = face.smilingProbability ?: 0f
                    val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 1f
                    val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 1f
                    val yawAngle = face.headEulerAngleY
                    val pitchAngle = face.headEulerAngleX
                    val now = System.currentTimeMillis()

                    val eyeClosed = leftEyeOpenProbability < 0.4 && rightEyeOpenProbability < 0.4
                    if (eyeClosed && now - lastBlinkTime > 300) {
                        blinkCount++
                        lastBlinkTime = now
                    }
                    val expression = when {
                        smilingProbability > 0.5 && pitchAngle > 15 -> "senyum dan angkat kepala"
                        rightEyeOpenProbability < 0.4 && face.headEulerAngleZ > 15 -> "tutup mata kiri dan miring kanan"
                        smilingProbability > 0.5 && eyeClosed -> "senyum dan kedip"
                        leftEyeOpenProbability < 0.4 && face.headEulerAngleZ < -15 -> "tutup mata kanan dan miring kiri"
                        smilingProbability > 0.5 && face.headEulerAngleZ > 15 -> "senyum dan miring kanan"
                        leftEyeOpenProbability < 0.4 && face.headEulerAngleZ > 15 -> "tutup mata kanan dan miring kanan"
                        rightEyeOpenProbability < 0.4 && face.headEulerAngleZ < -15 -> "tutup mata kiri dan miring kiri"
                        yawAngle > 20 -> "hadap kiri"
                        yawAngle < -20 -> "hadap kanan"
                        pitchAngle > 15 -> "angkat kepala"
                        pitchAngle < -15 -> "tunduk (angguk)"
                        blinkCount >= 2 -> {
                            blinkCount = 0
                            "kedip dua kali"
                        }
                        face.headEulerAngleZ > 15 -> "miring kanan"
                        face.headEulerAngleZ < -15 -> "miring kiri"
                        smilingProbability > 0.5 -> "senyum"
                        eyeClosed -> "kedip"
                        leftEyeOpenProbability < 0.4 -> "tutup mata kanan"
                        rightEyeOpenProbability < 0.4 -> "tutup mata kiri"
                        else -> "neutral"
                    }

                    Log.d(TAG, "Ekspresi terdeteksi: $expression")
                    onExpressionDetected(expression)
                }
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { image.close() }
    }
}