package com.example.ui.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.TextInputDialog
import com.example.ui.theme.EditorFontPreset
import com.example.ui.theme.InterfaceDensity
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.ThemeCatalog
import com.example.ui.theme.ThemeCustomization
import com.example.ui.theme.ThemeResolver
import com.example.ui.theme.ThemeResolver.toArgbLong
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumFonts
import com.example.ui.theme.VellumPillButton
import com.example.ui.theme.VellumSurface
import kotlin.math.roundToInt

private val accentSwatches =
  listOf(
    null to "Original",
    0xFF0A84FF to "Azure",
    0xFF7C3AED to "Violet",
    0xFFEC4899 to "Rose",
    0xFFFF3D00 to "Vermilion",
    0xFF10B981 to "Emerald",
    0xFFF59E0B to "Amber",
    0xFF06B6D4 to "Cyan",
    0xFF18181B to "Ink",
    0xFFFFFFFF to "Chalk",
  )

private val backgroundSwatchesLight =
  listOf(
    null to "Original",
    0xFFFFFFFF to "Pure White",
    0xFFF6F2E9 to "Warm Ivory",
    0xFFEBF2FA to "Cool Mist",
    0xFFEFF4EC to "Soft Sage",
    0xFFFDF2F6 to "Blush",
  )

private val backgroundSwatchesDark =
  listOf(
    null to "Original",
    0xFF000000 to "True Black",
    0xFF0F172A to "Deep Slate",
    0xFF141218 to "Obsidian",
    0xFF091A16 to "Night Pine",
    0xFF1E1914 to "Espresso",
  )

@Composable
fun ThemeCustomizerScreen(
  themeId: String,
  initialCustomization: ThemeCustomization,
  onBack: () -> Unit,
  onSaveAndApply: (ThemeCustomization) -> Unit,
  onResetOriginal: () -> Unit,
  onSaveAsPreset: (name: String, customization: ThemeCustomization) -> Unit,
  modifier: Modifier = Modifier,
) {
  val appTheme = LocalVellumTheme.current
  val context = LocalContext.current
  val spec = remember(themeId) { ThemeCatalog.findById(themeId) }

  var customization by remember(themeId, initialCustomization) { mutableStateOf(initialCustomization) }
  var showPresetNameDialog by remember { mutableStateOf(false) }

  val livePreviewTheme =
    remember(spec, customization) {
      ThemeResolver.resolve(spec = spec, customization = customization, context = context)
    }

  var customAccentHue by remember { mutableFloatStateOf(210f) }

  VellumBackdrop(theme = appTheme, modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("customizer_back_button")) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = appTheme.textPrimary,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.customize_theme),
            fontFamily = appTheme.displayFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = appTheme.textPrimary,
          )
          Text(
            text = spec.name,
            fontSize = 12.sp,
            color = appTheme.textSecondary,
          )
        }
        IconButton(
          onClick = {
            customization = ThemeCustomization()
            onResetOriginal()
          },
          modifier = Modifier.testTag("reset_theme_button"),
        ) {
          Icon(
            Icons.Outlined.RestartAlt,
            contentDescription = stringResource(R.string.reset_original),
            tint = appTheme.textPrimary,
          )
        }
        IconButton(
          onClick = { showPresetNameDialog = true },
          modifier = Modifier.testTag("save_preset_button"),
        ) {
          Icon(
            Icons.Outlined.BookmarkAdd,
            contentDescription = stringResource(R.string.save_preset),
            tint = appTheme.accent,
          )
        }
      }

      // Live Preview Card pinned at top
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(195.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, appTheme.border, RoundedCornerShape(18.dp))
      ) {
        ThemeLivePreviewCanvas(theme = livePreviewTheme)
      }

      Spacer(Modifier.height(10.dp))

      // Scrollable Customization Controls
      Column(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Light / Dark Polarity
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                "Dark appearance",
                fontWeight = FontWeight.SemiBold,
                color = appTheme.textPrimary,
              )
              Text(
                if (livePreviewTheme.isDark) "Dark variant active" else "Light variant active",
                fontSize = 12.sp,
                color = appTheme.textSecondary,
              )
            }
            Switch(
              checked = livePreviewTheme.isDark,
              onCheckedChange = { isDark ->
                customization =
                  customization.copy(
                    isDarkOverride = isDark,
                    backgroundColorArgb = null,
                    surfaceColorArgb = null,
                  )
              },
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = appTheme.onAccent,
                  checkedTrackColor = appTheme.accent,
                ),
              modifier = Modifier.testTag("customizer_dark_switch"),
            )
          }
        }

        // Accent Colour Picker
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              "Accent colour",
              fontWeight = FontWeight.SemiBold,
              color = appTheme.textPrimary,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              accentSwatches.forEach { (argb, label) ->
                val color =
                  if (argb == null) {
                    Color(
                      if (livePreviewTheme.isDark) spec.darkPalette.accent
                      else spec.lightPalette.accent
                    )
                  } else {
                    Color(argb)
                  }
                val isSelected = customization.accentColorArgb == argb
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Box(
                    modifier =
                      Modifier.size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                          width = if (isSelected) 3.dp else 1.dp,
                          color = if (isSelected) appTheme.textPrimary else appTheme.border,
                          shape = CircleShape,
                        )
                        .clickable { customization = customization.copy(accentColorArgb = argb) }
                  )
                  Spacer(Modifier.height(3.dp))
                  Text(label, fontSize = 10.sp, color = appTheme.textSecondary)
                }
              }
            }

            Text("Custom hue", fontSize = 12.sp, color = appTheme.textSecondary)
            Slider(
              value = customAccentHue,
              onValueChange = { hue ->
                customAccentHue = hue
                val hslColor = Color.hsl(hue, 0.78f, if (livePreviewTheme.isDark) 0.62f else 0.45f)
                customization = customization.copy(accentColorArgb = hslColor.toArgbLong())
              },
              valueRange = 0f..360f,
              colors =
                SliderDefaults.colors(
                  thumbColor = livePreviewTheme.accent,
                  activeTrackColor = livePreviewTheme.accent,
                ),
            )
          }
        }

        // Background & Surface Colour
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              "Background & surface",
              fontWeight = FontWeight.SemiBold,
              color = appTheme.textPrimary,
            )
            val bgSwatches =
              if (livePreviewTheme.isDark) backgroundSwatchesDark else backgroundSwatchesLight
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              bgSwatches.forEach { (argb, label) ->
                val bgColor =
                  if (argb == null) {
                    Color(
                      if (livePreviewTheme.isDark) spec.darkPalette.background
                      else spec.lightPalette.background
                    )
                  } else {
                    Color(argb)
                  }
                val isSelected = customization.backgroundColorArgb == argb
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Box(
                    modifier =
                      Modifier.size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(
                          width = if (isSelected) 2.5.dp else 1.dp,
                          color = if (isSelected) appTheme.accent else appTheme.border,
                          shape = RoundedCornerShape(10.dp),
                        )
                        .clickable {
                          val derivedSurface =
                            argb?.let { raw ->
                              val c = Color(raw)
                              if (livePreviewTheme.isDark) {
                                c.copy(
                                  red = (c.red + 0.05f).coerceAtMost(1f),
                                  green = (c.green + 0.05f).coerceAtMost(1f),
                                  blue = (c.blue + 0.06f).coerceAtMost(1f),
                                ).toArgbLong()
                              } else {
                                Color.White.toArgbLong()
                              }
                            }
                          customization =
                            customization.copy(
                              backgroundColorArgb = argb,
                              surfaceColorArgb = derivedSurface,
                            )
                        }
                  )
                  Spacer(Modifier.height(3.dp))
                  Text(label, fontSize = 10.sp, color = appTheme.textSecondary)
                }
              }
            }
          }
        }

        // Typography Preset
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              "Typography preset",
              fontWeight = FontWeight.SemiBold,
              color = appTheme.textPrimary,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              EditorFontPreset.entries.forEach { preset ->
                val isSelected =
                  (customization.fontOverride ?: EditorFontPreset.THEME_DEFAULT) == preset
                VellumPillButton(
                  theme = appTheme,
                  selected = isSelected,
                  onClick = {
                    customization =
                      customization.copy(
                        fontOverride =
                          if (preset == EditorFontPreset.THEME_DEFAULT) null else preset
                      )
                  },
                ) {
                  Text(
                    preset.label,
                    fontFamily = VellumFonts.resolve(preset, fallback = livePreviewTheme.bodyFont),
                    fontSize = 13.sp,
                  )
                }
              }
            }
          }
        }

        // Corner Radius & Density
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            val currentRadius = customization.cornerRadiusDp ?: spec.defaultCornerRadiusDp
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Corner radius", fontWeight = FontWeight.SemiBold, color = appTheme.textPrimary)
              Text(
                "${currentRadius.roundToInt()} dp",
                color = appTheme.accent,
                fontWeight = FontWeight.SemiBold,
              )
            }
            Slider(
              value = currentRadius,
              onValueChange = { r -> customization = customization.copy(cornerRadiusDp = r) },
              valueRange = 0f..28f,
              colors =
                SliderDefaults.colors(
                  thumbColor = appTheme.accent,
                  activeTrackColor = appTheme.accent,
                ),
            )

            Text("Interface density", fontWeight = FontWeight.SemiBold, color = appTheme.textPrimary)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              InterfaceDensity.entries.forEach { density ->
                val selected = (customization.density ?: spec.defaultDensity) == density
                VellumPillButton(
                  theme = appTheme,
                  selected = selected,
                  onClick = { customization = customization.copy(density = density) },
                  modifier = Modifier.weight(1f),
                ) {
                  Text(density.label, fontSize = 12.5.sp)
                }
              }
            }
          }
        }

        // Blur & Transparency
        VellumSurface(theme = appTheme, modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val blurOn = customization.blurEnabled ?: spec.defaultBlurEnabled
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  "Ambient blur & glow",
                  fontWeight = FontWeight.SemiBold,
                  color = appTheme.textPrimary,
                )
                Text(
                  "Soft atmospheric backdrop effect",
                  fontSize = 12.sp,
                  color = appTheme.textSecondary,
                )
              }
              Switch(
                checked = blurOn,
                onCheckedChange = { checked ->
                  customization = customization.copy(blurEnabled = checked)
                },
                colors =
                  SwitchDefaults.colors(
                    checkedThumbColor = appTheme.onAccent,
                    checkedTrackColor = appTheme.accent,
                  ),
              )
            }

            val alpha = customization.transparency ?: spec.defaultTransparency
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Surface translucency", color = appTheme.textPrimary, fontSize = 14.sp)
              Text(
                "${(alpha * 100).roundToInt()}%",
                color = appTheme.accent,
                fontWeight = FontWeight.SemiBold,
              )
            }
            Slider(
              value = alpha,
              onValueChange = { t -> customization = customization.copy(transparency = t) },
              valueRange = 0f..0.85f,
              colors =
                SliderDefaults.colors(
                  thumbColor = appTheme.accent,
                  activeTrackColor = appTheme.accent,
                ),
            )
          }
        }

        Spacer(Modifier.height(8.dp))
      }

      // Bottom Apply Bar
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(appTheme.surfaceElevated.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedButton(
          onClick = { showPresetNameDialog = true },
          modifier = Modifier.weight(1f),
        ) {
          Text(stringResource(R.string.save_preset), color = appTheme.textPrimary, maxLines = 1)
        }
        Button(
          onClick = {
            onSaveAndApply(customization)
            onBack()
          },
          colors =
            ButtonDefaults.buttonColors(
              containerColor = appTheme.accent,
              contentColor = appTheme.onAccent,
            ),
          modifier = Modifier.weight(1f).testTag("apply_customized_theme_button"),
        ) {
          Text(stringResource(R.string.apply), fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }

  if (showPresetNameDialog) {
    TextInputDialog(
      title = stringResource(R.string.save_preset),
      label = stringResource(R.string.preset_name_hint),
      initialValue = "${spec.name} Custom",
      onConfirm = { name ->
        onSaveAsPreset(name, customization)
        showPresetNameDialog = false
        onBack()
      },
      onDismiss = { showPresetNameDialog = false },
    )
  }
}
