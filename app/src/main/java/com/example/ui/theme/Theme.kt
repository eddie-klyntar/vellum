package com.example.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalVellumTheme =
  staticCompositionLocalOf<ResolvedVellumTheme> {
    ThemeResolver.resolve(ThemeCatalog.themes.first())
  }

@Composable
fun VellumTheme(resolvedTheme: ResolvedVellumTheme, content: @Composable () -> Unit) {
  val colorScheme = ThemeResolver.toMaterialColorScheme(resolvedTheme)
  val typography = ThemeResolver.toTypography(resolvedTheme)
  val selectionColors =
    TextSelectionColors(
      handleColor = resolvedTheme.accent,
      backgroundColor = resolvedTheme.selectionHighlight,
    )

  CompositionLocalProvider(
    LocalVellumTheme provides resolvedTheme,
    LocalTextSelectionColors provides selectionColors,
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
  }
}
