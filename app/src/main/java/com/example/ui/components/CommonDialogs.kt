package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EditorSettings
import com.example.data.FolderEntity
import com.example.editor.DetectedUrl
import com.example.ui.theme.EditorFontPreset
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.VellumFonts
import com.example.ui.theme.VellumPillButton
import kotlin.math.roundToInt

@Composable
fun ConfirmDeleteDialog(
  title: String = stringResource(R.string.confirm_delete_title),
  message: String = stringResource(R.string.confirm_delete_message),
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = theme.surfaceElevated,
    titleContentColor = theme.textPrimary,
    textContentColor = theme.textSecondary,
    title = { Text(title, fontFamily = theme.displayFont, fontWeight = FontWeight.SemiBold) },
    text = { Text(message, fontFamily = theme.bodyFont) },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        modifier = Modifier.testTag("confirm_delete_button"),
      ) {
        Text(
          stringResource(R.string.confirm_delete_action),
          color = theme.dangerColor,
          fontWeight = FontWeight.SemiBold,
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.cancel), color = theme.textSecondary)
      }
    },
  )
}

@Composable
fun TextInputDialog(
  title: String,
  label: String,
  initialValue: String = "",
  confirmLabel: String = stringResource(R.string.save),
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  var value by remember(initialValue) { mutableStateOf(initialValue) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = theme.surfaceElevated,
    titleContentColor = theme.textPrimary,
    textContentColor = theme.textPrimary,
    title = { Text(title, fontFamily = theme.displayFont, fontWeight = FontWeight.SemiBold) },
    text = {
      OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("dialog_text_input"),
      )
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(value) },
        modifier = Modifier.testTag("dialog_confirm_button"),
      ) {
        Text(confirmLabel, color = theme.accent, fontWeight = FontWeight.SemiBold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.cancel), color = theme.textSecondary)
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFolderSheet(
  folders: List<FolderEntity>,
  currentFolderId: Long?,
  onSelectFolder: (Long?) -> Unit,
  onCreateFolderRequested: () -> Unit,
  onDismiss: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = theme.surfaceElevated,
    contentColor = theme.textPrimary,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          stringResource(R.string.move_to_folder),
          fontFamily = theme.displayFont,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          color = theme.textPrimary,
        )
        TextButton(onClick = onCreateFolderRequested) {
          Text("+ ${stringResource(R.string.new_folder)}", color = theme.accent)
        }
      }
      Spacer(Modifier.height(8.dp))

      ListItem(
        headlineContent = { Text(stringResource(R.string.no_folder)) },
        leadingContent = {
          Icon(Icons.Outlined.FolderOff, contentDescription = null, tint = theme.textSecondary)
        },
        trailingContent = {
          if (currentFolderId == null) {
            Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = theme.accent)
          }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier =
          Modifier.clickable {
            onSelectFolder(null)
            onDismiss()
          },
      )

      folders.forEach { folder ->
        ListItem(
          headlineContent = { Text(folder.name) },
          leadingContent = {
            Icon(Icons.Outlined.Folder, contentDescription = null, tint = theme.accent)
          },
          trailingContent = {
            if (currentFolderId == folder.id) {
              Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = theme.accent)
            }
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier =
            Modifier.clickable {
              onSelectFolder(folder.id)
              onDismiss()
            },
        )
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorSettingsSheet(
  settings: EditorSettings,
  onUpdateSettings: ((EditorSettings) -> EditorSettings) -> Unit,
  onDismiss: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = theme.surfaceElevated,
    contentColor = theme.textPrimary,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        stringResource(R.string.editor_settings),
        fontFamily = theme.displayFont,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = theme.textPrimary,
      )

      // Font Family picker
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          stringResource(R.string.font_family),
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = theme.textSecondary,
        )
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          EditorFontPreset.entries.forEach { preset ->
            val selected = settings.fontPreset == preset
            VellumPillButton(
              theme = theme,
              selected = selected,
              onClick = { onUpdateSettings { it.copy(fontPreset = preset) } },
            ) {
              Text(
                text = preset.label,
                fontFamily =
                  VellumFonts.resolve(preset, fallback = theme.bodyFont),
                fontSize = 13.sp,
              )
            }
          }
        }
      }

      // Font Size
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(stringResource(R.string.font_size), color = theme.textPrimary, fontSize = 14.sp)
          Text(
            "${settings.fontSizeSp.roundToInt()} sp",
            color = theme.accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
          )
        }
        Slider(
          value = settings.fontSizeSp,
          onValueChange = { value -> onUpdateSettings { it.copy(fontSizeSp = value) } },
          valueRange = 12f..28f,
          steps = 15,
          colors = SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
        )
      }

      // Line Spacing
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(stringResource(R.string.line_spacing), color = theme.textPrimary, fontSize = 14.sp)
          Text(
            String.format("%.2fx", settings.lineHeightMultiplier),
            color = theme.accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
          )
        }
        Slider(
          value = settings.lineHeightMultiplier,
          onValueChange = { value -> onUpdateSettings { it.copy(lineHeightMultiplier = value) } },
          valueRange = 1.15f..2.0f,
          colors = SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
        )
      }

      // Editor Padding
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(stringResource(R.string.editor_padding), color = theme.textPrimary, fontSize = 14.sp)
          Text(
            "${settings.editorPaddingDp} dp",
            color = theme.accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
          )
        }
        Slider(
          value = settings.editorPaddingDp.toFloat(),
          onValueChange = { value ->
            onUpdateSettings { it.copy(editorPaddingDp = value.roundToInt()) }
          },
          valueRange = 8f..36f,
          colors = SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
        )
      }

      SheetSwitchRow(
        title = stringResource(R.string.word_wrap),
        checked = settings.wordWrap,
        onCheckedChange = { checked -> onUpdateSettings { it.copy(wordWrap = checked) } },
      )
      SheetSwitchRow(
        title = stringResource(R.string.word_count_label),
        checked = settings.showWordCount,
        onCheckedChange = { checked -> onUpdateSettings { it.copy(showWordCount = checked) } },
      )
      SheetSwitchRow(
        title = stringResource(R.string.detect_urls_label),
        checked = settings.detectUrls,
        onCheckedChange = { checked -> onUpdateSettings { it.copy(detectUrls = checked) } },
      )

      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
fun SheetSwitchRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  subtitle: String? = null,
) {
  val theme = LocalVellumTheme.current
  Row(
    modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(title, color = theme.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
      if (!subtitle.isNullOrBlank()) {
        Text(subtitle, color = theme.textSecondary, fontSize = 12.sp)
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = theme.onAccent,
          checkedTrackColor = theme.accent,
        ),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectedLinksSheet(
  urls: List<DetectedUrl>,
  onOpenUrl: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val theme = LocalVellumTheme.current
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = theme.surfaceElevated,
    contentColor = theme.textPrimary,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Text(
        stringResource(R.string.detected_links),
        fontFamily = theme.displayFont,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = theme.textPrimary,
      )
      Spacer(Modifier.height(8.dp))
      urls.distinctBy { it.url }.forEach { link ->
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clickable {
                onOpenUrl(link.url)
                onDismiss()
              }
              .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Outlined.Link, contentDescription = null, tint = theme.accent)
          Spacer(Modifier.width(12.dp))
          Text(
            text = link.url,
            color = theme.accent,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = {
              onOpenUrl(link.url)
              onDismiss()
            }
          ) {
            Icon(
              Icons.AutoMirrored.Outlined.OpenInNew,
              contentDescription = stringResource(R.string.open_link),
              tint = theme.textSecondary,
            )
          }
        }
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}
