package com.mfa.preprocessor

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

}
