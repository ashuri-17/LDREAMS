package com.ldreams.app.service

import android.content.Context
import android.media.MediaRecorder
import java.io.File

/**
 * Audio recording utility that uses the [MediaRecorder] API.
 *
 * Records audio to the app's internal cache directory by default, or to a
 * specified file path. Designed for short dream journal voice notes.
 */
class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    /**
     * Start recording audio from the device microphone.
     *
     * @param filePath Optional absolute file path for the output. When null a
     *   unique path inside the app's internal cache directory is generated.
     * @return true if recording started successfully, false otherwise.
     */
    fun startRecording(filePath: String? = null): Boolean {
        val outputFile = filePath ?: File(
            context.cacheDir,
            "dream_recording_${System.currentTimeMillis()}.3gp"
        ).absolutePath

        return try {
            releaseRecorder()

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            currentFilePath = outputFile
            true
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        } catch (e: RuntimeException) {
            // MediaRecorder can throw RuntimeException for various hardware / codec issues
            e.printStackTrace()
            false
        }
    }

    /**
     * Stop an active recording and release the recorder.
     *
     * @return The file path of the recorded audio, or null if no recording was
     *   active or an error occurred while stopping.
     */
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    // Thrown when stop() is called before enough data has been recorded
                    e.printStackTrace()
                }
                release()
            }
            mediaRecorder = null
            currentFilePath
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder?.release()
            mediaRecorder = null
            null
        } finally {
            currentFilePath = null
        }
    }

    /**
     * Return the current maximum amplitude of the active recording.
     *
     * Useful for audio visualisation while recording. Returns 0 when no
     * recording is active.
     */
    fun getAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: IllegalStateException) {
            0
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: RuntimeException) {
                    // Ignore — recorder may not have started
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            currentFilePath = null
        }
    }
}
