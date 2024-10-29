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
import android.provider.OpenableColumns
import android.util.Log
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
import com.nasahacker.convertit.util.Constants.FORMAT_ARRAY
import com.nasahacker.convertit.util.Constants.STORAGE_PERMISSION_CODE
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10
import kotlin.math.pow

object AppUtils {

    fun openFilePicker(activity: Activity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted(activity)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(activity)
        }
    }

    fun openLink(context: Context, link: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    fun shareMusicFile(context: Context, file: File) {
        if (file.exists()) {
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Music File"))
        } else {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFileSizeInReadableFormat(context: Context, file: File): String {
        val sizeInBytes = file.length()
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            context.getString(R.string.label_file_size),
            sizeInBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups]
        )
    }

    fun openMusicFileInPlayer(context: Context, file: File) {
        if (file.exists()) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app found to open the file.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFilesFromUris(context: Context, uriList: List<Uri>): List<File> {
        return uriList.mapNotNull { uri -> getFileFromUri(context, uri) }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: return null
            val file = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
        } else File(uri.path!!).name
    }

    fun getAudioFilesFromConvertedFolder(): List<File> {
        val convertedDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ConvertIt")
        return convertedDir.takeIf { it.exists() && it.isDirectory }?.listFiles()?.filter { file ->
            FORMAT_ARRAY.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
        }?.toList() ?: emptyList()
    }

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: "unknown"
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = getFileName(context.contentResolver, uri)
            val tempFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileUtils", "Error copying file: ${e.message}")
            null
        }
    }

    fun editAudioMetadata(
        context: Context,
        inputUri: Uri,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        genre: String? = null,
        track: String? = null,
        year: String? = null,
        coverArtUri: Uri? = null,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val musicDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ConvertIt").apply { mkdirs() }
        val inputPath = copyUriToInternalStorage(context, inputUri) ?: run {
            onFailure("Failed to copy file from Uri: $inputUri")
            return
        }
        val outputFilePath = File(musicDir, "${File(inputPath).nameWithoutExtension}-output.mp3").absolutePath
        val command = mutableListOf("-y", "-i", inputPath).apply {
            coverArtUri?.let {
                val coverArtPath = copyUriToInternalStorage(context, it)
                if (coverArtPath != null) {
                    addAll(listOf("-i", coverArtPath, "-map", "0:0", "-map", "1:0", "-disposition:v:0", "attached_pic"))
                }
            }
            title?.let { addAll(listOf("-metadata", "title=\"$it\"")) }
            artist?.let { addAll(listOf("-metadata", "artist=\"$it\"")) }
            album?.let { addAll(listOf("-metadata", "album=\"$it\"")) }
            genre?.let { addAll(listOf("-metadata", "genre=\"$it\"")) }
            track?.let { addAll(listOf("-metadata", "track=\"$it\"")) }
            year?.let { addAll(listOf("-metadata", "date=\"$it\"")) }
            add(outputFilePath)
        }

        FFmpeg.executeAsync(command.toTypedArray()) { _, returnCode ->
            if (returnCode == Config.RETURN_CODE_SUCCESS) onSuccess(outputFilePath)
            else onFailure("Metadata update failed with return code: $returnCode")
        }
    }

    fun convertAudio(
        context: Context,
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate = AudioBitrate.BITRATE_192K,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ConvertIt").apply { mkdirs() }
        val outputPaths = mutableListOf<String>()
        uris.forEach { uri ->
            val inputPath = copyUriToInternalStorage(context, uri) ?: run {
                onFailure("Failed to copy file from Uri: $uri")
                return
            }
            val outputFilePath = File(musicDir, "${File(inputPath).nameWithoutExtension}${outputFormat.extension}").absolutePath
            val command = arrayOf("-y", "-i", inputPath, "-map", "0", "-map_metadata", "0", "-c:a", AudioCodec.fromFormat(outputFormat).codec, "-b:a", bitrate.bitrate, "-c:v", "copy", outputFilePath)
            FFmpeg.executeAsync(command) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    outputPaths.add(outputFilePath)
                    if (outputPaths.size == uris.size) onSuccess(outputPaths)
                } else {
                    onFailure("Conversion failed for file: $inputPath with return code: $returnCode")
                }
            }
        }
    }

    fun handleNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1011)
        }
    }

    private fun isStoragePermissionGranted(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions(activity: Activity) {
        if (!isStoragePermissionGranted(activity)) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_CODE)
        } else {
            Toast.makeText(activity, "Storage permissions are already granted", Toast.LENGTH_SHORT).show()
        }
    }
}
