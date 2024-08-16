package com.mfa.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityCaptureFaceBinding
import com.mfa.facedetector.FaceAntiSpoofing
import com.mfa.facedetector.FaceRecognizer

class FaceProcessorActivity : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private val TAG = "RegisterFaceActivity"
    private lateinit var binding: ActivityCaptureFaceBinding
    private lateinit var cameraManager: CameraManager

    private lateinit var faceRecognizer: FaceRecognizer // face recognizer
    private lateinit var fas: FaceAntiSpoofing // face antispoof
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faceRecognizer = FaceRecognizer(assets)
        fas = FaceAntiSpoofing(assets)
        cameraManager = CameraManager(
            this,
            binding.viewCameraPreview,
            binding.viewGraphicOverlay,
            this
        )

        askCameraPermission()
        buttonClicks()
    }

    private fun buttonClicks() {
        binding.buttonTurnCamera.setOnClickListener {
            cameraManager.changeCamera()
        }
        binding.buttonStopCamera.setOnClickListener {
            //todo : show loading screen when processing
            cameraManager.onTakeImage(this)
        }
    }

    private fun askCameraPermission() {
        if (arrayOf(android.Manifest.permission.CAMERA).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            cameraManager.cameraStart()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraManager.cameraStart()
        } else {
            Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTakeImageSuccess(image: Bitmap) {
        // Handle image processing directly without confirmation dialog
        if (antiSpoofDetection(image)) {
            // Process and get embeddings
            val embeddings: Array<FloatArray> = faceRecognizer.getEmbeddingsOfImage(image)
            Log.d(TAG, "Embeddings: ${embeddings.joinToString()}")
            val embeddingFloatList = embeddings[0].map { it.toString() }.toCollection(ArrayList())

            // Pass embeddings back to previous activity
            val intent = Intent().apply {
                putStringArrayListExtra(EXTRA_FACE_EMBEDDING, embeddingFloatList)
            }
            setResult(RESULT_OK, intent)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        // Preprocessing part
        val laplaceScore: Int = fas.laplacian(faceBitmap)
        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            Toast.makeText(this, "Image too blurry!", Toast.LENGTH_LONG).show()
            return false
        } else {
            // Liveness detection
            val start = System.currentTimeMillis()
            val score = fas.antiSpoofing(faceBitmap)
            val end = System.currentTimeMillis()
            Log.d(TAG, "Spoof detection process time: ${end - start} ms")
            if (score < FaceAntiSpoofing.THRESHOLD) {
                return true
            }
            Toast.makeText(this, "Face appears to be a spoof!", Toast.LENGTH_LONG).show()
            return false
        }
    }

    override fun onTakeImageError(exception: ImageCaptureException) {
        Toast.makeText(this, "Image capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        val EXTRA_FACE_EMBEDDING = "EXTRA_FACE_EMBEDDING"
    }
}
