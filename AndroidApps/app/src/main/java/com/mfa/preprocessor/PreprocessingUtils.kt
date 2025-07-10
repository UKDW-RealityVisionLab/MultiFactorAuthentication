package com.mfa.preprocessor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Arrays
import kotlin.math.exp
import kotlin.math.pow


class PreprocessingUtils {
    private val conv : Convolution = Convolution();
    companion object {
        lateinit var appContext: Context
    }
    private fun gaussian(x: Int, y: Int, sigma: Double): Double {
        require(!(sigma <= 0)) { "Sigma must be positive." }
        val squaredDistance = (x * x + y * y).toDouble()
        val denominator = 2 * sigma * sigma
        val exponent = -squaredDistance / denominator
//        return 1 / (2 * Math.PI * sigma * sigma) * exp(exponent)
        return Math.exp(exponent)
    }

    fun medianFilter(pixels: Array<IntArray>, ksize: Int) : Array<IntArray>{
        val width = pixels.size
        val height = pixels[0].size
        val out = arrayOfNulls<IntArray>(pixels.size)
        for (i in pixels.indices) {
            out[i] = Arrays.copyOf(pixels[i], pixels[i].size)
        }
        val kernel = FloatArray(ksize * ksize)
        val half_k = ksize / 2
        for (i in half_k until width - half_k) {
            for (j in half_k until height - half_k) {
                for (ki in kernel.indices) {
                    val kx = ki % ksize
                    val ky = ki / ksize
                    val offset_x = kx - half_k
                    val offset_y = ky - half_k
                    kernel[ki] =  pixels[i + offset_y][j + offset_x].toFloat()
                }
                kernel.sort()
                out[i]!![j] = kernel[ksize*ksize / 2].toInt();
            }
        }
        return out.requireNoNulls();
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

    fun generateGaussianKernel(size: Int, sigma: Float): FloatArray {
        val kernel = FloatArray(size * size)
        val mean = size / 2
        var sum = 0f

        for (x in 0 until size) {
            for (y in 0 until size) {
                val dx = x - mean
                val dy = y - mean
                println("dx=$dx dy=$dy")
                val value = gaussian(dx, dy, sigma.toDouble())
                kernel[y * size + x] = value.toFloat()
                sum += value.toFloat()
            }
        }
        // Normalize the kernel
        for (i in kernel.indices) {
            kernel[i] /= sum
        }

        return kernel
    }

    fun convertBitmapToGray(bitmap : Bitmap) : Bitmap {
        val grayIntArray = convertRawGreyImg(bitmap)
        val grayBitmap = convertArrayToBitmap(grayIntArray)
        return  grayBitmap
    }

    fun convolve(pixels: Array<IntArray>, kernel: FloatArray, ksize: Int): Array<IntArray> {
        return conv.convolve(pixels, kernel, ksize, ksize);
    }

    fun convolveGrayscale(bitmap : Bitmap, kernel: FloatArray, ksize: Int): Bitmap {
        return conv.convolveBitmapGrayScale(bitmap, kernel, ksize, ksize);
    }

    fun convolveRGB(bitmap: Bitmap, kernel: FloatArray, ksize: Int) : Bitmap {
        return conv.convolveBitmap(bitmap, kernel, ksize, ksize)
    }

    fun isBlurry(pixels: Array<IntArray>): Boolean {
        val threshold = 1.2
        val variance = isBlurryD(pixels)
        return variance < threshold
    }

    fun  isBlurryD(pixels: Array<IntArray>, kernelSize: Int = 0 ): Double {
        var lKernel2 : FloatArray = arrayOf(
            1f, 1f, 1f,
            1f, -8f, 1f,
            1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray>;
        if (kernelSize > 0) {
            val gaussianKernel = generateGaussianKernel(kernelSize, kernelSize.toFloat()/6f);
            newPixels = convolve(pixels, gaussianKernel, 5)
            newPixels = medianFilter(newPixels, 7)
            newPixels = convolve(newPixels, lKernel2, 3)
        } else {
            newPixels = convolve(pixels, lKernel2, 3)
        }
        val mean = calculateAverageBrightness(newPixels)
        val variance = calculateVariance(newPixels)
        return Math.sqrt(variance) / mean;
    }


    fun calculateAverageBrightness(pixels: Array<IntArray>): Double {
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

    fun convertRawGreyImg(bitmap: Bitmap): Array<IntArray> {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        val result = Array(h) { IntArray(w) }
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in 0 until h) {
            for (j in 0 until w) {
                val data = pixels[w * i + j]
                val red = ((data shr 16) and 0xFF)
                val green = ((data shr 8) and 0xFF)
                val blue = (data and 0xFF)

                var grey = (red.toFloat() * 0.299 + green.toFloat() * 0.587 + blue.toFloat() * 0.114).toInt()
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
                val gray = pixelArray[y][x].coerceIn(0, 255)
                pixels[y * width + x] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun saveBitmapToPicturesSubfolder(
        bitmap: Bitmap,
        fileName: String,
        subfolder: String // e.g., "sharp" or "blur"
    ): Boolean {
        val context = appContext
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$subfolder")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = context.contentResolver
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return try {
            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
                // Mark as not pending (available to gallery)
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun medianFilterRGB(bitmap: Bitmap, kernelSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Get pixels of the original bitmap
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // The median filter process
        val halfKernelSize = kernelSize / 2
        for (y in halfKernelSize until height - halfKernelSize) {
            for (x in halfKernelSize until width - halfKernelSize) {
                val rValues = mutableListOf<Int>()
                val gValues = mutableListOf<Int>()
                val bValues = mutableListOf<Int>()

                // Collect pixels from the neighborhood
                for (ky in -halfKernelSize..halfKernelSize) {
                    for (kx in -halfKernelSize..halfKernelSize) {
                        val px = pixels[(y + ky) * width + (x + kx)]
                        val r = Color.red(px)
                        val g = Color.green(px)
                        val b = Color.blue(px)

                        rValues.add(r)
                        gValues.add(g)
                        bValues.add(b)
                    }
                }

                // Sort values and get the median
                rValues.sort()
                gValues.sort()
                bValues.sort()

                val medianR = rValues[rValues.size / 2]
                val medianG = gValues[gValues.size / 2]
                val medianB = bValues[bValues.size / 2]

                // Set the new pixel value for the filtered image
                val newPixel = Color.rgb(medianR, medianG, medianB)
                resultBitmap.setPixel(x, y, newPixel)
            }
        }

        return resultBitmap
    }
    fun bilateralRGB(bitmap: Bitmap, diameter: Int, sigmaColor: Double, sigmaSpace: Double): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Get pixels of the original bitmap
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Define the kernel size (diameter of the filter)
        val halfDiameter = diameter / 2
        val gaussianSpace = Array(diameter) { DoubleArray(diameter) }

        // Precompute spatial Gaussian kernel (based on sigmaSpace)
        for (y in -halfDiameter..halfDiameter) {
            for (x in -halfDiameter..halfDiameter) {
                val distance = (x * x + y * y).toDouble()
                gaussianSpace[y + halfDiameter][x + halfDiameter] = exp(-distance / (2 * sigmaSpace.pow(2)))
            }
        }

        // Apply bilateral filter
        for (y in halfDiameter until height - halfDiameter) {
            for (x in halfDiameter until width - halfDiameter) {
                var rSum = 0.0
                var gSum = 0.0
                var bSum = 0.0
                var weightSum = 0.0

                for (dy in -halfDiameter..halfDiameter) {
                    for (dx in -halfDiameter..halfDiameter) {
                        val pixel = pixels[(y + dy) * width + (x + dx)]
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val b = Color.blue(pixel)

                        // Calculate color similarity using Gaussian function
                        val colorDistance = (r - Color.red(pixels[y * width + x])).toDouble().pow(2) +
                                (g - Color.green(pixels[y * width + x])).toDouble().pow(2) +
                                (b - Color.blue(pixels[y * width + x])).toDouble().pow(2)

                        val gaussianColor = exp(-colorDistance / (2 * sigmaColor.pow(2)))

                        // Calculate the weight using spatial and color Gaussian functions
                        val weight = gaussianSpace[dy + halfDiameter][dx + halfDiameter] * gaussianColor
                        rSum += r * weight
                        gSum += g * weight
                        bSum += b * weight
                        weightSum += weight
                    }
                }

                // Set the new pixel value based on weighted sum
                val newR = (rSum / weightSum).toInt().coerceIn(0, 255)
                val newG = (gSum / weightSum).toInt().coerceIn(0, 255)
                val newB = (bSum / weightSum).toInt().coerceIn(0, 255)

                val newPixel = Color.rgb(newR, newG, newB)
                resultBitmap.setPixel(x, y, newPixel)
            }
        }

        return resultBitmap
    }

    fun writeLog(fileName: String, logText: String): Boolean {
        val file = File(Environment.getExternalStorageDirectory(), "Documents/logs/$fileName")
        return try {
            // Create directories if they don't exist
            file.parentFile?.mkdirs()

            // Append the text to the file
            file.appendText(logText + "\n")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun loadBitmapsFromLuxSharp(): List<Bitmap> {
        val sharpDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "lux_90-100/sharp"
        )

        if (!sharpDir.exists() || !sharpDir.isDirectory) return emptyList()

        val imageExtensions = listOf("jpg", "jpeg", "png", "webp")

        return sharpDir.listFiles { file ->
            file.isFile && file.extension.lowercase() in imageExtensions
        }?.mapNotNull { file ->
            BitmapFactory.decodeFile(file.absolutePath)?.also {
                Log.d("BitmapLoad", "Loaded: ${file.name}")
            }
        } ?: emptyList()
    }


    fun loadBitmapsFromMediaStore(): List<Bitmap> {
        val context = appContext
        val bitmaps = mutableListOf<Bitmap>()

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf("Pictures/lux_90-100/sharp/")

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = Uri.withAppendedPath(collection, id.toString())

                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    bitmap?.let { bitmaps.add(it) }
                } catch (e: Exception) {
                    Log.e("MediaStoreLoader", "Error loading bitmap: $uri", e)
                }
            }
        }

        return bitmaps
    }

    fun bitmapToGrayIntArray(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val result = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val gray = Color.red(pixel)  // or green/blue, since they’re equal in grayscale
                result[y][x] = gray
            }
        }

        return result
    }

    fun grayIntArrayToBitmap(data: Array<IntArray>): Bitmap {
        val height = data.size
        val width = data[0].size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val gray = data[y][x].coerceIn(0, 255)
                val color = Color.rgb(gray, gray, gray)
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }



}