package com.mfa.camerax

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,
    private val graphicOverlay: GraphicOverlay<*>,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var camera: Camera
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var cameraSelector: CameraSelector

    init {
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraOption)
            .build()
    }

    fun cameraStart() {
        val cameraProcessProvider = ProcessCameraProvider.getInstance(context)

        cameraProcessProvider.addListener(
            {
                cameraProvider = cameraProcessProvider.get()
                /*   previewUseCase = Preview.Builder().build()*/

                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, MlKitAnalyzer(graphicOverlay))
                    }

                setCameraConfig()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun bindPreviewUseCase() {
        val previewUseCase: Preview = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
    }

    fun bindAnalysisUseCase() {
        val activity: Activity = context as Activity
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(activity.windowManager.defaultDisplay.rotation)
            .build()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
    }

    fun onTakeImage() {
        imageCapture.takePicture(cameraExecutor, object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                @SuppressLint("UnsafeOptInUsageError")
                val image = imageProxy.image
                var inputImage: InputImage? = null
                val sourceImageBitmap: Bitmap = BitmapUtils.convertJPEGImageProxyJPEGToBitmap(imageProxy)
                val imageRotation = imageProxy.imageInfo.rotationDegrees
                if (image != null) {
                    inputImage = InputImage.fromBitmap(sourceImageBitmap, imageRotation)
                }
            }

            override fun onError(exception: ImageCaptureException) {
            }
        })
    }

    private fun setCameraConfig() {
        try {
            cameraProvider.unbindAll()
            /*            camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalysis
                        )
                        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                        */
            bindPreviewUseCase()
            bindAnalysisUseCase()
        } catch (e: Exception) {
            Log.e(TAG, "setCameraConfig : $e")
        }
    }

    fun changeCamera() {
        cameraStop()
        cameraOption = if (cameraOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK
        CameraUtils.toggleSelector()
        cameraStart()
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
    }

    companion object {
        private const val TAG: String = "CameraManager"
        var cameraOption: Int = CameraSelector.LENS_FACING_FRONT
    }
}