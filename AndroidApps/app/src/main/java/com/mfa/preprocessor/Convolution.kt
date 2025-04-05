package com.mfa.preprocessor

import kotlinx.coroutines.*
import kotlin.math.abs

class Convolution {
    fun convolve(
        input: Array<IntArray>,
        kernel: FloatArray,
        kernelRows: Int,
        kernelCols: Int
    ): Array<IntArray> = runBlocking {
        val inputRows = input.size
        val inputCols = input[0].size
        val outputRows = inputRows - kernelRows + 1
        val outputCols = inputCols - kernelCols + 1
        val output = Array(outputRows) { IntArray(outputCols) }

        // Use coroutines for actual parallelism
        coroutineScope {
            (0 until outputRows).map { i ->
                launch(Dispatchers.Default) {  // Launch parallel coroutines
                    for (j in 0 until outputCols) {
                        var sum = 0f
                        for (ki in 0 until kernelRows) {
                            for (kj in 0 until kernelCols) {
                                sum += input[i + ki][j + kj] * kernel[ki * kernelCols + kj]
                            }
                        }
                        output[i][j] = abs(sum.toInt()).coerceIn(0, 255) // More readable
                    }
                }
            }.joinAll() // Wait for all coroutines to finish
        }

        output
    }
}
