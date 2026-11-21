package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.CustomThemePreset
import com.example.ui.theme.EditorFontPreset
import com.example.ui.theme.InterfaceDensity
import com.example.ui.theme.ThemeCustomization
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.vellumDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "vellum_preferences")

enum class NoteSortOption(val id: String, val label: String) {
  MODIFIED_DESC("modified_desc", "Last modified"),
  CREATED_DESC("created_desc", "Created date"),
  TITLE_ASC("title_asc", "Title (A–Z)");

  companion object {
    fun fromId(id: String?): NoteSortOption =
      entries.firstOrNull { it.id == id } ?: MODIFIED_DESC
  }
}

data class EditorSettings(
  val fontPreset: EditorFontPreset = EditorFontPreset.THEME_DEFAULT,
  val fontSizeSp: Float = 16f,
  val lineHeightMultiplier: Float = 1.5f,
  val editorPaddingDp: Int = 20,
  val wordWrap: Boolean = true,
  val showWordCount: Boolean = true,
  val confirmBeforeDelete: Boolean = true,
  val detectUrls: Boolean = true,
  val sortOption: NoteSortOption = NoteSortOption.MODIFIED_DESC,
  val isGridView: Boolean = false,
)

data class ThemeUserState(
  val activeThemeId: String = "liquid_glass",
  val activePresetId: String? = null,
  val customizationsByThemeId: Map<String, ThemeCustomization> = emptyMap(),
  val savedPresets: List<CustomThemePreset> = emptyList(),
)

class PreferencesRepository(private val context: Context) {

  val editorSettings: Flow<EditorSettings> =
    context.vellumDataStore.data.map { prefs ->
      EditorSettings(
        fontPreset = EditorFontPreset.fromId(prefs[KEY_EDITOR_FONT]),
        fontSizeSp = prefs[KEY_FONT_SIZE] ?: 16f,
        lineHeightMultiplier = prefs[KEY_LINE_HEIGHT] ?: 1.5f,
        editorPaddingDp = prefs[KEY_EDITOR_PADDING] ?: 20,
        wordWrap = prefs[KEY_WORD_WRAP] ?: true,
        showWordCount = prefs[KEY_SHOW_WORD_COUNT] ?: true,
        confirmBeforeDelete = prefs[KEY_CONFIRM_DELETE] ?: true,
        detectUrls = prefs[KEY_DETECT_URLS] ?: true,
        sortOption = NoteSortOption.fromId(prefs[KEY_SORT_OPTION]),
        isGridView = prefs[KEY_GRID_VIEW] ?: false,
      )
    }

  val themeUserState: Flow<ThemeUserState> =
    context.vellumDataStore.data.map { prefs ->
      ThemeUserState(
        activeThemeId = prefs[KEY_ACTIVE_THEME_ID] ?: "liquid_glass",
        activePresetId = prefs[KEY_ACTIVE_PRESET_ID]?.takeIf { it.isNotBlank() },
        customizationsByThemeId = decodeCustomizationsMap(prefs[KEY_THEME_CUSTOMIZATIONS]),
        savedPresets = decodePresetsList(prefs[KEY_SAVED_PRESETS]),
      )
    }

  suspend fun updateEditorSettings(transform: (EditorSettings) -> EditorSettings) {
    context.vellumDataStore.edit { prefs ->
      val current =
        EditorSettings(
          fontPreset = EditorFontPreset.fromId(prefs[KEY_EDITOR_FONT]),
          fontSizeSp = prefs[KEY_FONT_SIZE] ?: 16f,
          lineHeightMultiplier = prefs[KEY_LINE_HEIGHT] ?: 1.5f,
          editorPaddingDp = prefs[KEY_EDITOR_PADDING] ?: 20,
          wordWrap = prefs[KEY_WORD_WRAP] ?: true,
          showWordCount = prefs[KEY_SHOW_WORD_COUNT] ?: true,
          confirmBeforeDelete = prefs[KEY_CONFIRM_DELETE] ?: true,
          detectUrls = prefs[KEY_DETECT_URLS] ?: true,
          sortOption = NoteSortOption.fromId(prefs[KEY_SORT_OPTION]),
          isGridView = prefs[KEY_GRID_VIEW] ?: false,
        )
      val updated = transform(current)
      prefs[KEY_EDITOR_FONT] = updated.fontPreset.id
      prefs[KEY_FONT_SIZE] = updated.fontSizeSp
      prefs[KEY_LINE_HEIGHT] = updated.lineHeightMultiplier
      prefs[KEY_EDITOR_PADDING] = updated.editorPaddingDp
      prefs[KEY_WORD_WRAP] = updated.wordWrap
      prefs[KEY_SHOW_WORD_COUNT] = updated.showWordCount
      prefs[KEY_CONFIRM_DELETE] = updated.confirmBeforeDelete
      prefs[KEY_DETECT_URLS] = updated.detectUrls
      prefs[KEY_SORT_OPTION] = updated.sortOption.id
      prefs[KEY_GRID_VIEW] = updated.isGridView
    }
  }

  suspend fun setActiveTheme(themeId: String, presetId: String? = null) {
    context.vellumDataStore.edit { prefs ->
      prefs[KEY_ACTIVE_THEME_ID] = themeId
      prefs[KEY_ACTIVE_PRESET_ID] = presetId ?: ""
    }
  }

  suspend fun saveThemeCustomization(themeId: String, customization: ThemeCustomization) {
    context.vellumDataStore.edit { prefs ->
      val map = decodeCustomizationsMap(prefs[KEY_THEME_CUSTOMIZATIONS]).toMutableMap()
      if (customization.isDefault()) {
        map.remove(themeId)
      } else {
        map[themeId] = customization
      }
      prefs[KEY_THEME_CUSTOMIZATIONS] = encodeCustomizationsMap(map)
    }
  }

  suspend fun resetThemeCustomization(themeId: String) {
    context.vellumDataStore.edit { prefs ->
      val map = decodeCustomizationsMap(prefs[KEY_THEME_CUSTOMIZATIONS]).toMutableMap()
      map.remove(themeId)
      prefs[KEY_THEME_CUSTOMIZATIONS] = encodeCustomizationsMap(map)
      if (prefs[KEY_ACTIVE_THEME_ID] == themeId) {
        prefs[KEY_ACTIVE_PRESET_ID] = ""
      }
    }
  }

  suspend fun saveCustomPreset(preset: CustomThemePreset) {
    context.vellumDataStore.edit { prefs ->
      val list = decodePresetsList(prefs[KEY_SAVED_PRESETS]).toMutableList()
      val existingIndex = list.indexOfFirst { it.id == preset.id }
      if (existingIndex >= 0) {
        list[existingIndex] = preset
      } else {
        list.add(0, preset)
      }
      prefs[KEY_SAVED_PRESETS] = encodePresetsList(list)
      prefs[KEY_ACTIVE_THEME_ID] = preset.baseThemeId
      prefs[KEY_ACTIVE_PRESET_ID] = preset.id
    }
  }

  suspend fun deleteCustomPreset(presetId: String) {
    context.vellumDataStore.edit { prefs ->
      val list = decodePresetsList(prefs[KEY_SAVED_PRESETS]).filterNot { it.id == presetId }
      prefs[KEY_SAVED_PRESETS] = encodePresetsList(list)
      if (prefs[KEY_ACTIVE_PRESET_ID] == presetId) {
        prefs[KEY_ACTIVE_PRESET_ID] = ""
      }
    }
  }

  private fun encodeCustomization(c: ThemeCustomization): JSONObject =
    JSONObject().apply {
      c.isDarkOverride?.let { put("isDark", it) }
      c.accentColorArgb?.let { put("accent", it) }
      c.backgroundColorArgb?.let { put("bg", it) }
      c.surfaceColorArgb?.let { put("surface", it) }
      c.fontOverride?.let { put("font", it.id) }
      c.cornerRadiusDp?.let { put("radius", it.toDouble()) }
      c.density?.let { put("density", it.id) }
      c.blurEnabled?.let { put("blur", it) }
      c.transparency?.let { put("alpha", it.toDouble()) }
    }

  private fun decodeCustomization(obj: JSONObject): ThemeCustomization =
    ThemeCustomization(
      isDarkOverride = if (obj.has("isDark")) obj.getBoolean("isDark") else null,
      accentColorArgb = if (obj.has("accent")) obj.getLong("accent") else null,
      backgroundColorArgb = if (obj.has("bg")) obj.getLong("bg") else null,
      surfaceColorArgb = if (obj.has("surface")) obj.getLong("surface") else null,
      fontOverride = if (obj.has("font")) EditorFontPreset.fromId(obj.getString("font")) else null,
      cornerRadiusDp = if (obj.has("radius")) obj.getDouble("radius").toFloat() else null,
      density = if (obj.has("density")) InterfaceDensity.fromId(obj.getString("density")) else null,
      blurEnabled = if (obj.has("blur")) obj.getBoolean("blur") else null,
      transparency = if (obj.has("alpha")) obj.getDouble("alpha").toFloat() else null,
    )

  private fun encodeCustomizationsMap(map: Map<String, ThemeCustomization>): String {
    val root = JSONObject()
    map.forEach { (themeId, custom) -> root.put(themeId, encodeCustomization(custom)) }
    return root.toString()
  }

  private fun decodeCustomizationsMap(json: String?): Map<String, ThemeCustomization> {
    if (json.isNullOrBlank()) return emptyMap()
    return runCatching {
        val root = JSONObject(json)
        buildMap {
          val keys = root.keys()
          while (keys.hasNext()) {
            val key = keys.next()
            put(key, decodeCustomization(root.getJSONObject(key)))
          }
        }
      }
      .getOrDefault(emptyMap())
  }

  private fun encodePresetsList(list: List<CustomThemePreset>): String {
    val arr = JSONArray()
    list.forEach { preset ->
      arr.put(
        JSONObject().apply {
          put("id", preset.id)
          put("name", preset.name)
          put("baseThemeId", preset.baseThemeId)
          put("createdAt", preset.createdAt)
          put("custom", encodeCustomization(preset.customization))
        }
      )
    }
    return arr.toString()
  }

  private fun decodePresetsList(json: String?): List<CustomThemePreset> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        List(arr.length()) { index ->
          val obj = arr.getJSONObject(index)
          CustomThemePreset(
            id = obj.getString("id"),
            name = obj.getString("name"),
            baseThemeId = obj.getString("baseThemeId"),
            createdAt = obj.optLong("createdAt", 0L),
            customization = decodeCustomization(obj.getJSONObject("custom")),
          )
        }
      }
      .getOrDefault(emptyList())
  }

  private companion object {
    val KEY_EDITOR_FONT = stringPreferencesKey("editor_font")
    val KEY_FONT_SIZE = floatPreferencesKey("editor_font_size")
    val KEY_LINE_HEIGHT = floatPreferencesKey("editor_line_height")
    val KEY_EDITOR_PADDING = intPreferencesKey("editor_padding")
    val KEY_WORD_WRAP = booleanPreferencesKey("editor_word_wrap")
    val KEY_SHOW_WORD_COUNT = booleanPreferencesKey("editor_show_word_count")
    val KEY_CONFIRM_DELETE = booleanPreferencesKey("confirm_before_delete")
    val KEY_DETECT_URLS = booleanPreferencesKey("detect_urls")
    val KEY_SORT_OPTION = stringPreferencesKey("sort_option")
    val KEY_GRID_VIEW = booleanPreferencesKey("is_grid_view")
    val KEY_ACTIVE_THEME_ID = stringPreferencesKey("active_theme_id")
    val KEY_ACTIVE_PRESET_ID = stringPreferencesKey("active_preset_id")
    val KEY_THEME_CUSTOMIZATIONS = stringPreferencesKey("theme_customizations_json")
    val KEY_SAVED_PRESETS = stringPreferencesKey("saved_presets_json")
  }
}
