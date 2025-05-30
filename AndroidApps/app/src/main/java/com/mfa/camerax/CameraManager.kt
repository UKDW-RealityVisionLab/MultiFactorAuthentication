package com.mfa.camerax

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.mfa.facedetector.MlKitAnalyzer
import com.mfa.utils.BitmapUtils
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,
    private val graphicOverlay: GraphicOverlay<*>,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var imageAnalyzer: MlKitAnalyzer
    private lateinit var cameraProvider: ProcessCameraProvider

    //usecases
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var previewUseCase: Preview
    private lateinit var camera: Camera
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var cameraSelector: CameraSelector
    var flipX: Boolean = false

    fun cameraStart() {
        val cameraProcessProvider = ProcessCameraProvider.getInstance(context)
        //After requesting a CameraProvider, verify that its initialization succeeded when the view is created.
        cameraProcessProvider.addListener(
            {
                // Camera provider is now guaranteed to be available
                cameraProvider = cameraProcessProvider.get()

                // Choose the camera by requiring a lens facing
                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraOption)
                    .build()
                //clear prev usecase binding
                cameraProvider.unbindAll()

                bindPreviewUseCase()
                bindImageCaptureUseCase()
                bindImageAnalysisUseCase()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindPreviewUseCase() {
        /*            camera = cameraProvider.bindToLifecycle(
                          lifecycleOwner,
                          cameraSelector,
                          previewUseCase,
                          imageAnalysis
                      )
                      previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
          */
        previewUseCase = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
    }

    private fun bindImageCaptureUseCase() {
        val activity: Activity = context as Activity
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(activity.windowManager.defaultDisplay.rotation)
            .build()
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
    }

    private fun bindImageAnalysisUseCase() {
        imageAnalyzer = MlKitAnalyzer(graphicOverlay)
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, imageAnalyzer)
            }
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
    }

    fun onTakeImage(callback: OnTakeImageCallback) {
//        Handler(Looper.getMainLooper()).post {
//            callback.onTakeImageStart()
//        }
        imageCapture.takePicture(cameraExecutor, object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                @SuppressLint("UnsafeOptInUsageError")
                val image = imageProxy.image
                if (image != null) {
                    try {
                        // Process the image immediately
                        val sourceImageBitmap: Bitmap = BitmapUtils.convertJPEGImageProxyJPEGToBitmap(imageProxy)
                        val imageRotation = imageProxy.imageInfo.rotationDegrees
                        val inputImage = InputImage.fromBitmap(sourceImageBitmap, imageRotation)

                        imageAnalyzer.detectInImage(inputImage).addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                val face: Face = faces[0]
                                val frameBitmap = BitmapUtils.rotateBitmap(sourceImageBitmap, imageRotation, flipX, false)
                                val boundingBox = RectF(face.boundingBox)
                                var croppedFace = BitmapUtils.getCropBitmapByCPU(frameBitmap, boundingBox)

                                if (flipX) {
                                    croppedFace = BitmapUtils.rotateBitmap(croppedFace, 0, flipX, false)
                                }

                                // Return the result to the main thread for UI updates
                                Handler(Looper.getMainLooper()).post {
                                    callback.onTakeImageSuccess(croppedFace)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Handler(Looper.getMainLooper()).post {
                                callback.onTakeImageError(exception)
                            }
                        }
                    } finally {
                        // Close the ImageProxy after processing is complete
                        imageProxy.close()
                    }
                } else {
                    imageProxy.close() // Close the ImageProxy if the image is null
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Handler(Looper.getMainLooper()).post {
                    callback.onTakeImageError(exception)
                }
            }
        })
    }

    fun changeCamera() {
        cameraStop()
//        cameraOption = if (cameraOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
//        else CameraSelector.LENS_FACING_BACK
        if (cameraOption == CameraSelector.LENS_FACING_BACK) {
            cameraOption = CameraSelector.LENS_FACING_FRONT
            flipX = true
        } else {
            cameraOption = CameraSelector.LENS_FACING_BACK
            flipX = false
        }

        CameraUtils.toggleSelector()
        cameraStart()
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
    }

    interface OnTakeImageCallback {
        fun onTakeImageSuccess(image : Bitmap)
        fun onTakeImageError(exception: Exception)
//        fun onTakeImageStart()
    }

    companion object {
        private const val TAG: String = "CameraManager"
        var cameraOption: Int = CameraSelector.LENS_FACING_FRONT
    }
}