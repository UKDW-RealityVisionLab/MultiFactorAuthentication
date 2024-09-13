package com.mfa.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityCaptureFaceBinding
import com.mfa.databinding.DialogAddFaceBinding
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
//        binding.buttonStopCamera.setOnClickListener {
//            cameraManager.cameraStop()
//            buttonVisibility(false)
//        }
//        binding.buttonStartCamera.setOnClickListener {
//            cameraManager.cameraStart()
//            buttonVisibility(true)
//        }
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
        //todo : dismiss loading screen
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)
        AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Confirm Face")
            .setPositiveButton("OK") { dialog, which ->
                //check face spoof
                if (antiSpoofDetection(image)) {
                    //add image to embeddings process
                    val embedings: Array<FloatArray> = faceRecognizer.getEmbeddingsOfImage(image)
                    Log.d(TAG, "embedings : $embedings")
                    val embedingFloatList = ArrayList<String>()
                    for (value in embedings.get(0)) {
                        embedingFloatList.add(value.toString())
                    }
                    Toast.makeText(this, "Save Face Success", Toast.LENGTH_LONG).show()
                    val intent = Intent()
                    intent.putStringArrayListExtra(EXTRA_FACE_EMBEDDING, embedingFloatList)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                setResult(RESULT_CANCELED)
                finish()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    private fun antiSpoofDetection(faceBitmap: Bitmap): Boolean {
        //preprocessing part
        val laplaceScore: Int = fas.laplacian(faceBitmap)
        if (laplaceScore < FaceAntiSpoofing.LAPLACIAN_THRESHOLD) {
            Toast.makeText(this, "Image too blurry!", Toast.LENGTH_LONG).show()
        } else {
            // Liveness detection
            val start = System.currentTimeMillis()
            val score = fas.antiSpoofing(faceBitmap)
            val end = System.currentTimeMillis()
            Log.d(TAG, "Spoof detection process time : " + (end - start))
            if (score < FaceAntiSpoofing.THRESHOLD) {
                return true
            }
            Toast.makeText(this, "Face are spoof!", Toast.LENGTH_LONG).show()
        }
        return false
    }


    override fun onTakeImageError(exception: ImageCaptureException) {
        Toast.makeText(this, "onTakeImageError : " + exception.message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        val EXTRA_FACE_EMBEDDING = "EXTRA_FACE_EMBEDDING"
    }
}