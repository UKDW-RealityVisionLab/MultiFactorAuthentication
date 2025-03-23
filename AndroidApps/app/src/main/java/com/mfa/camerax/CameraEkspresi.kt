package com.mfa.camerax

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
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
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewUseCase: Preview
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var imageAnalyzer: MlKitEkspresiAnalyzer
    private lateinit var ekspresiRecognizer: EkspresiRecognizer
    private var detection_enable = true
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
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
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
        previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
    }

    private fun bindImageCaptureUseCase(cameraSelector: CameraSelector) {
        val activity: Activity = context as Activity
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(activity.windowManager.defaultDisplay.rotation)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // Maksimalkan kualitas gambar
            .build()

        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
    }

    private fun bindImageAnalysisUseCase(cameraSelector: CameraSelector) {
        imageAnalyzer = MlKitEkspresiAnalyzer { expression ->
            onExpressionDetected(expression) // Send detected expression through the callback
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer)
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class) // Menandai penggunaan API eksperimental pada function ini
    private fun getBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap? {
        val mediaImage = imageProxy.image // Menggunakan API eksperimental, harus ditandai
        if (mediaImage == null) {
            Log.e("CameraEkspresi", "Media image null!")
            imageProxy.close()
            return null
        }
        return BitmapUtils.getBitmap(imageProxy) // Menggunakan API eksperimental, harus ditandai
    }

    fun onTakeImage(callback: OnTakeImageCallback) {
        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val bitmap = getBitmapFromImageProxy(imageProxy)
                imageProxy.close()
                bitmap?.let { img ->
                    detectFace(img) { faces ->
                        faces.firstOrNull()?.let { face ->
                            val boundingBox = face.boundingBox
                            Log.d("CameraEkspresi", "Bounding Box Verifikasi: $boundingBox")
                            val adjustedBoundingBox = RectF(
                                boundingBox.left.toFloat().coerceAtLeast(0f),
                                boundingBox.top.toFloat().coerceAtLeast(0f),
                                boundingBox.right.toFloat().coerceAtMost(img.width.toFloat()),
                                boundingBox.bottom.toFloat().coerceAtMost(img.height.toFloat())
                            )
                            var croppedBitmap = BitmapUtils.getCropBitmapByCPU(img, adjustedBoundingBox)

                            if (CameraManager.cameraOption == CameraSelector.LENS_FACING_FRONT) {
                                croppedBitmap = flipBitmap(croppedBitmap)
                            }

                            croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 256, 256, false)

                            Log.d("CameraEkspresi", "Verifikasi - Ukuran Cropped Face: ${croppedBitmap.width}x${croppedBitmap.height}")

                            callback.onTakeImageSuccess(croppedBitmap)
                        }
                    }
                }
            }
        })
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

    interface OnTakeImageCallback {
        //fun onTakeBitmap(bitmap:Bitmap?)
        fun onTakeImageSuccess(bitmap: Bitmap?)
        fun onTakeImageError(exception: ImageCaptureException)
    }
}
