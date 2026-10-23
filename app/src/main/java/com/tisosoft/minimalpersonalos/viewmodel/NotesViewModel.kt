package com.tisosoft.minimalpersonalos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tisosoft.minimalpersonalos.data.notes.Note
import com.tisosoft.minimalpersonalos.data.notes.NotesRepository
import com.tisosoft.minimalpersonalos.model.VisualProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotesState(
    val notes: List<Note> = emptyList(),
    val currentNote: Note? = null,
    val originalNote: Note? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val noteToDelete: Note? = null,
    val showDiscardDialog: Boolean = false
)

class NotesViewModel(
    private val repository: NotesRepository,
    private val visualProfile: VisualProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesState())
    val uiState: StateFlow<NotesState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                _uiState.value = _uiState.value.copy(
                    notes = notes.sortedByDescending { it.timestamp },
                    isLoading = false
                )
            }
        }
    }

    fun onAddNote() {
        val newNote = Note(content = "")
        _uiState.value = _uiState.value.copy(
            currentNote = newNote,
            originalNote = newNote,
            isEditing = true
        )
    }

    fun onNoteClick(note: Note) {
        _uiState.value = _uiState.value.copy(
            currentNote = note,
            originalNote = note,
            isEditing = true
        )
    }

    private var saveJob: Job? = null

    fun onContentChange(newContent: String) {
        val current = _uiState.value.currentNote ?: return
        val updatedNote = current.copy(content = newContent, timestamp = System.currentTimeMillis())
        _uiState.value = _uiState.value.copy(currentNote = updatedNote)
        
        // Auto-save for SIMPLE profile
        if (visualProfile == VisualProfile.SIMPLE) {
            saveNote(updatedNote)
        }
    }

    fun onSaveClick() {
        val current = _uiState.value.currentNote ?: return
        saveNote(current)
        _uiState.value = _uiState.value.copy(isEditing = false, currentNote = null)
    }

    private fun saveNote(note: Note) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (note.content.isBlank() && note.id != 0L) {
                repository.deleteNote(note)
                _uiState.value = _uiState.value.copy(currentNote = null, isEditing = false)
            } else if (note.content.isNotBlank()) {
                if (note.id == 0L) {
                    val newId = repository.insertNote(note)
                    // Update current note with the new ID
                    val current = _uiState.value.currentNote
                    if (current != null && current.id == 0L) {
                        _uiState.value = _uiState.value.copy(currentNote = current.copy(id = newId))
                    }
                } else {
                    repository.updateNote(note)
                }
            }
        }
    }

    fun showDeleteConfirmation(note: Note?) {
        _uiState.value = _uiState.value.copy(noteToDelete = note)
    }

    fun confirmDelete() {
        val note = _uiState.value.noteToDelete ?: return
        onDeleteNote(note)
        showDeleteConfirmation(null)
    }

    fun onDeleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            if (_uiState.value.currentNote?.id == note.id) {
                _uiState.value = _uiState.value.copy(isEditing = false, currentNote = null)
            }
        }
    }

    fun onBackClick() {
        val state = _uiState.value
        if (visualProfile == VisualProfile.MINIMALISTIC && state.isEditing) {
            val isModified = state.currentNote?.content != state.originalNote?.content
            if (isModified) {
                _uiState.value = state.copy(showDiscardDialog = true)
                return
            }
        }
        _uiState.value = state.copy(isEditing = false, currentNote = null, originalNote = null, showDiscardDialog = false)
    }

    fun confirmDiscard() {
        _uiState.value = _uiState.value.copy(isEditing = false, currentNote = null, originalNote = null, showDiscardDialog = false)
    }

    fun dismissDiscardDialog() {
        _uiState.value = _uiState.value.copy(showDiscardDialog = false)
    }

    class Factory(
        private val repository: NotesRepository,
        private val visualProfile: VisualProfile
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(repository, visualProfile) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
