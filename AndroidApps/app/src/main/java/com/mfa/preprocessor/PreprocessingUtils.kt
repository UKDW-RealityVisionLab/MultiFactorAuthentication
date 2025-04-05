package com.mfa.preprocessor

import android.graphics.Bitmap
import android.widget.Toast
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
        val histogram = IntArray(256) // Histogram for pixel values 0 to 255
        val cdf = IntArray(256) // Cumulative Distribution Function
        val equalizedImage = Array(height) { IntArray(width) }

        // Calculate the histogram
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Clamp the pixel value between 0 and 255
                val pixelValue = image[y][x].coerceIn(0, 255)
                histogram[pixelValue]++
            }
        }

        // Calculate the CDF (Cumulative Distribution Function)
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

        // Apply the equalization map to get the equalized image
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelValue = image[y][x].coerceIn(0, 255)
                equalizedImage[y][x] = equalizationMap[pixelValue]
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
        val threshold = 500;
        val gaussianKernel = generateGaussianKernel(3, 3f/6f);
        val lKernel2 : FloatArray = arrayOf(
            1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray>;
        val eqPixels = histogramEqualization(pixels, pixels[0].size, pixels.size);
        newPixels = convolve(eqPixels, gaussianKernel, 3);
        newPixels = convolve(newPixels, lKernel2, 3);
        val variance = calculateVariance(newPixels);
        println(variance);
//        Toast.makeText(this, "${variance}", Toast.LENGTH_SHORT).show();
        return variance < threshold;
    }

    fun  isBlurryD(pixels: Array<IntArray>): Double {
        val threshold = 500;
        val gaussianKernel = generateGaussianKernel(3, 3f/6f);
        val lKernel2 : FloatArray = arrayOf(
            1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray>;
        val eqPixels = histogramEqualization(pixels, pixels[0].size, pixels.size);
        newPixels = convolve(eqPixels, gaussianKernel, 3);
        newPixels = convolve(newPixels, lKernel2, 3);
        val variance = calculateVariance(newPixels);
        println(variance);
//        Toast.makeText(this, "${variance}", Toast.LENGTH_SHORT).show();
        return variance;
    }

    fun convertRawGreyImg(bitmap: Bitmap): Array<IntArray> {
        val w = bitmap.width
        val h = bitmap.height

        val pixels = IntArray(h * w)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val result = Array(h) { IntArray(w) }
        for (i in 0 until h) {
            for (j in 0 until w) {
                val data = pixels[w * i + j]

                val red = ((data shr 16) and 0xFF)
                val green = ((data shr 8) and 0xFF)
                val blue = (data and 0xFF)

                var grey = (red.toFloat() * 0.3 + green.toFloat() * 0.59 + blue.toFloat() * 0.11).toInt()
                result[i][j] = grey
            }
        }
        return result
    }

    fun convertGreyImg(bitmap: Bitmap): Array<IntArray> {
        val w = bitmap.width
        val h = bitmap.height

        val pixels = IntArray(h * w)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val result = Array(h) { IntArray(w) }
        val alpha = 0xFF shl 24
        for (i in 0 until h) {
            for (j in 0 until w) {
                val data = pixels[w * i + j]

                val red = ((data shr 16) and 0xFF)
                val green = ((data shr 8) and 0xFF)
                val blue = (data and 0xFF)

                var grey = (red.toFloat() * 0.3 + green.toFloat() * 0.59 + blue.toFloat() * 0.11).toInt()
                grey = alpha or (grey shl 16) or (grey shl 8) or grey
                result[i][j] = grey
            }
        }
        return result
    }

    fun convertArrayToBitmap(pixelArray: Array<IntArray>): Bitmap {
        val height = pixelArray.size
        val width = pixelArray[0].size

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        for (y in pixelArray.indices) {
            for (x in pixelArray[0].indices) {
                val gray = pixelArray[y][x].coerceIn(0, 255) // Ensure valid grayscale range
                pixels[y * width + x] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }



}