package com.mfa.camerax

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.camerax.CameraManager.Companion.cameraOption
import com.mfa.facedetector.EkspresiRecognizer
import com.mfa.facedetector.MlKitEkspresiAnalyzer
import com.mfa.utils.BitmapUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraEkspresi(
    private val context: Context,
    private val previewView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val onExpressionDetected: (String) -> Unit // Callback untuk ekspresi yang terdeteksi
) {
    private val TAG:String = "Camera ekspresi"
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewUseCase: Preview
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var imageAnalyzer: MlKitEkspresiAnalyzer
    private lateinit var ekspresiRecognizer: EkspresiRecognizer
    private var detection_enable = true
    var flipX: Boolean = false

    private var onCameraReady: (() -> Unit)? = null

    fun setOnCameraReadyListener(listener: () -> Unit) {
        this.onCameraReady = listener
    }

    val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }
    fun detection_disable(){
        detection_enable = false
    }
    fun detection_enable(){
        detection_enable = true
    }
    fun cameraStart() {
        ekspresiRecognizer = EkspresiRecognizer {
            onExpressionDetected("Expression detected!")
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraOption)  // Use the cameraOption variable here
                    .build()

                cameraProvider.unbindAll()
                bindPreviewUseCase(cameraSelector)
                bindImageCaptureUseCase(cameraSelector)
                bindImageAnalysisUseCase(cameraSelector)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindPreviewUseCase(cameraSelector: CameraSelector) {
        previewUseCase = Preview.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
    }
    private fun bindImageCaptureUseCase(cameraSelector: CameraSelector) {
        val activity = context as Activity
        val rotation = previewView.display.rotation

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(rotation)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
    }

    private fun bindImageAnalysisUseCase(cameraSelector: CameraSelector) {
        // Pastikan analyzer baru dibuat setiap kali kamera diubah
        imageAnalyzer = MlKitEkspresiAnalyzer { expression ->
            onExpressionDetected(expression)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)

        // Panggil callback ketika kamera siap
        onCameraReady?.invoke()
    }
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun getBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap? {
        val mediaImage = imageProxy.image ?: return null
        try {
            val bitmap = BitmapUtils.getBitmap(imageProxy)

            // Dapatkan rotasi yang diperlukan
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            Log.d(TAG, "Rotation degrees: $rotationDegrees")

            // Rotasi bitmap jika diperlukan
            return if (rotationDegrees != 0) {
                rotateBitmap(bitmap!!, rotationDegrees.toFloat())
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal konversi bitmap: ${e.message}")
            return null
        } finally {
            imageProxy.close()
        }
    }


    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
            // Untuk kamera depan, tambahkan flip horizontal
            if (CameraManager.cameraOption == CameraSelector.LENS_FACING_FRONT) {
                postScale(-1f, 1f)
            }
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    // Define a callback interface
    fun cropFace(bitmap: Bitmap, onCropComplete: (Bitmap?) -> Unit) {
        // Convert the bitmap to InputImage for ML Kit
        val image = InputImage.fromBitmap(bitmap, 0)

        // Use ML Kit to detect faces
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    Log.e(TAG, "No faces detected")
                    onCropComplete(null) // No face detected, callback with null
                    return@addOnSuccessListener
                }

                // Get the bounding box for the first detected face
                val face = faces[0]
                val bounds = face.boundingBox

                // Make sure the bounds are valid
                if (bounds.width() <= 0 || bounds.height() <= 0) {
                    Log.e(TAG, "Invalid bounding box")
                    onCropComplete(null) // Invalid bounding box, callback with null
                    return@addOnSuccessListener
                }

                // Crop the bitmap based on the bounding box
                val croppedBitmap = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())

                // Optionally, scale the cropped bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 256, 256, true)

                // Call the callback with the scaled cropped bitmap
                onCropComplete(scaledBitmap)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed: ${e.message}")
                onCropComplete(null) // If face detection fails, callback with null
            }
    }



    fun onTakeImage(callback: OnTakeImageCallback) {
        Log.d(TAG, "Memulai proses pengambilan gambar...")

        try {
            if (!this::imageCapture.isInitialized) {
                Log.e(TAG, "ImageCapture belum diinisialisasi")
                callback.onTakeImageError(
                    ImageCaptureException(
                        ImageCapture.ERROR_INVALID_CAMERA,
                        "ImageCapture not initialized",
                        null
                    )
                )
                return
            }

            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        Log.d(TAG, "ImageProxy diterima, format: ${imageProxy.format}")
                        try {
                            val bitmap = getBitmapFromImageProxy(imageProxy)
                            if (bitmap == null) {
                                callback.onTakeImageError(
                                    ImageCaptureException(
                                        ImageCapture.ERROR_CAPTURE_FAILED,
                                        "Failed to convert image to bitmap",
                                        null
                                    )
                                )
                                return
                            }

//                            val croppedBitmap = cropFace(bitmap)
//                            callback.onTakeImageSuccess(croppedBitmap)
////                            callback.onTakeImageSuccess(bitmap)
                            cropFace(bitmap) { croppedBitmap ->
                                if (croppedBitmap != null) {
                                    // If cropping was successful, pass it to the callback
                                    callback.onTakeImageSuccess(croppedBitmap)
                                } else {
                                    // If cropping failed, handle it
                                    callback.onTakeImageError(
                                        ImageCaptureException(
                                            ImageCapture.ERROR_CAPTURE_FAILED,
                                            "Face cropping failed",
                                            null
                                        )
                                    )
                                }
                            }

                        } catch (e: Exception) {
                            callback.onTakeImageError(
                                ImageCaptureException(
                                    ImageCapture.ERROR_CAPTURE_FAILED,
                                    "Image processing failed",
                                    e
                                )
                            )
                        } finally {
                            imageProxy.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        callback.onTakeImageError(
                            ImageCaptureException(
                                exception.imageCaptureError,
                                exception.message ?: "Unknown capture error",
                                exception.cause
                            )
                        )
                    }
                }
            )
        } catch (e: Exception) {
            callback.onTakeImageError(
                ImageCaptureException(
                    ImageCapture.ERROR_UNKNOWN,
                    "Unexpected error: ${e.message}",
                    e
                )
            )
        }
    }


    private fun flipBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun detectFace(bitmap: Bitmap, onFacesDetected: (List<Face>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                onFacesDetected(faces)
            }
            .addOnFailureListener { e ->
                Log.e("FaceVerification", "Error saat deteksi wajah: ${e.message}")
            }
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
        cameraExecutor.shutdown()
    }

    // In CameraEkspresi.kt
    fun changeCamera(): Boolean {
        try {
            // Save current state
            val previousCameraOption = cameraOption

            // Stop current camera
            cameraStop()

            // Toggle camera option
            cameraOption = if (cameraOption == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }

            flipX = (cameraOption == CameraSelector.LENS_FACING_FRONT)

            // Restart with new camera
            cameraStart()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error changing camera: ${e.message}")
            return false
        }
    }
    interface OnTakeImageCallback {
        //fun onTakeBitmap(bitmap:Bitmap?)
        fun onTakeImageSuccess(bitmap: Bitmap?)
        fun onTakeImageError(exception: ImageCaptureException)
    }
}
