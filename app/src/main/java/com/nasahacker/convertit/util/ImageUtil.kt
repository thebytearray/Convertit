package com.nasahacker.convertit.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object ImageUtil {

    private const val TAG = "ImageUtil"
    private const val MAX_DIMENSION = 800
    private const val MAX_SIZE_BYTES = 500 * 1024 // 500KB
    private const val DEFAULT_JPEG_QUALITY = 85

    suspend fun processSelectedCoverImage(context: Context, imageUri: Uri): Pair<ByteArray?, String?>? {
        return withContext(Dispatchers.IO) {
            try {
                // Read Bitmap
                var bitmap: Bitmap? = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                } ?: throw IOException("Failed to open input stream for URI: $imageUri")

                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode bitmap from URI: $imageUri")
                    return@withContext null
                }

                // Resizing Logic
                if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
                    val newWidth: Int
                    val newHeight: Int
                    if (bitmap.width > bitmap.height) {
                        newWidth = MAX_DIMENSION
                        newHeight = (bitmap.height.toFloat() * MAX_DIMENSION.toFloat() / bitmap.width.toFloat()).toInt()
                    } else {
                        newHeight = MAX_DIMENSION
                        newWidth = (bitmap.width.toFloat() * MAX_DIMENSION.toFloat() / bitmap.height.toFloat()).toInt()
                    }
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                    if (resizedBitmap != bitmap) { // Only recycle if a new bitmap was created
                        bitmap.recycle()
                    }
                    bitmap = resizedBitmap
                }

                // Compression Logic
                val outputStream = ByteArrayOutputStream()
                val compressFormat = Bitmap.CompressFormat.JPEG // Default to JPEG
                val mimeType = "image/jpeg"
                var currentQuality = DEFAULT_JPEG_QUALITY

                bitmap.compress(compressFormat, currentQuality, outputStream)
                var imageData = outputStream.toByteArray()

                // Optional: Size Check and Re-compression (simple version)
                if (imageData.size > MAX_SIZE_BYTES) {
                    Log.d(TAG, "Image size after initial compression (${imageData.size} bytes) > MAX_SIZE_BYTES (${MAX_SIZE_BYTES} bytes). Attempting re-compression with lower quality.")
                    outputStream.reset() // Reset the stream for re-compression
                    currentQuality = 70 // Try a lower quality
                    bitmap.compress(compressFormat, currentQuality, outputStream)
                    imageData = outputStream.toByteArray()
                    if (imageData.size > MAX_SIZE_BYTES) {
                        Log.w(TAG, "Image size still too large after re-compression: ${imageData.size} bytes.")
                        // Potentially return null or the oversized image based on requirements.
                        // For now, we'll return it as is, but logging the warning.
                    }
                }

                bitmap.recycle() // Recycle the final bitmap

                return@withContext Pair(imageData, mimeType)

            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "OutOfMemoryError while processing image: $imageUri", e)
                return@withContext null
            } catch (e: IOException) {
                Log.e(TAG, "IOException while processing image: $imageUri", e)
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while processing image: $imageUri", e)
                return@withContext null
            }
        }
    }
}

// Helper function to convert ByteArray to ImageBitmap - moved here for broader access
fun ByteArray?.toImageBitmap(): ImageBitmap? {
    return this?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
}
