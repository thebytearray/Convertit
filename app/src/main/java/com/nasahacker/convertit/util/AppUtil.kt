package com.nasahacker.convertit.util

import android.app.Activity
import android.Manifest
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
import com.nasahacker.convertit.App
import com.nasahacker.convertit.R
import com.nasahacker.convertit.dto.AudioBitrate
import com.nasahacker.convertit.dto.AudioCodec
import com.nasahacker.convertit.dto.AudioFile
import com.nasahacker.convertit.dto.AudioFormat
import com.nasahacker.convertit.dto.AudioMetadata
import com.nasahacker.convertit.dto.Constant.AUDIO_FORMAT
import com.nasahacker.convertit.dto.Constant.BITRATE
import com.nasahacker.convertit.dto.Constant.FOLDER_DIR
import com.nasahacker.convertit.dto.Constant.FORMAT_ARRAY
import com.nasahacker.convertit.dto.Constant.STORAGE_PERMISSION_CODE
import com.nasahacker.convertit.dto.Constant.URI_LIST
import com.nasahacker.convertit.service.AudioConversionService
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10
import kotlin.math.pow

object AppUtil {

    fun openFilePicker(context: Context, pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted(context)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(context)
        }
    }

    fun receiverFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.RECEIVER_EXPORTED
    } else {
        ContextCompat.RECEIVER_NOT_EXPORTED
    }

    fun openLink(context: Context, link: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    fun shareMusicFile(context: Context, file: File) {
        if (file.exists()) {
            val fileUri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.label_share_music_file)
                )
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.label_file_does_not_exist), Toast.LENGTH_SHORT
            ).show()
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
            Toast.makeText(
                context,
                context.getString(R.string.label_no_app_found_to_open_the_file), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getFilesFromUris(context: Context, uriList: List<Uri>): List<AudioFile> {
        return uriList
            .mapNotNull { uri -> getFileFromUri(context, uri) }
            .map { file ->
                AudioFile(
                    name = file.name,
                    size = getFileSizeInReadableFormat(context, file),
                    file = file
                )
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

    fun getAudioFilesFromConvertedFolder(context: Context): List<AudioFile> {
        val convertedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR
        )

        return convertedDir
            .takeIf { it.exists() && it.isDirectory }
            ?.listFiles()
            ?.filter { file ->
                FORMAT_ARRAY.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
            }
            ?.map { file ->
                AudioFile(
                    name = file.name,
                    size = getFileSizeInReadableFormat(context, file),
                    file = file
                )
            } ?: emptyList()
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


    fun startAudioConvertService(
        uriList: ArrayList<Uri>,
        bitrate: String,
        format: String
    ) {
        Log.d(
            "ZERO_DOLLAR",
            "Starting audio conversion with the following details:\n" +
                    "URI List Size: ${uriList.size}\n" +
                    "Bitrate: $bitrate\n" +
                    "Format: $format"
        )

        val intent = Intent(App.application, AudioConversionService::class.java).apply {
            putParcelableArrayListExtra(URI_LIST, uriList)
            putExtra(BITRATE, bitrate)
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


    fun deleteFile(context: Context, file: File) {
        val success = file.delete()
        val resultMessage = if (success) {
            context.getString(R.string.label_file_deleted_successfully)
        } else {
            context.getString(R.string.label_something_went_wrong_status_failed)
        }
        Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
    }

    fun convertAudio(
        context: Context,
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR
        ).apply { mkdirs() }
        val outputPaths = mutableListOf<String>()
        uris.forEach { uri ->
            val inputPath = copyUriToInternalStorage(context, uri) ?: run {
                onFailure(context.getString(R.string.label_failed_to_copy_file_from_uri, uri))
                return
            }
            val outputFilePath = File(
                musicDir,
                "${File(inputPath).nameWithoutExtension}${outputFormat.extension}"
            ).absolutePath
            val command = arrayOf(
                "-y",
                "-i",
                inputPath,
                "-map",
                "0",
                "-map_metadata",
                "0",
                "-c:a",
                AudioCodec.fromFormat(outputFormat).codec,
                "-b:a",
                bitrate.bitrate,
                "-c:v",
                "copy",
                outputFilePath
            )
            FFmpeg.executeAsync(command) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    outputPaths.add(outputFilePath)
                    if (outputPaths.size == uris.size) onSuccess(outputPaths)
                } else {
                    onFailure(
                        context.getString(
                            R.string.label_conversion_failed_for_file_with_return_code,
                            inputPath,
                            returnCode
                        )
                    )
                }
            }
        }
    }

    fun handleNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
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

    private fun isStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions(context: Context) {
        if (!isStoragePermissionGranted(context)) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                STORAGE_PERMISSION_CODE
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.label_storage_permissions_are_already_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}