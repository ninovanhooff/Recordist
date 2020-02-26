package com.ninovanhooff.recordist.presentation.recording

import android.content.Intent
import androidx.lifecycle.LiveData
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

    private val _recording: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }
    val amplitudeUpdates: MutableLiveData<AmplitudeUpdate> = MutableLiveData()

    init {
        appRecorderCallback = initCallback()
        appRecorder.addRecordingCallback(appRecorderCallback)
    }

    fun getRecording(): LiveData<Boolean> {
        return _recording
    }

    fun toggleRecording() {
        _recording.value = _recording.value!!.not()

        if (_recording.value!!) {
            try {
                appRecorder.startRecording(fileRepository.provideRecordFile().absolutePath)
            } catch (e: CantCreateFileException) {
               Timber.e(e)
                throw NotImplementedError("Notfiy user")
            }
        } else {
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
                _recording.postValue(false)
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

class RecordingViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val injector = RecordistApplication.injector
        return RecordingViewModel(
                injector.provideAppRecorder(),
                injector.provideFileRepository()) as T
    }

}