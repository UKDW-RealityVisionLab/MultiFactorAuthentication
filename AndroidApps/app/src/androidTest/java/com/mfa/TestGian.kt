import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mfa.preprocessor.PreprocessingUtils
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class ConvolutionPerformanceTest {
    private val convolution = PreprocessingUtils()

    @Test
    fun testConvolutionPerformance() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Log.d("PerformanceTest", "Running on: ${Build.MODEL}, ${Build.BRAND}")

        val testCases = listOf(
            TestCase("Tiny", 32, 50),
            TestCase("Small", 64, 30),
            TestCase("Medium", 128, 10),
            TestCase("Large", 224, 60),
            TestCase("XLarge", 512, 5)
        )

        testCases.forEach { case ->
            val image = createTestImage(case.size)
            val results = mutableListOf<Long>()

            // Warmup (run once before measuring)
            convolution.convolve(image, testKernel, 3)

            repeat(case.iterations) {
                val time = measureTimeMillis {
                    convolution.convolve(image, testKernel, 3)
                }
                results.add(time)
            }

            val avgTime = results.average().toLong()
            val minTime = results.min()
            val maxTime = results.max()
            val mpixels = case.size * case.size / 1_000_000f
            val throughput = (mpixels / (avgTime / 1000f)).roundToInt()

            Log.d("PerformanceTest", "${case.name} (${case.size}x${case.size}): " +
                    "Time (min/avg/max): ${minTime}ms / ${avgTime}ms / ${maxTime}ms | Throughput: $throughput MP/s"
            )
        }
    }

    private fun createTestImage(size: Int): Array<IntArray> {
        return Array(size) { y -> IntArray(size) { x -> (x + y) % 256 } }
    }

    private data class TestCase(val name: String, val size: Int, val iterations: Int)

    private val testKernel = floatArrayOf(
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9
    )
}
