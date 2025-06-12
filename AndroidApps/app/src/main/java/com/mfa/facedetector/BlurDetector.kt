package com.mfa.facedetector

import android.graphics.Bitmap
import android.util.Log
import com.mfa.preprocessor.PreprocessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BlurDetector {
    fun isBlurry(pixels: Array<IntArray>): Boolean {
        val threshold = 1.0
        val variance = calculateLaplaceScore(pixels)
        Log.d("Is BLUR", "$variance")
        return variance < threshold
    }

    fun isBlurry(bitmap: Bitmap) : Boolean {
        val p = PreprocessingUtils()
        val array = p.convertRawGreyImg(bitmap)
        return isBlurry(array)
    }

    private fun  calculateLaplaceScore(pixels: Array<IntArray>): Double {
        val p = PreprocessingUtils()
        var laplaceKernel : FloatArray = arrayOf(
            1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray>
        val gaussianKernel = p.generateGaussianKernel(9, 1.5f);
        newPixels = p.convolve(pixels, gaussianKernel, 9)
        newPixels = p.convolve(newPixels, laplaceKernel, 3)

        val mean = calculateAverageBrightness(newPixels)
        val variance = calculateVariance(newPixels)
        return Math.sqrt(variance) / mean;
    }

    fun calculateVariance(array: Array<IntArray>): Double = runBlocking {
        val rows = array.size
        val cols = array[0].size
        val totalElements = rows * cols

        if (totalElements == 0) return@runBlocking 0.0
        val sum = withContext(Dispatchers.Default) {
            array.map { row ->
                async { row.sum() }
            }.awaitAll().sum()
        }
        val mean = sum.toDouble() / totalElements
        val sumSquaredDifferences = withContext(Dispatchers.Default) {
            array.map { row ->
                async {
                    row.sumOf { value ->
                        val diff = value - mean
                        diff * diff
                    }
                }
            }.awaitAll().sum()
        }
        sumSquaredDifferences / totalElements
    }


    private fun calculateAverageBrightness(pixels: Array<IntArray>): Double {
        if (pixels.isEmpty() || pixels[0].isEmpty()) return 0.0 // Edge case
        var totalBrightness = 0.0
        val width = pixels.size
        val height = pixels[0].size
        for (x in 0 until width) {
            for (y in 0 until height) {
                totalBrightness += pixels[x][y] // Clamp to valid range
            }
        }
        return totalBrightness / (width * height)
    }
}