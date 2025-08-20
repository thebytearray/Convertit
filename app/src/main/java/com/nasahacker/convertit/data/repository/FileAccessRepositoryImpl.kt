package com.nasahacker.convertit.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import com.nasahacker.convertit.R
import com.nasahacker.convertit.domain.model.AudioFile
import com.nasahacker.convertit.domain.repository.FileAccessRepository
import com.nasahacker.convertit.util.AppConfig.FOLDER_DIR
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow

class FileAccessRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : FileAccessRepository {
        
        companion object {
            private const val TAG = "FileAccessRepositoryImpl"
        }
        override fun getAudioFilesFromConvertedFolder(customSaveUri: Uri?): List<AudioFile> {
            Log.d(TAG, "getAudioFilesFromConvertedFolder called with URI: $customSaveUri")
            val convertedDir = getOutputDirectory(customSaveUri)
            Log.d(TAG, "Resolved directory: ${convertedDir.absolutePath}")
            Log.d(TAG, "Directory exists: ${convertedDir.exists()}, isDirectory: ${convertedDir.isDirectory}")
            
            val formats = context.resources.getStringArray(R.array.format_array).toList()
            Log.d(TAG, "Looking for formats: $formats")

            val allFiles = convertedDir
                .takeIf { it.exists() && it.isDirectory }
                ?.listFiles()
            
            Log.d(TAG, "Total files in directory: ${allFiles?.size ?: 0}")
            allFiles?.forEach { file ->
                Log.d(TAG, "Found file: ${file.name} (extension: ${file.extension})")
            }

            val audioFiles = allFiles
                ?.filter { file ->
                    val isAudioFile = formats.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
                    Log.d(TAG, "File ${file.name} is audio file: $isAudioFile")
                    isAudioFile
                }?.map { file ->
                    AudioFile(
                        name = file.name,
                        size = getReadableFileSize(file),
                        format = file.extension,
                        file = file,
                    )
                } ?: emptyList()
            
            Log.d(TAG, "Returning ${audioFiles.size} audio files")
            return audioFiles
        }

        override fun getFileFromUri(uri: Uri): File? {
            return try {
                val fileName = getFileName(context, uri) ?: return null
                val fileSize = context.contentResolver.openFileDescriptor(uri, "rw")?.statSize ?: 0
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

        override fun getReadableFileSize(file: File): String {
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

        override fun getOutputDirectory(customSaveUri: Uri?): File {
            Log.d(TAG, "getOutputDirectory called with: $customSaveUri")
            return if (customSaveUri != null) {
                try {
                    val customPath = customSaveUri.path
                    Log.d(TAG, "Custom URI path: '$customPath'")
                    if (customPath != null && customPath.contains("/tree/primary:")) {
                        val actualPath = customPath.replace("/tree/primary:", "/storage/emulated/0/")
                        Log.d(TAG, "Converted to actual path: '$actualPath'")
                        Log.d(TAG, "Original customPath was: '$customPath'")
                        val customDir = File(actualPath)
                        if (customDir.exists() || customDir.mkdirs()) {
                            if (customDir.canWrite()) {
                                Log.d(TAG, "Using custom directory: ${customDir.absolutePath}")
                                customDir
                            } else {
                                Log.d(TAG, "Custom directory not writable, using default")
                                getDefaultOutputDirectory()
                            }
                        } else {
                            Log.d(TAG, "Failed to create custom directory, using default")
                            getDefaultOutputDirectory()
                        }
                    } else if (customPath != null && customPath.contains("/tree/")) {
                        val actualPath = customPath.replace("/tree/", "/storage/").replace(":", "/")
                        Log.d(TAG, "Alternative path conversion: '$actualPath'")
                        val customDir = File(actualPath)
                        if ((customDir.exists() || customDir.mkdirs()) && customDir.canWrite()) {
                            Log.d(TAG, "Using alternative custom directory: ${customDir.absolutePath}")
                            customDir
                        } else {
                            Log.d(TAG, "Alternative custom directory failed, using default")
                            getDefaultOutputDirectory()
                        }
                    } else {
                        Log.d(TAG, "Custom path doesn't match expected patterns: '$customPath', using default")
                        getDefaultOutputDirectory()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception processing custom URI: ${e.message}")
                    getDefaultOutputDirectory()
                }
            } else {
                Log.d(TAG, "No custom URI provided, using default directory")
                getDefaultOutputDirectory()
            }
        }

        private fun getDefaultOutputDirectory(): File =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                FOLDER_DIR,
            ).apply {
                setReadable(true)
                setWritable(true)
                mkdirs()
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
            } else {
                File(uri.path!!).name
            }

        // maybe we need in future so keep it
//    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String =
//        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
//        } ?: "unknown"
    }
