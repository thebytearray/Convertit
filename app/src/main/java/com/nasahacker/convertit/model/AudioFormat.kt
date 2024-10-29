package com.nasahacker.convertit.model

enum class AudioFormat(val extension: String) {
    MP3(".mp3"),
    WAV(".wav"),
    AAC(".aac"),
    OGG(".ogg"),
    M4A(".m4a");

    companion object {
        fun fromExtension(extension: String?): AudioFormat {
            return entries.find { it.extension.equals(extension, ignoreCase = true) }
                ?: MP3
        }
    }
}
