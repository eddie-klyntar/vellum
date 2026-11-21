package com.example.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.abs

data class UndoSnapshot(
  val text: String,
  val selectionStart: Int,
  val selectionEnd: Int,
  val timestampMs: Long = System.currentTimeMillis(),
) {
  fun toTextFieldValue(): TextFieldValue {
    val safeStart = selectionStart.coerceIn(0, text.length)
    val safeEnd = selectionEnd.coerceIn(0, text.length)
    return TextFieldValue(text = text, selection = TextRange(safeStart, safeEnd))
  }
}

class UndoRedoManager(private val maxHistory: Int = 200) {
  private val undoStack = ArrayDeque<UndoSnapshot>()
  private val redoStack = ArrayDeque<UndoSnapshot>()
  private var lastRecorded: UndoSnapshot? = null
  private var lastChangeWasSingleChar: Boolean = false

  val canUndo: Boolean
    get() = undoStack.isNotEmpty()

  val canRedo: Boolean
    get() = redoStack.isNotEmpty()

  fun initialize(text: String, selection: TextRange = TextRange(text.length)) {
    undoStack.clear()
    redoStack.clear()
    lastRecorded = UndoSnapshot(text, selection.start, selection.end)
    lastChangeWasSingleChar = false
  }

  fun onTextChanged(newValue: TextFieldValue) {
    val prev = lastRecorded
    if (prev == null) {
      lastRecorded = UndoSnapshot(newValue.text, newValue.selection.start, newValue.selection.end)
      lastChangeWasSingleChar = false
      return
    }
    if (prev.text == newValue.text) {
      lastRecorded =
        prev.copy(
          selectionStart = newValue.selection.start,
          selectionEnd = newValue.selection.end,
        )
      return
    }

    val now = System.currentTimeMillis()
    val deltaLen = newValue.text.length - prev.text.length
    val isSingleCharInsert =
      deltaLen == 1 &&
        abs(newValue.selection.start - prev.selectionStart) <= 1 &&
        !isWhitespaceBoundary(prev.text, newValue.text)

    val topUndo = undoStack.lastOrNull()
    val canCoalesce =
      isSingleCharInsert &&
        lastChangeWasSingleChar &&
        topUndo != null &&
        (now - prev.timestampMs) < 600L &&
        (now - topUndo.timestampMs) < 1200L

    if (!canCoalesce) {
      undoStack.addLast(prev)
      if (undoStack.size > maxHistory) {
        undoStack.removeFirst()
      }
    }
    redoStack.clear()
    lastRecorded =
      UndoSnapshot(newValue.text, newValue.selection.start, newValue.selection.end, now)
    lastChangeWasSingleChar = isSingleCharInsert
  }

  fun undo(): TextFieldValue? {
    val current = lastRecorded ?: return null
    val target = undoStack.removeLastOrNull() ?: return null
    redoStack.addLast(current)
    lastRecorded = target
    lastChangeWasSingleChar = false
    return target.toTextFieldValue()
  }

  fun redo(): TextFieldValue? {
    val current = lastRecorded ?: return null
    val target = redoStack.removeLastOrNull() ?: return null
    undoStack.addLast(current)
    lastRecorded = target
    lastChangeWasSingleChar = false
    return target.toTextFieldValue()
  }

  private fun isWhitespaceBoundary(oldText: String, newText: String): Boolean {
    if (newText.length != oldText.length + 1) return false
    for (i in newText.indices) {
      if (i >= oldText.length || newText[i] != oldText[i]) {
        return newText[i].isWhitespace()
      }
    }
    return false
  }
}
