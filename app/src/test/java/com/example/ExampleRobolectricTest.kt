package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun appNameIsVellum() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Vellum", appName)
  }

  @Test
  fun createNoteTypeUnrestrictedTextAndPersistInList() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("new_note_fab").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("editor_body_input").fetchSemanticsNodes().isNotEmpty()
    }

    val sampleTitle = "Scratchpad #1"
    val sampleBody = "Check https://example.com/path?a=1&b=2 — λ = 42 🚀\nLine 2: git status"

    composeTestRule.onNodeWithTag("editor_title_input").performTextInput(sampleTitle)
    composeTestRule.onNodeWithTag("editor_body_input").performTextInput(sampleBody)
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("detected_links_pill").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("detected_links_pill").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editor_back_button").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText(sampleTitle).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText(sampleTitle).assertIsDisplayed()
  }

  @Test
  fun openThemeGalleryAndApplyTheme() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("theme_gallery_button").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("theme_gallery_grid").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("theme_gallery_grid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("theme_card_liquid_glass").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("preview_apply_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("new_note_fab").assertIsDisplayed()
  }
}
