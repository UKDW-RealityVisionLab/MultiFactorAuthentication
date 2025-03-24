package com.mfa

import com.mfa.preprocessor.PreprocessingUtils
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.math.roundToInt

class ConvolutionPerformanceTest {
    private val convolution = PreprocessingUtils()

    // Test configurations - adjust based on your needs
    private val testCases = listOf(
        TestCase("Tiny", 32, 50),
        TestCase("Small", 64, 30),
        TestCase("Medium", 128, 10),
        TestCase("Large", 224, 60),
        TestCase("XLarge", 512, 5)
    )

    // Simple 3x3 blur kernel
    private val testKernel = floatArrayOf(
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9
    )

    @Test
    fun testConvolutionPerformance() {
        println("\n==== Convolution Performance Test ====")
        println("Available processors: ${Runtime.getRuntime().availableProcessors()}")
        println("Note: Times include coroutine setup overhead\n")

        testCases.forEach { case ->
            val image = createTestImage(case.size)
            val results = mutableListOf<Long>()

            // Warmup (run once before measuring)
            convolution.isBlurry(image)

            // Main measurements
            repeat(case.iterations) {
                val time = measureTimeMillis {
                    convolution.isBlurry(image)
                }
                results.add(time)
            }

            val avgTime = results.average().toLong()
            val minTime = results.min()
            val maxTime = results.max()
            val mpixels = case.size * case.size / 1_000_000f
            val throughput = (mpixels / (avgTime / 1000f)).roundToInt()

            println("${case.name} (${case.size}x${case.size}):")
            println("  Iterations: ${case.iterations}")
            println("  Time (min/avg/max): ${minTime}ms / ${avgTime}ms / ${maxTime}ms")
            println("  Throughput: $throughput MP/s")
            println("  ---------------------------------")

            // Basic performance assertions
            when (case.size) {
                32 -> assert(avgTime < 20) { "32x32 should take <20ms" }
                64 -> assert(avgTime < 50) { "64x64 should take <50ms" }
                128 -> assert(avgTime < 150) { "128x128 should take <150ms" }
                224 -> assert(avgTime < 300) { "224x224 should take <300ms" }
                512 -> assert(avgTime < 1500) { "512x512 should take <1500ms" }
            }
        }
    }

    private fun createTestImage(size: Int): Array<IntArray> {
        return Array(size) { y ->
            IntArray(size) { x -> (x + y) % 256 }
        }
    }

    private data class TestCase(val name: String, val size: Int, val iterations: Int)
}