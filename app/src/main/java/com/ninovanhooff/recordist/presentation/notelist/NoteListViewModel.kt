package com.ninovanhooff.recordist.presentation.notelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ninovanhooff.recordist.domain.Note
import com.ninovanhooff.recordist.domain.NotesManager

class NoteListViewModel : ViewModel() {
    private val noteList = MutableLiveData<List<Note>>()

    val observableNoteList: LiveData<List<Note>>
        get() = noteList

    init {
        load()
    }

    fun load() {
        noteList.value = NotesManager.getNoteList()
    }
}