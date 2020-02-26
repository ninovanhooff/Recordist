package com.ninovanhooff.recordist

import android.content.Context
import com.dimowner.phonograph.AppRecorder
import com.dimowner.phonograph.BackgroundQueue
import com.dimowner.phonograph.Phonograph
import com.dimowner.phonograph.PhonographConstants
import com.dimowner.phonograph.audio.recorder.AudioRecorder
import com.dimowner.phonograph.audio.recorder.RecorderContract.Recorder
import com.dimowner.phonograph.audio.recorder.WavRecorder
import com.dimowner.phonograph.data.FileRepository
import com.ninovanhooff.recordist.data.Prefs
import com.ninovanhooff.recordist.data.PrefsImpl

class Injector constructor(private val context: Context) {

    private val loadingTasks: BackgroundQueue by lazy {
        BackgroundQueue("LoadingTasks")
    }
    private val recordingTasks: BackgroundQueue by lazy {
        BackgroundQueue("RecordingTasks")
    }
    private val processingTasks: BackgroundQueue by lazy {
        BackgroundQueue("ProcessingTasks")
    }

    fun provideApplicationContext(): Context = context

    fun providePrefs(): Prefs {
        return PrefsImpl.getInstance(context) as Prefs
    }

    fun provideLoadingTasksQueue(): BackgroundQueue = loadingTasks

    fun provideRecordingTasksQueue(): BackgroundQueue = recordingTasks

    fun provideProcessingTasksQueue(): BackgroundQueue = processingTasks

    fun provideAudioRecorder(): Recorder {
        return if (providePrefs().getFormat() == PhonographConstants.RECORDING_FORMAT_WAV) {
            WavRecorder.getInstance()
        } else {
            AudioRecorder.getInstance()
        }
    }

    fun provideAppRecorder(): AppRecorder {
        return AppRecorderImpl.getInstance(provideAudioRecorder(),
                provideLoadingTasksQueue(), provideProcessingTasksQueue(), providePrefs())
    }

    fun provideFileRepository(): FileRepository {
        return Phonograph.getFileRepository()
    }

    fun closeTasks() {
        loadingTasks.cleanupQueue()
        loadingTasks.close()
        processingTasks.cleanupQueue()
        processingTasks.close()
        recordingTasks.cleanupQueue()
        recordingTasks.close()
    }
}
