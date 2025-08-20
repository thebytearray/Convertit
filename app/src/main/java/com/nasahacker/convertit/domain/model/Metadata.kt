package com.nasahacker.convertit.domain.model

import com.kyant.taglib.Picture

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



data class Metadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val genre: String = "",
    val year: String = "",
    val track: String = "",
    val comment: String = "",
    val pictures: List<Picture> = emptyList(),
    val propertyMap: Map<String, Array<String>> = emptyMap()
) {
    fun toPropertyMap(): Map<String, Array<String>> {
        val map = mutableMapOf<String, Array<String>>()
        
        if (title.isNotBlank()) map["TITLE"] = arrayOf(title)
        if (artist.isNotBlank()) map["ARTIST"] = arrayOf(artist)
        if (album.isNotBlank()) map["ALBUM"] = arrayOf(album)
        if (albumArtist.isNotBlank()) map["ALBUMARTIST"] = arrayOf(albumArtist)
        if (genre.isNotBlank()) map["GENRE"] = arrayOf(genre)
        if (year.isNotBlank()) map["DATE"] = arrayOf(year)
        if (track.isNotBlank()) map["TRACKNUMBER"] = arrayOf(track)
        if (comment.isNotBlank()) map["COMMENT"] = arrayOf(comment)
        
        return map
    }
    
    companion object {
        fun fromPropertyMap(propertyMap: Map<String, Array<String>>, pictures: List<Picture> = emptyList()): Metadata {
            return Metadata(
                title = propertyMap["TITLE"]?.firstOrNull() ?: "",
                artist = propertyMap["ARTIST"]?.firstOrNull() ?: "",
                album = propertyMap["ALBUM"]?.firstOrNull() ?: "",
                albumArtist = propertyMap["ALBUMARTIST"]?.firstOrNull() ?: "",
                genre = propertyMap["GENRE"]?.firstOrNull() ?: "",
                year = propertyMap["DATE"]?.firstOrNull() ?: propertyMap["YEAR"]?.firstOrNull() ?: "",
                track = propertyMap["TRACKNUMBER"]?.firstOrNull() ?: "",
                comment = propertyMap["COMMENT"]?.firstOrNull() ?: "",
                pictures = pictures,
                propertyMap = propertyMap
            )
        }
    }
} 