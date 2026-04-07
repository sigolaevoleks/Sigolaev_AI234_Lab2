package com.sigolaev.lab2.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sigolaev.lab2.R
import com.sigolaev.lab2.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val expandedNoteId by viewModel.expandedNoteId.collectAsStateWithLifecycle()
    val noteCount by viewModel.noteCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var editingNoteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var titleText by rememberSaveable { mutableStateOf("") }
    var bodyText by rememberSaveable { mutableStateOf("") }
    var selectedColorIndex by rememberSaveable { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deletedMessage = stringResource(R.string.note_deleted)
    val undoLabel = stringResource(R.string.undo)
    val copiedMessage = stringResource(R.string.note_copied)

    // Drag-to-reorder state
    var draggedNoteId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()

    fun handleDelete(noteId: Int) {
        val deleted = viewModel.deleteNote(noteId) ?: return
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreNote(deleted)
            }
        }
    }

    fun handleCopy(note: com.sigolaev.lab2.model.Note) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("note", "${note.title}\n${note.text}")
        clipboard.setPrimaryClip(clip)
        scope.launch {
            snackbarHostState.showSnackbar(copiedMessage, duration = SnackbarDuration.Short)
        }
    }

    fun handleEdit(note: com.sigolaev.lab2.model.Note) {
        editingNoteId = note.id
        titleText = note.title
        bodyText = note.text
        selectedColorIndex = note.colorIndex
        showBottomSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_title)) },
                actions = {
                    Text(
                        text = pluralStringResource(R.plurals.note_count, noteCount, noteCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingNoteId = null
                titleText = ""
                bodyText = ""
                selectedColorIndex = 0
                showBottomSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note_heading))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (notes.isEmpty() && searchQuery.isNotBlank()) {
                // Empty search results
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_search_results_title),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.no_search_results_description, searchQuery),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (notes.isEmpty()) {
                // No notes at all
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.empty_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val currentNotesState = rememberUpdatedState(notes)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                        val isDragging = draggedNoteId == note.id

                        Box(
                            modifier = Modifier
                                .then(
                                    if (isDragging) {
                                        Modifier
                                            .zIndex(1f)
                                            .graphicsLayer {
                                                translationY = dragOffsetY
                                                shadowElevation = 8f
                                            }
                                    } else {
                                        Modifier.animateItem()
                                    }
                                )
                                .then(
                                    if (searchQuery.isBlank()) {
                                        Modifier.pointerInput(note.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedNoteId = note.id
                                                    dragOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y

                                                    val currentNotes = currentNotesState.value
                                                    val currentIndex = currentNotes.indexOfFirst { it.id == draggedNoteId }
                                                    if (currentIndex == -1) return@detectDragGesturesAfterLongPress

                                                    val itemHeight = listState.layoutInfo.visibleItemsInfo
                                                        .firstOrNull()?.size?.toFloat()
                                                        ?: return@detectDragGesturesAfterLongPress
                                                    val threshold = itemHeight * 0.5f

                                                    if (dragOffsetY > threshold && currentIndex < currentNotes.lastIndex) {
                                                        val current = currentNotes[currentIndex]
                                                        val next = currentNotes[currentIndex + 1]
                                                        if (current.isPinned == next.isPinned) {
                                                            viewModel.moveNote(current.id, next.id)
                                                            dragOffsetY -= itemHeight
                                                        }
                                                    } else if (dragOffsetY < -threshold && currentIndex > 0) {
                                                        val current = currentNotes[currentIndex]
                                                        val prev = currentNotes[currentIndex - 1]
                                                        if (current.isPinned == prev.isPinned) {
                                                            viewModel.moveNote(current.id, prev.id)
                                                            dragOffsetY += itemHeight
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggedNoteId = null
                                                    dragOffsetY = 0f
                                                },
                                                onDragCancel = {
                                                    draggedNoteId = null
                                                    dragOffsetY = 0f
                                                }
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        handleDelete(note.id)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                MaterialTheme.colorScheme.errorContainer,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_note),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                NoteCard(
                                    note = note,
                                    isExpanded = note.id == expandedNoteId,
                                    onToggleExpanded = { viewModel.toggleExpanded(note.id) },
                                    onTogglePin = { viewModel.togglePin(note.id) },
                                    onDelete = { handleDelete(note.id) },
                                    onEdit = { handleEdit(note) },
                                    onCopy = { handleCopy(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom sheet for creating/editing a note
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isEditing = editingNoteId != null
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                editingNoteId = null
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(if (isEditing) R.string.edit_note_heading else R.string.new_note_heading),
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_title_label)) },
                    placeholder = { Text(stringResource(R.string.note_title_placeholder)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_input_label)) },
                    placeholder = { Text(stringResource(R.string.note_input_placeholder)) },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6
                )
                // Color picker
                Column {
                    Text(
                        text = stringResource(R.string.color_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    ColorPicker(
                        selectedIndex = selectedColorIndex,
                        onColorSelected = { selectedColorIndex = it }
                    )
                }
                Button(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateNote(editingNoteId!!, titleText.trim(), bodyText.trim(), selectedColorIndex)
                        } else {
                            viewModel.addNote(titleText.trim(), bodyText.trim(), selectedColorIndex)
                        }
                        titleText = ""
                        bodyText = ""
                        selectedColorIndex = 0
                        editingNoteId = null
                        showBottomSheet = false
                    },
                    enabled = titleText.isNotBlank() && bodyText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(if (isEditing) R.string.save_note else R.string.add_note))
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val colors = listOf(
        MaterialTheme.colorScheme.surfaceContainerLow,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.errorContainer,
        if (dark) Color(0xFF2D4A2D) else Color(0xFFDCEDC8),  // Green
        if (dark) Color(0xFF1A3A5C) else Color(0xFFBBDEFB),  // Blue
        if (dark) Color(0xFF4A3520) else Color(0xFFFFE0B2)   // Orange
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (index == selectedIndex) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        }
                    )
                    .clickable { onColorSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                if (index == selectedIndex) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
