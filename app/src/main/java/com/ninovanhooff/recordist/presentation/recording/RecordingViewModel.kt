package com.ninovanhooff.recordist.presentation.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordingViewModel : ViewModel() {
    private val _playing: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }

    fun getPlaying(): LiveData<Boolean> {
        return _playing
    }

    fun togglePlaying() {
        _playing.value = _playing.value!!.not()
    }

}