package com.ninovanhooff.recordist.presentation.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ninovanhooff.recordist.domain.Note
import com.ninovanhooff.recordist.domain.NotesManager

class NoteDetailViewModel : ViewModel() {
    private val note = MutableLiveData<Note>()

    val observableNote: LiveData<Note>
        get() = note

    fun getNote(id: Int) {
        note.value = NotesManager.getNote(id)
    }
}