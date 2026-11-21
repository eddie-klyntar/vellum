package com.example.ui.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.EditorSettings
import com.example.data.FolderEntity
import com.example.editor.EditorHighlightTransformation
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DetectedLinksSheet
import com.example.ui.components.EditorSettingsSheet
import com.example.ui.components.MoveToFolderSheet
import com.example.ui.components.TextInputDialog
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumFonts
import com.example.ui.theme.VellumSurface

@Composable
fun WritingScreen(
  uiState: EditorUiState,
  editorSettings: EditorSettings,
  folders: List<FolderEntity>,
  showBackButton: Boolean,
  onBack: () -> Unit,
  onTitleChange: (String) -> Unit,
  onContentChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onSelectAll: () -> Unit,
  onPasteAtCursor: (String) -> Unit,
  onToggleFindBar: () -> Unit,
  onUpdateFindQuery: (String) -> Unit,
  onToggleCaseSensitive: () -> Unit,
  onNextMatch: () -> Unit,
  onPreviousMatch: () -> Unit,
  onTogglePin: () -> Unit,
  onMoveToFolder: (Long?) -> Unit,
  onCreateFolder: (String) -> Unit,
  onDuplicateNote: () -> Unit,
  onDeleteNote: () -> Unit,
  onShareNote: () -> Unit,
  onExportNote: () -> Unit,
  onFlushSave: () -> Unit,
  onUpdateEditorSettings: ((EditorSettings) -> EditorSettings) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalVellumTheme.current
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var showMoreMenu by remember { mutableStateOf(false) }
  var showEditorSettings by remember { mutableStateOf(false) }
  var showMoveFolderSheet by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showConfirmDelete by remember { mutableStateOf(false) }
  var showLinksSheet by remember { mutableStateOf(false) }

  // Flush unsaved edits immediately when app backgrounds or screen leaves composition
  DisposableEffect(lifecycleOwner, uiState.noteId) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
        onFlushSave()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      onFlushSave()
    }
  }

  val editorFontFamily =
    remember(editorSettings.fontPreset, theme.bodyFont) {
      VellumFonts.resolve(editorSettings.fontPreset, fallback = theme.bodyFont)
    }

  val highlightTransformation =
    remember(
      uiState.findState.matches,
      uiState.findState.activeIndex,
      uiState.detectedUrls,
      editorSettings.detectUrls,
      theme.accent,
      theme.onAccent,
    ) {
      EditorHighlightTransformation(
        findMatches = uiState.findState.matches,
        activeMatchIndex = uiState.findState.activeIndex,
        detectedUrls = uiState.detectedUrls,
        detectUrls = editorSettings.detectUrls,
        matchBackground = theme.accent.copy(alpha = 0.25f),
        activeMatchBackground = theme.accent,
        activeMatchForeground = theme.onAccent,
        urlAccentColor = theme.accent,
      )
    }

  VellumBackdrop(theme = theme, modifier = modifier) {
    Column(
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
      // Subtle top navigation bar
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (showBackButton) {
          IconButton(
            onClick = {
              onFlushSave()
              onBack()
            },
            modifier = Modifier.testTag("editor_back_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back),
              tint = theme.textPrimary,
            )
          }
        } else {
          Spacer(Modifier.width(12.dp))
        }

        // Editable note title in top bar
        Box(
          modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (uiState.title.isEmpty()) {
            Text(
              text = stringResource(R.string.note_title_hint),
              fontFamily = theme.displayFont,
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = theme.textSecondary.copy(alpha = 0.65f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          if (uiState.isLoaded) {
            BasicTextField(
              value = uiState.title,
              onValueChange = onTitleChange,
              singleLine = true,
              textStyle =
                TextStyle(
                  fontFamily = theme.displayFont,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = theme.textPrimary,
                ),
              cursorBrush = SolidColor(theme.accent),
              modifier = Modifier.fillMaxWidth().testTag("editor_title_input"),
            )
          }
        }

        // Save status badge
        AnimatedContent(
          targetState = uiState.saveStatus,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "saveStatusAnim",
        ) { status ->
          Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (status == SaveStatus.SAVED) {
              Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = theme.textSecondary,
                modifier = Modifier.size(13.dp),
              )
              Spacer(Modifier.width(3.dp))
            }
            Text(
              text =
                stringResource(
                  if (status == SaveStatus.SAVED) R.string.status_saved
                  else R.string.status_saving
                ),
              fontFamily = theme.bodyFont,
              fontSize = 11.5.sp,
              color = if (status == SaveStatus.SAVING) theme.accent else theme.textSecondary,
              modifier = Modifier.testTag("save_status_text"),
            )
          }
        }

        IconButton(
          onClick = onUndo,
          enabled = uiState.canUndo,
          modifier = Modifier.testTag("undo_button"),
        ) {
          Icon(
            Icons.AutoMirrored.Outlined.Undo,
            contentDescription = stringResource(R.string.undo),
            tint =
              if (uiState.canUndo) theme.textPrimary else theme.textSecondary.copy(alpha = 0.35f),
          )
        }

        IconButton(
          onClick = onRedo,
          enabled = uiState.canRedo,
          modifier = Modifier.testTag("redo_button"),
        ) {
          Icon(
            Icons.AutoMirrored.Outlined.Redo,
            contentDescription = stringResource(R.string.redo),
            tint =
              if (uiState.canRedo) theme.textPrimary else theme.textSecondary.copy(alpha = 0.35f),
          )
        }

        Box {
          IconButton(
            onClick = { showMoreMenu = true },
            modifier = Modifier.testTag("editor_more_button"),
          ) {
            Icon(
              Icons.Outlined.MoreVert,
              contentDescription = stringResource(R.string.more_options),
              tint = theme.textPrimary,
            )
          }
          DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false },
            containerColor = theme.surfaceElevated,
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.find_in_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onToggleFindBar()
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.copy_all), color = theme.textPrimary) },
              leadingIcon = {
                Icon(
                  Icons.Outlined.ContentCopy,
                  contentDescription = null,
                  tint = theme.textPrimary,
                )
              },
              onClick = {
                clipboardManager.setText(AnnotatedString(uiState.textFieldValue.text))
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.paste_at_cursor), color = theme.textPrimary) },
              leadingIcon = {
                Icon(
                  Icons.Outlined.ContentPaste,
                  contentDescription = null,
                  tint = theme.textPrimary,
                )
              },
              onClick = {
                clipboardManager.getText()?.text?.let { onPasteAtCursor(it) }
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.select_all), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.SelectAll, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onSelectAll()
                showMoreMenu = false
              },
            )
            HorizontalDivider(color = theme.border.copy(alpha = 0.5f))
            DropdownMenuItem(
              text = {
                Text(
                  if (uiState.isPinned) stringResource(R.string.unpin_note)
                  else stringResource(R.string.pin_note),
                  color = theme.textPrimary,
                )
              },
              leadingIcon = {
                Icon(Icons.Outlined.PushPin, contentDescription = null, tint = theme.pinColor)
              },
              onClick = {
                onTogglePin()
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.move_to_folder), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Folder, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                showMoveFolderSheet = true
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.editor_settings), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Tune, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                showEditorSettings = true
                showMoreMenu = false
              },
            )
            if (editorSettings.detectUrls && uiState.detectedUrls.isNotEmpty()) {
              DropdownMenuItem(
                text = {
                  Text(
                    "${stringResource(R.string.detected_links)} (${uiState.detectedUrls.size})",
                    color = theme.accent,
                  )
                },
                leadingIcon = {
                  Icon(Icons.Outlined.Link, contentDescription = null, tint = theme.accent)
                },
                onClick = {
                  showLinksSheet = true
                  showMoreMenu = false
                },
              )
            }
            HorizontalDivider(color = theme.border.copy(alpha = 0.5f))
            DropdownMenuItem(
              text = { Text(stringResource(R.string.share_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onShareNote()
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.export_txt), color = theme.textPrimary) },
              leadingIcon = {
                Icon(Icons.Outlined.SaveAlt, contentDescription = null, tint = theme.textPrimary)
              },
              onClick = {
                onExportNote()
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.duplicate_note), color = theme.textPrimary) },
              leadingIcon = {
                Icon(
                  Icons.Outlined.ContentCopy,
                  contentDescription = null,
                  tint = theme.textPrimary,
                )
              },
              onClick = {
                onDuplicateNote()
                showMoreMenu = false
              },
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.delete_note), color = theme.dangerColor) },
              leadingIcon = {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = theme.dangerColor)
              },
              onClick = {
                if (editorSettings.confirmBeforeDelete) showConfirmDelete = true
                else onDeleteNote()
                showMoreMenu = false
              },
            )
          }
        }
      }

      HorizontalDivider(color = theme.border.copy(alpha = 0.45f), thickness = 0.8.dp)

      // Collapsible Find in Note bar
      AnimatedVisibility(visible = uiState.findState.isVisible) {
        VellumSurface(
          theme = theme,
          elevated = true,
          cornerRadiusOverride = 12.dp,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              Icons.Outlined.Search,
              contentDescription = null,
              tint = theme.textSecondary,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
              if (uiState.findState.query.isEmpty()) {
                Text(
                  stringResource(R.string.find_hint),
                  color = theme.textSecondary,
                  fontSize = 14.sp,
                )
              }
              BasicTextField(
                value = uiState.findState.query,
                onValueChange = onUpdateFindQuery,
                singleLine = true,
                textStyle =
                  TextStyle(
                    color = theme.textPrimary,
                    fontFamily = theme.bodyFont,
                    fontSize = 14.sp,
                  ),
                cursorBrush = SolidColor(theme.accent),
                modifier = Modifier.fillMaxWidth().testTag("find_query_input"),
              )
            }

            val totalMatches = uiState.findState.matches.size
            val activeHuman = if (totalMatches == 0) 0 else uiState.findState.activeIndex + 1
            Text(
              text = "$activeHuman/$totalMatches",
              fontSize = 12.sp,
              color = theme.textSecondary,
              modifier = Modifier.padding(horizontal = 6.dp),
            )

            Box(
              modifier =
                Modifier.clip(RoundedCornerShape(6.dp))
                  .background(
                    if (uiState.findState.caseSensitive) theme.accent.copy(alpha = 0.2f)
                    else Color.Transparent
                  )
                  .border(
                    width = 1.dp,
                    color =
                      if (uiState.findState.caseSensitive) theme.accent
                      else theme.border.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                  )
                  .clickable(onClick = onToggleCaseSensitive)
                  .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
              Text(
                "Aa",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                  if (uiState.findState.caseSensitive) theme.accent else theme.textSecondary,
              )
            }

            IconButton(
              onClick = onPreviousMatch,
              enabled = totalMatches > 0,
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                Icons.Outlined.KeyboardArrowUp,
                contentDescription = stringResource(R.string.previous_match),
                tint = theme.textPrimary,
              )
            }
            IconButton(
              onClick = onNextMatch,
              enabled = totalMatches > 0,
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(R.string.next_match),
                tint = theme.textPrimary,
              )
            }
            IconButton(
              onClick = onToggleFindBar,
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(R.string.close_find),
                tint = theme.textSecondary,
              )
            }
          }
        }
      }

      // Main unrestricted plain-text writing canvas
      val padDp = editorSettings.editorPaddingDp.dp
      val vScroll = rememberScrollState()
      val hScroll = rememberScrollState()

      BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
        val minWidth = maxWidth
        val minHeight = maxHeight
        val scrollContainerModifier =
          if (editorSettings.wordWrap) {
            Modifier.fillMaxSize().verticalScroll(vScroll)
          } else {
            Modifier.fillMaxSize().verticalScroll(vScroll).horizontalScroll(hScroll)
          }

        Box(modifier = scrollContainerModifier) {
          val fieldWidthModifier =
            if (editorSettings.wordWrap) {
              Modifier.fillMaxWidth().heightIn(min = minHeight)
            } else {
              Modifier.widthIn(min = minWidth).heightIn(min = minHeight)
            }

          Box(modifier = fieldWidthModifier.padding(padDp)) {
            if (uiState.textFieldValue.text.isEmpty()) {
              Text(
                text = stringResource(R.string.note_body_hint),
                fontFamily = editorFontFamily,
                fontSize = editorSettings.fontSizeSp.sp,
                lineHeight = (editorSettings.fontSizeSp * editorSettings.lineHeightMultiplier).sp,
                color = theme.textSecondary.copy(alpha = 0.52f),
              )
            }

            if (uiState.isLoaded) {
              BasicTextField(
                value = uiState.textFieldValue,
                onValueChange = onContentChange,
                textStyle =
                  TextStyle(
                    fontFamily = editorFontFamily,
                    fontSize = editorSettings.fontSizeSp.sp,
                    lineHeight = (editorSettings.fontSizeSp * editorSettings.lineHeightMultiplier).sp,
                    color = theme.textPrimary,
                  ),
                cursorBrush = SolidColor(theme.accent),
                visualTransformation = highlightTransformation,
                modifier =
                  Modifier.then(
                      if (editorSettings.wordWrap) Modifier.fillMaxWidth() else Modifier
                    )
                    .fillMaxHeight()
                    .heightIn(min = minHeight - padDp * 2)
                    .testTag("editor_body_input"),
              )
            }
          }
        }
      }

      // Minimal bottom status / utility strip
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(theme.surface.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (editorSettings.showWordCount) {
            Text(
              text = "${uiState.wordCount} words · ${uiState.charCount} chars",
              fontFamily = theme.bodyFont,
              fontSize = 11.5.sp,
              color = theme.textSecondary,
              modifier = Modifier.testTag("word_count_text"),
            )
          }
          if (!editorSettings.wordWrap) {
            Text(
              text = "Wrap: Off",
              fontFamily = theme.bodyFont,
              fontSize = 11.sp,
              color = theme.accent,
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (editorSettings.detectUrls && uiState.detectedUrls.isNotEmpty()) {
            Row(
              modifier =
                Modifier.clip(RoundedCornerShape(999.dp))
                  .background(theme.accent.copy(alpha = 0.14f))
                  .clickable { showLinksSheet = true }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
                  .testTag("detected_links_pill"),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                Icons.Outlined.Link,
                contentDescription = stringResource(R.string.detected_links),
                tint = theme.accent,
                modifier = Modifier.size(14.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text =
                  if (uiState.detectedUrls.size == 1) stringResource(R.string.open_link)
                  else "Links (${uiState.detectedUrls.size})",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = theme.accent,
              )
            }
            Spacer(Modifier.width(4.dp))
          }

          IconButton(
            onClick = onToggleFindBar,
            modifier = Modifier.size(36.dp).testTag("bottom_find_button"),
          ) {
            Icon(
              Icons.Outlined.Search,
              contentDescription = stringResource(R.string.find_in_note),
              tint = theme.textSecondary,
              modifier = Modifier.size(18.dp),
            )
          }

          IconButton(
            onClick = { showEditorSettings = true },
            modifier = Modifier.size(36.dp).testTag("bottom_editor_settings_button"),
          ) {
            Icon(
              Icons.Outlined.Tune,
              contentDescription = stringResource(R.string.editor_settings),
              tint = theme.textSecondary,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }
  }

  if (showEditorSettings) {
    EditorSettingsSheet(
      settings = editorSettings,
      onUpdateSettings = onUpdateEditorSettings,
      onDismiss = { showEditorSettings = false },
    )
  }

  if (showMoveFolderSheet) {
    MoveToFolderSheet(
      folders = folders,
      currentFolderId = uiState.folderId,
      onSelectFolder = onMoveToFolder,
      onCreateFolderRequested = {
        showMoveFolderSheet = false
        showCreateFolderDialog = true
      },
      onDismiss = { showMoveFolderSheet = false },
    )
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

  if (showConfirmDelete) {
    ConfirmDeleteDialog(
      onConfirm = {
        showConfirmDelete = false
        onDeleteNote()
      },
      onDismiss = { showConfirmDelete = false },
    )
  }

  if (showLinksSheet) {
    DetectedLinksSheet(
      urls = uiState.detectedUrls,
      onOpenUrl = { url ->
        runCatching {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
      },
      onDismiss = { showLinksSheet = false },
    )
  }
}
