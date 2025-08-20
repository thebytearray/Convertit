package com.nasahacker.convertit.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
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
        override fun getAudioFilesFromConvertedFolder(customSaveUri: Uri?): List<AudioFile> {
            android.util.Log.d("FileAccessRepositoryImpl", "getAudioFilesFromConvertedFolder called with URI: $customSaveUri")
            val convertedDir = getOutputDirectory(customSaveUri)
            android.util.Log.d("FileAccessRepositoryImpl", "Resolved directory: ${convertedDir.absolutePath}")
            android.util.Log.d("FileAccessRepositoryImpl", "Directory exists: ${convertedDir.exists()}, isDirectory: ${convertedDir.isDirectory}")
            
            val formats = context.resources.getStringArray(R.array.format_array).toList()
            android.util.Log.d("FileAccessRepositoryImpl", "Looking for formats: $formats")

            val allFiles = convertedDir
                .takeIf { it.exists() && it.isDirectory }
                ?.listFiles()
            
            android.util.Log.d("FileAccessRepositoryImpl", "Total files in directory: ${allFiles?.size ?: 0}")
            allFiles?.forEach { file ->
                android.util.Log.d("FileAccessRepositoryImpl", "Found file: ${file.name} (extension: ${file.extension})")
            }

            val audioFiles = allFiles
                ?.filter { file ->
                    val isAudioFile = formats.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
                    android.util.Log.d("FileAccessRepositoryImpl", "File ${file.name} is audio file: $isAudioFile")
                    isAudioFile
                }?.map { file ->
                    AudioFile(
                        name = file.name,
                        size = getReadableFileSize(file),
                        format = file.extension,
                        file = file,
                    )
                } ?: emptyList()
            
            android.util.Log.d("FileAccessRepositoryImpl", "Returning ${audioFiles.size} audio files")
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
            android.util.Log.d("FileAccessRepositoryImpl", "getOutputDirectory called with: $customSaveUri")
            return if (customSaveUri != null) {
                try {
                    val customPath = customSaveUri.path
                    android.util.Log.d("FileAccessRepositoryImpl", "Custom URI path: '$customPath'")
                    if (customPath != null && customPath.contains("/tree/primary:")) {
                        val actualPath = customPath.replace("/tree/primary:", "/storage/emulated/0/")
                        android.util.Log.d("FileAccessRepositoryImpl", "Converted to actual path: '$actualPath'")
                        android.util.Log.d("FileAccessRepositoryImpl", "Original customPath was: '$customPath'")
                        val customDir = File(actualPath)
                        if (customDir.exists() || customDir.mkdirs()) {
                            if (customDir.canWrite()) {
                                android.util.Log.d("FileAccessRepositoryImpl", "Using custom directory: ${customDir.absolutePath}")
                                customDir
                            } else {
                                android.util.Log.d("FileAccessRepositoryImpl", "Custom directory not writable, using default")
                                getDefaultOutputDirectory()
                            }
                        } else {
                            android.util.Log.d("FileAccessRepositoryImpl", "Failed to create custom directory, using default")
                            getDefaultOutputDirectory()
                        }
                    } else if (customPath != null && customPath.contains("/tree/")) {
                        val actualPath = customPath.replace("/tree/", "/storage/").replace(":", "/")
                        android.util.Log.d("FileAccessRepositoryImpl", "Alternative path conversion: '$actualPath'")
                        val customDir = File(actualPath)
                        if ((customDir.exists() || customDir.mkdirs()) && customDir.canWrite()) {
                            android.util.Log.d("FileAccessRepositoryImpl", "Using alternative custom directory: ${customDir.absolutePath}")
                            customDir
                        } else {
                            android.util.Log.d("FileAccessRepositoryImpl", "Alternative custom directory failed, using default")
                            getDefaultOutputDirectory()
                        }
                    } else {
                        android.util.Log.d("FileAccessRepositoryImpl", "Custom path doesn't match expected patterns: '$customPath', using default")
                        getDefaultOutputDirectory()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FileAccessRepositoryImpl", "Exception processing custom URI: ${e.message}")
                    getDefaultOutputDirectory()
                }
            } else {
                android.util.Log.d("FileAccessRepositoryImpl", "No custom URI provided, using default directory")
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
