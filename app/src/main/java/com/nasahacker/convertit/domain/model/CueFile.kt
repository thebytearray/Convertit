package com.nasahacker.convertit.domain.model

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
data class CueTrack(
    val trackNumber: Int,
    val title: String,
    val performer: String? = null,
    val startTime: String,
    val endTime: String? = null,
    val startTimeSeconds: Double,
    val endTimeSeconds: Double? = null
)

data class CueFile(
    val title: String? = null,
    val performer: String? = null,
    val file: String? = null,
    val tracks: List<CueTrack> = emptyList()
) {
    fun hasValidTracks(): Boolean = tracks.size > 1
    fun getTracksWithEndTimes(): List<CueTrack> {
        if (tracks.isEmpty()) return emptyList()
        
        return tracks.mapIndexed { index, track ->
            val endTime = if (index < tracks.size - 1) {
                tracks[index + 1].startTime
            } else {
                null 
            }
            
            val endTimeSeconds = if (index < tracks.size - 1) {
                tracks[index + 1].startTimeSeconds
            } else {
                null
            }
            
            track.copy(
                endTime = endTime,
                endTimeSeconds = endTimeSeconds
            )
        }
    }
}
