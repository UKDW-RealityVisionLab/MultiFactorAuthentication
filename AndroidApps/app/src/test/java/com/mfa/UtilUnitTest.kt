package com.mfa

import com.mfa.utils.Utils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UtilUnitTest {

    @Test
    fun testl2Norm(){
        val embeddings = arrayOf(
            floatArrayOf(1.0f, 2.0f, 3.0f),
            floatArrayOf(4.0f, 5.0f, 6.0f),
            floatArrayOf(7.0f, 8.0f, 9.0f),
        )
        val epsilon = 1e-10

        val normalizedEmbeddings = Utils.l2Normalize(embeddings, epsilon)

        // Expected normalized embeddings
        val expected = arrayOf(
            floatArrayOf(0.26726124f, 0.5345225f, 0.8017837f),
            floatArrayOf(0.4558423f, 0.5698029f, 0.6837635f),
            floatArrayOf(0.5025707f, 0.5743665f, 0.64616233f)
        )

        // Compare each element of the normalized embeddings
        for (i in embeddings.indices) {
            for (j in embeddings[i].indices) {
                assertEquals(expected[i][j], normalizedEmbeddings[i][j], 1e-6f) // Tolerate small differences due to floating point precision
            }
        }
    }

}