package com.nasahacker.convertit.model

import android.net.Uri

import android.graphics.Bitmap

data class AudioMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val track: String? = null,
    val year: String? = null,
    val coverArtUri: Uri? = null,
    val coverArtBitmap: Bitmap? = null,
    val uri: Uri
)
