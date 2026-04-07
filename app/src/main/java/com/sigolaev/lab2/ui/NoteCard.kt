package com.sigolaev.lab2.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sigolaev.lab2.R
import com.sigolaev.lab2.model.Note
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NoteCard(
    note: Note,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = noteContainerColor(note.colorIndex, isExpanded)
    val contentColor = noteContentColor(note.colorIndex, isExpanded)
    val secondaryColor = noteSecondaryContentColor(note.colorIndex, isExpanded)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onToggleExpanded),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp)) {
            // Header: drag handle + pin icon + timestamp + overflow menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pinned_label),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = formatRelativeTime(note.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_note)) },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.copy_note)) },
                            onClick = {
                                onCopy()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (note.isPinned) R.string.unpin_note else R.string.pin_note
                                    )
                                )
                            },
                            onClick = {
                                onTogglePin()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_note)) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Title
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp)
            )

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 12.dp))
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Body
            Text(
                text = if (isExpanded) {
                    note.text
                } else {
                    note.text.take(50) + if (note.text.length > 50) "…" else ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isExpanded) contentColor else secondaryColor,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

// Custom tonal container colors for green, blue, orange
private val GreenContainerLight = Color(0xFFDCEDC8)
private val GreenContainerDark = Color(0xFF2D4A2D)
private val OnGreenContainerLight = Color(0xFF1B3A1A)
private val OnGreenContainerDark = Color(0xFFDCEDC8)

private val BlueContainerLight = Color(0xFFBBDEFB)
private val BlueContainerDark = Color(0xFF1A3A5C)
private val OnBlueContainerLight = Color(0xFF0D3B66)
private val OnBlueContainerDark = Color(0xFFBBDEFB)

private val OrangeContainerLight = Color(0xFFFFE0B2)
private val OrangeContainerDark = Color(0xFF4A3520)
private val OnOrangeContainerLight = Color(0xFF5D3A00)
private val OnOrangeContainerDark = Color(0xFFFFE0B2)

@Composable
private fun noteContainerColor(colorIndex: Int, isExpanded: Boolean): Color {
    val dark = isSystemInDarkTheme()
    return when (colorIndex) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        4 -> MaterialTheme.colorScheme.errorContainer
        5 -> if (dark) GreenContainerDark else GreenContainerLight
        6 -> if (dark) BlueContainerDark else BlueContainerLight
        7 -> if (dark) OrangeContainerDark else OrangeContainerLight
        else -> if (isExpanded) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow
    }
}

@Composable
private fun noteContentColor(colorIndex: Int, isExpanded: Boolean): Color {
    val dark = isSystemInDarkTheme()
    return when (colorIndex) {
        1 -> MaterialTheme.colorScheme.onPrimaryContainer
        2 -> MaterialTheme.colorScheme.onSecondaryContainer
        3 -> MaterialTheme.colorScheme.onTertiaryContainer
        4 -> MaterialTheme.colorScheme.onErrorContainer
        5 -> if (dark) OnGreenContainerDark else OnGreenContainerLight
        6 -> if (dark) OnBlueContainerDark else OnBlueContainerLight
        7 -> if (dark) OnOrangeContainerDark else OnOrangeContainerLight
        else -> if (isExpanded) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun noteSecondaryContentColor(colorIndex: Int, isExpanded: Boolean): Color {
    val dark = isSystemInDarkTheme()
    return when (colorIndex) {
        1 -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        2 -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        3 -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        4 -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
        5 -> (if (dark) OnGreenContainerDark else OnGreenContainerLight).copy(alpha = 0.7f)
        6 -> (if (dark) OnBlueContainerDark else OnBlueContainerLight).copy(alpha = 0.7f)
        7 -> (if (dark) OnOrangeContainerDark else OnOrangeContainerLight).copy(alpha = 0.7f)
        else -> if (isExpanded) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun formatRelativeTime(createdAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - createdAt
    val diffSeconds = diffMs / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffSeconds < 60 -> "Just now"
        diffMinutes < 60 -> "${diffMinutes}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val instant = Instant.ofEpochMilli(createdAt)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }
}
