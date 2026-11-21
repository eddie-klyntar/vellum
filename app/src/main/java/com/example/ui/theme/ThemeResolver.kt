package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ThemeResolver {

  fun resolve(
    spec: VellumThemeSpec,
    customization: ThemeCustomization = ThemeCustomization(),
    context: Context? = null,
    activePresetName: String? = null,
  ): ResolvedVellumTheme {
    val isDark = customization.isDarkOverride ?: spec.defaultIsDark
    val basePalette = if (isDark) spec.darkPalette else spec.lightPalette

    val dynamicScheme =
      if (
        spec.supportsDynamicColor &&
          context != null &&
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
      ) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      } else {
        null
      }

    val background =
      customization.backgroundColorArgb?.let(::Color)
        ?: dynamicScheme?.background
        ?: Color(basePalette.background)

    val backgroundSecondary =
      customization.backgroundColorArgb?.let {
        val c = Color(it)
        if (isDark) c.copy(alpha = 1f) else c
      }
        ?: dynamicScheme?.surfaceContainerLow
        ?: Color(basePalette.backgroundSecondary)

    val surface =
      customization.surfaceColorArgb?.let(::Color)
        ?: dynamicScheme?.surfaceContainer
        ?: Color(basePalette.surface)

    val surfaceElevated =
      customization.surfaceColorArgb?.let { Color(it) }
        ?: dynamicScheme?.surfaceContainerHigh
        ?: Color(basePalette.surfaceElevated)

    val accent =
      customization.accentColorArgb?.let(::Color)
        ?: dynamicScheme?.primary
        ?: Color(basePalette.accent)

    val onAccent =
      if (customization.accentColorArgb != null) {
        if (accent.luminance() > 0.55f) Color(0xFF0F172A) else Color.White
      } else {
        dynamicScheme?.onPrimary ?: Color(basePalette.onAccent)
      }

    val textPrimary =
      if (customization.backgroundColorArgb != null) {
        if (background.luminance() > 0.5f) Color(0xFF111827) else Color(0xFFF8FAFC)
      } else {
        dynamicScheme?.onBackground ?: Color(basePalette.textPrimary)
      }

    val textSecondary =
      if (customization.backgroundColorArgb != null) {
        textPrimary.copy(alpha = 0.68f)
      } else {
        dynamicScheme?.onSurfaceVariant ?: Color(basePalette.textSecondary)
      }

    val border = dynamicScheme?.outlineVariant ?: Color(basePalette.border)

    val displayFont =
      VellumFonts.resolve(
        customization.fontOverride ?: spec.defaultDisplayFont,
        fallback = VellumFonts.PlusJakartaSans,
      )
    val bodyFont =
      VellumFonts.resolve(
        customization.fontOverride ?: spec.defaultBodyFont,
        fallback = VellumFonts.Inter,
      )

    val cornerRadius = (customization.cornerRadiusDp ?: spec.defaultCornerRadiusDp).dp
    val density = customization.density ?: spec.defaultDensity
    val blurEnabled = customization.blurEnabled ?: spec.defaultBlurEnabled
    val transparency = (customization.transparency ?: spec.defaultTransparency).coerceIn(0f, 0.9f)

    return ResolvedVellumTheme(
      spec = spec,
      isDark = isDark,
      treatment = spec.treatment,
      background = background,
      backgroundSecondary = backgroundSecondary,
      ambientGlowA = Color(basePalette.ambientGlowA),
      ambientGlowB = Color(basePalette.ambientGlowB),
      surface = surface,
      surfaceElevated = surfaceElevated,
      border = border,
      textPrimary = textPrimary,
      textSecondary = textSecondary,
      accent = accent,
      onAccent = onAccent,
      pinColor = Color(basePalette.pinColor),
      dangerColor = Color(basePalette.dangerColor),
      selectionHighlight = accent.copy(alpha = 0.28f),
      displayFont = displayFont,
      bodyFont = bodyFont,
      cornerRadius = cornerRadius,
      borderWidth = spec.defaultBorderWidthDp.dp,
      density = density,
      blurEnabled = blurEnabled,
      transparency = transparency,
      isCustomized = !customization.isDefault(),
      activePresetName = activePresetName,
    )
  }

  fun toMaterialColorScheme(theme: ResolvedVellumTheme): ColorScheme =
    if (theme.isDark) {
      darkColorScheme(
        primary = theme.accent,
        onPrimary = theme.onAccent,
        primaryContainer = theme.surfaceElevated,
        onPrimaryContainer = theme.textPrimary,
        secondary = theme.pinColor,
        onSecondary = theme.onAccent,
        background = theme.background,
        onBackground = theme.textPrimary,
        surface = theme.surface,
        onSurface = theme.textPrimary,
        surfaceVariant = theme.surfaceElevated,
        onSurfaceVariant = theme.textSecondary,
        outline = theme.border,
        error = theme.dangerColor,
      )
    } else {
      lightColorScheme(
        primary = theme.accent,
        onPrimary = theme.onAccent,
        primaryContainer = theme.surfaceElevated,
        onPrimaryContainer = theme.textPrimary,
        secondary = theme.pinColor,
        onSecondary = theme.onAccent,
        background = theme.background,
        onBackground = theme.textPrimary,
        surface = theme.surface,
        onSurface = theme.textPrimary,
        surfaceVariant = theme.surfaceElevated,
        onSurfaceVariant = theme.textSecondary,
        outline = theme.border,
        error = theme.dangerColor,
      )
    }

  fun toTypography(theme: ResolvedVellumTheme): Typography =
    Typography(
      headlineMedium =
        TextStyle(
          fontFamily = theme.displayFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 26.sp,
          lineHeight = 32.sp,
          letterSpacing = (-0.3).sp,
        ),
      titleLarge =
        TextStyle(
          fontFamily = theme.displayFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 20.sp,
          lineHeight = 26.sp,
        ),
      titleMedium =
        TextStyle(
          fontFamily = theme.displayFont,
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp,
          lineHeight = 22.sp,
        ),
      bodyLarge =
        TextStyle(
          fontFamily = theme.bodyFont,
          fontWeight = FontWeight.Normal,
          fontSize = 16.sp,
          lineHeight = 24.sp,
        ),
      bodyMedium =
        TextStyle(
          fontFamily = theme.bodyFont,
          fontWeight = FontWeight.Normal,
          fontSize = 14.sp,
          lineHeight = 20.sp,
        ),
      labelMedium =
        TextStyle(
          fontFamily = theme.bodyFont,
          fontWeight = FontWeight.Medium,
          fontSize = 12.sp,
          lineHeight = 16.sp,
        ),
      labelSmall =
        TextStyle(
          fontFamily = theme.bodyFont,
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp,
          lineHeight = 14.sp,
        ),
    )

  fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL
}
