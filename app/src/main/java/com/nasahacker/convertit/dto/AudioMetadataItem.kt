package com.nasahacker.convertit.dto

import android.graphics.Bitmap

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
data class AudioMetadataItem(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val trackNumber: String? = null,
    val totalTracks: String? = null, // Added to complement trackNumber often seen as TRACKNUMBER/TOTALTRACKS
    val discNumber: String? = null,
    val totalDiscs: String? = null, // Added to complement discNumber
    val year: String? = null, // Changed from date to year for simplicity, can be parsed from DATE
    val genre: String? = null,
    val composer: String? = null,
    // val lyricist: String? = null, // Consider if all these are primary, can be added later
    // val performer: String? = null,
    // val conductor: String? = null,
    // val remixer: String? = null,
    val comment: String? = null,
    val lyrics: String? = null,
    // Raw cover art bytes, Bitmap is for UI display only
    internal val coverArtBytes: ByteArray? = null
) {
    // Overriding equals and hashCode for ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioMetadataItem

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (albumArtist != other.albumArtist) return false
        if (trackNumber != other.trackNumber) return false
        if (totalTracks != other.totalTracks) return false
        if (discNumber != other.discNumber) return false
        if (totalDiscs != other.totalDiscs) return false
        if (year != other.year) return false
        if (genre != other.genre) return false
        if (composer != other.composer) return false
        if (comment != other.comment) return false
        if (lyrics != other.lyrics) return false
        if (coverArtBytes != null) {
            if (other.coverArtBytes == null) return false
            if (!coverArtBytes.contentEquals(other.coverArtBytes)) return false
        } else if (other.coverArtBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (albumArtist?.hashCode() ?: 0)
        result = 31 * result + (trackNumber?.hashCode() ?: 0)
        result = 31 * result + (totalTracks?.hashCode() ?: 0)
        result = 31 * result + (discNumber?.hashCode() ?: 0)
        result = 31 * result + (totalDiscs?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (composer?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (lyrics?.hashCode() ?: 0)
        result = 31 * result + (coverArtBytes?.contentHashCode() ?: 0)
        return result
    }
}

// Helper to get value from PropertyMap, joining if array
fun Array<String>?.joinOrNull(separator: String = ", "): String? {
    return this?.joinToString(separator)?.ifEmpty { null }
}

// Helper to get first or null
fun Array<String>?.firstOrNullIfEmpty(): String? {
    return this?.firstOrNull()?.ifEmpty { null }
}
