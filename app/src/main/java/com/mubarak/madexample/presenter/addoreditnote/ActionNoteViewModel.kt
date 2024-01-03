package com.mubarak.madexample.presenter.addoreditnote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubarak.madexample.R
import com.mubarak.madexample.data.Note
import com.mubarak.madexample.data.repository.NoteRepository
import com.mubarak.madexample.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActionNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    // Two-way data-binding
    val title: MutableStateFlow<String> = MutableStateFlow("")
    val description: MutableStateFlow<String> = MutableStateFlow("")

    private val _noteUpdateEvent = MutableLiveData<Event<Unit>>()
    val noteUpdateEvent: LiveData<Event<Unit>> = _noteUpdateEvent

    private val _snackBarEvent = MutableLiveData<Event<Int>>()
    val snackBarEvent: LiveData<Event<Int>> = _snackBarEvent

    private var isNewNote: Boolean = false

    private var noteId: Int = 0
    private var noteTitle: String? = null
    private var noteDescription: String? = null

    fun checkIsNewNoteOrExistingNote(noteTitle: String?, noteId: Int, noteDescription: String?) {

        this.noteId = noteId // for global reference
        this.noteTitle = noteTitle // for global reference
        this.noteDescription = noteDescription // for global reference

        if (noteId == 0 && noteTitle == null && noteDescription == null) { // that means create a new Note
            isNewNote = true
            return
        }

        isNewNote = false // this means update a existing note
        viewModelScope.launch {
            title.value = noteTitle.toString()
            description.value = noteDescription.toString()

            noteRepository.upsertNote(
                Note(
                    noteId,
                    title.value,
                    description.value
                )
            )
        }
    }

    fun saveNote() {    // called from Ui directly using (DataBinding)
        val currentTitle = title.value  // from ui
        val currentDescription = description.value

        if (isNewNote && noteId == 0 && noteTitle == null && noteDescription == null) { // that means this is for creating note (INSERT)
            viewModelScope.launch {
                _noteUpdateEvent.value =
                    Event(Unit) // this is like a flag for navigation we observe it when click the fab

                if (currentTitle.isEmpty() && currentDescription.isEmpty()) {
                    _snackBarEvent.value = Event(R.string.empty_note_message)
                    // need to notify field are empty note can't be created
                } else {
                    val note = Note(title = title.value, description = description.value)
                    createNote(note)
                }

            }
        } else { // update them. (UPDATE)
            viewModelScope.launch {
                _noteUpdateEvent.value =
                    Event(Unit)  // listen for updated note (because clear the backstack move to home)

                if (currentTitle.isEmpty() && currentDescription.isEmpty()) {
                    _snackBarEvent.value =
                        Event(R.string.empty_note_message) // need to notify field are empty note can't be created
                } else {
                    val updateNote = Note(noteId, currentTitle, currentDescription)
                    updateNote(updateNote)
                }


            }

        }
    }

    private suspend fun createNote(note: Note) { // create a new note
        noteRepository.insertNote(note)
    }

    private suspend fun updateNote(note: Note) { // update a existing note
        noteRepository.upsertNote(note)
    }


}