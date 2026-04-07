package com.sigolaev.lab2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigolaev.lab2.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NoteViewModel : ViewModel() {

    private var nextId = 1

    private val _notes = MutableStateFlow<List<Note>>(emptyList())

    private val _expandedNoteId = MutableStateFlow<Int?>(null)
    val expandedNoteId: StateFlow<Int?> = _expandedNoteId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /** Notes: pinned first (stable sort preserves manual order within each group). Filtered by search query. */
    val notes: StateFlow<List<Note>> = combine(_notes, _searchQuery) { list, query ->
        val filtered = if (query.isBlank()) list else list.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
                note.text.contains(query, ignoreCase = true)
        }
        filtered.sortedByDescending { it.isPinned }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val noteCount: StateFlow<Int> = _notes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun addNote(title: String, text: String, colorIndex: Int = 0) {
        val note = Note(
            id = nextId++,
            title = title,
            text = text,
            createdAt = System.currentTimeMillis(),
            colorIndex = colorIndex
        )
        _notes.value = listOf(note) + _notes.value
    }

    fun updateNote(id: Int, title: String, text: String, colorIndex: Int) {
        _notes.value = _notes.value.map { note ->
            if (note.id == id) note.copy(title = title, text = text, colorIndex = colorIndex) else note
        }
    }

    fun toggleExpanded(id: Int) {
        _expandedNoteId.value = if (_expandedNoteId.value == id) null else id
    }

    fun togglePin(id: Int) {
        _notes.value = _notes.value.map { note ->
            if (note.id == id) note.copy(isPinned = !note.isPinned) else note
        }
    }

    fun deleteNote(id: Int): Note? {
        if (_expandedNoteId.value == id) {
            _expandedNoteId.value = null
        }
        val deleted = _notes.value.find { it.id == id }
        _notes.value = _notes.value.filter { it.id != id }
        return deleted
    }

    fun restoreNote(note: Note) {
        _notes.value = _notes.value + note
    }

    fun moveNote(movedNoteId: Int, targetNoteId: Int) {
        val list = _notes.value.toMutableList()
        val movedIndex = list.indexOfFirst { it.id == movedNoteId }
        val targetIndex = list.indexOfFirst { it.id == targetNoteId }
        if (movedIndex == -1 || targetIndex == -1) return
        val note = list.removeAt(movedIndex)
        list.add(targetIndex, note)
        _notes.value = list
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
