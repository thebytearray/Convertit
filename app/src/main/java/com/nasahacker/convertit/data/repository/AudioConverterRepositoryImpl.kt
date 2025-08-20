package com.nasahacker.convertit.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.nasahacker.convertit.App
import com.nasahacker.convertit.R
import com.nasahacker.convertit.domain.model.AudioBitrate
import com.nasahacker.convertit.domain.model.AudioCodec
import com.nasahacker.convertit.domain.model.AudioFormat
import com.nasahacker.convertit.domain.repository.AudioConverterRepository
import com.nasahacker.convertit.domain.repository.FileAccessRepository
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.util.AppConfig.AUDIO_FORMAT
import com.nasahacker.convertit.util.AppConfig.AUDIO_PLAYBACK_SPEED
import com.nasahacker.convertit.util.AppConfig.BITRATE
import com.nasahacker.convertit.util.AppConfig.URI_LIST
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AudioConverterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileAccessRepository: FileAccessRepository
) : AudioConverterRepository {

    val TAG = "Audio"

    override suspend fun convertAudio(
        customSaveUri: Uri?,
        playbackSpeed: String,
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit,
        onProgress: (Int) -> Unit,
    ) {
        startConversionService(uris, bitrate, playbackSpeed, outputFormat)
    }

    private fun startConversionService(
        uris: List<Uri>, bitrate: AudioBitrate, playbackSpeed: String, outputFormat: AudioFormat
    ) {
        Log.d(
            TAG,
            "Starting audio conversion service with the following details:\n" + "URI List Size: ${uris.size}\n" + "Bitrate: ${bitrate.bitrate}\n" + "Format: ${outputFormat.extension}"
        )

        val intent = Intent(App.application, ConvertItService::class.java).apply {
            putParcelableArrayListExtra(URI_LIST, ArrayList(uris))
            putExtra(BITRATE, bitrate.bitrate)
            putExtra(AUDIO_PLAYBACK_SPEED, playbackSpeed)
            putExtra(AUDIO_FORMAT, outputFormat.extension)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            Log.d(TAG, "Starting foreground service...")
            App.application.startForegroundService(intent)
        } else {
            Log.d(TAG, "Starting regular service...")
            App.application.startService(intent)
        }
    }


    override suspend fun performConversion(
        customSaveUri: Uri?,
        playbackSpeed: String,
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit,
        onProgress: (Int) -> Unit,
    ) {
        val musicDir = fileAccessRepository.getOutputDirectory(customSaveUri)
        val outputPaths = mutableListOf<String>()
        val totalFiles = uris.size
        var processedFiles = 0
        val maxConcurrentConversions = 2
        val conversionQueue = mutableListOf<Pair<Uri, Int>>()

        onProgress(0)

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

            val (uri, _) = conversionQueue.removeAt(0)
            val inputFileName = getFileName(context.contentResolver, uri)
            val inputFileNameWithoutExtension = inputFileName.substringBeforeLast(".")

            var outputFileName = "${inputFileNameWithoutExtension}${outputFormat.extension}"
            var outputFilePath = File(musicDir, outputFileName).absolutePath

            var counter = 1
            while (File(outputFilePath).exists()) {
                outputFileName =
                    "${inputFileNameWithoutExtension}(${counter})${outputFormat.extension}"
                outputFilePath = File(musicDir, outputFileName).absolutePath
                counter++
            }

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val baseProgress = ((processedFiles.toFloat() / totalFiles) * 100).toInt()
                    onProgress(baseProgress)

                    val mediaDuration = getAudioDuration(tempFile.absolutePath)

                    FFmpegKit.executeWithArgumentsAsync(
                        arrayOf(
                        "-y",
                        "-i",
                        tempFile.absolutePath,
                        "-c:a",
                        AudioCodec.fromFormat(outputFormat).codec,
                        "-b:a",
                        bitrate.bitrate,
                        "-filter:a",
                        "atempo=$playbackSpeed",
                        outputFilePath
                    ), { session ->
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
                    }, null, { statistics ->
                        if (statistics.time > 0 && mediaDuration > 0) {
                            val fileProgress =
                                ((statistics.time.toFloat() / mediaDuration) * 100).toInt()
                            val totalProgress =
                                baseProgress + ((fileProgress * (100 / totalFiles)) / 100)
                            onProgress(minOf(totalProgress, 99))
                        }
                    })
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

    private fun getAudioDuration(filePath: String): Long {
        return try {
            val session = FFprobeKit.getMediaInformation(filePath)
            val mediaInformation = session.mediaInformation
            if (mediaInformation != null) {
                val duration = mediaInformation.duration
                if (duration != null) {
                    (duration.toDouble() * 1000).toLong()
                } else {
                    0L
                }
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e("AudioConverter", "Error getting audio duration: ${e.message}")
            0L
        }
    }

    private fun getFileName(contentResolver: android.content.ContentResolver, uri: Uri): String =
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: "unknown"
}
