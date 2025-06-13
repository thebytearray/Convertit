package com.nasahacker.convertit.dto

import java.util.Arrays

/**
 * Data Transfer Object for audio file metadata.
 */
data class AudioFileMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val trackNumber: String? = null,
    val albumArtist: String? = null,
    val composer: String? = null,
    val comment: String? = null,
    val coverArt: ByteArray? = null,
    val coverArtMimeType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioFileMetadata

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (genre != other.genre) return false
        if (year != other.year) return false
        if (trackNumber != other.trackNumber) return false
        if (albumArtist != other.albumArtist) return false
        if (composer != other.composer) return false
        if (comment != other.comment) return false
        if (coverArt != null) {
            if (other.coverArt == null) return false
            if (!coverArt.contentEquals(other.coverArt)) return false
        } else if (other.coverArt != null) return false
        if (coverArtMimeType != other.coverArtMimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (trackNumber?.hashCode() ?: 0)
        result = 31 * result + (albumArtist?.hashCode() ?: 0)
        result = 31 * result + (composer?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (coverArt?.contentHashCode() ?: 0)
        result = 31 * result + (coverArtMimeType?.hashCode() ?: 0)
        return result
    }
}
