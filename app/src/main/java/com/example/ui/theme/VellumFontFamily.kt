package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

enum class EditorFontPreset(val id: String, val label: String, val sampleTag: String) {
  THEME_DEFAULT("theme_default", "Theme Default", "Adaptive"),
  SYSTEM_SANS("system_sans", "System Sans", "Clean"),
  INTER("inter", "Inter", "Swiss"),
  PLUS_JAKARTA("plus_jakarta", "Plus Jakarta Sans", "Modern"),
  SPACE_GROTESK("space_grotesk", "Space Grotesk", "Geometric"),
  JETBRAINS_MONO("jetbrains_mono", "JetBrains Mono", "Code"),
  COURIER_PRIME("courier_prime", "Courier Prime", "Typewriter"),
  LORA("lora", "Lora", "Serif"),
  PLAYFAIR_DISPLAY("playfair_display", "Playfair Display", "Editorial");

  companion object {
    fun fromId(id: String?): EditorFontPreset =
      entries.firstOrNull { it.id == id } ?: THEME_DEFAULT
  }
}

object VellumFonts {
  val Inter = FontFamily(Font(R.font.inter, FontWeight.Normal))
  val PlusJakartaSans = FontFamily(Font(R.font.plus_jakarta_sans, FontWeight.Normal))
  val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))
  val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono, FontWeight.Normal))
  val CourierPrime = FontFamily(Font(R.font.courier_prime, FontWeight.Normal))
  val Lora = FontFamily(Font(R.font.lora, FontWeight.Normal))
  val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))

  fun resolve(preset: EditorFontPreset, fallback: FontFamily = PlusJakartaSans): FontFamily =
    when (preset) {
      EditorFontPreset.THEME_DEFAULT -> fallback
      EditorFontPreset.SYSTEM_SANS -> FontFamily.SansSerif
      EditorFontPreset.INTER -> Inter
      EditorFontPreset.PLUS_JAKARTA -> PlusJakartaSans
      EditorFontPreset.SPACE_GROTESK -> SpaceGrotesk
      EditorFontPreset.JETBRAINS_MONO -> JetBrainsMono
      EditorFontPreset.COURIER_PRIME -> CourierPrime
      EditorFontPreset.LORA -> Lora
      EditorFontPreset.PLAYFAIR_DISPLAY -> PlayfairDisplay
    }
}
