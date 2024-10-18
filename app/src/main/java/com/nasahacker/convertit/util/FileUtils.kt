package com.nasahacker.convertit.util

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.nasahacker.convertit.R
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioCodec
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.util.Constants.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constants.FORMAT_ARRAY
import com.nasahacker.convertit.util.Constants.STORAGE_PERMISSION_CODE
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    fun openFilePicker(activity: Activity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted(activity)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                // Allow multiple file selection
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(activity)
        }
    }

    fun openLink(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(link))
        context.startActivity(intent)
    }


    fun shareMusicFile(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a Uri for the file using FileProvider
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",  // Ensure the authority matches your FileProvider setup
            file
        )

        // Create the sharing intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*" // Set MIME type as audio
            putExtra(Intent.EXTRA_STREAM, fileUri) // Pass the file URI
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permission to the content
        }

        // Start the share intent
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, "Share Music File"))
        } else {
            // Display a toast if no app is available to share the file
            Toast.makeText(context, "No app found to share this file.", Toast.LENGTH_SHORT).show()
        }
    }


    fun getFileSizeInReadableFormat(context: Context, file: File): String {
        val sizeInBytes = file.length()
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            context.getString(R.string.label_file_size), sizeInBytes / 1024.0.pow(
                digitGroups.toDouble()
            ), units[digitGroups]
        )
    }


    fun openMusicFileInPlayer(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)

        // For Android Nougat (API 24) and above, use FileProvider to handle file URIs securely
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            Uri.fromFile(file)  // For Android versions below Nougat
        }

        // Set the type as audio and the file's Uri
        intent.setDataAndType(uri, "audio/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Ensure there is an app that can handle this intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app found to open the file.", Toast.LENGTH_SHORT).show()
        }
    }


    fun getFilesFromUris(context: Context, uriList: List<Uri>): List<File> {
        val files = mutableListOf<File>()

        uriList.forEach { uri ->
            val file = getFileFromUri(context, uri)
            file?.let { files.add(it) }
        }

        return files
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: return null
            val file = File(context.cacheDir, fileName)  // Store in cache

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            outputStream.close()
            inputStream?.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        } else if (uri.scheme == "file") {
            name = File(uri.path!!).name
        }
        return name
    }

    fun getAudioFilesFromConvertedFolder(context: Context): List<File> {
        // Get the Music/ConvertIt directory in the device's external storage
        val convertedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "ConvertIt"
        )
        val audioExtensions = FORMAT_ARRAY

        // Ensure the folder exists and is a directory
        if (convertedDir.exists() && convertedDir.isDirectory) {
            // Filter files that match the audio extensions
            return convertedDir.listFiles()?.filter { file ->
                audioExtensions.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
            }?.toList() ?: emptyList()  // Return an empty list if no files match
        }
        return emptyList()  // Return an empty list if folder does not exist
    }


    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var result: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                result = cursor.getString(nameIndex)
            }
        }
        return result ?: "unknown"
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = getFileName(context.contentResolver, uri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()

            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileUtils", "Error copying file: ${e.message}")
            null
        }
    }


    fun convertAudio(
        context: Context,
        uris: List<Uri>,                // Accept list of Uris
        outputFormat: AudioFormat,
        bitrate: AudioBitrate = AudioBitrate.BITRATE_192K,
        onSuccess: (List<String>) -> Unit,  // Success callback with a list of file paths
        onFailure: (String) -> Unit         // Failure callback with error message
    ) {
        val outputPaths = mutableListOf<String>()  // To store paths of successfully converted files

        // Create a directory named "ConvertIt" inside the device's default Music directory
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "ConvertIt"
        )
        if (!musicDir.exists()) {
            musicDir.mkdirs() // Create the directory if it doesn't exist
        }

        uris.forEach { uri ->
            // Copy the file to the app's internal storage
            val inputPath = copyUriToInternalStorage(context, uri)
            if (inputPath == null) {
                Log.e("AudioConverterUtil", "Failed to copy file from Uri: $uri")
                onFailure("Failed to copy file from Uri: $uri")
                return
            }

            // Extract file name and replace extension with the desired output format
            val fileNameWithoutExtension = File(inputPath).nameWithoutExtension
            val outputFilePath = File(
                musicDir,
                "$fileNameWithoutExtension${outputFormat.extension}"
            ).absolutePath

            // Get the audio codec dynamically based on the selected output format
            val audioCodec = AudioCodec.fromFormat(outputFormat).codec

            try {
                // FFmpeg command to convert audio with the specified format and bitrate, preserving metadata and cover art
                val command = arrayOf(
                    "-y",                      // Overwrite output files
                    "-i", inputPath,            // Input file
                    "-map", "0",                // Map all streams from the input
                    "-map_metadata", "0",       // Copy all metadata from input to output
                    "-c:a", audioCodec,         // Codec for audio (dynamically determined)
                    "-b:a", bitrate.bitrate,    // Audio bitrate
                    "-c:v", "copy",             // Copy the cover art without re-encoding
                    outputFilePath              // Output file
                )

                // Run the FFmpeg command asynchronously
                FFmpeg.executeAsync(command) { _, returnCode ->
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        Log.d(
                            "AudioConverterUtil",
                            "Conversion successful, file saved to: $outputFilePath"
                        )
                        outputPaths.add(outputFilePath) // Add successful output path to list

                        // If all conversions are done, trigger the success callback
                        if (outputPaths.size == uris.size) {
                            onSuccess(outputPaths)
                        }
                    } else {
                        Log.e(
                            "AudioConverterUtil",
                            "Conversion failed with return code: $returnCode"
                        )
                        onFailure("Conversion failed for file: $inputPath with return code: $returnCode")
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioConverterUtil", "Exception during conversion: ${e.message}")
                onFailure("Exception during conversion for file: $inputPath - ${e.message}")
            }
        }
    }

    fun handleNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1011
                )
            }
        }
    }


    private fun isStoragePermissionGranted(activity: Activity): Boolean {
        val readPermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) true else readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermissions(activity: Activity) {
        if (!isStoragePermissionGranted(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        } else {
            Toast.makeText(activity, "Storage permissions are already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
