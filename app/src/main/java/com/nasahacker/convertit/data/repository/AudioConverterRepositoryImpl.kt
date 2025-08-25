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
import com.nasahacker.convertit.util.CueParser
import com.nasahacker.convertit.domain.model.CueFile
import com.nasahacker.convertit.domain.model.CueTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AudioConverterRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val fileAccessRepository: FileAccessRepository,
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
            uris: List<Uri>,
            bitrate: AudioBitrate,
            playbackSpeed: String,
            outputFormat: AudioFormat,
        ) {
            Log.d(
                TAG,
                "Starting audio conversion service with the following details:\n" + "URI List Size: ${uris.size}\n" +
                    "Bitrate: ${bitrate.bitrate}\n" +
                    "Format: ${outputFormat.extension}",
            )

            val intent =
                Intent(App.application, ConvertItService::class.java).apply {
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

            for (uri in uris) {
                val fileName = getFileName(context.contentResolver, uri)
                val isFlacOrWav = fileName.endsWith(".flac", ignoreCase = true) || 
                                fileName.endsWith(".wav", ignoreCase = true)
                
                if (isFlacOrWav) {
                    val cueFile = findCueFileForUri(uri)
                    if (cueFile != null) {
                        Log.d(TAG, "Found CUE file for $fileName, using cue-based splitting")
                        // Use cue-based conversion for this file
                        convertWithCueSplitting(customSaveUri, playbackSpeed, uri, outputFormat, bitrate, onSuccess, onFailure, onProgress)
                        return
                    }
                }
            }
            

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
                        "$inputFileNameWithoutExtension($counter)${outputFormat.extension}"
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

                        val ffmpegArgs = buildFFmpegArgs(
                            tempFile.absolutePath,
                            outputFilePath,
                            outputFormat,
                            bitrate,
                            playbackSpeed
                        )
                        
                        FFmpegKit.executeWithArgumentsAsync(
                            ffmpegArgs,
                            { session ->
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
                            },
                            null,
                            { statistics ->
                                if (statistics.time > 0 && mediaDuration > 0) {
                                    val fileProgress =
                                        ((statistics.time.toFloat() / mediaDuration) * 100).toInt()
                                    val totalProgress =
                                        baseProgress + ((fileProgress * (100 / totalFiles)) / 100)
                                    onProgress(minOf(totalProgress, 99))
                                }
                            },
                        )
                    } ?: throw Exception("Failed to open input stream")
                } catch (e: Exception) {
                    onFailure(
                        context.getString(
                            R.string.label_conversion_failed_for_file_with_return_code,
                            inputFileName,
                            e.message ?: "Unknown error",
                        ),
                    )
                }
            }

            repeat(maxConcurrentConversions) {
                processNextFile()
            }
        }

        private fun buildFFmpegArgs(
            inputPath: String,
            outputPath: String,
            outputFormat: AudioFormat,
            bitrate: AudioBitrate,
            playbackSpeed: String
        ): Array<String> {
            Log.d(TAG, "Building FFmpeg args for format: ${outputFormat.extension}, bitrate: ${bitrate.bitrate}")
            return when (outputFormat) {
                AudioFormat.AMR_WB -> {
                    arrayOf(
                        "-y",
                        "-i", inputPath,
                        "-ar", "16000",           
                        "-ac", "1",             
                        "-c:a", AudioCodec.fromFormat(outputFormat).codec,
                        "-b:a", bitrate.bitrate,
                        "-filter:a", "atempo=$playbackSpeed",
                        outputPath
                    )
                }
                AudioFormat.OPUS -> {

                    if (bitrate.bitrate.replace("k", "").toIntOrNull()?.let { it <= 48 } == true) {
                        arrayOf(
                            "-y",
                            "-i", inputPath,
                            "-c:a", AudioCodec.fromFormat(outputFormat).codec,
                            "-b:a", bitrate.bitrate,
                            "-application", "voip",   
                            "-filter:a", "atempo=$playbackSpeed",
                            outputPath
                        )
                    } else {
                       
                        arrayOf(
                            "-y",
                            "-i", inputPath,
                            "-c:a", AudioCodec.fromFormat(outputFormat).codec,
                            "-b:a", bitrate.bitrate,
                            "-filter:a", "atempo=$playbackSpeed",
                            outputPath
                        )
                    }
                }
                else -> {
                    arrayOf(
                        "-y",
                        "-i", inputPath,
                        "-c:a", AudioCodec.fromFormat(outputFormat).codec,
                        "-b:a", bitrate.bitrate,
                        "-filter:a", "atempo=$playbackSpeed",
                        outputPath
                    )
                }
            }
        }

        private fun getAudioDuration(filePath: String): Long =
            try {
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

        override suspend fun convertWithCueSplitting(
            customSaveUri: Uri?,
            playbackSpeed: String,
            uri: Uri,
            outputFormat: AudioFormat,
            bitrate: AudioBitrate,
            onSuccess: (List<String>) -> Unit,
            onFailure: (String) -> Unit,
            onProgress: (Int) -> Unit,
        ) {
            val musicDir = fileAccessRepository.getOutputDirectory(customSaveUri)
            val outputPaths = mutableListOf<String>()
            
            onProgress(0)
            
            try {

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempAudioFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}")
                    FileOutputStream(tempAudioFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    

                    val cueFile = findCueFileForUri(uri)
                    if (cueFile == null) {

                        Log.d(TAG, "No CUE file found, performing regular conversion")
                        performConversion(customSaveUri, playbackSpeed, listOf(uri), outputFormat, bitrate, onSuccess, onFailure, onProgress)
                        tempAudioFile.delete()
                        return
                    }
                    
                    val parsedCue = CueParser.parseCueFile(cueFile)
                    if (parsedCue == null || !parsedCue.hasValidTracks()) {
                        Log.w(TAG, "Invalid or empty CUE file, performing regular conversion")
                        performConversion(customSaveUri, playbackSpeed, listOf(uri), outputFormat, bitrate, onSuccess, onFailure, onProgress)
                        tempAudioFile.delete()
                        return
                    }
                    
                    Log.d(TAG, "Found CUE file with ${parsedCue.tracks.size} tracks")
                    
                    val tracks = parsedCue.getTracksWithEndTimes()
                    val totalTracks = tracks.size
                    var processedTracks = 0
                    

                    for ((index, track) in tracks.withIndex()) {
                        val baseProgress = ((processedTracks.toFloat() / totalTracks) * 100).toInt()
                        onProgress(baseProgress)
                        
                        val trackFileName = sanitizeFileName("${track.trackNumber.toString().padStart(2, '0')} - ${track.title}")
                        var outputFileName = "$trackFileName${outputFormat.extension}"
                        var outputFilePath = File(musicDir, outputFileName).absolutePath
                        

                        var counter = 1
                        while (File(outputFilePath).exists()) {
                            outputFileName = "$trackFileName($counter)${outputFormat.extension}"
                            outputFilePath = File(musicDir, outputFileName).absolutePath
                            counter++
                        }
                        
                        val ffmpegArgs = buildFFmpegArgsForTrack(
                            tempAudioFile.absolutePath,
                            outputFilePath,
                            outputFormat,
                            bitrate,
                            playbackSpeed,
                            track
                        )
                        
                        try {
                            val session = FFmpegKit.executeWithArguments(ffmpegArgs)
                            
                            if (ReturnCode.isSuccess(session.returnCode)) {
                                outputPaths.add(outputFilePath)
                                processedTracks++
                                
                                val progress = ((processedTracks.toFloat() / totalTracks) * 100).toInt()
                                onProgress(progress)
                                
                                Log.d(TAG, "Successfully converted track ${track.trackNumber}: ${track.title}")
                            } else {
                                Log.e(TAG, "Failed to convert track ${track.trackNumber}: ${session.failStackTrace}")
                                onFailure("Failed to convert track ${track.trackNumber}: ${track.title}")
                                tempAudioFile.delete()
                                return
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting track ${track.trackNumber}: ${e.message}")
                            onFailure("Error converting track ${track.trackNumber}: ${e.message}")
                            tempAudioFile.delete()
                            return
                        }
                    }
                    
                    tempAudioFile.delete()
                    

                    if (cueFile.name.endsWith("_embedded.cue")) {
                        cueFile.delete()
                    }
                    
                    onSuccess(outputPaths)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in CUE-based conversion: ${e.message}")
                onFailure("Error in CUE-based conversion: ${e.message}")
            }
        }
        
        private fun findCueFileForUri(audioUri: Uri): File? {
            try {

                val fileName = getFileName(context.contentResolver, audioUri)
                val fileNameWithoutExt = fileName.substringBeforeLast(".")
                val isFlac = fileName.endsWith(".flac", ignoreCase = true)
                

                val possibleLocations = listOf(
                    context.getExternalFilesDir(null),
                    context.filesDir,
                    File("/storage/emulated/0/Download"),
                    File("/storage/emulated/0/Music")
                )
                

                for (dir in possibleLocations) {
                    if (dir?.exists() == true) {
                        val cueFile = File(dir, "$fileNameWithoutExt.cue")
                        if (cueFile.exists()) {
                            return cueFile
                        }
                    }
                }
                

                if (isFlac) {

                    context.contentResolver.openInputStream(audioUri)?.use { inputStream ->
                        val tempFlacFile = File(context.cacheDir, "temp_flac_${System.currentTimeMillis()}.flac")
                        FileOutputStream(tempFlacFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        
                        val embeddedCue = CueParser.extractEmbeddedCueFromFlac(tempFlacFile)
                        tempFlacFile.delete()
                        
                        if (embeddedCue != null) {
                            Log.d(TAG, "Found embedded CUE sheet in FLAC file")
                            return embeddedCue
                        }
                    }
                }
                
                return null
            } catch (e: Exception) {
                Log.e(TAG, "Error finding CUE file: ${e.message}")
                return null
            }
        }
        
        private fun buildFFmpegArgsForTrack(
            inputPath: String,
            outputPath: String,
            outputFormat: AudioFormat,
            bitrate: AudioBitrate,
            playbackSpeed: String,
            track: CueTrack
        ): Array<String> {
            val args = mutableListOf<String>()
            
            args.addAll(arrayOf("-y", "-i", inputPath))
            

            args.addAll(arrayOf("-ss", CueParser.formatSecondsForFFmpeg(track.startTimeSeconds)))
            

            track.endTimeSeconds?.let { endTime ->
                val duration = endTime - track.startTimeSeconds
                args.addAll(arrayOf("-t", CueParser.formatSecondsForFFmpeg(duration)))
            }
            

            args.addAll(arrayOf("-c:a", AudioCodec.fromFormat(outputFormat).codec))
            args.addAll(arrayOf("-b:a", bitrate.bitrate))
            

            if (playbackSpeed != "1.0") {
                args.addAll(arrayOf("-filter:a", "atempo=$playbackSpeed"))
            }
            

            when (outputFormat) {
                AudioFormat.AMR_WB -> {
                    args.addAll(arrayOf("-ar", "16000", "-ac", "1"))
                }
                AudioFormat.OPUS -> {
                    if (bitrate.bitrate.replace("k", "").toIntOrNull()?.let { it <= 48 } == true) {
                        args.addAll(arrayOf("-application", "voip"))
                    }
                }
                else -> {

                }
            }
            
            args.add(outputPath)
            
            Log.d(TAG, "FFmpeg args for track ${track.trackNumber}: ${args.joinToString(" ")}")
            return args.toTypedArray()
        }
        
        private fun sanitizeFileName(fileName: String): String {
            return fileName.replace(Regex("[^a-zA-Z0-9._\\-\\s]"), "_")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        override suspend fun convertWithManualCue(
            customSaveUri: Uri?,
            playbackSpeed: String,
            audioUri: Uri,
            cueUri: Uri,
            outputFormat: AudioFormat,
            bitrate: AudioBitrate,
            onSuccess: (List<String>) -> Unit,
            onFailure: (String) -> Unit,
            onProgress: (Int) -> Unit,
        ) {
            val musicDir = fileAccessRepository.getOutputDirectory(customSaveUri)
            val outputPaths = mutableListOf<String>()
            
            onProgress(0)
            
            try {
                context.contentResolver.openInputStream(audioUri)?.use { audioInputStream ->
                    val tempAudioFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}")
                    FileOutputStream(tempAudioFile).use { outputStream ->
                        audioInputStream.copyTo(outputStream)
                    }
                    
                    context.contentResolver.openInputStream(cueUri)?.use { cueInputStream ->
                        val cueFileName = getFileName(context.contentResolver, cueUri)
                        if (!cueFileName.lowercase().endsWith(".cue")) {
                            Log.w(TAG, "Selected file is not a CUE file: $cueFileName")
                            onFailure(context.getString(R.string.label_invalid_cue_file))
                            tempAudioFile.delete()
                            return
                        }
                        
                        val parsedCue = CueParser.parseCueFile(cueInputStream)
                        if (parsedCue == null || !parsedCue.hasValidTracks()) {
                            Log.w(TAG, "Invalid or empty CUE file, performing regular conversion")
                            performConversion(customSaveUri, playbackSpeed, listOf(audioUri), outputFormat, bitrate, onSuccess, onFailure, onProgress)
                            tempAudioFile.delete()
                            return
                        }
                        
                        Log.d(TAG, "Manual CUE file with ${parsedCue.tracks.size} tracks")
                        
                        val tracks = parsedCue.getTracksWithEndTimes()
                        val totalTracks = tracks.size
                        var processedTracks = 0
                        
                        for ((index, track) in tracks.withIndex()) {
                            val baseProgress = ((processedTracks.toFloat() / totalTracks) * 100).toInt()
                            onProgress(baseProgress)
                            
                            val trackFileName = sanitizeFileName("${track.trackNumber.toString().padStart(2, '0')} - ${track.title}")
                            var outputFileName = "$trackFileName${outputFormat.extension}"
                            var outputFilePath = File(musicDir, outputFileName).absolutePath
                            
                            Log.d(TAG, "Track ${track.trackNumber}: '$trackFileName' -> '$outputFilePath'")
                            
                            var counter = 1
                            while (File(outputFilePath).exists()) {
                                outputFileName = "$trackFileName($counter)${outputFormat.extension}"
                                outputFilePath = File(musicDir, outputFileName).absolutePath
                                counter++
                            }
                            
                            val ffmpegArgs = buildFFmpegArgsForTrack(
                                tempAudioFile.absolutePath,
                                outputFilePath,
                                outputFormat,
                                bitrate,
                                playbackSpeed,
                                track
                            )
                            
                            try {
                                Log.d(TAG, "FFmpeg command: ${ffmpegArgs.joinToString(" ")}")
                                val session = FFmpegKit.executeWithArguments(ffmpegArgs)
                                
                                if (ReturnCode.isSuccess(session.returnCode)) {
                                    outputPaths.add(outputFilePath)
                                    processedTracks++
                                    
                                    val progress = ((processedTracks.toFloat() / totalTracks) * 100).toInt()
                                    onProgress(progress)
                                    
                                    Log.d(TAG, "Successfully converted track ${track.trackNumber}: ${track.title}")
                                } else {
                                    Log.e(TAG, "Failed to convert track ${track.trackNumber}: ${session.failStackTrace}")
                                    onFailure("Failed to convert track ${track.trackNumber}: ${track.title}")
                                    tempAudioFile.delete()
                                    return
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error converting track ${track.trackNumber}: ${e.message}")
                                onFailure("Error converting track ${track.trackNumber}: ${e.message}")
                                tempAudioFile.delete()
                                return
                            }
                        }
                        
                        tempAudioFile.delete()
                        onSuccess(outputPaths)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in manual CUE-based conversion: ${e.message}")
                onFailure("Error in manual CUE-based conversion: ${e.message}")
            }
        }

        private fun getFileName(
            contentResolver: android.content.ContentResolver,
            uri: Uri,
        ): String =
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: "unknown"
    }
