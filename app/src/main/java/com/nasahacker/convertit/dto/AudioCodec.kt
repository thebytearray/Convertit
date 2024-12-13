package com.nasahacker.convertit.dto

enum class AudioCodec(val codec: String) {
    MP3("libmp3lame"),
    M4A("aac");

    companion object {
        fun fromFormat(format: AudioFormat): AudioCodec {
            return when (format) {
                AudioFormat.MP3 -> MP3
                AudioFormat.M4A -> M4A
            }
        }
    }
}