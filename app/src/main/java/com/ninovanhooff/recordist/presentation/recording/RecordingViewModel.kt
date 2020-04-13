package com.ninovanhooff.recordist.presentation.recording

import android.content.Intent
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.distinctUntilChanged
import androidx.lifecycle.Transformations.map
import com.ninovanhooff.phonograph.AppRecorder
import com.ninovanhooff.phonograph.AppRecorderCallback
import com.ninovanhooff.phonograph.RecordingService
import com.ninovanhooff.phonograph.data.FileRepository
import com.ninovanhooff.phonograph.exception.AppException
import com.ninovanhooff.phonograph.exception.CantCreateFileException
import com.ninovanhooff.phonograph.util.TimeUtils
import com.ninovanhooff.recordist.RecordistApplication
import com.ninovanhooff.recordist.presentation.BaseViewModel
import com.ninovanhooff.recordist.presentation.recording.RecordingFragmentDirections.actionRecordingFragmentToPermissionsFragment
import timber.log.Timber
import java.io.File

class RecordingViewModel(private val appRecorder: AppRecorder,
                         private val fileRepository: FileRepository)
    : BaseViewModel(), LifecycleObserver {
    private val appRecorderCallback: AppRecorderCallback

    val recordingState: MutableLiveData<RecordingState> by lazy {
        MutableLiveData(
            if (appRecorder.isRecording) RecordingState.RECORDING else RecordingState.IDLE
        )
    }
    val amplitudeUpdates: MutableLiveData<AmplitudeUpdate> = MutableLiveData()

    val progressTextUpdates = distinctUntilChanged(map(amplitudeUpdates) {
        TimeUtils.formatTimeIntervalHourMinSec2(it.millis)
    })

    init {
        appRecorderCallback = initCallback()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(){
        // Allow background recording, otherwise release the hardware
        if (!appRecorder.isRecording){
            appRecorder.release() // also removes recording callbacks
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        appRecorder.addRecordingCallback(appRecorderCallback)
        appRecorder.startVisualizing()
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

    fun toggleMonitoring() {
        if (appRecorder.isMonitoring){
            appRecorder.stopMonitoring()
        } else {
            appRecorder.startMonitoring()
        }
    }

    fun getRecordingData(): List<Int> = appRecorder.recordingData

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
                Timber.d("onRecordingPaused")
            }

            override fun onRecordProcessing() {
                Timber.d("onRecordProcessing")
            }

            override fun onRecordFinishProcessing() {
                Timber.d("onRecordFinishedProcessing")
            }

            override fun onRecordingStopped(id: Long, file: File) {
                val appContext = RecordistApplication.injector.provideApplicationContext()
                val intent = Intent(appContext, RecordingService::class.java)
                intent.action = RecordingService.ACTION_STOP_RECORDING_SERVICE
                appContext.startService(intent)
                recordingState.postValue(RecordingState.IDLE)
            }

            override fun onProgress(mills: Long, amp: Int, isRecording: Boolean) {
                amplitudeUpdates.postValue(AmplitudeUpdate(mills, amp, isRecording))
            }

            override fun onError(throwable: AppException) {
                Timber.e(throwable)
            }
        }
    }

    fun onRequiredPermissionsMissing() {
        navigate(actionRecordingFragmentToPermissionsFragment())
    }
}


data class AmplitudeUpdate(val millis: Long, val amplitude: Int, val isRecording: Boolean)

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