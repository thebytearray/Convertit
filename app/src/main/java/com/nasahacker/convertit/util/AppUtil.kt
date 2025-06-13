package com.nasahacker.convertit.util

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.provider.MediaStore
import android.content.ContentValues
import android.content.ContentUris
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
// StatisticsCallback is used via lambda, no direct import needed if not explicitly typed.
import com.nasahacker.convertit.App
import com.nasahacker.convertit.R
import com.nasahacker.convertit.dto.AudioBitrate
import com.nasahacker.convertit.dto.AudioCodec
import com.nasahacker.convertit.dto.AudioFile
import com.nasahacker.convertit.dto.AudioFormat
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.util.AppConfig.AUDIO_FORMAT
import com.nasahacker.convertit.util.AppConfig.AUDIO_PLAYBACK_SPEED
import com.nasahacker.convertit.util.AppConfig.BITRATE
import com.nasahacker.convertit.util.AppConfig.FOLDER_DIR
import com.nasahacker.convertit.util.AppConfig.FORMAT_ARRAY
import com.nasahacker.convertit.util.AppConfig.STORAGE_PERMISSION_CODE
import com.nasahacker.convertit.util.AppConfig.URI_LIST
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import androidx.core.net.toUri

/**
 * @author Tamim Hossain
 * @email tamimh.dev@gmail.com
 * @license Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

object AppUtil {
    fun openFilePicker(
        context: Context,
        pickFileLauncher: ActivityResultLauncher<Intent>,
    ) {
        if (isStoragePermissionGranted(context)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*, video/* ,*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*", "*/*"))
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            if (context is Activity) {
                requestStoragePermissions(context)
            } else {
                Log.e("AppUtil", "Context is not an Activity. Cannot request storage permissions.")
                Toast.makeText(context, context.getString(R.string.label_cannot_request_permission_without_activity), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun receiverFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.RECEIVER_EXPORTED
    } else {
        ContextCompat.RECEIVER_NOT_EXPORTED
    }

    fun openLink(
        context: Context,
        link: String,
    ) {
        context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }

    fun shareMusicFile(
        context: Context,
        uri: Uri,
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(uri) ?: "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.label_share_music_file),
                ),
            )
        } catch (e: Exception) {
            Log.e("AppUtil", "Error sharing file: $uri", e)
            Toast.makeText(
                context,
                context.getString(R.string.label_failed_to_share_file) + ": " + e.message, // Consider a more generic error for users
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getFileSizeInReadableFormat(
        context: Context,
        uri: Uri,
    ): String {
        val sizeInBytes = if (uri.scheme == "file") {
            File(uri.path!!).length()
        } else {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        }
        return formatFileSize(sizeInBytes) // Using the new helper
    }

    fun openMusicFileInPlayer(
        context: Context,
        uri: Uri,
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppUtil", "Error opening file: $uri", e)
            Toast.makeText(
                context,
                context.getString(R.string.label_no_app_found_to_open_the_file) + ": " + e.message, // Generic error
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getUriListFromIntent(intent: Intent): ArrayList<Uri> {
        val uriList = ArrayList<Uri>()
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                uriList.add(clipData.getItemAt(i).uri)
            }
        } ?: intent.data?.let { uriList.add(it) }
        return uriList
    }

    fun getFileFromUri(
        context: Context,
        uri: Uri,
    ): File? {
        return try {
            val fileName = getFileName(context, uri) ?: return null
            

            val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
            if (fileSize > 1024 * 1024 * 1024) {
                throw Exception("File too large")
            }

            val file = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(
        context: Context,
        uri: Uri,
    ): String? =
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            }
        } else if (uri.scheme == "file") {
            uri.path?.let { File(it).name }
        } else {
            null
        }

    fun getAudioFilesFromConvertedFolder(context: Context): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED
            )
            val selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_MUSIC}/${AppConfig.FOLDER_DIR}%")
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                // val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    // Filter by extension on client side if RELATIVE_PATH is not specific enough
                    if (FORMAT_ARRAY.any { name.endsWith(it, ignoreCase = true) }) {
                        audioFiles.add(AudioFile(name = name, size = formatFileSize(size), uri = uri))
                    }
                }
            }
        } else {
            val convertedDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), AppConfig.FOLDER_DIR)
            if (convertedDir.exists() && convertedDir.isDirectory) {
                convertedDir.listFiles()?.forEach { file ->
                    if (FORMAT_ARRAY.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }) {
                        val fileUri = file.toUri()
                        audioFiles.add(
                            AudioFile(
                                name = file.name,
                                size = getFileSizeInReadableFormat(context, fileUri), // old signature, will update this function next
                                uri = fileUri
                            )
                        )
                    }
                }
            }
        }
        return audioFiles
    }

    // Helper function to format file size, can be used by getAudioFilesFromConvertedFolder and getFileSizeInReadableFormat
    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        val size = sizeInBytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", size, units[digitGroups])
    }

    private fun getFileName(
        contentResolver: ContentResolver,
        uri: Uri,
    ): String = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
    } ?: "unknown"



    fun startAudioConvertService(
        speed: String = "1.0",
        uriList: ArrayList<Uri>,
        bitrate: String,
        format: String,
    ) {
        Log.d(
            "ZERO_DOLLAR",
            "Starting audio conversion with the following details:\n" + "URI List Size: ${uriList.size}\n" + "Bitrate: $bitrate\n" + "Format: $format",
        )

        val intent = Intent(App.application, ConvertItService::class.java).apply {
            putParcelableArrayListExtra(URI_LIST, uriList)
            putExtra(BITRATE, bitrate)
            putExtra(AUDIO_PLAYBACK_SPEED, speed)
            putExtra(AUDIO_FORMAT, format)
        }


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            Log.d("ZERO_DOLLAR", "Starting foreground service...")
            App.application.startForegroundService(intent)
        } else {
            Log.d("ZERO_DOLLAR", "Starting regular service...")
            App.application.startService(intent)
        }
    }

    fun deleteFile(
        context: Context,
        uri: Uri,
    ) {
        var success = false
        try {
            if (uri.scheme == "content") {
                val rowsDeleted = context.contentResolver.delete(uri, null, null)
                success = rowsDeleted > 0
            } else if (uri.scheme == "file") {
                val file = File(uri.path!!)
                success = file.delete()
            }
        } catch (e: Exception) {
            Log.e("AppUtil", "Error deleting file: $uri", e)
            success = false
        }

        val resultMessage = if (success) {
            context.getString(R.string.label_file_deleted_successfully)
        } else {
            context.getString(R.string.label_failed_to_delete_file) // Generic error message
        }
        Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
    }

    fun convertAudio(
        context: Context,
        playbackSpeed: String = "1.0",
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit,
        onProgress: (Int) -> Unit,
    ) {
        onProgress(0) // Initial progress update

        val outputPaths = mutableListOf<String>()
        val totalFiles = uris.size
        var processedFiles = 0
        val conversionQueue = uris.toMutableList()

        fun processNextFile() {
            if (conversionQueue.isEmpty()) {
                // All files from the queue have been picked up for processing.
                // onSuccess will be called if all processed files were successful.
                // If some failed, onFailure would have been called for them.
                // We need to ensure onSuccess is called only when all files actually succeeded.
                if (processedFiles == totalFiles && outputPaths.size == totalFiles) {
                    onSuccess(outputPaths)
                } else if (processedFiles == totalFiles && outputPaths.size < totalFiles) {
                     // This implies some files failed, and onFailure was called for them.
                     // No explicit onSuccess needed here as it's not a full success.
                }
                return
            }

            val uri = conversionQueue.removeAt(0)
            val inputFileName = getFileName(context.contentResolver, uri)
            val inputFileNameWithoutExtension = inputFileName.substringBeforeLast(".")
            val outputFileName = "${inputFileNameWithoutExtension}_convertit${outputFormat.extension}" // Initial output file name

            // Temporary input file handling
            val tempFile = File.createTempFile("convertit_input_", ".tmp", context.cacheDir)
            var pfd: ParcelFileDescriptor? = null
            var outputUriForCleanup: Uri? = null

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException("Failed to open input stream for $inputFileName")

                val ffmpegOutput: String
                var finalOutputFilePath: String? = null // Used for legacy and for adding to outputPaths before Q
                var durationInMs = 0L

                try {
                    // Get media duration using FFprobeKit
                    val mediaInfo = FFprobeKit.getMediaInformation(tempFile.absolutePath)
                    durationInMs = mediaInfo.mediaInformation?.duration?.toDoubleOrNull()?.times(1000)?.toLong() ?: 0L
                } catch (e: Exception) {
                    Log.e("AppUtil", "FFprobeKit failed to get media information for $inputFileName", e)
                    // durationInMs remains 0L, fallback progress will be used for this file
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(outputFormat.extension.substring(1)) ?: "audio/*"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + File.separator + AppConfig.FOLDER_DIR)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val insertedUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                    outputUriForCleanup = insertedUri
                    if (insertedUri == null) {
                        throw IOException("Failed to create MediaStore entry for $outputFileName")
                    }
                    pfd = context.contentResolver.openFileDescriptor(insertedUri, "w")
                    if (pfd == null) {
                        throw IOException("Failed to open ParcelFileDescriptor for $outputFileName")
                    }
                    ffmpegOutput = "pipe:${pfd.fd}"
                } else {
                    val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), AppConfig.FOLDER_DIR)
                    musicDir.mkdirs()
                    var outputFile = File(musicDir, outputFileName)
                    var counter = 1
                    while (outputFile.exists()) {
                        val nextOutputFileName = "${inputFileNameWithoutExtension}_convertit($counter)${outputFormat.extension}"
                        outputFile = File(musicDir, nextOutputFileName)
                        counter++
                    }
                    finalOutputFilePath = outputFile.absolutePath
                    ffmpegOutput = finalOutputFilePath
                }

                val command = "-y -i \"${tempFile.absolutePath}\" -c:a ${AudioCodec.fromFormat(outputFormat).codec} -b:a ${bitrate.bitrate} -filter:a \"atempo=$playbackSpeed\" \"$ffmpegOutput\""

                FFmpegKit.executeAsync(command, { session -> // Completion Callback
                    try {
                        if (ReturnCode.isSuccess(session.returnCode)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && outputUriForCleanup != null) {
                                val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                                context.contentResolver.update(outputUriForCleanup, values, null, null)
                                outputPaths.add(outputUriForCleanup.toString())
                            } else {
                                finalOutputFilePath?.let { outputPaths.add(it) } // Add legacy path
                            }
                            // Successfully processed one file
                            // Note: processedFiles is incremented *before* calling onProgress here
                            // to ensure the final progress for this file reflects its completion.
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && outputUriForCleanup != null) {
                                context.contentResolver.delete(outputUriForCleanup, null, null)
                            }
                            onFailure(context.getString(R.string.label_conversion_failed_for_file_with_return_code, inputFileName, session.returnCode.toString()))
                        }
                    } finally {
                        pfd?.close()
                        tempFile.delete()
                        // Increment processedFiles here, after success or failure is determined and cleanup is done for the current file.
                        // This ensures that processNextFile logic for onSuccess/onFailure conditions based on processedFiles count is accurate.
                        processedFiles++
                        // Update progress based on fully completed files
                        val overallProgress = ((processedFiles.toDouble() / totalFiles.toDouble()) * 100.0).toInt()
                        onProgress(overallProgress.coerceIn(0, 100))

                        processNextFile() // Process next file regardless of success or failure of current one
                    }
                }, null, { statistics -> // Statistics Callback
                    if (durationInMs > 0) {
                        val currentTimeProcessed = statistics.time.toLong() // time is in ms
                        val currentFileProgressPercent = (currentTimeProcessed.toDouble() / durationInMs * 100.0).coerceIn(0.0, 100.0)

                        // `processedFiles` is the count of *fully* completed files.
                        // Add the progress of the current, partially completed file.
                        val overallProgress = (((processedFiles.toDouble() + currentFileProgressPercent / 100.0) / totalFiles.toDouble()) * 100.0).toInt()
                        onProgress(overallProgress.coerceIn(0, 100))
                    } else {
                        // Fallback if duration couldn't be obtained: progress based on completed files only.
                        val overallProgress = ((processedFiles.toDouble() / totalFiles.toDouble()) * 100.0).toInt()
                        onProgress(overallProgress.coerceIn(0, 100))
                    }
                })
            } catch (e: Exception) {
                Log.e("AppUtil", "Error processing file $inputFileName", e)
                tempFile.delete()
                pfd?.close()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && outputUriForCleanup != null) {
                    try {
                        context.contentResolver.delete(outputUriForCleanup, null, null)
                    } catch (deleteEx: Exception) {
                        Log.e("AppUtil", "Error deleting MediaStore entry on failure: $outputUriForCleanup", deleteEx)
                    }
                }
                onFailure(context.getString(R.string.label_conversion_failed_for_file_with_return_code, inputFileName, e.message ?: "Unknown error"))
                processedFiles++ // Increment processed files even on outer catch error to ensure queue processing continues
                val overallProgress = ((processedFiles.toDouble() / totalFiles.toDouble()) * 100.0).toInt()
                onProgress(overallProgress.coerceIn(0, 100)) // Update progress
                processNextFile() // Ensure queue continues
            }
        }

        // Start initial concurrent conversions
        val maxConcurrentConversions = 2 // You can adjust this value
        repeat(maxConcurrentConversions.coerceAtMost(conversionQueue.size)) {
            processNextFile()
        }
    }


    fun handleNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1011,
            )
        }
    }

    private fun isStoragePermissionGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestStoragePermissions(activity: Activity) { // Changed context to activity
        if (!isStoragePermissionGranted(activity)) { // Use activity here for the check too
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(
                activity, // Use activity directly
                permissions,
                STORAGE_PERMISSION_CODE,
            )
        } else {
            Toast.makeText(
                    activity, // Use activity here
                    activity.getString(R.string.label_storage_permissions_are_already_granted),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }
}
