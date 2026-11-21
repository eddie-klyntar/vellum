package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.AppContainer
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.WritingScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.LocalVellumTheme
import com.example.ui.theme.ThemeCustomization
import com.example.ui.theme.VellumBackdrop
import com.example.ui.theme.VellumTheme
import com.example.ui.themes.ThemeCustomizerScreen
import com.example.ui.themes.ThemeGalleryScreen
import com.example.ui.themes.ThemeViewModel
import kotlinx.coroutines.launch

object VellumRoutes {
  const val HOME = "home"
  const val EDITOR = "editor/{noteId}"
  const val THEMES = "themes"
  const val THEME_CUSTOMIZER = "theme_customizer/{themeId}"
  const val SETTINGS = "settings"

  fun editorRoute(noteId: Long) = "editor/$noteId"

  fun customizerRoute(themeId: String) = "theme_customizer/$themeId"
}

@Composable
fun VellumApp(
  appContainer: AppContainer,
  onDarkThemeChanged: (Boolean) -> Unit = {},
) {
  val context = LocalContext.current
  val themeViewModel: ThemeViewModel =
    viewModel(factory = ThemeViewModel.Factory(appContainer.preferencesRepository, context))
  val homeViewModel: HomeViewModel =
    viewModel(
      factory =
        HomeViewModel.Factory(appContainer.notesRepository, appContainer.preferencesRepository)
    )
  val editorViewModel: EditorViewModel =
    viewModel(
      factory =
        EditorViewModel.Factory(appContainer.notesRepository, appContainer.draftRecoveryStore)
    )

  val activeTheme by themeViewModel.activeTheme.collectAsStateWithLifecycle()
  val themeUserState by themeViewModel.themeUserState.collectAsStateWithLifecycle()
  val editorSettings by themeViewModel.editorSettings.collectAsStateWithLifecycle()
  val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
  val editorUiState by editorViewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(activeTheme.isDark) { onDarkThemeChanged(activeTheme.isDark) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // SAF Export state
  var pendingExportContent by remember { mutableStateOf<String?>(null) }
  val exportLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
      val content = pendingExportContent
      pendingExportContent = null
      if (uri != null && content != null) {
        context.contentResolver.openOutputStream(uri)?.let { stream ->
          homeViewModel.exportPlainText(content, stream) {
            scope.launch { snackbarHostState.showSnackbar("Exported plain-text note") }
          }
        }
      }
    }

  // SAF Import state
  val importLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
      if (uris.isNotEmpty()) {
        uris.forEach { uri ->
          val fileName = resolveDisplayName(context, uri)
          context.contentResolver.openInputStream(uri)?.let { stream ->
            homeViewModel.importPlainText(fileName, stream)
          }
        }
        scope.launch {
          snackbarHostState.showSnackbar(
            if (uris.size == 1) "Imported plain-text note"
            else "Imported ${uris.size} plain-text notes"
          )
        }
      }
    }

  val startExport: (String, String) -> Unit = { title, content ->
    pendingExportContent = content
    val safeName =
      title.trim().replace(Regex("""[\\/:*?"<>|]"""), "_").ifBlank { "note" } + ".txt"
    exportLauncher.launch(safeName)
  }

  val startShare: (String, String) -> Unit = { title, content ->
    val intent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        if (title.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, content)
      }
    context.startActivity(Intent.createChooser(intent, "Share note"))
  }

  VellumTheme(resolvedTheme = activeTheme) {
    val navController = rememberNavController()
    var selectedTabletNoteId by remember { mutableLongStateOf(-1L) }

    Box(modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedLayout = maxWidth >= 840.dp

        NavHost(
          navController = navController,
          startDestination = VellumRoutes.HOME,
          enterTransition = {
            slideIntoContainer(
              AnimatedContentTransitionScope.SlideDirection.Start,
              tween(220),
            ) + fadeIn(tween(220))
          },
          exitTransition = { fadeOut(tween(180)) },
          popEnterTransition = { fadeIn(tween(200)) },
          popExitTransition = {
            slideOutOfContainer(
              AnimatedContentTransitionScope.SlideDirection.End,
              tween(220),
            ) + fadeOut(tween(200))
          },
        ) {
          composable(VellumRoutes.HOME) {
            if (isExpandedLayout) {
              // Canonical List-Detail Workspace for Tablets / Foldables
              Row(modifier = Modifier.fillMaxSize()) {
                HomeScreen(
                  uiState = homeUiState,
                  activeNoteId = selectedTabletNoteId.takeIf { it > 0L },
                  onOpenNote = { noteId ->
                    selectedTabletNoteId = noteId
                    editorViewModel.loadNote(noteId)
                  },
                  onCreateNote = {
                    homeViewModel.createNewNote { newId ->
                      selectedTabletNoteId = newId
                      editorViewModel.loadNote(newId)
                    }
                  },
                  onOpenThemeGallery = { navController.navigate(VellumRoutes.THEMES) },
                  onOpenSettings = { navController.navigate(VellumRoutes.SETTINGS) },
                  onSearchQueryChange = homeViewModel::setSearchQuery,
                  onSelectFolder = homeViewModel::selectFolder,
                  onCreateFolder = homeViewModel::createFolder,
                  onRenameFolder = homeViewModel::renameFolder,
                  onDeleteFolder = homeViewModel::deleteFolder,
                  onTogglePin = homeViewModel::togglePin,
                  onRenameNote = homeViewModel::renameNote,
                  onDuplicateNote = homeViewModel::duplicateNote,
                  onMoveNoteToFolder = homeViewModel::moveToFolder,
                  onDeleteNote = { id ->
                    if (selectedTabletNoteId == id) selectedTabletNoteId = -1L
                    homeViewModel.deleteNote(id)
                  },
                  onShareNote = { note -> startShare(note.title, note.content) },
                  onExportNote = { note -> startExport(note.title, note.content) },
                  onImportTxt = { importLauncher.launch(arrayOf("text/plain", "text/*")) },
                  onSetSortOption = homeViewModel::setSortOption,
                  onToggleGridView = homeViewModel::toggleGridView,
                  modifier = Modifier.width(370.dp).fillMaxHeight(),
                )

                VerticalDivider(color = activeTheme.border.copy(alpha = 0.5f), thickness = 1.dp)

                if (selectedTabletNoteId > 0L) {
                  LaunchedEffect(selectedTabletNoteId) {
                    editorViewModel.loadNote(selectedTabletNoteId)
                  }
                  WritingScreen(
                    uiState = editorUiState,
                    editorSettings = editorSettings,
                    folders = homeUiState.folders,
                    showBackButton = false,
                    onBack = {},
                    onTitleChange = editorViewModel::onTitleChange,
                    onContentChange = editorViewModel::onContentChange,
                    onUndo = editorViewModel::undo,
                    onRedo = editorViewModel::redo,
                    onSelectAll = editorViewModel::selectAll,
                    onPasteAtCursor = editorViewModel::insertTextAtSelection,
                    onToggleFindBar = editorViewModel::toggleFindBar,
                    onUpdateFindQuery = editorViewModel::updateFindQuery,
                    onToggleCaseSensitive = editorViewModel::toggleCaseSensitive,
                    onNextMatch = editorViewModel::nextMatch,
                    onPreviousMatch = editorViewModel::previousMatch,
                    onTogglePin = editorViewModel::togglePin,
                    onMoveToFolder = editorViewModel::moveToFolder,
                    onCreateFolder = { name -> homeViewModel.createFolder(name) },
                    onDuplicateNote = {
                      editorViewModel.duplicateCurrentNote { copyId ->
                        selectedTabletNoteId = copyId
                        editorViewModel.loadNote(copyId)
                      }
                    },
                    onDeleteNote = {
                      editorViewModel.deleteCurrentNote { selectedTabletNoteId = -1L }
                    },
                    onShareNote = {
                      startShare(editorUiState.title, editorUiState.textFieldValue.text)
                    },
                    onExportNote = {
                      startExport(editorUiState.title, editorUiState.textFieldValue.text)
                    },
                    onFlushSave = editorViewModel::flushPendingSaveSync,
                    onUpdateEditorSettings = themeViewModel::updateEditorSettings,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                  )
                } else {
                  TabletDetailEmptyState(
                    onCreateNote = {
                      homeViewModel.createNewNote { newId ->
                        selectedTabletNoteId = newId
                        editorViewModel.loadNote(newId)
                      }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                  )
                }
              }
            } else {
              HomeScreen(
                uiState = homeUiState,
                activeNoteId = null,
                onOpenNote = { noteId ->
                  navController.navigate(VellumRoutes.editorRoute(noteId))
                },
                onCreateNote = {
                  homeViewModel.createNewNote { newId ->
                    navController.navigate(VellumRoutes.editorRoute(newId))
                  }
                },
                onOpenThemeGallery = { navController.navigate(VellumRoutes.THEMES) },
                onOpenSettings = { navController.navigate(VellumRoutes.SETTINGS) },
                onSearchQueryChange = homeViewModel::setSearchQuery,
                onSelectFolder = homeViewModel::selectFolder,
                onCreateFolder = homeViewModel::createFolder,
                onRenameFolder = homeViewModel::renameFolder,
                onDeleteFolder = homeViewModel::deleteFolder,
                onTogglePin = homeViewModel::togglePin,
                onRenameNote = homeViewModel::renameNote,
                onDuplicateNote = homeViewModel::duplicateNote,
                onMoveNoteToFolder = homeViewModel::moveToFolder,
                onDeleteNote = homeViewModel::deleteNote,
                onShareNote = { note -> startShare(note.title, note.content) },
                onExportNote = { note -> startExport(note.title, note.content) },
                onImportTxt = { importLauncher.launch(arrayOf("text/plain", "text/*")) },
                onSetSortOption = homeViewModel::setSortOption,
                onToggleGridView = homeViewModel::toggleGridView,
              )
            }
          }

          composable(
            route = VellumRoutes.EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
          ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            LaunchedEffect(noteId) {
              if (noteId > 0L) editorViewModel.loadNote(noteId)
            }
            WritingScreen(
              uiState = editorUiState,
              editorSettings = editorSettings,
              folders = homeUiState.folders,
              showBackButton = true,
              onBack = { navController.popBackStack() },
              onTitleChange = editorViewModel::onTitleChange,
              onContentChange = editorViewModel::onContentChange,
              onUndo = editorViewModel::undo,
              onRedo = editorViewModel::redo,
              onSelectAll = editorViewModel::selectAll,
              onPasteAtCursor = editorViewModel::insertTextAtSelection,
              onToggleFindBar = editorViewModel::toggleFindBar,
              onUpdateFindQuery = editorViewModel::updateFindQuery,
              onToggleCaseSensitive = editorViewModel::toggleCaseSensitive,
              onNextMatch = editorViewModel::nextMatch,
              onPreviousMatch = editorViewModel::previousMatch,
              onTogglePin = editorViewModel::togglePin,
              onMoveToFolder = editorViewModel::moveToFolder,
              onCreateFolder = { name -> homeViewModel.createFolder(name) },
              onDuplicateNote = {
                editorViewModel.duplicateCurrentNote { copyId ->
                  navController.navigate(VellumRoutes.editorRoute(copyId)) {
                    popUpTo(VellumRoutes.HOME)
                  }
                }
              },
              onDeleteNote = {
                editorViewModel.deleteCurrentNote { navController.popBackStack() }
              },
              onShareNote = {
                startShare(editorUiState.title, editorUiState.textFieldValue.text)
              },
              onExportNote = {
                startExport(editorUiState.title, editorUiState.textFieldValue.text)
              },
              onFlushSave = editorViewModel::flushPendingSaveSync,
              onUpdateEditorSettings = themeViewModel::updateEditorSettings,
            )
          }

          composable(VellumRoutes.THEMES) {
            ThemeGalleryScreen(
              themeUserState = themeUserState,
              onBack = { navController.popBackStack() },
              onApplyTheme = { themeId, presetId ->
                themeViewModel.applyTheme(themeId, presetId)
              },
              onOpenCustomizer = { themeId ->
                navController.navigate(VellumRoutes.customizerRoute(themeId))
              },
              onDeletePreset = themeViewModel::deletePreset,
            )
          }

          composable(
            route = VellumRoutes.THEME_CUSTOMIZER,
            arguments = listOf(navArgument("themeId") { type = NavType.StringType }),
          ) { backStackEntry ->
            val themeId = backStackEntry.arguments?.getString("themeId") ?: "liquid_glass"
            val initialCustom =
              themeUserState.customizationsByThemeId[themeId] ?: ThemeCustomization()
            ThemeCustomizerScreen(
              themeId = themeId,
              initialCustomization = initialCustom,
              onBack = { navController.popBackStack() },
              onSaveAndApply = { custom ->
                themeViewModel.saveCustomization(themeId, custom, alsoApply = true)
              },
              onResetOriginal = { themeViewModel.resetThemeToOriginal(themeId) },
              onSaveAsPreset = { name, custom ->
                themeViewModel.saveAsPersonalPreset(name, themeId, custom)
              },
            )
          }

          composable(VellumRoutes.SETTINGS) {
            SettingsScreen(
              editorSettings = editorSettings,
              onBack = { navController.popBackStack() },
              onOpenThemeGallery = { navController.navigate(VellumRoutes.THEMES) },
              onOpenThemeCustomizer = {
                navController.navigate(VellumRoutes.customizerRoute(activeTheme.spec.id))
              },
              onImportTxt = { importLauncher.launch(arrayOf("text/plain", "text/*")) },
              onUpdateEditorSettings = themeViewModel::updateEditorSettings,
            )
          }
        }
      }

      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
      )
    }
  }
}

@Composable
private fun TabletDetailEmptyState(onCreateNote: () -> Unit, modifier: Modifier = Modifier) {
  val theme = LocalVellumTheme.current
  VellumBackdrop(theme = theme, modifier = modifier) {
    Column(
      modifier = Modifier.fillMaxSize().padding(32.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        Icons.Outlined.Description,
        contentDescription = null,
        tint = theme.accent,
        modifier = Modifier.size(48.dp),
      )
      Spacer(Modifier.height(14.dp))
      Text(
        "Select a note or start writing",
        fontFamily = theme.displayFont,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = theme.textPrimary,
      )
      Spacer(Modifier.height(6.dp))
      Text(
        "Plain text, links, snippets, or rough notes — saved automatically offline.",
        fontFamily = theme.bodyFont,
        fontSize = 14.sp,
        color = theme.textSecondary,
      )
      Spacer(Modifier.height(20.dp))
      Button(
        onClick = onCreateNote,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = theme.accent,
            contentColor = theme.onAccent,
          ),
      ) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.new_note))
      }
    }
  }
}

private fun resolveDisplayName(context: Context, uri: Uri): String? {
  return runCatching {
      context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
          if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) cursor.getString(idx) else null
          } else null
        }
    }
    .getOrNull()
}
