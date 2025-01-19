package com.nasahacker.convertit.dto

enum class AudioCodec(val codec: String) {
    MP3("libmp3lame"),
    M4A("aac"),
    WAV("pcm_s16le"),
    FLAC("flac"),
    AAC("aac"),
    OGG("libvorbis");

    companion object {
        fun fromFormat(format: AudioFormat): AudioCodec {
            return when (format) {
                AudioFormat.MP3 -> MP3
                AudioFormat.M4A -> M4A
                AudioFormat.WAV -> WAV
                AudioFormat.FLAC -> FLAC
                AudioFormat.AAC -> AAC
                AudioFormat.OGG -> OGG
            }
        }
    }
}
