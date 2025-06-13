package com.nasahacker.convertit.dto

import com.kyant.taglib.Picture

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