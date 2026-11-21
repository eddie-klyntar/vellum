package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EditorSettings
import com.example.data.NoteSortOption
import com.example.ui.components.SheetSwitchRow
import com.example.ui.theme.EditorFontPreset
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumFonts
import com.example.ui.theme.VellumPillButton
import com.example.ui.theme.VellumSurface
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
  editorSettings: EditorSettings,
  onBack: () -> Unit,
  onOpenThemeGallery: () -> Unit,
  onOpenThemeCustomizer: () -> Unit,
  onImportTxt: () -> Unit,
  onUpdateEditorSettings: ((EditorSettings) -> EditorSettings) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalVellumTheme.current

  VellumBackdrop(theme = theme, modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = theme.textPrimary,
          )
        }
        Text(
          text = stringResource(R.string.settings),
          fontFamily = theme.displayFont,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = theme.textPrimary,
        )
      }

      Column(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Theme section
        SettingsSectionLabel("Visual Theme")
        VellumSurface(theme = theme, modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clickable(onClick = onOpenThemeGallery)
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Outlined.Palette, contentDescription = null, tint = theme.accent)
              Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                  stringResource(R.string.theme_gallery),
                  fontWeight = FontWeight.SemiBold,
                  color = theme.textPrimary,
                )
                Text(
                  "Active: ${theme.activePresetName ?: theme.spec.name}",
                  fontSize = 12.5.sp,
                  color = theme.textSecondary,
                )
              }
              Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = theme.textSecondary)
            }

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clickable(onClick = onOpenThemeCustomizer)
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Outlined.Tune, contentDescription = null, tint = theme.accent)
              Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                  stringResource(R.string.customize_theme),
                  fontWeight = FontWeight.SemiBold,
                  color = theme.textPrimary,
                )
                Text(
                  "Colours, corners, blur, polarity & personal presets",
                  fontSize = 12.5.sp,
                  color = theme.textSecondary,
                )
              }
              Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = theme.textSecondary)
            }
          }
        }

        // Editor typography & layout
        SettingsSectionLabel("Editor Typography & Canvas")
        VellumSurface(theme = theme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              stringResource(R.string.font_family),
              fontWeight = FontWeight.SemiBold,
              color = theme.textPrimary,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              EditorFontPreset.entries.forEach { preset ->
                VellumPillButton(
                  theme = theme,
                  selected = editorSettings.fontPreset == preset,
                  onClick = { onUpdateEditorSettings { it.copy(fontPreset = preset) } },
                ) {
                  Text(
                    preset.label,
                    fontFamily = VellumFonts.resolve(preset, fallback = theme.bodyFont),
                    fontSize = 13.sp,
                  )
                }
              }
            }

            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(stringResource(R.string.font_size), color = theme.textPrimary)
                Text(
                  "${editorSettings.fontSizeSp.roundToInt()} sp",
                  color = theme.accent,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Slider(
                value = editorSettings.fontSizeSp,
                onValueChange = { v -> onUpdateEditorSettings { it.copy(fontSizeSp = v) } },
                valueRange = 12f..28f,
                colors =
                  SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
              )
            }

            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(stringResource(R.string.line_spacing), color = theme.textPrimary)
                Text(
                  String.format("%.2fx", editorSettings.lineHeightMultiplier),
                  color = theme.accent,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Slider(
                value = editorSettings.lineHeightMultiplier,
                onValueChange = { v ->
                  onUpdateEditorSettings { it.copy(lineHeightMultiplier = v) }
                },
                valueRange = 1.15f..2.0f,
                colors =
                  SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
              )
            }

            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(stringResource(R.string.editor_padding), color = theme.textPrimary)
                Text(
                  "${editorSettings.editorPaddingDp} dp",
                  color = theme.accent,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Slider(
                value = editorSettings.editorPaddingDp.toFloat(),
                onValueChange = { v ->
                  onUpdateEditorSettings { it.copy(editorPaddingDp = v.roundToInt()) }
                },
                valueRange = 8f..36f,
                colors =
                  SliderDefaults.colors(thumbColor = theme.accent, activeTrackColor = theme.accent),
              )
            }
          }
        }

        // Behaviour & Organisation
        SettingsSectionLabel("Behaviour & Organisation")
        VellumSurface(theme = theme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SheetSwitchRow(
              title = stringResource(R.string.word_wrap),
              subtitle = "Turn off for horizontal scrolling on long code or log lines",
              checked = editorSettings.wordWrap,
              onCheckedChange = { c -> onUpdateEditorSettings { it.copy(wordWrap = c) } },
            )
            SheetSwitchRow(
              title = stringResource(R.string.word_count_label),
              subtitle = "Display live word and character count while writing",
              checked = editorSettings.showWordCount,
              onCheckedChange = { c -> onUpdateEditorSettings { it.copy(showWordCount = c) } },
            )
            SheetSwitchRow(
              title = stringResource(R.string.detect_urls_label),
              subtitle = "Highlight links visually without altering plain text",
              checked = editorSettings.detectUrls,
              onCheckedChange = { c -> onUpdateEditorSettings { it.copy(detectUrls = c) } },
            )
            SheetSwitchRow(
              title = stringResource(R.string.confirm_delete_pref),
              subtitle = "Ask before permanently deleting a note",
              checked = editorSettings.confirmBeforeDelete,
              onCheckedChange = { c ->
                onUpdateEditorSettings { it.copy(confirmBeforeDelete = c) }
              },
            )

            Spacer(Modifier.height(4.dp))
            Text(
              stringResource(R.string.sort_by),
              fontWeight = FontWeight.SemiBold,
              color = theme.textPrimary,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              NoteSortOption.entries.forEach { option ->
                VellumPillButton(
                  theme = theme,
                  selected = editorSettings.sortOption == option,
                  onClick = { onUpdateEditorSettings { it.copy(sortOption = option) } },
                ) {
                  Text(option.label, fontSize = 12.5.sp)
                }
              }
            }
          }
        }

        // Import plain text
        SettingsSectionLabel("Files")
        VellumSurface(theme = theme, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onImportTxt).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Outlined.FileOpen, contentDescription = null, tint = theme.accent)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
              Text(
                stringResource(R.string.import_txt),
                fontWeight = FontWeight.SemiBold,
                color = theme.textPrimary,
              )
              Text(
                "Import plain-text (.txt) files offline with UTF-8 fidelity",
                fontSize = 12.5.sp,
                color = theme.textSecondary,
              )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = theme.textSecondary)
          }
        }

        Spacer(Modifier.height(20.dp))
      }
    }
  }
}

@Composable
private fun SettingsSectionLabel(title: String) {
  val theme = LocalVellumTheme.current
  Text(
    text = title.uppercase(),
    fontFamily = theme.displayFont,
    fontSize = 11.5.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.9.sp,
    color = theme.textSecondary,
    modifier = Modifier.padding(horizontal = 4.dp),
  )
}
