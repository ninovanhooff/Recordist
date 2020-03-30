package com.ninovanhooff.recordist.presentation.deletenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ninovanhooff.recordist.R
import com.ninovanhooff.recordist.domain.Note
import com.ninovanhooff.recordist.presentation.notedetail.NoteDetailFragmentArgs
import kotlinx.android.synthetic.main.delete_note_fragment.*

class DeleteNoteFragment : Fragment() {

    private lateinit var viewModel: DeleteNoteViewModel

    private val noteId by lazy {
        arguments?.let { NoteDetailFragmentArgs.fromBundle(it).noteId } ?: throw IllegalArgumentException("Expected arguments")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.delete_note_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeleteNoteViewModel::class.java)
        viewModel.observableCurrentNote.observe(viewLifecycleOwner, Observer { currentNote ->
            currentNote?.let { initCurrentNote(currentNote) }
        })
        viewModel.observableDeleteStatus.observe(viewLifecycleOwner, Observer { deleteStatus ->
            deleteStatus?.let { render(deleteStatus) }
        })

        viewModel.initNote(noteId)

        cancelDeleteButton.setOnClickListener {
            findNavController(it).popBackStack()
        }

        confirmDeleteButton.setOnClickListener {
            viewModel.deleteNote(noteId)
        }
    }

    private fun initCurrentNote(note: Note) {
        noteIdView.text = String.format(getString(R.string.note_detail_id), note.id)
        noteText.text = String.format(getString(R.string.note_detail_text), note.text)
    }

    private fun render(deleteStatus: Boolean) {
        when (deleteStatus) {
            true -> {
                view?.let {
                    findNavController(it).popBackStack(R.id.notesFragment, false)
                }
            }
            false -> Snackbar.make(confirmDeleteButton, R.string.error_deleting_note, Snackbar.LENGTH_LONG).show()
        }
    }
}
