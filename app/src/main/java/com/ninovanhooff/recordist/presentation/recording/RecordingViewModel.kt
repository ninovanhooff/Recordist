package com.ninovanhooff.recordist.presentation.recording

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dimowner.phonograph.AppRecorder
import com.dimowner.phonograph.AppRecorderCallback
import com.dimowner.phonograph.RecordingService
import com.dimowner.phonograph.data.FileRepository
import com.dimowner.phonograph.exception.AppException
import com.dimowner.phonograph.exception.CantCreateFileException
import com.ninovanhooff.recordist.RecordistApplication
import timber.log.Timber
import java.io.File

typealias AmplitudeUpdate = Pair<Long, Int>

class RecordingViewModel(private val appRecorder: AppRecorder,
                         private val fileRepository: FileRepository)
    : ViewModel() {
    private val appRecorderCallback: AppRecorderCallback

    val recordingState: MutableLiveData<RecordingState> by lazy { 
        MutableLiveData<RecordingState>(RecordingState.IDLE) 
    }
    val amplitudeUpdates: MutableLiveData<AmplitudeUpdate> = MutableLiveData()

    init {
        appRecorderCallback = initCallback()
        appRecorder.addRecordingCallback(appRecorderCallback)
    }

    fun toggleRecording() {
        when(recordingState.value){
            RecordingState.IDLE -> recordingState.value = RecordingState.STARTING
            RecordingState.RECORDING -> recordingState.value = RecordingState.STOPPING
            null -> throw IllegalStateException("recording state cannot be null")
            else -> return
        }

        if (recordingState.value == RecordingState.STARTING) {
            try {
                appRecorder.startRecording(fileRepository.provideRecordFile().absolutePath)
            } catch (e: CantCreateFileException) {
                recordingState.value = RecordingState.IDLE
               Timber.e(e)
                throw NotImplementedError("Notfiy user")
            }
        } else { // STOPPING
            appRecorder.stopRecording()
        }
    }

    private fun initCallback(): AppRecorderCallback {

        return object : AppRecorderCallback {
            override fun onRecordingStarted() {
                val appContext = RecordistApplication.injector.provideApplicationContext()
                val intent = Intent(appContext, RecordingService::class.java)
                intent.action = RecordingService.ACTION_START_RECORDING_SERVICE
                appContext.startService(intent)
                recordingState.postValue(RecordingState.RECORDING)
            }

            override fun onRecordingPaused() {
                throw NotImplementedError()
            }

            override fun onRecordProcessing() {
                Timber.d("onRecordProcessing")
            }

            override fun onRecordFinishProcessing() {
                Timber.d("onRecordFinishedProcessing")
            }

            override fun onRecordingStopped(id: Long, file: File) {
                recordingState.postValue(RecordingState.IDLE)
            }

            override fun onRecordingProgress(mills: Long, amp: Int) {
                amplitudeUpdates.postValue(AmplitudeUpdate(mills, amp))
            }

            override fun onError(throwable: AppException) {
                Timber.e(throwable)
            }
        }
    }
}

enum class RecordingState {
    IDLE, STARTING, RECORDING, STOPPING;
}

class RecordingViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val injector = RecordistApplication.injector
        return RecordingViewModel(
                injector.provideAppRecorder(),
                injector.provideFileRepository()) as T
    }

}