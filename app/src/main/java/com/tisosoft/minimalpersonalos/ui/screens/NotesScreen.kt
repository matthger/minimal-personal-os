package com.tisosoft.minimalpersonalos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tisosoft.minimalpersonalos.R
import com.tisosoft.minimalpersonalos.data.notes.Note
import com.tisosoft.minimalpersonalos.model.VisualProfile
import com.tisosoft.minimalpersonalos.ui.components.DeleteConfirmationDialog
import com.tisosoft.minimalpersonalos.ui.components.LazyVerticalScrollbar
import com.tisosoft.minimalpersonalos.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onButtonClick: () -> Unit,
    primaryColor: Color,
    visualProfile: VisualProfile,
    viewModel: NotesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_screen_title)) },
                navigationIcon = {
                    if (visualProfile == VisualProfile.MINIMALISTIC) {
                        IconButton(onClick = {
                            onButtonClick()
                            val wasEditing = uiState.isEditing
                            viewModel.onBackClick()
                            if (!wasEditing) {
                                onBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isEditing && visualProfile == VisualProfile.MINIMALISTIC) {
                        IconButton(onClick = {
                            onButtonClick()
                            viewModel.onSaveClick()
                        }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = stringResource(R.string.notes_save),
                                tint = primaryColor
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isEditing && visualProfile == VisualProfile.MINIMALISTIC) {
                FloatingActionButton(
                    onClick = {
                        onButtonClick()
                        viewModel.onAddNote()
                    },
                    containerColor = primaryColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isEditing) {
                NoteEditor(
                    note = uiState.currentNote ?: Note(content = ""),
                    onContentChange = { viewModel.onContentChange(it) },
                    onDeleteClick = { 
                        onButtonClick()
                        viewModel.showDeleteConfirmation(it) 
                    },
                    onBack = { 
                        onButtonClick()
                        viewModel.onBackClick()
                    },
                    visualProfile = visualProfile,
                    primaryColor = primaryColor
                )
            } else {
                NoteList(
                    notes = uiState.notes,
                    onNoteClick = { 
                        onButtonClick()
                        viewModel.onNoteClick(it) 
                    },
                    onNoteLongClick = {
                        if (visualProfile == VisualProfile.MINIMALISTIC) {
                            onButtonClick()
                            viewModel.showDeleteConfirmation(it)
                        }
                    },
                    onAddNote = { 
                        onButtonClick()
                        viewModel.onAddNote() 
                    },
                    onBack = {
                        onButtonClick()
                        onBack()
                    },
                    visualProfile = visualProfile,
                    primaryColor = primaryColor,
                    isLoading = uiState.isLoading
                )
            }

            uiState.noteToDelete?.let {
                DeleteConfirmationDialog(
                    onDismiss = { 
                        onButtonClick()
                        viewModel.showDeleteConfirmation(null) 
                    },
                    onConfirm = { 
                        onButtonClick()
                        viewModel.confirmDelete() 
                    },
                    primaryColor = primaryColor,
                    title = stringResource(R.string.notes_delete),
                    text = stringResource(R.string.notes_delete_confirm)
                )
            }

            if (uiState.showDiscardDialog) {
                DeleteConfirmationDialog(
                    onDismiss = { 
                        onButtonClick()
                        viewModel.dismissDiscardDialog() 
                    },
                    onConfirm = { 
                        onButtonClick()
                        viewModel.confirmDiscard() 
                    },
                    primaryColor = primaryColor,
                    title = stringResource(R.string.notes_discard_title),
                    text = stringResource(R.string.notes_discard_message)
                )
            }
        }
    }
}

@Composable
fun NoteList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onAddNote: () -> Unit,
    onBack: () -> Unit,
    visualProfile: VisualProfile,
    primaryColor: Color,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.notes_no_notes),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(32.dp)
                )
            }

            if (visualProfile == VisualProfile.SIMPLE) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Note Button
                    Button(
                        onClick = onAddNote,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.notes_add_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Back Button
                    Button(
                        onClick = onBack,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onLongClick = { onNoteLongClick(note) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            LazyVerticalScrollbar(listState = listState, totalItems = notes.size)

            if (visualProfile == VisualProfile.SIMPLE) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Note Button
                    Button(
                        onClick = onAddNote,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.notes_add_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Back Button
                    Button(
                        onClick = onBack,
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.content,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDate(note.timestamp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoteEditor(
    note: Note,
    onContentChange: (String) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onBack: () -> Unit,
    visualProfile: VisualProfile,
    primaryColor: Color
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = note.content,
                selection = TextRange(note.content.length)
            )
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onContentChange(it.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                textStyle = TextStyle(fontSize = 18.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (visualProfile == VisualProfile.SIMPLE) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button
                Button(
                    onClick = { onDeleteClick(note) },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete, contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.notes_delete),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Back Button
                Button(
                    onClick = onBack,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.back),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
