package com.mfa.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCaptureException
import androidx.drawerlayout.widget.DrawerLayout
import com.mfa.R
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityFaceVerificationBinding
import com.mfa.databinding.DialogAddFaceBinding
import com.mfa.facedetector.FaceAntiSpoofing
import com.mfa.facedetector.FaceRecognizer
import com.mfa.utils.Utils
import kotlin.math.sqrt

class FaceVerificationActivity : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private val TAG = "FaceVerificationActivity"
    private lateinit var binding: ActivityFaceVerificationBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var faceRecognizer: FaceRecognizer
    private lateinit var fas: FaceAntiSpoofing

    private val EMBEDDING_THRESHOLD = 0.8 // Example threshold for cosine similarity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        cameraManager = CameraManager(
            this,
            findViewById(R.id.content_frame),
            findViewById(R.id.viewGraphicOverlay),
            this
        )

        askCameraPermission()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnFaceDetection.setOnClickListener {
            cameraManager.onTakeImage(this)
        }
    }

    private fun askCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraManager.cameraStart()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraManager.cameraStart()
        } else {
            Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTakeImageSuccess(image: Bitmap) {
        // Display confirmation dialog
        AlertDialog.Builder(this)
            .setView(DialogAddFaceBinding.inflate(layoutInflater).apply {
                capturedFace.setImageBitmap(image)
            }.root)
            .setTitle("Confirm Face")
            .setPositiveButton("OK") { _, _ ->
                if (antiSpoofDetection(image)) {
                    val newEmbedding = faceRecognizer.getEmbeddingsOfImage(image)
                    Log.d(TAG, "New embedding: ${newEmbedding.toList()}")

                    Utils.getFirebaseEmbedding().get().addOnSuccessListener { dataSnapshot ->
                        val savedEmbeddingList = (dataSnapshot.value as? List<*>)?.map { it.toString().toFloat() }?.toFloatArray()

                        if (savedEmbeddingList != null) {
                            val similarity = calculateCosineSimilarity(newEmbedding[0], savedEmbeddingList)

                            if (similarity > EMBEDDING_THRESHOLD) {
                                Toast.makeText(this, "Face verified successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Face verification failed!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "No saved embedding found!", Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error fetching saved embeddings: ${exception.message}")
                    }
                } else {
                    Toast.makeText(this, "Face is spoofed!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        val laplaceScore = fas.laplacian(faceBitmap)
        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            Toast.makeText(this, "Image too blurry!", Toast.LENGTH_LONG).show()
            return false
        } else {
            val score = fas.antiSpoofing(faceBitmap)
            if (score < FaceAntiSpoofing.THRESHOLD) {
                return true
            }
            Toast.makeText(this, "Face is spoofed!", Toast.LENGTH_LONG).show()
        }
        return false
    }

    override fun onTakeImageError(exception: ImageCaptureException) {
        Toast.makeText(this, "Error taking image: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    private fun calculateCosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        val dotProduct = vecA.zip(vecB).sumOf { (a, b) -> (a * b).toDouble() }.toFloat()
        val magnitudeA = sqrt(vecA.sumOf { (it * it).toDouble() }).toFloat()
        val magnitudeB = sqrt(vecB.sumOf { (it * it).toDouble() }).toFloat()
        return dotProduct / (magnitudeA * magnitudeB)
    }
}
