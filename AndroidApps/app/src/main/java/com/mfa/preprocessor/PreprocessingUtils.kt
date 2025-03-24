package com.mfa.preprocessor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

class PreprocessingUtils {
    private val conv : Convolution = Convolution();
    private fun gaussian(x: Int, y: Int, sigma: Double): Double {
        require(!(sigma <= 0)) { "Sigma must be positive." }
        val squaredDistance = (x * x + y * y).toDouble()
        val denominator = 2 * sigma * sigma
        val exponent = -squaredDistance / denominator
        return 1 / (2 * Math.PI * sigma * sigma) * Math.exp(exponent)
    }

    fun calculateVariance(array: Array<IntArray>): Double = runBlocking {
        val rows = array.size
        val cols = array[0].size
        val totalElements = rows * cols

        if (totalElements == 0) return@runBlocking 0.0

        // Step 1: Calculate the mean in parallel
        val sum = withContext(Dispatchers.Default) {
            array.map { row ->
                async { row.sum() } // Each row is summed in parallel
            }.awaitAll().sum() // Combine results from all coroutines
        }
        val mean = sum.toDouble() / totalElements

        // Step 2: Calculate the sum of squared differences in parallel
        val sumSquaredDifferences = withContext(Dispatchers.Default) {
            array.map { row ->
                async {
                    row.sumOf { value ->
                        val diff = value - mean
                        diff * diff
                    }
                }
            }.awaitAll().sum() // Combine results from all coroutines
        }

        // Step 3: Divide by the number of elements to get the variance
        sumSquaredDifferences / totalElements
    }

    fun histogramEqualization(image: Array<IntArray>, width: Int, height: Int): Array<IntArray> {
        val histogram = IntArray(256)
        val cdf = IntArray(256)
        val equalizedImage = Array(width) { IntArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                histogram[image[x][y]]++
            }
        }
        cdf[0] = histogram[0]
        for (i in 1..255) {
            cdf[i] = cdf[i - 1] + histogram[i]
        }
        val cdfMin = cdf[0]
        val totalPixels = width * height
        val equalizationMap = IntArray(256)
        for (i in 0..255) {
            equalizationMap[i] = Math.round((cdf[i] - cdfMin) / (totalPixels - cdfMin).toFloat() * 255)
            if (equalizationMap[i] < 0) equalizationMap[i] = 0
            if (equalizationMap[i] > 255) equalizationMap[i] = 255
        }
        for (x in 0 until width) {
            for (y in 0 until height) {
                equalizedImage[x][y] = equalizationMap[image[x][y]]
            }
        }
        return equalizedImage
    }

    fun generateGaussianKernel(size: Int, sigma: Float): FloatArray {
        val arr = Array(size * size) { FloatArray(2) }
        var x = 0f
        var y = 0f
        val offset = size / 2
        for (i in 0 until size * size) {
            arr[i] = floatArrayOf(x - offset, y - offset)
            y++
            if (y >= size) {
                y = 0f
                x++
            }
        }
        val doubleResult = Arrays.stream(arr)
            .mapToDouble { n: FloatArray -> gaussian(n[0].toInt(), offset, sigma.toDouble()).toFloat().toDouble() }
            .toArray()
        val sum = Arrays.stream(doubleResult).sum()
        for (i in doubleResult.indices) {
            doubleResult[i] /= sum
        }
        val result = FloatArray(doubleResult.size)
        for (i in result.indices) {
            result[i] = doubleResult[i].toFloat()
        }
        return result
    }

    fun convolve(pixels: Array<IntArray>, kernel: FloatArray, ksize: Int): Array<IntArray> {
        return conv.convolve(pixels, kernel, ksize, ksize);
    }


    fun  isBlurry(pixels: Array<IntArray>): Boolean {
        val threshold = 650;
        val gaussianKernel = generateGaussianKernel(3, 3/6f);
        val lKernel2 : FloatArray = arrayOf(
            1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray>;
        val eqPixels = histogramEqualization(pixels, 224, 224);
        newPixels = convolve(eqPixels, gaussianKernel, 3);
        newPixels = convolve(newPixels, lKernel2, 3);
        val variance = calculateVariance(newPixels);
        println(variance);
        return variance < threshold;
    }
}