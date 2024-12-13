package com.nasahacker.convertit.dto


enum class AudioFormat(val extension: String) {
    MP3(".mp3"),
    M4A(".m4a");

    companion object {
        fun fromExtension(extension: String?): AudioFormat {
            return entries.find { it.extension.equals(extension, ignoreCase = true) }
                ?: MP3
        }
    }
}