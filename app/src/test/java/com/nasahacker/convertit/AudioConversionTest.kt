package com.nasahacker.convertit

import android.content.Context
import android.net.Uri
import com.nasahacker.convertit.dto.AudioBitrate
import com.nasahacker.convertit.dto.AudioFormat
import com.nasahacker.convertit.util.AppUtil
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

class AudioConversionTest {
    @Mock
    private lateinit var mockContext: Context

    private lateinit var testFile: File
    private lateinit var outputDir: File
    private lateinit var testResourcesDir: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Create test directories
        testResourcesDir = File("src/test/resources")
        testResourcesDir.mkdirs()
        
        outputDir = File("build/test_output")
        outputDir.mkdirs()

        // Create a test FLAC file if it doesn't exist
        testFile = File(testResourcesDir, "sample.flac")
        if (!testFile.exists()) {
            // Create a minimal valid FLAC file for testing
            testFile.writeBytes(createMinimalFlacFile())
        }

        // Mock context methods
        `when`(mockContext.getExternalFilesDir(null)).thenReturn(outputDir)
        `when`(mockContext.getString(any())).thenReturn("Test String")
    }

    @After
    fun cleanup() {
        // Clean up test files
        outputDir.listFiles()?.forEach { it.delete() }
        outputDir.delete()
    }

    @Test
    fun testConvertToAllFormats() {
        val formats = AudioFormat.values()
        val bitrates = AudioBitrate.values()

        formats.forEach { format ->
            bitrates.forEach { bitrate ->
                testConversion(format, bitrate)
            }
        }
    }

    private fun testConversion(format: AudioFormat, bitrate: AudioBitrate) {
        val latch = CountDownLatch(1)
        var conversionSuccess = false
        var errorMessage: String? = null

        AppUtil.convertAudio(
            context = mockContext,
            playbackSpeed = "1.0",
            uris = listOf(Uri.fromFile(testFile)),
            outputFormat = format,
            bitrate = bitrate,
            onSuccess = { outputPaths ->
                // Verify output file exists and has correct extension
                val outputFile = File(outputPaths.first())
                assertTrue(
                    "Output file should exist for format ${format.name} and bitrate ${bitrate.name}",
                    outputFile.exists()
                )
                assertTrue(
                    "Output file should have correct extension for format ${format.name}",
                    outputFile.extension.equals(format.extension.trimStart('.'), ignoreCase = true)
                )
                conversionSuccess = true
                latch.countDown()
            },
            onFailure = { error ->
                errorMessage = error
                latch.countDown()
            },
            onProgress = { /* Progress updates not needed for test */ }
        )

        assertTrue(
            "Conversion timed out for format ${format.name} and bitrate ${bitrate.name}",
            latch.await(30, TimeUnit.SECONDS)
        )

        assertTrue(
            "Conversion failed for format ${format.name} and bitrate ${bitrate.name}: $errorMessage",
            conversionSuccess
        )
    }

    private fun createMinimalFlacFile(): ByteArray {
        // This creates a minimal valid FLAC file header
        // Note: This is just a placeholder. In a real test, you should use a proper FLAC file
        return byteArrayOf(
            0x66, 0x4C, 0x61, 0x43, // "fLaC" marker
            0x00, 0x00, 0x00, 0x22, // Metadata block header
            0x10, // Last metadata block flag + block type
            0x00, 0x00, 0x00, 0x00, // Block length
            0x00, 0x00, 0x00, 0x00, // Sample rate
            0x00, 0x00, // Channels
            0x00, 0x00, // Bits per sample
            0x00, 0x00, 0x00, 0x00, // Total samples
            0x00, 0x00, 0x00, 0x00, // MD5 signature
            0x00, 0x00, 0x00, 0x00, // MD5 signature (continued)
            0x00, 0x00, 0x00, 0x00, // MD5 signature (continued)
            0x00, 0x00, 0x00, 0x00  // MD5 signature (continued)
        )
    }
} 