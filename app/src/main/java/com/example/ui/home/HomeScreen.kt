package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FolderEntity
import com.example.data.NoteEntity
import com.example.data.NoteSortOption
import com.example.editor.PlainTextUtilities
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.MoveToFolderSheet
import com.example.ui.components.TextInputDialog
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.SurfaceTreatment
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumPillButton
import com.example.ui.theme.VellumSurface

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
  uiState: HomeUiState,
  activeNoteId: Long?,
  onOpenNote: (Long) -> Unit,
  onCreateNote: () -> Unit,
  onOpenThemeGallery: () -> Unit,
  onOpenSettings: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSelectFolder: (Long?) -> Unit,
  onCreateFolder: (String) -> Unit,
  onRenameFolder: (Long, String) -> Unit,
  onDeleteFolder: (Long) -> Unit,
  onTogglePin: (NoteEntity) -> Unit,
  onRenameNote: (Long, String) -> Unit,
  onDuplicateNote: (Long) -> Unit,
  onMoveNoteToFolder: (Long, Long?) -> Unit,
  onDeleteNote: (Long) -> Unit,
  onShareNote: (NoteEntity) -> Unit,
  onExportNote: (NoteEntity) -> Unit,
  onImportTxt: () -> Unit,
  onSetSortOption: (NoteSortOption) -> Unit,
  onToggleGridView: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalVellumTheme.current
  var showSortMenu by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var folderToRename by remember { mutableStateOf<FolderEntity?>(null) }
  var folderMenuTarget by remember { mutableStateOf<FolderEntity?>(null) }
  var noteToRename by remember { mutableStateOf<NoteEntity?>(null) }
  var noteToMove by remember { mutableStateOf<NoteEntity?>(null) }
  var notePendingDelete by remember { mutableStateOf<NoteEntity?>(null) }

  val horizontalPad = (16 * theme.density.horizontalScale).dp
  val itemGap = (10 * theme.density.verticalScale).dp

  VellumBackdrop(theme = theme, modifier = modifier) {
    Scaffold(
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      floatingActionButton = {
        FloatingActionButton(
          onClick = onCreateNote,
          modifier =
            Modifier.navigationBarsPadding().padding(bottom = 8.dp).testTag("new_note_fab"),
          shape =
            RoundedCornerShape(
              if (theme.treatment == SurfaceTreatment.BRUTALIST ||
                theme.treatment == SurfaceTreatment.RETRO_TERMINAL
              ) {
                theme.cornerRadius
              } else {
                18.dp
              }
            ),
          containerColor = theme.accent,
          contentColor = theme.onAccent,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.new_note),
            modifier = Modifier.size(26.dp),
          )
        }
      },
    ) { innerPadding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding()
      ) {
        // Header bar
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = horizontalPad, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            val brandTitle =
              when (theme.treatment) {
                SurfaceTreatment.RETRO_TERMINAL -> "[ VELLUM ]"
                SurfaceTreatment.BRUTALIST -> "VELLUM"
                else -> stringResource(R.string.app_name)
              }
            Text(
              text = brandTitle,
              fontFamily = theme.displayFont,
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold,
              color = theme.textPrimary,
              maxLines = 1,
            )
            Text(
              text =
                theme.activePresetName?.let { "$it · ${uiState.allNotesCount} notes" }
                  ?: "${theme.spec.name} · ${uiState.allNotesCount} notes",
              fontFamily = theme.bodyFont,
              fontSize = 12.sp,
              color = theme.textSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          IconButton(
            onClick = onImportTxt,
            modifier = Modifier.testTag("import_txt_button"),
          ) {
            Icon(
              Icons.Outlined.FileOpen,
              contentDescription = stringResource(R.string.import_txt),
              tint = theme.textPrimary,
            )
          }

          IconButton(
            onClick = onToggleGridView,
            modifier = Modifier.testTag("toggle_view_button"),
          ) {
            Icon(
              if (uiState.isGridView) Icons.AutoMirrored.Outlined.ViewList
              else Icons.Outlined.GridView,
              contentDescription = stringResource(R.string.toggle_grid_view),
              tint = theme.textPrimary,
            )
          }

          Box {
            IconButton(
              onClick = { showSortMenu = true },
              modifier = Modifier.testTag("sort_button"),
            ) {
              Icon(
                Icons.AutoMirrored.Outlined.Sort,
                contentDescription = stringResource(R.string.sort_by),
                tint = theme.textPrimary,
              )
            }
            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false },
              containerColor = theme.surfaceElevated,
            ) {
              NoteSortOption.entries.forEach { option ->
                DropdownMenuItem(
                  text = {
                    Text(
                      option.label,
                      color = if (uiState.sortOption == option) theme.accent else theme.textPrimary,
                      fontWeight =
                        if (uiState.sortOption == option) FontWeight.SemiBold else FontWeight.Normal,
                    )
                  },
                  onClick = {
                    onSetSortOption(option)
                    showSortMenu = false
                  },
                )
              }
            }
          }

          IconButton(
            onClick = onOpenThemeGallery,
            modifier = Modifier.testTag("theme_gallery_button"),
          ) {
            Icon(
              Icons.Outlined.Palette,
              contentDescription = stringResource(R.string.theme_gallery),
              tint = theme.accent,
            )
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button"),
          ) {
            Icon(
              Icons.Outlined.Settings,
              contentDescription = stringResource(R.string.settings),
              tint = theme.textPrimary,
            )
          }
        }

        // Search input
        VellumSurface(
          theme = theme,
          elevated = false,
          modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPad),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              Icons.Outlined.Search,
              contentDescription = null,
              tint = theme.textSecondary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
              if (uiState.searchQuery.isEmpty()) {
                Text(
                  text = stringResource(R.string.search_notes_hint),
                  color = theme.textSecondary,
                  fontFamily = theme.bodyFont,
                  fontSize = 15.sp,
                )
              }
              BasicTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle =
                  TextStyle(
                    color = theme.textPrimary,
                    fontFamily = theme.bodyFont,
                    fontSize = 15.sp,
                  ),
                cursorBrush = SolidColor(theme.accent),
                modifier = Modifier.fillMaxWidth().testTag("home_search_input"),
              )
            }
            AnimatedVisibility(
              visible = uiState.searchQuery.isNotEmpty(),
              enter = fadeIn(),
              exit = fadeOut(),
            ) {
              IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.size(28.dp),
              ) {
                Icon(
                  Icons.Outlined.Close,
                  contentDescription = "Clear search",
                  tint = theme.textSecondary,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }

        Spacer(Modifier.height(10.dp))

        // Lightweight folders strip
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = horizontalPad),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          VellumPillButton(
            theme = theme,
            selected = uiState.selectedFolderId == null,
            onClick = { onSelectFolder(null) },
            modifier = Modifier.testTag("folder_chip_all"),
          ) {
            Text(stringResource(R.string.all_notes), fontSize = 13.sp, fontWeight = FontWeight.Medium)
          }

          uiState.folders.forEach { folder ->
            Box {
              VellumPillButton(
                theme = theme,
                selected = uiState.selectedFolderId == folder.id,
                onClick = { onSelectFolder(folder.id) },
                modifier =
                  Modifier.combinedClickable(
                    onClick = { onSelectFolder(folder.id) },
                    onLongClick = { folderMenuTarget = folder },
                  ),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                  )
                  Spacer(Modifier.width(5.dp))
                  Text(folder.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
              }

              DropdownMenu(
                expanded = folderMenuTarget?.id == folder.id,
                onDismissRequest = { folderMenuTarget = null },
                containerColor = theme.surfaceElevated,
              ) {
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.rename_folder), color = theme.textPrimary) },
                  onClick = {
                    folderToRename = folder
                    folderMenuTarget = null
                  },
                )
                DropdownMenuItem(
                  text = { Text(stringResource(R.string.delete_folder), color = theme.dangerColor) },
                  onClick = {
                    onDeleteFolder(folder.id)
                    folderMenuTarget = null
                  },
                )
              }
            }
          }

          VellumPillButton(
            theme = theme,
            selected = false,
            onClick = { showCreateFolderDialog = true },
            modifier = Modifier.testTag("add_folder_chip"),
          ) {
            Text("+ ${stringResource(R.string.folders)}", fontSize = 13.sp)
          }
        }

        Spacer(Modifier.height(10.dp))

        // Notes List / Grid or Empty State
        if (uiState.isEmpty) {
          EmptyNotesView(
            isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedFolderId != null,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
          )
        } else if (uiState.isGridView) {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding =
              PaddingValues(
                start = horizontalPad,
                end = horizontalPad,
                top = 4.dp,
                bottom = 96.dp,
              ),
            horizontalArrangement = Arrangement.spacedBy(itemGap),
            verticalArrangement = Arrangement.spacedBy(itemGap),
          ) {
            if (uiState.pinnedNotes.isNotEmpty()) {
              item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(stringResource(R.string.pinned))
              }
              items(uiState.pinnedNotes, key = { "pin_${it.id}" }) { note ->
                NoteItemCard(
                  note = note,
                  isSelected = note.id == activeNoteId,
                  isGrid = true,
                  onClick = { onOpenNote(note.id) },
                  onTogglePin = { onTogglePin(note) },
                  onRename = { noteToRename = note },
                  onDuplicate = { onDuplicateNote(note.id) },
                  onMoveToFolder = { noteToMove = note },
                  onShare = { onShareNote(note) },
                  onExport = { onExportNote(note) },
                  onDelete = {
                    if (uiState.confirmBeforeDelete) notePendingDelete = note
                    else onDeleteNote(note.id)
                  },
                )
              }
            }
            if (uiState.regularNotes.isNotEmpty()) {
              if (uiState.pinnedNotes.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                  SectionHeader(stringResource(R.string.recent))
                }
              }
              items(uiState.regularNotes, key = { "note_${it.id}" }) { note ->
                NoteItemCard(
                  note = note,
                  isSelected = note.id == activeNoteId,
                  isGrid = true,
                  onClick = { onOpenNote(note.id) },
                  onTogglePin = { onTogglePin(note) },
                  onRename = { noteToRename = note },
                  onDuplicate = { onDuplicateNote(note.id) },
                  onMoveToFolder = { noteToMove = note },
                  onShare = { onShareNote(note) },
                  onExport = { onExportNote(note) },
                  onDelete = {
                    if (uiState.confirmBeforeDelete) notePendingDelete = note
                    else onDeleteNote(note.id)
                  },
                )
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("notes_list"),
            contentPadding =
              PaddingValues(
                start = horizontalPad,
                end = horizontalPad,
                top = 4.dp,
                bottom = 96.dp,
              ),
            verticalArrangement = Arrangement.spacedBy(itemGap),
          ) {
            if (uiState.pinnedNotes.isNotEmpty()) {
              item(key = "header_pinned") { SectionHeader(stringResource(R.string.pinned)) }
              items(uiState.pinnedNotes, key = { "pin_${it.id}" }) { note ->
                NoteItemCard(
                  note = note,
                  isSelected = note.id == activeNoteId,
                  isGrid = false,
                  onClick = { onOpenNote(note.id) },
                  onTogglePin = { onTogglePin(note) },
                  onRename = { noteToRename = note },
                  onDuplicate = { onDuplicateNote(note.id) },
                  onMoveToFolder = { noteToMove = note },
                  onShare = { onShareNote(note) },
                  onExport = { onExportNote(note) },
                  onDelete = {
                    if (uiState.confirmBeforeDelete) notePendingDelete = note
                    else onDeleteNote(note.id)
                  },
                )
              }
            }
            if (uiState.regularNotes.isNotEmpty()) {
              if (uiState.pinnedNotes.isNotEmpty()) {
                item(key = "header_recent") { SectionHeader(stringResource(R.string.recent)) }
              }
              items(uiState.regularNotes, key = { "note_${it.id}" }) { note ->
                NoteItemCard(
                  note = note,
                  isSelected = note.id == activeNoteId,
                  isGrid = false,
                  onClick = { onOpenNote(note.id) },
                  onTogglePin = { onTogglePin(note) },
                  onRename = { noteToRename = note },
                  onDuplicate = { onDuplicateNote(note.id) },
                  onMoveToFolder = { noteToMove = note },
                  onShare = { onShareNote(note) },
                  onExport = { onExportNote(note) },
                  onDelete = {
                    if (uiState.confirmBeforeDelete) notePendingDelete = note
                    else onDeleteNote(note.id)
                  },
                )
              }
            }
          }
        }
      }
    }
  }

  if (showCreateFolderDialog) {
    TextInputDialog(
      title = stringResource(R.string.new_folder),
      label = stringResource(R.string.folder_name_hint),
      onConfirm = { name ->
        onCreateFolder(name)
        showCreateFolderDialog = false
      },
      onDismiss = { showCreateFolderDialog = false },
    )
  }

  folderToRename?.let { folder ->
    TextInputDialog(
      title = stringResource(R.string.rename_folder),
      label = stringResource(R.string.folder_name_hint),
      initialValue = folder.name,
      onConfirm = { name ->
        onRenameFolder(folder.id, name)
        folderToRename = null
      },
      onDismiss = { folderToRename = null },
    )
  }

  noteToRename?.let { note ->
    TextInputDialog(
      title = stringResource(R.string.rename_note),
      label = stringResource(R.string.note_title_hint),
      initialValue = note.title,
      onConfirm = { newTitle ->
        onRenameNote(note.id, newTitle)
        noteToRename = null
      },
      onDismiss = { noteToRename = null },
    )
  }

  noteToMove?.let { note ->
    MoveToFolderSheet(
      folders = uiState.folders,
      currentFolderId = note.folderId,
      onSelectFolder = { folderId -> onMoveNoteToFolder(note.id, folderId) },
      onCreateFolderRequested = {
        noteToMove = null
        showCreateFolderDialog = true
      },
      onDismiss = { noteToMove = null },
    )
  }

  notePendingDelete?.let { note ->
    ConfirmDeleteDialog(
      onConfirm = {
        onDeleteNote(note.id)
        notePendingDelete = null
      },
      onDismiss = { notePendingDelete = null },
    )
  }
}

@Composable
private fun SectionHeader(title: String) {
  val theme = LocalVellumTheme.current
  Text(
    text =
      if (theme.treatment == SurfaceTreatment.BRUTALIST ||
        theme.treatment == SurfaceTreatment.SWISS
      ) {
        title.uppercase()
      } else {
        title
      },
    fontFamily = theme.displayFont,
    fontSize = 12.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.8.sp,
    color = theme.textSecondary,
    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteItemCard(
  note: NoteEntity,
  isSelected: Boolean,
  isGrid: Boolean,
  onClick: () -> Unit,
  onTogglePin: () -> Unit,
  onRename: () -> Unit,
  onDuplicate: () -> Unit,
  onMoveToFolder: () -> Unit,
  onShare: () -> Unit,
  onExport: () -> Unit,
  onDelete: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  var showMenu by remember { mutableStateOf(false) }

  val cardPad = (14 * theme.density.horizontalScale).dp
  val displayTitle = note.title.ifBlank { stringResource(R.string.untitled_note) }
  val preview = PlainTextUtilities.firstLinePreview(note.content, maxChars = if (isGrid) 110 else 160)

  VellumSurface(
    theme = theme,
    elevated = isSelected,
    modifier =
      Modifier.fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
        .testTag("note_card_${note.id}"),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(cardPad)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (note.isPinned) {
          Icon(
            imageVector = Icons.Filled.PushPin,
            contentDescription = stringResource(R.string.pinned),
            tint = theme.pinColor,
            modifier = Modifier.size(15.dp).padding(end = 2.dp),
          )
          Spacer(Modifier.width(5.dp))
        }
        Text(
          text = displayTitle,
          fontFamily = theme.displayFont,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (note.title.isBlank()) theme.textSecondary else theme.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(32.dp).testTag("note_menu_${note.id}"),
          ) {
            Icon(
              Icons.Outlined.MoreVert,
              contentDescription = stringResource(R.string.more_options),
              tint = theme.textSecondary,
              modifier = Modifier.size(18.dp),
            )
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = theme.surfaceElevated,
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  if (note.isPinned) stringResource(R.string.unpin_note)
                  else stringResource(R.string.pin_note),
                  color = theme.textPrimary,
                )
              },
              leadingIcon = {
                Icon(Icons.Outlined.PushPin, contentDescription = null, tint = theme.pinColor)
              },
              onClick = {
                onTogglePin()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.rename_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(
                  Icons.Outlined.DriveFileRenameOutline,
                  contentDescription = null,
                  tint = theme.textPrimary,
                )
              },
              onClick = {
                onRename()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.duplicate_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onDuplicate()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.move_to_folder), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Folder, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onMoveToFolder()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.share_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onShare()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.export_txt), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.SaveAlt, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onExport()
                showMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.delete_note), color = theme.dangerColor) },
              leadingIcon = {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = theme.dangerColor)
              },
              onClick = {
                onDelete()
                showMenu = false
              },
            )
          }
        }
      }

      if (preview.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(
          text = preview,
          fontFamily = theme.bodyFont,
          fontSize = 13.5.sp,
          lineHeight = 19.sp,
          color = theme.textSecondary,
          maxLines = if (isGrid) 4 else 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.height(8.dp))
      Text(
        text = TimeFormatter.formatRelative(note.updatedAt),
        fontFamily = theme.bodyFont,
        fontSize = 11.5.sp,
        color = theme.textSecondary.copy(alpha = 0.78f),
      )
    }
  }
}

@Composable
private fun EmptyNotesView(isFiltered: Boolean, modifier: Modifier = Modifier) {
  val theme = LocalVellumTheme.current
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    VellumSurface(theme = theme, elevated = true, cornerRadiusOverride = 20.dp) {
      Icon(
        imageVector = Icons.Outlined.Description,
        contentDescription = null,
        tint = theme.accent,
        modifier = Modifier.padding(20.dp).size(36.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    Text(
      text =
        stringResource(
          if (isFiltered) R.string.empty_search_title else R.string.empty_notes_title
        ),
      fontFamily = theme.displayFont,
      fontSize = 18.sp,
      fontWeight = FontWeight.SemiBold,
      color = theme.textPrimary,
    )
    Spacer(Modifier.height(6.dp))
    Text(
      text =
        stringResource(
          if (isFiltered) R.string.empty_search_subtitle else R.string.empty_notes_subtitle
        ),
      fontFamily = theme.bodyFont,
      fontSize = 14.sp,
      color = theme.textSecondary,
    )
  }
}
