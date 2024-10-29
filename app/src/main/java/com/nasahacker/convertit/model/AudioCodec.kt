package com.nasahacker.convertit.model

enum class AudioCodec(val codec: String) {
    MP3("libmp3lame"),
    WAV("pcm_s16le"),
    AAC("aac"),
    OGG("libvorbis"),
    M4A("aac");

    companion object {
        fun fromFormat(format: AudioFormat): AudioCodec {
            return when (format) {
                AudioFormat.MP3 -> MP3
                AudioFormat.WAV -> WAV
                AudioFormat.AAC -> AAC
                AudioFormat.OGG -> OGG
                AudioFormat.M4A -> M4A
            }
        }
    }
}
