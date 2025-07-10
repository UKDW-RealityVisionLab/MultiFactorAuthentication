package com.mfa.preprocessor

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.abs

class Convolution {
    fun padWithReplicate(input: Array<IntArray>, padY: Int, padX: Int): Array<IntArray> {
        val inputRows = input.size
        val inputCols = input[0].size
        val paddedRows = inputRows + 2 * padY
        val paddedCols = inputCols + 2 * padX

        val padded = Array(paddedRows) { IntArray(paddedCols) }

        for (i in 0 until paddedRows) {
            for (j in 0 until paddedCols) {
                val srcI = when {
                    i < padY -> 0
                    i >= padY + inputRows -> inputRows - 1
                    else -> i - padY
                }

                val srcJ = when {
                    j < padX -> 0
                    j >= padX + inputCols -> inputCols - 1
                    else -> j - padX
                }

                padded[i][j] = input[srcI][srcJ]
            }
        }

        return padded
    }

    fun convolve(
        input: Array<IntArray>,
        kernel: FloatArray,
        kernelRows: Int,
        kernelCols: Int
    ): Array<IntArray> = runBlocking {
        val inputRows = input.size
        val inputCols = input[0].size

        val padY = kernelRows / 2
        val padX = kernelCols / 2

        val paddedInput = padWithReplicate(input, padY, padX)
        val output = Array(inputRows) { IntArray(inputCols) }

        coroutineScope {
            (0 until inputRows).map { i ->
                launch(Dispatchers.Default) {
                    for (j in 0 until inputCols) {
                        var sum = 0f
                        for (ki in 0 until kernelRows) {
                            for (kj in 0 until kernelCols) {
                                val pi = i + ki
                                val pj = j + kj
                                sum += paddedInput[pi][pj] * kernel[ki * kernelCols + kj]
                            }
                        }
                        output[i][j] = abs(sum.toInt()).coerceIn(0, 255)
                    }
                }
            }.joinAll()
        }

        output
    }

    fun convolveBitmap(
        input: Bitmap,
        kernel: FloatArray,
        kernelRows: Int,
        kernelCols: Int
    ): Bitmap = runBlocking {
        val width = input.width
        val height = input.height
        val outputWidth = width - kernelCols + 1
        val outputHeight = height - kernelRows + 1
        val rChannel = Array(height) { IntArray(width) }
        val gChannel = Array(height) { IntArray(width) }
        val bChannel = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = input.getPixel(x, y)
                rChannel[y][x] = Color.red(color)
                gChannel[y][x] = Color.green(color)
                bChannel[y][x] = Color.blue(color)
            }
        }

        val channels = arrayOf(rChannel, gChannel, bChannel)
        val outputChannels = Array(3) { Array(outputHeight) { IntArray(outputWidth) } }

        coroutineScope {
            for (c in 0 until 3) {
                (0 until outputHeight).map { i ->
                    launch(Dispatchers.Default) {
                        for (j in 0 until outputWidth) {
                            var sum = 0f
                            for (ki in 0 until kernelRows) {
                                for (kj in 0 until kernelCols) {
                                    sum += channels[c][i + ki][j + kj] * kernel[ki * kernelCols + kj]
                                }
                            }
                            outputChannels[c][i][j] = abs(sum.toInt()).coerceIn(0, 255)
                        }
                    }
                }.joinAll()
            }
        }

        // Combine RGB back into bitmap
        val outputBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        for (i in 0 until outputHeight) {
            for (j in 0 until outputWidth) {
                val r = outputChannels[0][i][j]
                val g = outputChannels[1][i][j]
                val b = outputChannels[2][i][j]
                val color = Color.rgb(r, g, b)
                outputBitmap.setPixel(j, i, color)
            }
        }

        outputBitmap
    }

    fun convolveBitmapGrayScale(
        input: Bitmap,
        kernel: FloatArray,
        kernelRows: Int,
        kernelCols: Int
    ): Bitmap {
        val p = PreprocessingUtils()
        val inputArray = p.bitmapToGrayIntArray(input);
        val outputArray = convolve(inputArray, kernel, kernelRows, kernelCols);
        val out = p.grayIntArrayToBitmap(outputArray)
        return  out;
    }
}
