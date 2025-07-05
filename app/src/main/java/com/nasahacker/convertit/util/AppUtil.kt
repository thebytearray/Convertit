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
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
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
import com.nasahacker.convertit.util.AppConfig.APP_PREF
import com.nasahacker.convertit.util.AppConfig.PREF_CUSTOM_SAVE_LOCATION
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10
import kotlin.math.pow
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import android.graphics.Bitmap
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import com.nasahacker.convertit.dto.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
                type = "audio/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*"))
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(context)
        }
    }

    fun openVideoFilePicker(
        context: Context,
        pickFileLauncher: ActivityResultLauncher<Intent>,
    ) {
        if (isStoragePermissionGranted(context)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(context)
        }
    }

    fun openFolderPicker(
        context: Context,
        pickFolderLauncher: ActivityResultLauncher<Intent>,
    ) {
        if (isStoragePermissionGranted(context)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            pickFolderLauncher.launch(intent)
        } else {
            requestStoragePermissions(context)
        }
    }


    fun openMetadataEditorFilePicker(
        context: Context,
        pickFileLauncher: ActivityResultLauncher<Intent>,
    ) {
        if (isStoragePermissionGranted(context)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*"))
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

    fun openLink(
        context: Context,
        link: String,
    ) {
        context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }

    fun shareMusicFile(
        context: Context,
        file: File,
    ) {
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
                    context.getString(R.string.label_share_music_file),
                ),
            )
        } else {
            Toast.makeText(
                    context,
                    context.getString(R.string.label_file_does_not_exist),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun getFileSizeInReadableFormat(
        context: Context,
        file: File,
    ): String {
        val sizeInBytes = file.length()
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        val size = sizeInBytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(
            context.getString(R.string.label_file_size),
            if (size >= 100) size.toInt().toDouble() else size,
            units[digitGroups],
        )
    }

    fun openMusicFileInPlayer(
        context: Context,
        file: File,
    ) {
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
                    context.getString(R.string.label_no_app_found_to_open_the_file),
                    Toast.LENGTH_SHORT,
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
    ): String? = if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
    } else {
        File(uri.path!!).name
    }

    fun getAudioFilesFromConvertedFolder(context: Context): List<AudioFile> {
        val convertedDir = getOutputDirectory(context)

        return convertedDir.takeIf { it.exists() && it.isDirectory }?.listFiles()?.filter { file ->
                FORMAT_ARRAY.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
            }?.map { file ->
                AudioFile(
                    name = file.name,
                    size = getFileSizeInReadableFormat(context, file),
                    file = file,
                )
            } ?: emptyList()
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
        file: File,
    ) {
        val success = file.delete()
        val resultMessage = if (success) {
            context.getString(R.string.label_file_deleted_successfully)
        } else {
            context.getString(R.string.label_something_went_wrong_status_failed)
        }
        Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
    }

    fun saveCustomSaveLocation(context: Context, uri: Uri) {
        val sharedPrefs = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(PREF_CUSTOM_SAVE_LOCATION, uri.toString()).apply()
    }

    fun getCustomSaveLocation(context: Context): Uri? {
        val sharedPrefs = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(PREF_CUSTOM_SAVE_LOCATION, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun clearCustomSaveLocation(context: Context) {
        val sharedPrefs = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(PREF_CUSTOM_SAVE_LOCATION).apply()
    }

    fun getOutputDirectory(context: Context): File {
        return getDefaultOutputDirectory()
    }

    private fun getDefaultOutputDirectory(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR,
        ).apply {
            setReadable(true)
            setWritable(true)
            mkdirs()
        }
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
        val musicDir = getOutputDirectory(context)

        val outputPaths = mutableListOf<String>()
        val totalFiles = uris.size
        var processedFiles = 0
        val maxConcurrentConversions = 2
        val conversionQueue = mutableListOf<Pair<Uri, Int>>()

        uris.forEachIndexed { index, uri ->
            conversionQueue.add(uri to index)
        }

        fun processNextFile() {
            if (conversionQueue.isEmpty()) {
                if (processedFiles == totalFiles) {
                    onSuccess(outputPaths)
                }
                return
            }

            val (uri, index) = conversionQueue.removeAt(0)
            val inputFileName = getFileName(context.contentResolver, uri)
            val inputFileNameWithoutExtension = inputFileName.substringBeforeLast(".")
            
            var outputFileName = "${inputFileNameWithoutExtension}_convertit${outputFormat.extension}"
            var outputFilePath = File(musicDir, outputFileName).absolutePath

            var counter = 1
            while (File(outputFilePath).exists()) {
                outputFileName = "${inputFileNameWithoutExtension}_convertit($counter)${outputFormat.extension}"
                outputFilePath = File(musicDir, outputFileName).absolutePath
                counter++
            }

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->

                    val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val command = "-y -i \"${tempFile.absolutePath}\" -c:a ${
                        AudioCodec.fromFormat(outputFormat).codec
                    } -b:a ${bitrate.bitrate} -filter:a \"atempo=$playbackSpeed\" \"$outputFilePath\""

                    FFmpegKit.executeAsync(command) { session ->
                        tempFile.delete()
                        
                        if (ReturnCode.isSuccess(session.returnCode)) {
                            outputPaths.add(outputFilePath)
                            processedFiles++
                            
                            val progress = ((processedFiles.toFloat() / totalFiles) * 100).toInt()
                            onProgress(progress)
                            processNextFile()
                        } else {
                            onFailure(
                                context.getString(
                                    R.string.label_conversion_failed_for_file_with_return_code,
                                    inputFileName,
                                    session.returnCode.toString(),
                                ),
                            )
                        }
                    }
                } ?: throw Exception("Failed to open input stream")
            } catch (e: Exception) {
                onFailure(
                    context.getString(
                        R.string.label_conversion_failed_for_file_with_return_code,
                        inputFileName,
                        e.message ?: "Unknown error"
                    )
                )
            }
        }

        repeat(maxConcurrentConversions) {
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

    private fun requestStoragePermissions(context: Context) {
        if (!isStoragePermissionGranted(context)) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                STORAGE_PERMISSION_CODE,
            )
        } else {
            Toast.makeText(
                    context,
                    context.getString(R.string.label_storage_permissions_are_already_granted),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Loads metadata from an audio file using TagLib
     */
    suspend fun loadMetadata(context: Context, audioUri: Uri): Metadata {
        return withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(audioUri, "rw")
                    ?: return@withContext Metadata()
                
                parcelFileDescriptor.use { fd ->
                    val taglibMetadata = TagLib.getMetadata(fd.dup().detachFd(), readPictures = true)
                        ?: return@withContext Metadata()
                    
                    Metadata.fromPropertyMap(taglibMetadata.propertyMap, taglibMetadata.pictures.toList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Metadata()
            }
        }
    }

    /**
     * Saves metadata to an audio file using TagLib
     */
    suspend fun saveMetadata(context: Context, audioUri: Uri, metadata: Metadata): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(audioUri, "rw")
                    ?: return@withContext false
                
                parcelFileDescriptor.use { fd ->
                    val propertyMap = HashMap(metadata.toPropertyMap())
                    TagLib.savePropertyMap(fd.dup().detachFd(), propertyMap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Saves cover art to an audio file using TagLib
     */
    suspend fun saveCoverArt(context: Context, audioUri: Uri, bitmap: Bitmap?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(audioUri, "rw")
                    ?: return@withContext false
                
                parcelFileDescriptor.use { fd ->
                    if (bitmap != null) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                        val imageData = byteArrayOutputStream.toByteArray()
                        
                        val picture = Picture(
                            data = imageData,
                            description = "Front Cover",
                            pictureType = "Front Cover",
                            mimeType = "image/jpeg"
                        )
                        
                        TagLib.savePictures(fd.dup().detachFd(), arrayOf(picture))
                    } else {
                        TagLib.savePictures(fd.dup().detachFd(), arrayOf())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
