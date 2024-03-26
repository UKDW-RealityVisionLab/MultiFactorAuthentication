package com.mfa.facedetector

import android.content.res.AssetManager
import android.graphics.Bitmap
import com.mfa.utils.Utils
import org.tensorflow.lite.Interpreter
import java.io.IOException
import kotlin.jvm.Throws


class FaceRecognizer @Throws(IOException::class) constructor(assetManager: AssetManager) {
    //this model only have 1 face output
    private val MODEL_FILE = "mobile_face_net.tflite"

    // The image width and height of the placeholder that requires feed data
    val INPUT_IMAGE_SIZE: Int = 112

    // Set a threshold value. If the value is greater than this value, it is considered to be the same person.
    val THRESHOLD: Float = 0.8f
    private val EMBEEDINGS_SIZE = 192

    private var interpreter: Interpreter

    init {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        interpreter = Interpreter(Utils.loadModelFile(assetManager, MODEL_FILE), options)
    }

    fun compare(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        // Resize the face to 112X112, because the shape of the placeholder that requires feed data below is (2, 112, 112, 3)
        val bitmapScale1 = Bitmap.createScaledBitmap(bitmap1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)
        val bitmapScale2 = Bitmap.createScaledBitmap(bitmap2, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)

        val datasets: Array<Array<Array<FloatArray>>> = getTwoImageDatasets(bitmapScale1, bitmapScale2)
        val embeddings = Array(2) { FloatArray(EMBEEDINGS_SIZE) }
        interpreter.run(datasets, embeddings)
        Utils.l2Normalize(embeddings, 1e-10)
        return evaluate(embeddings)
    }

    fun getEmbeddingsOfImage(bitmapImage: Bitmap): Array<FloatArray> {
        val bitmapScale = Bitmap.createScaledBitmap(bitmapImage, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)
        val datasets: Array<Array<Array<FloatArray>>> = getImagesDataset(bitmapScale)
        val embeddings = Array(1) { FloatArray(EMBEEDINGS_SIZE) }
        interpreter.run(datasets, embeddings)
        val normalizedEmbendings = Utils.l2Normalize(embeddings, 1e-10)
        return normalizedEmbendings
    }

    /**
     * Calculate the similarity between two images, using l2 loss
     * @param embeddings
     * @return
     */
    private fun evaluate(embeddings: Array<FloatArray>): Float {
        val embeddings1 = embeddings[0]
        val embeddings2 = embeddings[1]
        var dist = 0.0

        for (i in 0..191) {
            dist += Math.pow((embeddings1[i] - embeddings2[i]).toDouble(), 2.0)
        }
        var same = 0f
        for (i in 0..399) {
            val threshold = 0.01f * (i + 1)
            if (dist < threshold) {
                same += (1.0 / 400).toFloat()
            }
        }
        return same
    }

    private fun getImagesDataset(vararg bitmaps: Bitmap): Array<Array<Array<FloatArray>>> {
        val ddims = intArrayOf(bitmaps.size, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, 3)
        val datasets = Array(ddims[0]) {
            Array(ddims[1]) {
                Array(ddims[2]) {
                    FloatArray(ddims[3])
                }
            }
        }
        for (i in 0 until ddims[0]) {
            val bitmap = bitmaps[i]
            datasets[i] = Utils.normalizeImage(bitmap)
        }
        return datasets
    }

    /**
     * Convert two images into normalized data
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    private fun getTwoImageDatasets(bitmap1: Bitmap, bitmap2: Bitmap): Array<Array<Array<FloatArray>>> {
        val bitmaps = arrayOf(bitmap1, bitmap2)

        val ddims = intArrayOf(bitmaps.size, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, 3)
        val datasets = Array(ddims[0]) {
            Array(ddims[1]) {
                Array(ddims[2]) {
                    FloatArray(ddims[3])
                }
            }
        }

        for (i in 0 until ddims[0]) {
            val bitmap = bitmaps[i]
            datasets[i] = Utils.normalizeImage(bitmap)
        }
        return datasets
    }
}