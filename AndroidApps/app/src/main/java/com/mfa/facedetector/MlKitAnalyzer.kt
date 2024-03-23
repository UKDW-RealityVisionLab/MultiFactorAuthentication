package com.mfa.facedetector

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.camerax.BaseFaceAnalyzer
import com.mfa.camerax.GraphicOverlay
import com.mfa.camerax.RectangleOverlay

/*ML Kit Analyzer is an implementation of the ImageAnalysis.Analyzer interface.
 It overrides the default target resolution (if needed) to optimize for ML Kit usage,
  handles the coordinate transformations, and passes the frames to ML Kit,
   which returns the aggregated analysis results.
   https://developer.android.com/media/camera/camerax/mlkitanalyzer*/
class MlKitAnalyzer(
    private val overlay: GraphicOverlay<*>
) : BaseFaceAnalyzer<List<Face>>(){

    override val graphicOverlay: GraphicOverlay<*>
        get() = overlay

    private val cameraOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(cameraOptions)

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
       try {
           detector.close()
       } catch (e : Exception) {
           Log.e(TAG , "stop : $e")
       }
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay<*>, rect: Rect) {
        graphicOverlay.clear()
        results.forEach {
            val faceGraphic = RectangleOverlay(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "onFailure : $e")
    }

    companion object {
        private const val TAG = "CameraAnalyzer"
    }
}