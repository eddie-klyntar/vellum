package com.example.ui.themes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EditorSettings
import com.example.data.PreferencesRepository
import com.example.data.ThemeUserState
import com.example.ui.theme.CustomThemePreset
import com.example.ui.theme.ResolvedVellumTheme
import com.example.ui.theme.ThemeCatalog
import com.example.ui.theme.ThemeCustomization
import com.example.ui.theme.ThemeResolver
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
  private val preferencesRepository: PreferencesRepository,
  private val appContext: Context,
) : ViewModel() {

  val themeUserState: StateFlow<ThemeUserState> =
    preferencesRepository.themeUserState.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ThemeUserState(),
    )

  val editorSettings: StateFlow<EditorSettings> =
    preferencesRepository.editorSettings.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = EditorSettings(),
    )

  val activeTheme: StateFlow<ResolvedVellumTheme> =
    themeUserState
      .map { state -> resolveActiveTheme(state) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeResolver.resolve(ThemeCatalog.themes.first(), context = appContext),
      )

  fun resolveActiveTheme(state: ThemeUserState): ResolvedVellumTheme {
    val activePreset = state.savedPresets.firstOrNull { it.id == state.activePresetId }
    if (activePreset != null) {
      val spec = ThemeCatalog.findById(activePreset.baseThemeId)
      return ThemeResolver.resolve(
        spec = spec,
        customization = activePreset.customization,
        context = appContext,
        activePresetName = activePreset.name,
      )
    }
    val spec = ThemeCatalog.findById(state.activeThemeId)
    val custom = state.customizationsByThemeId[spec.id] ?: ThemeCustomization()
    return ThemeResolver.resolve(spec = spec, customization = custom, context = appContext)
  }

  fun resolveForPreview(
    themeId: String,
    overrideCustomization: ThemeCustomization? = null,
  ): ResolvedVellumTheme {
    val spec = ThemeCatalog.findById(themeId)
    val custom =
      overrideCustomization
        ?: themeUserState.value.customizationsByThemeId[themeId]
        ?: ThemeCustomization()
    return ThemeResolver.resolve(spec = spec, customization = custom, context = appContext)
  }

  fun applyTheme(themeId: String, presetId: String? = null) {
    viewModelScope.launch { preferencesRepository.setActiveTheme(themeId, presetId) }
  }

  fun applyPreset(preset: CustomThemePreset) {
    viewModelScope.launch { preferencesRepository.setActiveTheme(preset.baseThemeId, preset.id) }
  }

  fun saveCustomization(themeId: String, customization: ThemeCustomization, alsoApply: Boolean = true) {
    viewModelScope.launch {
      preferencesRepository.saveThemeCustomization(themeId, customization)
      if (alsoApply) {
        preferencesRepository.setActiveTheme(themeId, null)
      }
    }
  }

  fun resetThemeToOriginal(themeId: String) {
    viewModelScope.launch { preferencesRepository.resetThemeCustomization(themeId) }
  }

  fun saveAsPersonalPreset(
    name: String,
    baseThemeId: String,
    customization: ThemeCustomization,
  ) {
    val cleanName = name.trim()
    if (cleanName.isEmpty()) return
    viewModelScope.launch {
      val preset =
        CustomThemePreset(
          id = UUID.randomUUID().toString(),
          name = cleanName,
          baseThemeId = baseThemeId,
          customization = customization,
        )
      preferencesRepository.saveCustomPreset(preset)
    }
  }

  fun deletePreset(presetId: String) {
    viewModelScope.launch { preferencesRepository.deleteCustomPreset(presetId) }
  }

  fun updateEditorSettings(transform: (EditorSettings) -> EditorSettings) {
    viewModelScope.launch { preferencesRepository.updateEditorSettings(transform) }
  }

  class Factory(
    private val preferencesRepository: PreferencesRepository,
    private val context: Context,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      ThemeViewModel(preferencesRepository, context.applicationContext) as T
  }
}
