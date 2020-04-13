package com.ninovanhooff.recordist

import com.ninovanhooff.phonograph.*
import com.ninovanhooff.phonograph.audio.recorder.RecorderContract
import com.ninovanhooff.phonograph.audio.recorder.RecorderContract.Recorder
import com.ninovanhooff.phonograph.audio.recorder.RecorderContract.RecorderCallback
import com.ninovanhooff.phonograph.exception.AppException
import com.ninovanhooff.phonograph.exception.CantProcessRecord
import com.ninovanhooff.phonograph.util.AndroidUtils
import com.ninovanhooff.recordist.data.Prefs
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*

class AppRecorderImpl private constructor(private var audioRecorder: Recorder, tasks: BackgroundQueue,
                                          processingTasks: BackgroundQueue, pr: Prefs) : AppRecorder {
    private val recordingsTasks: BackgroundQueue
    private val processingTasks: BackgroundQueue
    private val recorderCallback: RecorderCallback
    private val appCallbacks: MutableList<AppRecorderCallback>
    private val prefs: Prefs
    private val recordingData: MutableList<Int>
    private var isProcessing = false

    init {
        recordingsTasks = tasks
        this.processingTasks = processingTasks
        prefs = pr
        appCallbacks = ArrayList()
        recordingData = ArrayList()
        recorderCallback = object : RecorderCallback {
            override fun onStartRecord() {
                onRecordingStarted()
            }

            override fun onPauseRecord() {
                onRecordingPaused()
            }

            override fun onProgress(mills: Long, amplitude: Int, isRecordingActive: Boolean) {
                onRecordingProgress(mills, amplitude, isRecordingActive)
                if (isRecordingActive){
                    recordingData.add(amplitude)
                }
            }

            override fun onStopRecord(output: File) {
                onRecordProcessing()
                recordingsTasks.postRunnable(object : Runnable {
                    var id: Long = -1
                    override fun run() {
                        try {
//                            id = if (recordingData.size.toFloat() / Phonograph.getLongWaveformSampleCount().toFloat() > 1) {
//                                val duration: Long = AndroidUtils.readRecordDuration(output)
//                                val waveForm = convertRecordingData(recordingData, (duration / 1000000f).toInt())
//                                localRepository.insertFile(output.absolutePath, duration, waveForm)
//                            } else {
//                                localRepository.insertFile(output.absolutePath)
//                            }
//                            prefs.setActiveRecord(id)
                        } catch (e: IOException) {
                            Timber.e(e)
                        } catch (e: OutOfMemoryError) {
                            Timber.e(e)
                        } catch (e: IllegalStateException) {
                            Timber.e(e)
                        }
                        AndroidUtils.runOnUIThread {
                            onRecordingStopped(id, output)
                            if (recordingData.size.toFloat() / Phonograph.getLongWaveformSampleCount().toFloat() > 1) {
                                processingTasks.postRunnable {
                                    try {
                                        //localRepository.updateWaveform(id.toInt())
                                        AndroidUtils.runOnUIThread { onRecordFinishProcessing() }
                                    } catch (e: IOException) {
                                        AndroidUtils.runOnUIThread { onError(CantProcessRecord()) }
                                        Timber.e(e)
                                    } catch (e: OutOfMemoryError) {
                                        AndroidUtils.runOnUIThread { onError(CantProcessRecord()) }
                                        Timber.e(e)
                                    } catch (e: IllegalStateException) {
                                        AndroidUtils.runOnUIThread { onError(CantProcessRecord()) }
                                        Timber.e(e)
                                    }
                                }
                            } else {
                                onRecordFinishProcessing()
                            }
                            recordingData.clear()
                        }
                    }
                })
            }

            override fun onError(e: AppException) {
                Timber.e(e)
                onRecordingError(e)
            }
        }
        setRecorder(audioRecorder)
    }

    private fun convertRecordingData(list: List<Int>, durationSec: Int): IntArray {
        return if (durationSec > PhonographConstants.LONG_RECORD_THRESHOLD_SECONDS) {
            val sampleCount = Phonograph.getLongWaveformSampleCount()
            val waveForm = IntArray(sampleCount)
            val scale = (list.size.toFloat() / sampleCount.toFloat()).toInt()
            for (i in 0 until sampleCount) {
                var `val` = 0
                for (j in 0 until scale) {
                    `val` += list[i * scale + j]
                }
                `val` = (`val`.toFloat() / scale).toInt()
                waveForm[i] = convertAmp(`val`.toDouble())
            }
            waveForm
        } else {
            val waveForm = IntArray(recordingData.size)
            for (i in list.indices) {
                waveForm[i] = convertAmp(list[i].toDouble())
            }
            waveForm
        }
    }

    /**
     * Convert dB amp value to view amp.
     */
    private fun convertAmp(amp: Double): Int {
        return (255 * (amp / 32767f)).toInt()
    }

    override fun addRecordingCallback(callback: AppRecorderCallback) {
        appCallbacks.add(callback)
    }

    override fun removeRecordingCallback(callback: AppRecorderCallback) {
        appCallbacks.remove(callback)
    }

    override fun setRecorder(recorder: Recorder) {
        audioRecorder = recorder
        audioRecorder.setRecorderCallback(recorderCallback)
        audioRecorder.prepare(prefs.recordChannelCount, prefs.sampleRate, prefs.bitrate)
    }

    override fun startRecording(filePath: String) {
        if (!audioRecorder.isRecording) {
            audioRecorder.startRecording(filePath)
        }
    }

    override fun pauseRecording() {
        if (audioRecorder.isRecording) {
            audioRecorder.pauseRecording()
        }
    }

    override fun resumeRecording() {
        if (audioRecorder.isRecordingPaused) {
            audioRecorder.resumeRecording()
        }
    }

    override fun stopRecording() {
        if (audioRecorder.isRecording) {
            audioRecorder.stopRecording()
        }
    }

    override fun startVisualizing() {
        audioRecorder.startVisualizing()
    }

    override fun stopVisualizing() {
        audioRecorder.stopVisualizing()
    }

    override fun supportsMonitoring(): Boolean {
        return audioRecorder is RecorderContract.Monitor
    }

    override fun startMonitoring() {
        monitor()?.startMonitoring()
    }

    override fun stopMonitoring() {
        monitor()?.stopMonitoring()
    }

    override fun getRecordingData(): List<Int> {
        return recordingData
    }

    override fun isMonitoring(): Boolean = monitor()?.isMonitoring ?: false

    override fun isRecording(): Boolean {
        return audioRecorder.isRecording
    }

    override fun isPaused(): Boolean {
        return audioRecorder.isRecordingPaused
    }

    override fun release() {
        recordingData.clear()
        audioRecorder.stopRecording()
        appCallbacks.clear()
        audioRecorder.release()
    }

    override fun isProcessing(): Boolean {
        return isProcessing
    }

    private fun onRecordingStarted() {
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onRecordingStarted()
            }
        }

    }

    private fun onRecordingPaused() {
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onRecordingPaused()
            }
        }
    }

    private fun onRecordProcessing() {
        isProcessing = true
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onRecordProcessing()
            }
        }
    }

    private fun onRecordFinishProcessing() {
        isProcessing = false
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onRecordFinishProcessing()
            }
        }
    }

    private fun onRecordingStopped(id: Long, file: File) {
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onRecordingStopped(id, file)
            }
        }
    }

    private fun onRecordingProgress(mills: Long, amp: Int, isRecording: Boolean) {
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onProgress(mills, amp, isRecording)
            }
        }
    }

    private fun onRecordingError(e: AppException) {
        if (appCallbacks.isNotEmpty()) {
            for (i in appCallbacks.indices) {
                appCallbacks[i].onError(e)
            }
        }
    }

    private fun monitor() : RecorderContract.Monitor? {
        return if (supportsMonitoring()) audioRecorder as RecorderContract.Monitor else null
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRecorderImpl? = null

        fun getInstance(recorder: Recorder, tasks: BackgroundQueue,
                        processingTasks: BackgroundQueue, prefs: Prefs): AppRecorderImpl =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: AppRecorderImpl(recorder, tasks, processingTasks, prefs)
                                    .also { INSTANCE = it }
                }
    }
}
