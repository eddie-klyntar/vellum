package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeCategory(val label: String) {
  ALL("All"),
  GLASS_DEPTH("Glass & Depth"),
  MINIMAL_EDITORIAL("Minimal & Editorial"),
  DARK_NEON("Dark & Neon"),
  RETRO_TACTILE("Retro & Paper"),
  NATURE_COLOR("Nature & Color"),
  MY_PRESETS("My Presets"),
}

enum class SurfaceTreatment {
  LIQUID_GLASS,
  GLASSMORPHISM,
  NEUMORPHISM,
  MATERIAL_YOU,
  MINIMALIST,
  AMOLED,
  MIDNIGHT,
  CYBERPUNK,
  RETRO_TERMINAL,
  VINTAGE_PAPER,
  TYPEWRITER,
  SOFT_PASTEL,
  BRUTALIST,
  SWISS,
  MACOS,
  WINDOWS_FLUENT,
  Y2K,
  JAPANESE,
  EDITORIAL,
  ORGANIC,
  MONOCHROME,
  SUNSET,
  OCEAN,
  HIGH_CONTRAST,
}

enum class InterfaceDensity(
  val id: String,
  val label: String,
  val horizontalScale: Float,
  val verticalScale: Float,
) {
  COMPACT("compact", "Compact", 0.8f, 0.78f),
  COMFORTABLE("comfortable", "Comfortable", 1.0f, 1.0f),
  SPACIOUS("spacious", "Spacious", 1.2f, 1.25f);

  companion object {
    fun fromId(id: String?): InterfaceDensity =
      entries.firstOrNull { it.id == id } ?: COMFORTABLE
  }
}

data class ThemePalette(
  val background: Long,
  val backgroundSecondary: Long,
  val ambientGlowA: Long,
  val ambientGlowB: Long,
  val surface: Long,
  val surfaceElevated: Long,
  val border: Long,
  val textPrimary: Long,
  val textSecondary: Long,
  val accent: Long,
  val onAccent: Long,
  val pinColor: Long,
  val dangerColor: Long,
  val selectionHighlight: Long,
)

data class VellumThemeSpec(
  val id: String,
  val name: String,
  val tagline: String,
  val category: ThemeCategory,
  val treatment: SurfaceTreatment,
  val defaultIsDark: Boolean,
  val lightPalette: ThemePalette,
  val darkPalette: ThemePalette,
  val defaultDisplayFont: EditorFontPreset,
  val defaultBodyFont: EditorFontPreset,
  val defaultCornerRadiusDp: Float,
  val defaultBorderWidthDp: Float,
  val defaultDensity: InterfaceDensity = InterfaceDensity.COMFORTABLE,
  val supportsBlur: Boolean = false,
  val defaultBlurEnabled: Boolean = false,
  val defaultTransparency: Float = 0f,
  val supportsDynamicColor: Boolean = false,
)

data class ThemeCustomization(
  val isDarkOverride: Boolean? = null,
  val accentColorArgb: Long? = null,
  val backgroundColorArgb: Long? = null,
  val surfaceColorArgb: Long? = null,
  val fontOverride: EditorFontPreset? = null,
  val cornerRadiusDp: Float? = null,
  val density: InterfaceDensity? = null,
  val blurEnabled: Boolean? = null,
  val transparency: Float? = null,
) {
  fun isDefault(): Boolean =
    isDarkOverride == null &&
      accentColorArgb == null &&
      backgroundColorArgb == null &&
      surfaceColorArgb == null &&
      fontOverride == null &&
      cornerRadiusDp == null &&
      density == null &&
      blurEnabled == null &&
      transparency == null
}

data class CustomThemePreset(
  val id: String,
  val name: String,
  val baseThemeId: String,
  val customization: ThemeCustomization,
  val createdAt: Long = System.currentTimeMillis(),
)

data class ResolvedVellumTheme(
  val spec: VellumThemeSpec,
  val isDark: Boolean,
  val treatment: SurfaceTreatment,
  val background: Color,
  val backgroundSecondary: Color,
  val ambientGlowA: Color,
  val ambientGlowB: Color,
  val surface: Color,
  val surfaceElevated: Color,
  val border: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val accent: Color,
  val onAccent: Color,
  val pinColor: Color,
  val dangerColor: Color,
  val selectionHighlight: Color,
  val displayFont: FontFamily,
  val bodyFont: FontFamily,
  val cornerRadius: Dp,
  val borderWidth: Dp,
  val density: InterfaceDensity,
  val blurEnabled: Boolean,
  val transparency: Float,
  val isCustomized: Boolean,
  val activePresetName: String? = null,
)
