package com.example

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.DraftRecoveryStore
import com.example.data.NotesRepository
import com.example.data.VellumDatabase
import com.example.editor.PlainTextUtilities
import com.example.editor.UndoRedoManager
import com.example.ui.theme.EditorFontPreset
import com.example.ui.theme.InterfaceDensity
import com.example.ui.theme.ThemeCatalog
import com.example.ui.theme.ThemeCustomization
import com.example.ui.theme.ThemeResolver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VellumFeaturesTest {

  private lateinit var db: VellumDatabase
  private lateinit var repository: NotesRepository
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, VellumDatabase::class.java).allowMainThreadQueries().build()
    repository = NotesRepository(db.noteDao(), db.folderDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun unrestrictedPlainTextAndLongDocumentFidelity() = runTest {
    val weirdText =
      buildString {
        appendLine("https://sub.domain.org/a/b?x=1&y=2#frag")
        appendLine("   leading spaces and\ttabs\tpreserved   ")
        appendLine("日本語 • العربية • Ω≈ç√∫˜µ≤≥÷ • 🚀✨🧪")
        appendLine("```not-markdown``` **not bold** [not a link](url)")
        repeat(1000) { idx -> append("Long paragraph chunk #$idx with raw text. ") }
      }

    val id = repository.createNote(title = "Unrestricted", content = weirdText)
    val loaded = repository.getNoteById(id)
    assertNotNull(loaded)
    assertEquals(weirdText, loaded!!.content)

    // Export and re-import UTF-8 without modification
    val out = ByteArrayOutputStream()
    repository.exportPlainText(loaded.content, out)
    val importedId =
      repository.importPlainText("exported_copy.txt", ByteArrayInputStream(out.toByteArray()), null)
    val imported = repository.getNoteById(importedId)
    assertEquals("exported_copy", imported!!.title)
    assertEquals(weirdText, imported.content)
  }

  @Test
  fun folderDeleteUnassignsNotesWithoutDeletingThem() = runTest {
    val folderId = repository.createFolder("Scratch")
    val noteId = repository.createNote(title = "Keep me", content = "Important", folderId = folderId)
    repository.deleteFolder(folderId)

    val note = repository.getNoteById(noteId)
    assertNotNull(note)
    assertNull(note!!.folderId)
  }

  @Test
  fun duplicateAndPinNotesWorkCorrectly() = runTest {
    val id = repository.createNote(title = "Original", content = "Body", isPinned = true)
    val copyId = repository.duplicateNote(id)!!
    val copy = repository.getNoteById(copyId)!!
    assertEquals("Original (Copy)", copy.title)
    assertEquals("Body", copy.content)
    assertFalse(copy.isPinned)

    val all = repository.allNotes.first()
    assertEquals(2, all.size)
  }

  @Test
  fun undoRedoRestoresTextAndSelection() {
    val manager = UndoRedoManager()
    manager.initialize("Hello", TextRange(5))
    manager.onTextChanged(TextFieldValue("Hello World", TextRange(11)))
    manager.onTextChanged(TextFieldValue("Hello World!", TextRange(12)))

    assertTrue(manager.canUndo)
    val step1 = manager.undo()!!
    assertEquals("Hello World", step1.text)
    val step2 = manager.undo()!!
    assertEquals("Hello", step2.text)

    assertTrue(manager.canRedo)
    val redo1 = manager.redo()!!
    assertEquals("Hello World", redo1.text)
  }

  @Test
  fun plainTextUtilitiesHandleMetacharactersAndUrlsWithoutMutation() {
    val text = "Find [a+b]* here and https://vellum.app/docs?q=1&v=2."
    assertEquals(5, PlainTextUtilities.countWords(text))

    val matches = PlainTextUtilities.findMatches(text, "[a+b]*", caseSensitive = true)
    assertEquals(1, matches.size)
    assertEquals("[a+b]*", text.substring(matches.first()))

    val urls = PlainTextUtilities.extractUrls(text)
    assertEquals(1, urls.size)
    assertEquals("https://vellum.app/docs?q=1&v=2", urls.first().url)
  }

  @Test
  fun draftRecoveryStorePersistsAndClearsSnapshots() {
    val store = DraftRecoveryStore(context)
    store.saveDraftSnapshot(42L, "Crash Draft", "Unsaved line", immediate = true)
    val recovered = store.getPendingDraft()
    assertNotNull(recovered)
    assertEquals(42L, recovered!!.noteId)
    assertEquals("Crash Draft", recovered.title)
    assertEquals("Unsaved line", recovered.content)

    store.clearDraft(42L)
    assertNull(store.getPendingDraft())
  }

  @Test
  fun themeCatalogContainsAll24DistinctStylesAndResolvesLightAndDark() {
    assertEquals(24, ThemeCatalog.themes.size)
    assertEquals(24, ThemeCatalog.themes.map { it.id }.distinct().size)

    ThemeCatalog.themes.forEach { spec ->
      val light =
        ThemeResolver.resolve(spec, ThemeCustomization(isDarkOverride = false), context = context)
      val dark =
        ThemeResolver.resolve(spec, ThemeCustomization(isDarkOverride = true), context = context)
      assertFalse(light.isDark)
      assertTrue(dark.isDark)
    }

    val customized =
      ThemeResolver.resolve(
        ThemeCatalog.findById("liquid_glass"),
        ThemeCustomization(
          accentColorArgb = 0xFFFF3D00,
          cornerRadiusDp = 0f,
          density = InterfaceDensity.COMPACT,
          fontOverride = EditorFontPreset.JETBRAINS_MONO,
        ),
        context = context,
      )
    assertTrue(customized.isCustomized)
    assertEquals(0f, customized.cornerRadius.value)
  }
}
