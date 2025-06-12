package com.mfa.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.sqrt

class Utils {
    companion object {
        /**
         * Read images from assets
         * @param context
         * @param filename
         * @return
         */
        fun readFromAssets(context: Context, filename: String): Bitmap? {
            val bitmap: Bitmap
            try {
                val inputStream = context.assets.open(filename)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
            return bitmap
        }

        /**
         * Load model file
         * @param assetManager
         * @param modelPath
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

        /**
         * l2 norm normalization
         * @param embeddings
         * @param epsilon penalty item
         * @return
         */
        fun l2Normalize(embeddings: Array<FloatArray>, epsilon: Double): Array<FloatArray> {
            for (i in embeddings.indices) {
                var squareSum = 0.0
                for (j in embeddings[i].indices) {
                    squareSum += Math.pow(embeddings[i][j].toDouble(), 2.0)
                }
                val xInvNorm = sqrt(max(squareSum, epsilon)).toFloat()
                for (j in embeddings[i].indices) {
                    embeddings[i][j] /= xInvNorm
                }
            }
            return embeddings
        }

        /**
         * Normalize the image to [-1, 1]
         * @param bitmap
         * @return
         */
        fun normalizeImage(bitmap: Bitmap): Array<Array<FloatArray>> {
            val h = bitmap.height
            val w = bitmap.width
            val floatValues = Array(h) {
                Array(w) {
                    FloatArray(
                        3
                    )
                }
            }

            val imageMean = 127.5f
            val imageStd = 128f

            val pixels = IntArray(h * w)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, w, h)
            for (i in 0 until h) { // Note that the height is first and then the width.
                for (j in 0 until w) {
                    val value = pixels[i * w + j]
                    //bitshift
                    val r = (((value shr 16) and 0xFF) - imageMean) / imageStd
                    val g = (((value shr 8) and 0xFF) - imageMean) / imageStd
                    val b = ((value and 0xFF) - imageMean) / imageStd
                    val arr = floatArrayOf(r, g, b)
                    floatValues[i][j] = arr
                }
            }
            return floatValues
        }

        /**
         * Normalize the image to [-1, 1]
         * @param bitmap
         * @return
         */
        fun getFirebaseEmbedding(user: FirebaseUser): DatabaseReference {
            if (user == null) {
                Log.e("Utils", "FirebaseUser is null")
                throw IllegalStateException("FirebaseUser cannot be null")
            }
            Log.d("firebase Utils", "User UID: ${user.uid}, Email: ${user.email}, Display Name: ${user.displayName}")
            val userName = user.uid + "_" + (user.displayName ?: "unknown").replace(" ", "")
            val dataImage= FirebaseDatabase.getInstance().getReference("newfaceantispooflog/$userName/faceEmbeddings")
            Log.d("data image user login","$dataImage")
            return dataImage
        }

        fun setFirebaseEmbedding(embeddingFloatList: List<String>) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.e("FirebaseEmbedding", "User not logged in")
                    return
                }

                val userName = user.uid + "_" + (user.displayName ?: "unknown").replace(" ", "")
                val database = FirebaseDatabase.getInstance().reference

                database.child("newfaceantispooflog")
                    .child(userName)
                    .child("faceEmbeddings")
                    .setValue(embeddingFloatList)
                    .addOnSuccessListener {
                        Log.d("FirebaseEmbedding", "Embeddings saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseEmbedding", "Failed to save embeddings", e)
                    }
            } catch (e: Exception) {
                Log.e("FirebaseEmbedding", "Error saving embeddings", e)
            }
        }

        /**
         * Convert image to grayscale
         * @param bitmap
         * @return Grayscale data
         */
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
    }
}