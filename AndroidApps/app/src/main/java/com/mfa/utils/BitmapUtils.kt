package com.mfa.utils

import android.annotation.TargetApi
import android.content.ContentResolver
import android.graphics.*
import android.media.ExifInterface
import android.media.Image
import android.media.Image.Plane
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer


class BitmapUtils {
    companion object {
        private val TAG = "BitmapUtils"

        /**
         * Converts NV21 format byte buffer to bitmap.
         */
        fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
            data.rewind()
            val imageInBuffer = ByteArray(data.limit())
            data[imageInBuffer, 0, imageInBuffer.size]
            try {
                val image =
                    YuvImage(
                        imageInBuffer, ImageFormat.NV21, metadata.getWidth(), metadata.getHeight(), null
                    )
                val stream = ByteArrayOutputStream()
                image.compressToJpeg(Rect(0, 0, metadata.getWidth(), metadata.getHeight()), 80, stream)

                val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())

                stream.close()
                return rotateBitmap(bmp, metadata.getRotation(), false, false)
            } catch (e: Exception) {
                Log.e("VisionProcessorBase", "Error: " + e.message)
            }
            return null
        }

        /**
         * Converts a YUV_420_888 image from CameraX API to a bitmap.
         */
        @ExperimentalGetImage
        fun getBitmap(imageProxy: ImageProxy): Bitmap? {
            val image = imageProxy.image ?: run {
                Log.e("BitmapUtils", "Image is null or in an unexpected format")
                return null
            }

            return when (image.format) {
                ImageFormat.YUV_420_888 -> {
                    val nv21Bytes = yuv420ToNv21(image)
                    val yuvImage = YuvImage(nv21Bytes, ImageFormat.NV21, image.width, image.height, null)
                    val out = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
                    BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
                }
                ImageFormat.JPEG -> {
                    convertJPEGImageProxyJPEGToBitmap(imageProxy)
                }
                else -> {
                    Log.e("BitmapUtils", "Unsupported image format: ${image.format}")
                    null
                }
            }
        }


        private fun yuv420ToNv21(image: Image): ByteArray {
            val yBuffer = image.planes[0].buffer // Y
            val uBuffer = image.planes[1].buffer // U
            val vBuffer = image.planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            return nv21
        }



        fun convertNV21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap? {
            val ySize = width * height
            val uvSize = width * height / 2

            Log.d("CameraEkspresi", "Converting NV21 to Bitmap. Width: $width, Height: $height")

            // Create a YUV image from the NV21 byte array
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)

            // Create a ByteArrayOutputStream to hold the image data
            val out = ByteArrayOutputStream()

            // Compress the YUV image to JPEG format (or any other supported format)
            val rect = Rect(0, 0, width, height)
            val success = yuvImage.compressToJpeg(rect, 100, out)

            if (!success) {
                Log.e("CameraEkspresi", "Failed to compress YUV image to JPEG")
                return null
            }

            // Convert the output byte array into a Bitmap
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }




        /**
         * Rotates a bitmap if it is converted from a bytebuffer.
         */
        fun rotateBitmap(
            bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
        ): Bitmap {
            val matrix: Matrix = Matrix()

            // Rotate the image back to straight.
            matrix.postRotate(rotationDegrees.toFloat())

            // Mirror the image along the X or Y axis.
            matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Recycle the old bitmap if it has changed.
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            return rotatedBitmap
        }

        @Throws(IOException::class)
        fun getBitmapFromContentUri(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
            val decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri) ?: return null
            val orientation = getExifOrientationTag(contentResolver, imageUri)

            var rotationDegrees = 0
            var flipX = false
            var flipY = false
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
                ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    rotationDegrees = 90
                    flipX = true
                }

                ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
                ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    rotationDegrees = -90
                    flipX = true
                }

                ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {}
                else -> {}
            }
            return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY)
        }

        private fun getExifOrientationTag(resolver: ContentResolver, imageUri: Uri): Int {
            // We only support parsing EXIF orientation tag from local file on the device.
            // See also:
            // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
            if (ContentResolver.SCHEME_CONTENT != imageUri.scheme
                && ContentResolver.SCHEME_FILE != imageUri.scheme
            ) {
                return 0
            }

            var exif: ExifInterface
            try {
                resolver.openInputStream(imageUri).use { inputStream ->
                    if (inputStream == null) {
                        return 0
                    }
                    exif = ExifInterface(inputStream)
                }
            } catch (e: IOException) {
                Log.e(TAG, "failed to open file to read rotation meta data: $imageUri", e)
                return 0
            }

            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

        /**
         * Converts YUV_420_888 to NV21 bytebuffer.
         *
         *
         * The NV21 format consists of a single byte array containing the Y, U and V values. For an
         * image of size S, the first S positions of the array contain all the Y values. The remaining
         * positions contain interleaved V and U values. U and V are subsampled by a factor of 2 in both
         * dimensions, so there are S/4 U values and S/4 V values. In summary, the NV21 array will contain
         * S Y values followed by S/4 VU values: YYYYYYYYYYYYYY(...)YVUVUVUVU(...)VU
         *
         *
         * YUV_420_888 is a generic format that can describe any YUV image where U and V are subsampled
         * by a factor of 2 in both dimensions. [Image.getPlanes] returns an array with the Y, U and
         * V planes. The Y plane is guaranteed not to be interleaved, so we can just copy its values into
         * the first part of the NV21 array. The U and V planes may already have the representation in the
         * NV21 format. This happens if the planes share the same buffer, the V buffer is one position
         * before the U buffer and the planes have a pixelStride of 2. If this is case, we can just copy
         * them to the NV21 array.
         */
        private fun yuv420ThreePlanesToNV21(
            yuv420888planes: Array<Plane>, width: Int, height: Int
        ): ByteBuffer {
            val imageSize = width * height
            val out = ByteArray(imageSize + 2 * (imageSize / 4))

            if (areUVPlanesNV21(yuv420888planes, width, height)) {
                // Copy the Y values.
                yuv420888planes[0].buffer[out, 0, imageSize]

                val uBuffer = yuv420888planes[1].buffer
                val vBuffer = yuv420888planes[2].buffer
                // Get the first V value from the V buffer, since the U buffer does not contain it.
                vBuffer[out, imageSize, 1]
                // Copy the first U value and the remaining VU values from the U buffer.
                uBuffer[out, imageSize + 1, 2 * imageSize / 4 - 1]
            } else {
                // Fallback to copying the UV values one by one, which is slower but also works.
                // Unpack Y.
                unpackPlane(yuv420888planes[0], width, height, out, 0, 1)
                // Unpack U.
                unpackPlane(yuv420888planes[1], width, height, out, imageSize + 1, 2)
                // Unpack V.
                unpackPlane(yuv420888planes[2], width, height, out, imageSize, 2)
            }

            return ByteBuffer.wrap(out)
        }

        /**
         * Checks if the UV plane buffers of a YUV_420_888 image are in the NV21 format.
         */
        private fun areUVPlanesNV21(planes: Array<Plane>, width: Int, height: Int): Boolean {
            val imageSize = width * height

            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            // Backup buffer properties.
            val vBufferPosition = vBuffer.position()
            val uBufferLimit = uBuffer.limit()

            // Advance the V buffer by 1 byte, since the U buffer will not contain the first V value.
            vBuffer.position(vBufferPosition + 1)
            // Chop off the last byte of the U buffer, since the V buffer will not contain the last U value.
            uBuffer.limit(uBufferLimit - 1)

            // Check that the buffers are equal and have the expected number of elements.
            val areNV21 =
                (vBuffer.remaining() == (2 * imageSize / 4 - 2)) && (vBuffer.compareTo(uBuffer) == 0)

            // Restore buffers to their initial state.
            vBuffer.position(vBufferPosition)
            uBuffer.limit(uBufferLimit)

            return areNV21
        }

        /**
         * Unpack an image plane into a byte array.
         *
         *
         * The input plane data will be copied in 'out', starting at 'offset' and every pixel will be
         * spaced by 'pixelStride'. Note that there is no row padding on the output.
         */
        private fun unpackPlane(
            plane: Plane, width: Int, height: Int, out: ByteArray, offset: Int, pixelStride: Int
        ) {
            val buffer = plane.buffer
            buffer.rewind()

            // Compute the size of the current plane.
            // We assume that it has the aspect ratio as the original image.
            val numRow = (buffer.limit() + plane.rowStride - 1) / plane.rowStride
            if (numRow == 0) {
                return
            }
            val scaleFactor = height / numRow
            val numCol = width / scaleFactor

            // Extract the data in the output buffer.
            var outputPos = offset
            var rowStart = 0
            for (row in 0 until numRow) {
                var inputPos = rowStart
                for (col in 0 until numCol) {
                    out[outputPos] = buffer[inputPos]
                    outputPos += pixelStride
                    inputPos += plane.pixelStride
                }
                rowStart += plane.rowStride
            }
        }

        //    https://stackoverflow.com/questions/62373908/android-camera-app-using-camerax-to-save-images-in-yuv-420-888-format
        fun convertJPEGImageProxyJPEGToBitmap(image: ImageProxy): Bitmap {
            val byteBuffer = image.planes[0].buffer
            byteBuffer.rewind()
            val bytes = ByteArray(byteBuffer.capacity())
            byteBuffer[bytes]
            val clonedBytes = bytes.clone()
            return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.size)
        }


        fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            val width = bm.width
            val height = bm.height
            val scaleWidth = (newWidth.toFloat()) / width
            val scaleHeight = (newHeight.toFloat()) / height
            // CREATE A MATRIX FOR THE MANIPULATION
            val matrix: Matrix = Matrix()
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight)

            // "RECREATE" THE NEW BITMAP
            val resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false
            )
            bm.recycle()
            return resizedBitmap
        }

        fun getCropBitmapByCPU(source: Bitmap?, cropRectF: RectF): Bitmap {
            val resultBitmap = Bitmap.createBitmap(
                cropRectF.width().toInt(),
                cropRectF.height().toInt(), Bitmap.Config.ARGB_8888
            )
            val cavas = Canvas(resultBitmap)

            // draw background
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            paint.color = Color.WHITE
            cavas.drawRect(
                RectF(0f, 0f, cropRectF.width(), cropRectF.height()),
                paint
            )

            val matrix: Matrix = Matrix()
            matrix.postTranslate(-cropRectF.left, -cropRectF.top)

            cavas.drawBitmap(source!!, matrix, paint)

            if (!source.isRecycled) {
                source.recycle()
            }

            return resultBitmap
        }
    }

}
