package com.nasahacker.convertit.dto

import android.graphics.Bitmap
import android.net.Uri


data class AudioMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val track: String? = null,
    val year: String? = null,
    val coverArtBitmap: Bitmap? = null,
)