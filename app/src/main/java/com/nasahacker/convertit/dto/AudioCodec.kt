package com.nasahacker.convertit.dto

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

enum class AudioCodec(
    val codec: String,
) {
    FLAC("flac"),
    MP3("libmp3lame"),
    M4A("aac"),
    WAV("pcm_s16le"),
    AAC("aac"),
    OGG("libvorbis"),
    OPUS("libopus"),
    AIFF("pcm_s16le"),
    WMA("wmav2"),
    MKA("libvorbis"),
    SPX("libspeex"),
    ;

    companion object {
        fun fromFormat(format: AudioFormat): AudioCodec =
            when (format) {
                AudioFormat.MP3 -> MP3
                AudioFormat.M4A -> M4A
                AudioFormat.WAV -> WAV
                AudioFormat.AAC -> AAC
                AudioFormat.OGG -> OGG
                AudioFormat.OPUS -> OPUS
                AudioFormat.AIFF -> AIFF
                AudioFormat.WMA -> WMA
                AudioFormat.MKA -> MKA
                AudioFormat.SPX -> SPX
                AudioFormat.FLAC -> FLAC
            }
    }
}
