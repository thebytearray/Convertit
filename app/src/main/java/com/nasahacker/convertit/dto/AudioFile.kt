package com.nasahacker.convertit.dto

import java.io.File

data class AudioFile(
    val name: String,
    val size: String,
    val file: File
)