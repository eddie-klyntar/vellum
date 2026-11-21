package com.example.ui.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ThemeUserState
import com.example.ui.theme.CustomThemePreset
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.ResolvedVellumTheme
import com.example.ui.theme.ThemeCatalog
import com.example.ui.theme.ThemeCategory
import com.example.ui.theme.ThemeCustomization
import com.example.ui.theme.ThemeResolver
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumPillButton
import com.example.ui.theme.VellumSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeGalleryScreen(
  themeUserState: ThemeUserState,
  onBack: () -> Unit,
  onApplyTheme: (themeId: String, presetId: String?) -> Unit,
  onOpenCustomizer: (themeId: String) -> Unit,
  onDeletePreset: (presetId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val activeTheme = LocalVellumTheme.current
  val context = LocalContext.current
  var selectedCategory by remember { mutableStateOf(ThemeCategory.ALL) }

  // Live preview modal target
  var previewThemeId by remember { mutableStateOf<String?>(null) }
  var previewPreset by remember { mutableStateOf<CustomThemePreset?>(null) }
  var previewDarkOverride by remember { mutableStateOf<Boolean?>(null) }

  val filteredThemes =
    remember(selectedCategory) {
      when (selectedCategory) {
        ThemeCategory.ALL -> ThemeCatalog.themes
        ThemeCategory.MY_PRESETS -> emptyList()
        else -> ThemeCatalog.themes.filter { it.category == selectedCategory }
      }
    }

  VellumBackdrop(theme = activeTheme, modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("gallery_back_button"),
        ) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = activeTheme.textPrimary,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.theme_gallery),
            fontFamily = activeTheme.displayFont,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = activeTheme.textPrimary,
          )
          Text(
            text = "24 handcrafted visual worlds · Tap to preview",
            fontFamily = activeTheme.bodyFont,
            fontSize = 12.sp,
            color = activeTheme.textSecondary,
          )
        }
        IconButton(
          onClick = { onOpenCustomizer(activeTheme.spec.id) },
          modifier = Modifier.testTag("customize_active_theme_button"),
        ) {
          Icon(
            Icons.Outlined.Tune,
            contentDescription = stringResource(R.string.customize_theme),
            tint = activeTheme.accent,
          )
        }
      }

      // Category Filter Strip
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ThemeCategory.entries.forEach { category ->
          val label =
            if (category == ThemeCategory.MY_PRESETS) {
              "${category.label} (${themeUserState.savedPresets.size})"
            } else {
              category.label
            }
          VellumPillButton(
            theme = activeTheme,
            selected = selectedCategory == category,
            onClick = { selectedCategory = category },
          ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
          }
        }
      }

      // Grid of live-rendered theme previews
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.weight(1f).fillMaxWidth().testTag("theme_gallery_grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (selectedCategory == ThemeCategory.MY_PRESETS) {
          if (themeUserState.savedPresets.isEmpty()) {
            item {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  "No custom presets saved yet.",
                  color = activeTheme.textPrimary,
                  fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  "Open any theme and tap Customize to save your own personal preset.",
                  color = activeTheme.textSecondary,
                  fontSize = 13.sp,
                )
              }
            }
          } else {
            items(themeUserState.savedPresets, key = { "preset_${it.id}" }) { preset ->
              val spec = ThemeCatalog.findById(preset.baseThemeId)
              val resolved =
                ThemeResolver.resolve(
                  spec = spec,
                  customization = preset.customization,
                  context = context,
                  activePresetName = preset.name,
                )
              val isActive = themeUserState.activePresetId == preset.id
              ThemeMiniPreviewCard(
                title = preset.name,
                subtitle = "Based on ${spec.name}",
                resolved = resolved,
                isActive = isActive,
                onClick = {
                  previewPreset = preset
                  previewThemeId = spec.id
                  previewDarkOverride = resolved.isDark
                },
                onDelete = { onDeletePreset(preset.id) },
              )
            }
          }
        } else {
          items(filteredThemes, key = { it.id }) { spec ->
            val custom = themeUserState.customizationsByThemeId[spec.id] ?: ThemeCustomization()
            val resolved =
              ThemeResolver.resolve(spec = spec, customization = custom, context = context)
            val isActive =
              themeUserState.activeThemeId == spec.id && themeUserState.activePresetId == null
            ThemeMiniPreviewCard(
              title = spec.name,
              subtitle = spec.tagline,
              resolved = resolved,
              isActive = isActive,
              onClick = {
                previewPreset = null
                previewThemeId = spec.id
                previewDarkOverride = resolved.isDark
              },
            )
          }
        }
      }
    }
  }

  // Live Interactive Theme Preview Bottom Sheet (before applying)
  val targetThemeId = previewThemeId
  if (targetThemeId != null) {
    val spec = ThemeCatalog.findById(targetThemeId)
    val baseCustomization =
      previewPreset?.customization
        ?: themeUserState.customizationsByThemeId[targetThemeId]
        ?: ThemeCustomization()
    val effectiveCustom = baseCustomization.copy(isDarkOverride = previewDarkOverride)
    val previewResolved =
      ThemeResolver.resolve(
        spec = spec,
        customization = effectiveCustom,
        context = context,
        activePresetName = previewPreset?.name,
      )

    ModalBottomSheet(
      onDismissRequest = { previewThemeId = null },
      containerColor = activeTheme.surfaceElevated,
      contentColor = activeTheme.textPrimary,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = previewPreset?.name ?: spec.name,
              fontFamily = activeTheme.displayFont,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = activeTheme.textPrimary,
            )
            Text(
              text = spec.tagline,
              fontSize = 13.sp,
              color = activeTheme.textSecondary,
            )
          }

          // Instant Light / Dark variant toggle inside preview
          IconButton(
            onClick = { previewDarkOverride = !(previewDarkOverride ?: spec.defaultIsDark) },
            modifier = Modifier.testTag("preview_polarity_toggle"),
          ) {
            Icon(
              imageVector =
                if (previewResolved.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
              contentDescription = "Toggle light or dark variant",
              tint = activeTheme.accent,
            )
          }
        }

        // Live preview canvas showing the selected theme's actual backdrop, card, and editor
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(220.dp)
              .clip(RoundedCornerShape(18.dp))
              .border(1.dp, activeTheme.border, RoundedCornerShape(18.dp))
        ) {
          ThemeLivePreviewCanvas(theme = previewResolved)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = {
              previewThemeId = null
              onOpenCustomizer(spec.id)
            },
            modifier = Modifier.weight(1f).testTag("preview_customize_button"),
          ) {
            Icon(
              Icons.Outlined.Tune,
              contentDescription = null,
              tint = activeTheme.textPrimary,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Customize", color = activeTheme.textPrimary)
          }

          Button(
            onClick = {
              val preset = previewPreset
              if (preset != null) {
                onApplyTheme(preset.baseThemeId, preset.id)
              } else {
                onApplyTheme(spec.id, null)
              }
              previewThemeId = null
              onBack()
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = activeTheme.accent,
                contentColor = activeTheme.onAccent,
              ),
            modifier = Modifier.weight(1f).testTag("preview_apply_button"),
          ) {
            Text(stringResource(R.string.apply), fontWeight = FontWeight.SemiBold)
          }
        }
        Spacer(Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun ThemeMiniPreviewCard(
  title: String,
  subtitle: String,
  resolved: ResolvedVellumTheme,
  isActive: Boolean,
  onClick: () -> Unit,
  onDelete: (() -> Unit)? = null,
) {
  val outerBorderColor = if (isActive) resolved.accent else resolved.border.copy(alpha = 0.6f)
  val outerBorderWidth = if (isActive) 2.2.dp else 1.dp

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(outerBorderWidth, outerBorderColor, RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("theme_card_${resolved.spec.id}")
  ) {
    // Mini visual world preview
    Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
      VellumBackdrop(theme = resolved, modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier.fillMaxSize().padding(10.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Aa",
              fontFamily = resolved.displayFont,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = resolved.textPrimary,
            )
            if (isActive) {
              Box(
                modifier =
                  Modifier.size(20.dp).clip(CircleShape).background(resolved.accent),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  Icons.Filled.Check,
                  contentDescription = "Active theme",
                  tint = resolved.onAccent,
                  modifier = Modifier.size(13.dp),
                )
              }
            } else {
              Box(
                modifier =
                  Modifier.size(12.dp).clip(CircleShape).background(resolved.accent)
              )
            }
          }

          VellumSurface(theme = resolved, elevated = false, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text(
                text = "Quick thought",
                fontFamily = resolved.displayFont,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = resolved.textPrimary,
                maxLines = 1,
              )
              Spacer(Modifier.height(2.dp))
              Text(
                text = "Plain text, links & code.",
                fontFamily = resolved.bodyFont,
                fontSize = 10.sp,
                color = resolved.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }

    // Label footer
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(resolved.surfaceElevated)
          .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontFamily = resolved.displayFont,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = resolved.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          fontFamily = resolved.bodyFont,
          fontSize = 11.sp,
          color = resolved.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (onDelete != null) {
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
          Icon(
            Icons.Outlined.Delete,
            contentDescription = "Delete preset",
            tint = resolved.dangerColor,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
fun ThemeLivePreviewCanvas(theme: ResolvedVellumTheme, modifier: Modifier = Modifier) {
  VellumBackdrop(theme = theme, modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = theme.activePresetName ?: theme.spec.name,
          fontFamily = theme.displayFont,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = theme.textPrimary,
        )
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(999.dp))
              .background(theme.accent)
              .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            "Saved",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.onAccent,
          )
        }
      }

      VellumSurface(theme = theme, elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Filled.PushPin,
              contentDescription = null,
              tint = theme.pinColor,
              modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
              "Morning Ideas",
              fontFamily = theme.displayFont,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = theme.textPrimary,
            )
          }
          Spacer(Modifier.height(4.dp))
          Text(
            "Unrestricted plain text: https://example.org, commands, or late-night sketches.",
            fontFamily = theme.bodyFont,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            color = theme.textSecondary,
          )
        }
      }

      VellumSurface(theme = theme, elevated = true, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "18 words · 109 chars",
            fontFamily = theme.bodyFont,
            fontSize = 11.5.sp,
            color = theme.textSecondary,
          )
          Text(
            "+ New note",
            fontFamily = theme.displayFont,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.accent,
          )
        }
      }
    }
  }
}
