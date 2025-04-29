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

    private fun euclideanDistance(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {
        val dx = p1.first - p2.first
        val dy = p1.second - p2.second
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    private fun List<Pair<Float, Float>>.averageXY(): Pair<Float, Float> {
        val xAvg = this.map { it.first }.average().toFloat()
        val yAvg = this.map { it.second }.average().toFloat()
        return xAvg to yAvg
    }



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
                    val upperLipTop = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
                    val lowerLipBottom = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points

                    val boundingBoxHeight = face.boundingBox.height().toFloat()
                    var mouthOpen = false
                    var mouthGap = 0f
                    var mouthOpenRatio = 0f
                    if (!upperLipTop.isNullOrEmpty() && !lowerLipBottom.isNullOrEmpty()) {
                        val upperIndexes = listOf(6, 7, 8)
                        val lowerIndexes = listOf(6, 7, 8)

                        val upperAvg = upperIndexes.mapNotNull { upperLipTop.getOrNull(it) }.map { it.x to it.y }.averageXY()
                        val lowerAvg = lowerIndexes.mapNotNull { lowerLipBottom.getOrNull(it) }.map { it.x to it.y }.averageXY()

                        mouthGap = euclideanDistance(upperAvg, lowerAvg)
                        mouthOpenRatio = mouthGap / boundingBoxHeight
                        mouthOpen = mouthOpenRatio > 0.20f // Threshold rasio (0.18 bisa disesuaikan)

                        Log.d(TAG, "Mouth gap: $mouthGap, Face height: $boundingBoxHeight, Ratio: $mouthOpenRatio, Mouth open? $mouthOpen")
                    }


                    val expression = when {
                        mouthOpen && smilingProbability < 0.3f && face.headEulerAngleZ > 15 -> "kaget dan miringkan kepala ke kanan"
                        mouthOpen && smilingProbability < 0.3f && face.headEulerAngleZ < -15 -> "kaget dan miringkan kepala ke kiri"
                        mouthOpen && smilingProbability < 0.3f &&
                                leftEyeOpenProbability > 0.6f && rightEyeOpenProbability > 0.6f -> "kaget (buka mulut)"
                        smilingProbability > 0.5 && pitchAngle > 15 -> "senyum dan angkat kepala"
                        rightEyeOpenProbability < 0.4 && face.headEulerAngleZ > 15 -> "tutup mata kiri dan miringkan kepala ke kanan"
                        smilingProbability > 0.5 && eyeClosed -> "senyum dan kedip"
                        leftEyeOpenProbability < 0.4 && face.headEulerAngleZ < -15 -> "tutup mata kanan dan miringkan kepala ke kiri"
                        smilingProbability > 0.5 && face.headEulerAngleZ > 15 -> "senyum dan miringkan kepala ke kanan"
                        smilingProbability > 0.5 && face.headEulerAngleZ < -15 -> "senyum dan miringkan kepala ke kiri"
                        leftEyeOpenProbability < 0.4 && face.headEulerAngleZ > 15 -> "tutup mata kanan dan miringkan kepala ke kanan"
                        rightEyeOpenProbability < 0.4 && face.headEulerAngleZ < -15 -> "tutup mata kiri dan miringkan kepala ke kiri"
                        yawAngle > 20 -> "hadap kiri"
                        yawAngle < -20 -> "hadap kanan"
                        pitchAngle > 15 -> "angkat kepala"
                        pitchAngle < -15 -> "tunduk (angguk)"
                        blinkCount >= 2 -> {
                            blinkCount = 0
                            "kedip dua kali"
                        }
                        face.headEulerAngleZ > 15 -> "miringkan kepala ke kanan"
                        face.headEulerAngleZ < -15 -> "miringkan kepala ke kiri"
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