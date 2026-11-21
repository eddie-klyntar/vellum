package com.example.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

data class DetectedUrl(val url: String, val range: IntRange)

object PlainTextUtilities {

  private val urlRegex = Regex("""https?://[^\s<>"{}|\\^`\[\]]+""", RegexOption.IGNORE_CASE)

  /**
   * Fast linear-time word counter safe for very large plain-text documents.
   */
  fun countWords(text: String): Int {
    var count = 0
    var inWord = false
    for (i in text.indices) {
      if (text[i].isWhitespace()) {
        inWord = false
      } else if (!inWord) {
        inWord = true
        count++
      }
    }
    return count
  }

  /**
   * Pure literal substring search (safe with arbitrary symbols, regex metacharacters, and Unicode).
   */
  fun findMatches(text: String, query: String, caseSensitive: Boolean): List<IntRange> {
    if (query.isEmpty() || text.isEmpty()) return emptyList()
    val matches = mutableListOf<IntRange>()
    var startIndex = 0
    while (startIndex <= text.length - query.length) {
      val found = text.indexOf(query, startIndex = startIndex, ignoreCase = !caseSensitive)
      if (found < 0) break
      matches.add(found until (found + query.length))
      startIndex = found + query.length.coerceAtLeast(1)
    }
    return matches
  }

  /**
   * Read-only URL scanner. Never modifies or converts plain text into Markdown.
   */
  fun extractUrls(text: String): List<DetectedUrl> {
    if (!text.contains("http://", ignoreCase = true) && !text.contains("https://", ignoreCase = true)) {
      return emptyList()
    }
    return urlRegex
      .findAll(text)
      .map { result ->
        val cleaned = result.value.trimEnd('.', ',', ';', ')', ']', '!', '?')
        val end = result.range.first + cleaned.length
        DetectedUrl(url = cleaned, range = result.range.first until end)
      }
      .filter { it.url.length > 8 }
      .toList()
  }

  fun firstLinePreview(content: String, maxChars: Int = 140): String {
    if (content.isEmpty()) return ""
    val normalized = content.replace('\n', ' ').trim()
    return if (normalized.length <= maxChars) normalized else normalized.take(maxChars) + "…"
  }
}

/**
 * Applies strictly non-mutating visual highlights for Find matches and optional URL underlines.
 * Character indices remain 1:1 identical (OffsetMapping.Identity).
 */
class EditorHighlightTransformation(
  private val findMatches: List<IntRange>,
  private val activeMatchIndex: Int,
  private val detectedUrls: List<DetectedUrl>,
  private val detectUrls: Boolean,
  private val matchBackground: Color,
  private val activeMatchBackground: Color,
  private val activeMatchForeground: Color,
  private val urlAccentColor: Color,
) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    if (findMatches.isEmpty() && (!detectUrls || detectedUrls.isEmpty())) {
      return TransformedText(text, OffsetMapping.Identity)
    }

    val styled = buildAnnotatedString {
      append(text.text)

      if (detectUrls) {
        detectedUrls.forEach { link ->
          if (link.range.first >= 0 && link.range.last < text.length) {
            addStyle(
              style =
                SpanStyle(
                  color = urlAccentColor,
                  textDecoration = TextDecoration.Underline,
                ),
              start = link.range.first,
              end = link.range.last + 1,
            )
          }
        }
      }

      findMatches.forEachIndexed { index, range ->
        if (range.first >= 0 && range.last < text.length) {
          val isActive = index == activeMatchIndex
          addStyle(
            style =
              if (isActive) {
                SpanStyle(
                  background = activeMatchBackground,
                  color = activeMatchForeground,
                )
              } else {
                SpanStyle(background = matchBackground)
              },
            start = range.first,
            end = range.last + 1,
          )
        }
      }
    }
    return TransformedText(styled, OffsetMapping.Identity)
  }
}
