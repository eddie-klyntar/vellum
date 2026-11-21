package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws the atmospheric backdrop tailored to each of the 24 theme styles.
 * Keeps text surfaces crisp and readable while expressing distinct visual personality.
 */
@Composable
fun VellumBackdrop(
  theme: ResolvedVellumTheme,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  val bgPrimary by animateColorAsState(theme.background, tween(260), label = "bgPrimary")
  val bgSecondary by animateColorAsState(theme.backgroundSecondary, tween(260), label = "bgSecondary")
  val glowA by animateColorAsState(theme.ambientGlowA, tween(260), label = "glowA")
  val glowB by animateColorAsState(theme.ambientGlowB, tween(260), label = "glowB")

  Box(modifier = modifier.fillMaxSize().background(bgPrimary)) {
    // Decorative atmospheric layer (optional blur applied strictly to decorative layer only)
    val blurModifier =
      if (theme.blurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(36.dp)
      } else {
        Modifier
      }

    Box(
      modifier =
        Modifier.fillMaxSize().then(blurModifier).drawBehind {
          when (theme.treatment) {
            SurfaceTreatment.LIQUID_GLASS,
            SurfaceTreatment.GLASSMORPHISM,
            SurfaceTreatment.Y2K -> {
              drawRect(
                brush =
                  Brush.verticalGradient(colors = listOf(bgPrimary, bgSecondary))
              )
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.42f), Color.Transparent),
                    center = Offset(size.width * 0.18f, size.height * 0.16f),
                    radius = size.minDimension * 0.72f,
                  ),
                center = Offset(size.width * 0.18f, size.height * 0.16f),
                radius = size.minDimension * 0.72f,
              )
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(glowB.copy(alpha = 0.36f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.74f),
                    radius = size.minDimension * 0.82f,
                  ),
                center = Offset(size.width * 0.85f, size.height * 0.74f),
                radius = size.minDimension * 0.82f,
              )
            }
            SurfaceTreatment.SUNSET,
            SurfaceTreatment.OCEAN -> {
              drawRect(
                brush =
                  Brush.linearGradient(
                    colors =
                      listOf(
                        bgPrimary,
                        glowA.copy(alpha = if (theme.isDark) 0.25f else 0.20f),
                        bgSecondary,
                        glowB.copy(alpha = if (theme.isDark) 0.22f else 0.18f),
                      ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                  )
              )
            }
            SurfaceTreatment.WINDOWS_FLUENT,
            SurfaceTreatment.SOFT_PASTEL,
            SurfaceTreatment.ORGANIC -> {
              drawRect(
                brush = Brush.verticalGradient(colors = listOf(bgPrimary, bgSecondary))
              )
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                    radius = size.minDimension * 0.65f,
                  ),
                center = Offset(size.width * 0.88f, size.height * 0.12f),
                radius = size.minDimension * 0.65f,
              )
            }
            SurfaceTreatment.CYBERPUNK -> {
              drawRect(brush = Brush.verticalGradient(listOf(bgPrimary, bgSecondary)))
              val step = 36.dp.toPx()
              val gridColor = theme.accent.copy(alpha = 0.055f)
              var x = 0f
              while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
              }
              var y = 0f
              while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
              }
            }
            SurfaceTreatment.RETRO_TERMINAL -> {
              drawRect(color = bgPrimary)
              val lineGap = 6.dp.toPx()
              val scanColor = theme.accent.copy(alpha = 0.035f)
              var y = 0f
              while (y < size.height) {
                drawLine(scanColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
                y += lineGap
              }
            }
            SurfaceTreatment.VINTAGE_PAPER -> {
              drawRect(brush = Brush.verticalGradient(listOf(bgPrimary, bgSecondary)))
              val ruleGap = 32.dp.toPx()
              val ruleColor = theme.border.copy(alpha = 0.22f)
              var y = ruleGap
              while (y < size.height) {
                drawLine(ruleColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += ruleGap
              }
            }
            SurfaceTreatment.TYPEWRITER -> {
              drawRect(color = bgPrimary)
              val marginX = 22.dp.toPx()
              drawLine(
                color = theme.accent.copy(alpha = 0.16f),
                start = Offset(marginX, 0f),
                end = Offset(marginX, size.height),
                strokeWidth = 1.5f,
              )
            }
            SurfaceTreatment.MIDNIGHT -> {
              drawRect(brush = Brush.verticalGradient(listOf(bgPrimary, bgSecondary)))
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.maxDimension * 0.6f,
                  ),
                center = Offset(size.width * 0.5f, 0f),
                radius = size.maxDimension * 0.6f,
              )
            }
            else -> Unit
          }
        }
    )

    content()
  }
}

/**
 * Renders a themed surface/card honoring each theme's tactile & structural identity:
 * Liquid Glass specular rims, Neumorphism extruded shadows, Brutalist offset blocks, etc.
 */
@Composable
fun VellumSurface(
  theme: ResolvedVellumTheme,
  modifier: Modifier = Modifier,
  elevated: Boolean = false,
  cornerRadiusOverride: Dp? = null,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit,
) {
  val radius = cornerRadiusOverride ?: theme.cornerRadius
  val shape = RoundedCornerShape(radius)
  val rawSurface = if (elevated) theme.surfaceElevated else theme.surface

  // Apply user-configured transparency on translucent styles while maintaining readability
  val effectiveSurfaceColor =
    when (theme.treatment) {
      SurfaceTreatment.LIQUID_GLASS,
      SurfaceTreatment.GLASSMORPHISM,
      SurfaceTreatment.WINDOWS_FLUENT,
      SurfaceTreatment.MACOS,
      SurfaceTreatment.Y2K,
      SurfaceTreatment.SUNSET,
      SurfaceTreatment.OCEAN -> {
        val minAlpha = if (theme.isDark) 0.52f else 0.68f
        val computedAlpha = (1f - theme.transparency * 0.42f).coerceIn(minAlpha, 0.98f)
        rawSurface.copy(alpha = computedAlpha)
      }
      else -> rawSurface
    }

  val animatedSurface by
    animateColorAsState(effectiveSurfaceColor, tween(220), label = "surfaceColor")
  val animatedBorder by animateColorAsState(theme.border, tween(220), label = "borderColor")

  val treatmentModifier =
    Modifier.drawBehind {
      val cr = CornerRadius(radius.toPx(), radius.toPx())
      when (theme.treatment) {
        SurfaceTreatment.NEUMORPHISM -> {
          val lightOffset = 4.dp.toPx()
          val darkOffset = 5.dp.toPx()
          drawRoundRect(
            color = theme.ambientGlowA.copy(alpha = if (theme.isDark) 0.45f else 0.78f),
            topLeft = Offset(-lightOffset, -lightOffset),
            size = size,
            cornerRadius = cr,
          )
          drawRoundRect(
            color = theme.ambientGlowB.copy(alpha = if (theme.isDark) 0.68f else 0.55f),
            topLeft = Offset(darkOffset, darkOffset),
            size = size,
            cornerRadius = cr,
          )
        }
        SurfaceTreatment.BRUTALIST -> {
          val shadowOffset = 4.dp.toPx()
          drawRoundRect(
            color = theme.border,
            topLeft = Offset(shadowOffset, shadowOffset),
            size = size,
            cornerRadius = cr,
          )
        }
        else -> Unit
      }
    }

  val innerModifier =
    Modifier.clip(shape)
      .background(animatedSurface, shape)
      .then(
        when (theme.treatment) {
          SurfaceTreatment.LIQUID_GLASS -> {
            Modifier.border(
              width = theme.borderWidth.coerceAtLeast(1.dp),
              brush =
                Brush.verticalGradient(
                  colors =
                    listOf(
                      Color.White.copy(alpha = if (theme.isDark) 0.36f else 0.85f),
                      animatedBorder.copy(alpha = 0.25f),
                      Color.White.copy(alpha = if (theme.isDark) 0.14f else 0.45f),
                    )
                ),
              shape = shape,
            )
          }
          SurfaceTreatment.Y2K -> {
            Modifier.border(
              width = 1.5.dp,
              brush =
                Brush.linearGradient(
                  colors = listOf(theme.accent, Color.White, theme.pinColor)
                ),
              shape = shape,
            )
          }
          SurfaceTreatment.CYBERPUNK -> {
            Modifier.border(1.2.dp, animatedBorder.copy(alpha = 0.65f), shape).drawBehind {
              val tick = 10.dp.toPx()
              val accent = theme.accent
              drawLine(accent, Offset(0f, 0f), Offset(tick, 0f), strokeWidth = 2.dp.toPx())
              drawLine(accent, Offset(0f, 0f), Offset(0f, tick), strokeWidth = 2.dp.toPx())
              drawLine(
                accent,
                Offset(size.width - tick, size.height),
                Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx(),
              )
              drawLine(
                accent,
                Offset(size.width, size.height - tick),
                Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx(),
              )
            }
          }
          else -> {
            if (theme.borderWidth > 0.dp) {
              Modifier.border(theme.borderWidth, animatedBorder, shape)
            } else {
              Modifier
            }
          }
        }
      )
      .then(
        if (onClick != null) {
          Modifier.clickable(role = Role.Button, onClick = onClick)
        } else {
          Modifier
        }
      )

  Box(modifier = modifier.then(treatmentModifier).then(innerModifier), content = content)
}

@Composable
fun VellumPillButton(
  theme: ResolvedVellumTheme,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  val corner =
    if (theme.treatment == SurfaceTreatment.BRUTALIST ||
      theme.treatment == SurfaceTreatment.RETRO_TERMINAL
    ) {
      theme.cornerRadius
    } else {
      999.dp
    }
  val shape = RoundedCornerShape(corner)
  val bg = if (selected) theme.accent else theme.surfaceElevated.copy(alpha = 0.85f)
  val borderStroke =
    when {
      theme.treatment == SurfaceTreatment.BRUTALIST -> BorderStroke(1.8.dp, theme.border)
      selected -> null
      theme.borderWidth > 0.dp -> BorderStroke(1.dp, theme.border.copy(alpha = 0.7f))
      else -> null
    }

  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    color = bg,
    contentColor = if (selected) theme.onAccent else theme.textPrimary,
    border = borderStroke,
    shadowElevation =
      if (theme.treatment == SurfaceTreatment.NEUMORPHISM && !selected) 2.dp else 0.dp,
  ) {
    Box(
      modifier =
        Modifier.padding(
          horizontal = (14 * theme.density.horizontalScale).dp,
          vertical = (8 * theme.density.verticalScale).dp,
        ),
      content = content,
    )
  }
}

/**
 * Draws subtle Liquid Glass top-specular highlight line on panels when applicable.
 */
fun Modifier.drawGlassSpecularSheen(theme: ResolvedVellumTheme): Modifier =
  drawBehind {
    if (theme.treatment == SurfaceTreatment.LIQUID_GLASS ||
      theme.treatment == SurfaceTreatment.Y2K
    ) {
      drawRoundRect(
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                Color.White.copy(alpha = if (theme.isDark) 0.14f else 0.32f),
                Color.Transparent,
              ),
            startY = 0f,
            endY = size.height * 0.45f,
          ),
        size = Size(size.width, size.height * 0.45f),
      )
    }
    if (theme.treatment == SurfaceTreatment.BRUTALIST) {
      drawRect(
        color = theme.border,
        style = Stroke(width = 2.dp.toPx()),
      )
    }
  }
