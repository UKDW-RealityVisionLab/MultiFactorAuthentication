package com.mfa.view

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mfa.camerax.CameraManager
import com.mfa.databinding.ActivityRegisterFaceBinding
import com.mfa.databinding.DialogAddFaceBinding
import com.mfa.facedetector.TFLiteFaceRecognizer
import com.mfa.utils.PreferenceUtils
import com.mfa.utils.Utils

class RegisterFaceActivity : AppCompatActivity(), CameraManager.OnTakeImageCallback {
    private val TAG = "RegisterFaceActivity"
    private lateinit var binding: ActivityRegisterFaceBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var faceRecognizer: TFLiteFaceRecognizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterFaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faceRecognizer = TFLiteFaceRecognizer(assets)
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


    private fun buttonVisibility(forStart: Boolean) {
        if (forStart) {
            binding.buttonStopCamera.visibility = View.VISIBLE
            binding.buttonStartCamera.visibility = View.INVISIBLE
        } else {
            binding.buttonStopCamera.visibility = View.INVISIBLE
            binding.buttonStartCamera.visibility = View.VISIBLE
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
        val addFaceBinding = DialogAddFaceBinding.inflate(layoutInflater)
        addFaceBinding.capturedFace.setImageBitmap(image)
        AlertDialog.Builder(this)
            .setView(addFaceBinding.root)
            .setTitle("Confirm Face")
            .setPositiveButton("OK", { dialog, which ->
                //add image to embeddings process
                val embedings: Array<FloatArray> = faceRecognizer.getEmbeddingsOfImage(image)
                Log.d(TAG, "embedings : " + embedings.toString())
                val embedingFloatList: MutableList<String> = ArrayList()
                for (value in embedings.get(0)) {
                    embedingFloatList.add(value.toString())
                }
                Utils.setFirebaseEmbedding(embedingFloatList)
                PreferenceUtils.saveFaceEmbeddings(applicationContext, embedingFloatList)
                Toast.makeText(this, "Save Face Success", Toast.LENGTH_LONG).show()
                finish()
            })
            .setNegativeButton("Cancel", { dialog, which ->
                dialog.cancel()
            })
            .show()
    }


    override fun onTakeImageError(exception: ImageCaptureException) {
        Toast.makeText(this, "onTakeImageError : " + exception.message, Toast.LENGTH_SHORT).show()
    }
}