package com.ninovanhooff.recordist.presentation.deletenote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ninovanhooff.recordist.domain.Note
import com.ninovanhooff.recordist.domain.NotesManager

class DeleteNoteViewModel : ViewModel() {
    private val currentNote = MutableLiveData<Note>()

    private val deleteStatus = MutableLiveData<Boolean>()

    val observableCurrentNote: LiveData<Note>
        get() = currentNote

    val observableDeleteStatus: LiveData<Boolean>
        get() = deleteStatus

    fun deleteNote(id: Int) {
        deleteStatus.value = try {
            NotesManager.deleteNote(id)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun initNote(id: Int) {
        currentNote.value = NotesManager.getNote(id)
    }
}