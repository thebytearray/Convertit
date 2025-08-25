package com.nasahacker.convertit.util

import android.net.Uri
import android.util.Log
import com.nasahacker.convertit.domain.model.CueFile
import com.nasahacker.convertit.domain.model.CueTrack
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Convertit Android app
 * <a href="https://github.com/thebytearray/Convertit">GitHub Repository</a>
 *
 * Created by Tamim Hossain.
 * Copyright (c) 2025 The Byte Array LTD.
 *
 * This file is part of the Convertit Android app.
 *
 * The Convertit Android app is free software: you can redistribute it and/or
 * modify it under the terms of the Apache License, Version 2.0 as published by
 * the Apache Software Foundation.
 *
 * The Convertit Android app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Apache License for more
 * details.
 *
 * You should have received a copy of the Apache License
 * along with the Convertit Android app. If not, see <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.
 *
 * @author Tamim Hossain
 * @company The Byte Array LTD
 * @year 2025
 * @license Apache-2.0
 */
object CueParser {
    
    private const val TAG = "CueParser"
    

    fun parseCueFile(inputStream: InputStream): CueFile? {
        return try {
            val lines = BufferedReader(InputStreamReader(inputStream)).readLines()
            parseCueLines(lines)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing CUE file: ${e.message}")
            null
        }
    }
    

    fun parseCueFile(file: File): CueFile? {
        return try {
            if (!file.exists() || !file.canRead()) {
                Log.w(TAG, "CUE file does not exist or cannot be read: ${file.absolutePath}")
                return null
            }
            
            val lines = file.readLines()
            parseCueLines(lines)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing CUE file: ${e.message}")
            null
        }
    }
    

    fun findCueFileForAudio(audioFile: File): File? {
        val audioPath = audioFile.absolutePath
        val audioName = audioFile.nameWithoutExtension
        val audioDir = audioFile.parentFile ?: return null
        

        val possibleCueNames = listOf(
            "$audioName.cue",
            "${audioName.lowercase()}.cue", 
            "${audioName.uppercase()}.cue",
            "CDImage.cue",
            "cd.cue"
        )
        
        for (cueName in possibleCueNames) {
            val cueFile = File(audioDir, cueName)
            if (cueFile.exists() && cueFile.canRead()) {
                Log.d(TAG, "Found CUE file: ${cueFile.absolutePath}")
                return cueFile
            }
        }
        
        Log.d(TAG, "No CUE file found for audio: $audioPath")
        return null
    }
    

    fun extractEmbeddedCueFromFlac(flacFile: File): File? {
        return try {
            val tempCueFile = File(flacFile.parentFile, "${flacFile.nameWithoutExtension}_embedded.cue")
            

            val process = ProcessBuilder(
                "ffprobe", 
                "-v", "quiet",
                "-show_entries", "format_tags=cuesheet",
                "-of", "csv=p=0",
                flacFile.absolutePath
            ).start()
            
            process.waitFor()
            val output = process.inputStream.bufferedReader().readText().trim()
            
            if (output.isNotEmpty() && output != "N/A") {
                tempCueFile.writeText(output)
                Log.d(TAG, "Extracted embedded CUE sheet from FLAC: ${tempCueFile.absolutePath}")
                return tempCueFile
            }
            
            Log.d(TAG, "No embedded CUE sheet found in FLAC file")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting embedded CUE sheet: ${e.message}")
            null
        }
    }
    

    private fun parseCueLines(lines: List<String>): CueFile {
        var title: String? = null
        var performer: String? = null
        var file: String? = null
        val tracks = mutableListOf<CueTrack>()
        
        var currentTrackNumber: Int? = null
        var currentTrackTitle: String? = null
        var currentTrackPerformer: String? = null
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("TITLE", ignoreCase = true) -> {
                    val titleValue = extractQuotedValue(trimmedLine)
                    if (currentTrackNumber != null) {
                        currentTrackTitle = titleValue
                    } else {
                        title = titleValue
                    }
                }
                
                trimmedLine.startsWith("PERFORMER", ignoreCase = true) -> {
                    val performerValue = extractQuotedValue(trimmedLine)
                    if (currentTrackNumber != null) {
                        currentTrackPerformer = performerValue
                    } else {
                        performer = performerValue
                    }
                }
                
                trimmedLine.startsWith("FILE", ignoreCase = true) -> {
                    file = extractQuotedValue(trimmedLine)
                }
                
                trimmedLine.startsWith("TRACK", ignoreCase = true) -> {
                    currentTrackNumber = extractTrackNumber(trimmedLine)
                    currentTrackTitle = null
                    currentTrackPerformer = null
                }
                
                trimmedLine.startsWith("INDEX 01", ignoreCase = true) -> {
                    val timeStamp = extractTimeStamp(trimmedLine)
                    if (currentTrackNumber != null && timeStamp != null) {
                        val track = CueTrack(
                            trackNumber = currentTrackNumber,
                            title = currentTrackTitle ?: "Track $currentTrackNumber",
                            performer = currentTrackPerformer ?: performer,
                            startTime = timeStamp,
                            startTimeSeconds = convertTimeToSeconds(timeStamp)
                        )
                        tracks.add(track)
                    }
                }
            }
        }
        
        return CueFile(
            title = title,
            performer = performer,
            file = file,
            tracks = tracks
        )
    }
    

    private fun extractQuotedValue(line: String): String? {
        val regex = "\"([^\"]*)\"\$".toRegex()
        return regex.find(line)?.groupValues?.get(1)
    }
    

    private fun extractTrackNumber(line: String): Int? {
        val parts = line.split("\\s+".toRegex())
        return if (parts.size >= 2) {
            parts[1].toIntOrNull()
        } else null
    }
    

    private fun extractTimeStamp(line: String): String? {
        val parts = line.split("\\s+".toRegex())
        return if (parts.size >= 3) {
            parts[2]
        } else null
    }
    

    private fun convertTimeToSeconds(timeStamp: String): Double {
        val parts = timeStamp.split(":")
        return if (parts.size == 3) {
            val minutes = parts[0].toDoubleOrNull() ?: 0.0
            val seconds = parts[1].toDoubleOrNull() ?: 0.0
            val frames = parts[2].toDoubleOrNull() ?: 0.0
            
            minutes * 60 + seconds + (frames / 75.0)
        } else {
            0.0
        }
    }
    

    fun formatSecondsForFFmpeg(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val secs = seconds % 60
        
        return String.format("%02d:%02d:%06.3f", hours, minutes, secs)
    }
}
