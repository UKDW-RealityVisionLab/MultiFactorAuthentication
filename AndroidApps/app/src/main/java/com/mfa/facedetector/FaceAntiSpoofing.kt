package com.mfa.facedetector

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.mfa.utils.Utils
import org.tensorflow.lite.Interpreter
import java.io.IOException
import kotlin.math.abs

//implementation of mode from
//https://github.com/yaojieliu/CVPR2019-DeepTreeLearningForZeroShotFaceAntispoofing
// only supports print attack and replay attack
class FaceAntiSpoofing @Throws(IOException::class) constructor(assetManager: AssetManager) {

    private var interpreter: Interpreter

    init {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        interpreter = Interpreter(Utils.loadModelFile(assetManager, MODEL_FILE), options)
    }

    fun antiSpoofing(bitmap: Bitmap): Float {
        // Resize the face to 256X256, because the shape of the placeholder required for the feed data below is (1, 256, 256, 3)
        val bitmapScale = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)
        val img: Array<Array<FloatArray>> = normalizeImage(bitmapScale)
        val input: Array<Array<Array<FloatArray>>?> = arrayOfNulls(1)
        input[0] = img
        val clss_pred = Array(1) { FloatArray(8) }
        val leaf_node_mask = Array(1) { FloatArray(8) }
        val outputs: MutableMap<Int, Any> = HashMap()
        outputs[interpreter.getOutputIndex("Identity")] = clss_pred
        outputs[interpreter.getOutputIndex("Identity_1")] = leaf_node_mask
        interpreter.runForMultipleInputsOutputs(arrayOf<Any>(input), outputs)

        Log.i(
            TAG, "[" + clss_pred[0][0] + ", " + clss_pred[0][1] + ", "
                    + clss_pred[0][2] + ", " + clss_pred[0][3] + ", " + clss_pred[0][4] + ", "
                    + clss_pred[0][5] + ", " + clss_pred[0][6] + ", " + clss_pred[0][7] + "]"
        )
        Log.i(
            TAG, "[" + leaf_node_mask[0][0] + ", " + leaf_node_mask[0][1] + ", "
                    + leaf_node_mask[0][2] + ", " + leaf_node_mask[0][3] + ", " + leaf_node_mask[0][4] + ", "
                    + leaf_node_mask[0][5] + ", " + leaf_node_mask[0][6] + ", " + leaf_node_mask[0][7] + "]"
        )

        return leaf_score1(clss_pred, leaf_node_mask);
    }

    private fun leaf_score1(clss_pred: Array<FloatArray>, leaf_node_mask: Array<FloatArray>): Float {
        var score = 0f
        for (i in 0..7) {
            score += abs(clss_pred[0][i]) * leaf_node_mask[0][i]
        }
        return score
    }

    private fun leaf_score2(clss_pred: Array<FloatArray>): Float {
        return clss_pred[0][ROUTE_INDEX]
    }

    fun normalizeImage(bitmap: Bitmap): Array<Array<FloatArray>> {
        val h = bitmap.height
        val w = bitmap.width
        val floatValues = Array(h) {
            Array(w) {
                FloatArray(
                    3
                )
            }
        }

        val imageStd = 255f
        val pixels = IntArray(h * w)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, w, h)
        // Note that the height is first and then the width.
        for (i in 0 until h) {
            for (j in 0 until w) {
                val `val` = pixels[i * w + j]
                val r = ((`val` shr 16) and 0xFF) / imageStd
                val g = ((`val` shr 8) and 0xFF) / imageStd
                val b = (`val` and 0xFF) / imageStd

                val arr = floatArrayOf(r, g, b)
                floatValues[i][j] = arr
            }
        }
        return floatValues
    }

    /**
     * Laplacian algorithm to calculate sharpness
     * [laplacian as blur detection](https://medium.com/@sagardhungel/laplacian-and-its-use-in-blur-detection-fbac689f0f88)
     * @param bitmap
     * @return float
     */
    fun laplacian(bitmap: Bitmap): Int {
        // Resize the face to a size of 256X256, because the shape of the placeholder that needs feed data below is (1, 256, 256, 3)
        val bitmapScale = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)

        val laplace = arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, -4, 1), intArrayOf(0, 1, 0))
        val size = laplace.size
        val img: Array<IntArray> = Utils.convertGreyImg(bitmapScale)
        val height = img.size
        val width = img[0].size

        var score = 0
        for (x in 0 until height - size + 1) {
            for (y in 0 until width - size + 1) {
                var result = 0
                // Convolve the size*size area
                for (i in 0 until size) {
                    for (j in 0 until size) {
                        result += (img[x + i][y + j] and 0xFF) * laplace[i][j]
                    }
                }
                if (result > LAPLACE_THRESHOLD) {
                    score++
                }
            }
        }
        return score
    }

    companion object {
        private val TAG = "FaceAntiSpoofing"
        private val MODEL_FILE = "DeepTreeFaceAntiSpoofing.tflite"

        val INPUT_IMAGE_SIZE: Int = 256 // The width and height of the placeholder image that needs feed data
        val THRESHOLD: Float = 0.2f // Set a threshold, greater than this value is considered an attack

        val ROUTE_INDEX: Int = 6 // Route indices observed during training

        val LAPLACE_THRESHOLD: Int = 50 // Laplace sampling threshold
        val LAPLACIAN_THRESHOLD: Int = 700 // Picture sharpness judgment threshold
    }
}